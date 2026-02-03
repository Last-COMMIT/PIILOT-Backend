package com.lastcommit.piilot.domain.filescan.service;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.lastcommit.piilot.domain.filescan.dto.internal.FileMetadataDTO;
import com.lastcommit.piilot.domain.filescan.entity.FileServerConnection;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

@Slf4j
@Component
public class FileSchemaScanner {

    private static final int CONNECTION_TIMEOUT = 10000;
    private static final String[] SUPPORTED_EXTENSIONS = {
            // DOCUMENT
            "txt", "pdf", "docx",
            // PHOTO
            "jpg", "jpeg", "png", "heic",
            // AUDIO
            "mp3", "wav",
            // VIDEO
            "mp4", "avi", "mov"
    };

    public List<FileMetadataDTO> scanFiles(FileServerConnection connection, String decryptedPassword) {
        String serverType = connection.getServerType().getName().toUpperCase();

        return switch (serverType) {
            case "SFTP" -> scanSftpFiles(connection, decryptedPassword);
            case "FTP" -> scanFtpFiles(connection, decryptedPassword);
            case "WEBDAV" -> scanWebDavFiles(connection, decryptedPassword);
            default -> {
                log.warn("Unsupported server type: {}", serverType);
                yield List.of();
            }
        };
    }

    private List<FileMetadataDTO> scanSftpFiles(FileServerConnection connection, String password) {
        List<FileMetadataDTO> files = new ArrayList<>();
        Session session = null;
        ChannelSftp channel = null;

        try {
            JSch jsch = new JSch();
            session = jsch.getSession(connection.getUsername(), connection.getHost(), connection.getPort());
            session.setPassword(password);
            // 내부구축형 시스템으로 신뢰할 수 있는 내부 네트워크에서만 운영됨
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(CONNECTION_TIMEOUT);

            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect(CONNECTION_TIMEOUT);

            scanSftpDirectory(channel, connection.getDefaultPath(), files);

        } catch (Exception e) {
            log.error("SFTP scan failed for connection {}: {}", connection.getId(), e.getMessage());
        } finally {
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }

        return files;
    }

