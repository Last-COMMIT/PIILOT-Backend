package com.lastcommit.piilot.domain.filescan.service;

import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.lastcommit.piilot.domain.filescan.entity.FileServerConnection;
import com.lastcommit.piilot.domain.filescan.exception.FileMaskingErrorStatus;
import com.lastcommit.piilot.global.error.exception.GeneralException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Slf4j
@Component
public class FileDownloader {

    private static final int CONNECTION_TIMEOUT = 10000;
    private static final int BUFFER_SIZE = 8192;

    public byte[] download(FileServerConnection connection, String decryptedPassword, String filePath) {
        String serverType = connection.getServerType().getName().toUpperCase();

        return switch (serverType) {
            case "SFTP" -> downloadViaSftp(connection, decryptedPassword, filePath);
            case "FTP" -> downloadViaFtp(connection, decryptedPassword, filePath);
            case "WEBDAV" -> downloadViaWebDav(connection, decryptedPassword, filePath);
            default -> {
                log.error("Unsupported server type: {}", serverType);
                throw new GeneralException(FileMaskingErrorStatus.FILE_DOWNLOAD_FAILED);
            }
        };
    }

    private byte[] downloadViaSftp(FileServerConnection connection, String password, String filePath) {
        Session session = null;
        ChannelSftp channel = null;

        try {
            JSch jsch = new JSch();
            session = jsch.getSession(connection.getUsername(), connection.getHost(), connection.getPort());
            session.setPassword(password);
            // 내부구축형 시스템으로 신뢰할 수 있는 내부 네트워크에서만 운영됨
            // 운영 환경 보안 강화 필요 시 known_hosts 기반 검증으로 전환 가능
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(CONNECTION_TIMEOUT);

            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect(CONNECTION_TIMEOUT);

            try (InputStream is = channel.get(filePath);
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
                return baos.toByteArray();
            }

        } catch (Exception e) {
            log.error("SFTP download failed for path {}: {}", filePath, e.getMessage());
            throw new GeneralException(FileMaskingErrorStatus.FILE_DOWNLOAD_FAILED);
        } finally {
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    private byte[] downloadViaFtp(FileServerConnection connection, String password, String filePath) {
        FTPClient ftpClient = new FTPClient();

        try {
            ftpClient.setConnectTimeout(CONNECTION_TIMEOUT);
            ftpClient.connect(connection.getHost(), connection.getPort());
            ftpClient.login(connection.getUsername(), password);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);

            try (InputStream is = ftpClient.retrieveFileStream(filePath);
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                if (is == null) {
                    throw new GeneralException(FileMaskingErrorStatus.FILE_DOWNLOAD_FAILED);
                }
                byte[] buffer = new byte[BUFFER_SIZE];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
                if (!ftpClient.completePendingCommand()) {
                    log.error("FTP transfer failed: server returned error response");
                    throw new GeneralException(FileMaskingErrorStatus.FILE_DOWNLOAD_FAILED);
                }
                return baos.toByteArray();
            }

        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            log.error("FTP download failed for path {}: {}", filePath, e.getMessage());
            throw new GeneralException(FileMaskingErrorStatus.FILE_DOWNLOAD_FAILED);
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (Exception e) {
                log.warn("Failed to disconnect FTP client: {}", e.getMessage());
            }
        }
    }

    private byte[] downloadViaWebDav(FileServerConnection connection, String password, String filePath) {
        Sardine sardine = null;

        try {
            sardine = SardineFactory.begin(connection.getUsername(), password);

            String fullUrl = buildWebDavUrl(connection) + filePath;
            log.debug("WebDAV downloading from: {}", fullUrl);

            try (InputStream is = sardine.get(fullUrl);
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
                return baos.toByteArray();
            }

        } catch (Exception e) {
            log.error("WebDAV download failed for path {}: {}", filePath, e.getMessage());
            throw new GeneralException(FileMaskingErrorStatus.FILE_DOWNLOAD_FAILED);
        } finally {
            if (sardine != null) {
                try {
                    sardine.shutdown();
                } catch (Exception e) {
                    log.warn("Failed to shutdown Sardine client: {}", e.getMessage());
                }
            }
        }
    }

    private String buildWebDavUrl(FileServerConnection connection) {
        String host = connection.getHost();
        int port = connection.getPort();

        if (host.startsWith("http://") || host.startsWith("https://")) {
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

        String protocol = (port == 443) ? "https" : "http";
        if (port == 80 || port == 443) {
            return protocol + "://" + host;
        }
        return protocol + "://" + host + ":" + port;
    }
}