    @SuppressWarnings("unchecked")
    private void scanSftpDirectory(ChannelSftp channel, String path, List<FileMetadataDTO> files) {
        try {
            Vector<ChannelSftp.LsEntry> entries = channel.ls(path);

            for (ChannelSftp.LsEntry entry : entries) {
                String name = entry.getFilename();
                if (".".equals(name) || "..".equals(name)) {
                    continue;
                }

                String fullPath = path.endsWith("/") ? path + name : path + "/" + name;
                SftpATTRS attrs = entry.getAttrs();

                if (attrs.isDir()) {
                    scanSftpDirectory(channel, fullPath, files);
                } else {
                    String extension = getFileExtension(name);
                    if (isSupportedExtension(extension)) {
                        LocalDateTime modTime = LocalDateTime.ofInstant(
                                Instant.ofEpochSecond(attrs.getMTime()),
                                ZoneId.systemDefault()
                        );
                        files.add(new FileMetadataDTO(
                                fullPath,
                                name,
                                extension,
                                attrs.getSize(),
                                modTime
                        ));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to scan SFTP directory {}: {}", path, e.getMessage());
        }
    }

    private List<FileMetadataDTO> scanFtpFiles(FileServerConnection connection, String password) {
        List<FileMetadataDTO> files = new ArrayList<>();
        FTPClient ftpClient = new FTPClient();

        try {
            ftpClient.setConnectTimeout(CONNECTION_TIMEOUT);
            ftpClient.connect(connection.getHost(), connection.getPort());
            ftpClient.login(connection.getUsername(), password);
            ftpClient.enterLocalPassiveMode();

            scanFtpDirectory(ftpClient, connection.getDefaultPath(), files);

        } catch (IOException e) {
            log.error("FTP scan failed for connection {}: {}", connection.getId(), e.getMessage());
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException e) {
                log.warn("Failed to disconnect FTP client: {}", e.getMessage());
            }
        }

        return files;
    }

    private void scanFtpDirectory(FTPClient ftpClient, String path, List<FileMetadataDTO> files) {
        try {
            FTPFile[] ftpFiles = ftpClient.listFiles(path);

            for (FTPFile ftpFile : ftpFiles) {
                String name = ftpFile.getName();
                if (".".equals(name) || "..".equals(name)) {
                    continue;
                }

                String fullPath = path.endsWith("/") ? path + name : path + "/" + name;

                if (ftpFile.isDirectory()) {
                    scanFtpDirectory(ftpClient, fullPath, files);
                } else {
                    String extension = getFileExtension(name);
                    if (isSupportedExtension(extension)) {
                        LocalDateTime modTime = ftpFile.getTimestamp() != null
                                ? LocalDateTime.ofInstant(ftpFile.getTimestamp().toInstant(), ZoneId.systemDefault())
                                : LocalDateTime.now();
                        files.add(new FileMetadataDTO(
                                fullPath,
                                name,
                                extension,
                                ftpFile.getSize(),
                                modTime
                        ));
                    }
                }
            }
        } catch (IOException e) {
            log.warn("Failed to scan FTP directory {}: {}", path, e.getMessage());
        }
    }

    private List<FileMetadataDTO> scanWebDavFiles(FileServerConnection connection, String password) {
        List<FileMetadataDTO> files = new ArrayList<>();
        Sardine sardine = null;

        try {
            sardine = SardineFactory.begin(connection.getUsername(), password);

            // WebDAV URL 구성
            String baseUrl = buildWebDavUrl(connection);
            String startPath = connection.getDefaultPath();
            if (startPath == null || startPath.isEmpty()) {
                startPath = "/";
            }

            String fullUrl = baseUrl + (startPath.startsWith("/") ? startPath : "/" + startPath);
            log.info("WebDAV scanning URL: {}", fullUrl);

            scanWebDavDirectory(sardine, baseUrl, startPath, files);

        } catch (Exception e) {
            log.error("WebDAV scan failed for connection {}: {}", connection.getId(), e.getMessage());
        } finally {
            if (sardine != null) {
                try {
                    sardine.shutdown();
                } catch (Exception e) {
                    log.warn("Failed to shutdown Sardine client: {}", e.getMessage());
                }
            }
        }

        return files;
    }

    private String buildWebDavUrl(FileServerConnection connection) {
        String host = connection.getHost();
        int port = connection.getPort();

        // 이미 URL 형태인 경우 (https://... 또는 http://...)
        if (host.startsWith("http://") || host.startsWith("https://")) {
            // 포트가 기본값이 아니면 추가
            if (port != 80 && port != 443 && !host.contains(":" + port)) {
                int protocolEnd = host.indexOf("://") + 3;
                int pathStart = host.indexOf("/", protocolEnd);
                if (pathStart == -1) {
                    return host + ":" + port;
                } else {
                    return host.substring(0, pathStart) + ":" + port + host.substring(pathStart);
                }
            }
            return host;
        }

        // host만 있는 경우 URL 구성
        String protocol = (port == 443) ? "https" : "http";
        if (port == 80 || port == 443) {
            return protocol + "://" + host;
        }
        return protocol + "://" + host + ":" + port;
    }

    private void scanWebDavDirectory(Sardine sardine, String baseUrl, String path, List<FileMetadataDTO> files) {
        try {
            String fullUrl = baseUrl + (path.startsWith("/") ? path : "/" + path);
            if (!fullUrl.endsWith("/")) {
                fullUrl += "/";
            }

            List<DavResource> resources = sardine.list(fullUrl, 1);  // Depth: 1 명시

            for (DavResource resource : resources) {
                String resourcePath = resource.getPath();

                // 현재 디렉토리 자체는 스킵
                if (resourcePath.equals(path) || resourcePath.equals(path + "/")) {
                    continue;
                }

                String name = resource.getName();
                if (name == null || name.isEmpty() || ".".equals(name) || "..".equals(name)) {
                    continue;
                }

                if (resource.isDirectory()) {
                    // 재귀적으로 하위 디렉토리 탐색
                    scanWebDavDirectory(sardine, baseUrl, resourcePath, files);
                } else {
                    String extension = getFileExtension(name);
                    if (isSupportedExtension(extension)) {
                        Date modified = resource.getModified();
                        LocalDateTime modTime = modified != null
                                ? LocalDateTime.ofInstant(modified.toInstant(), ZoneId.systemDefault())
                                : LocalDateTime.now();

                        Long contentLength = resource.getContentLength();
                        long fileSize = contentLength != null ? contentLength : 0L;

                        files.add(new FileMetadataDTO(
                                resourcePath,
                                name,
                                extension,
                                fileSize,
                                modTime
                        ));
                    }
                }
            }
        } catch (IOException e) {
            log.warn("Failed to scan WebDAV directory {}: {}", path, e.getMessage());
        }
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < fileName.length() - 1) {
            return fileName.substring(lastDot + 1).toLowerCase();
        }
        return "";
    }

    private boolean isSupportedExtension(String extension) {
        for (String supported : SUPPORTED_EXTENSIONS) {
            if (supported.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }
}
