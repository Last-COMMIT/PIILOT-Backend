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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Slf4j
@Component
public class FileUploader {

    private static final int CONNECTION_TIMEOUT = 10000;

    public void upload(FileServerConnection connection, String decryptedPassword, String filePath, byte[] content) {
        String serverType = connection.getServerType().getName().toUpperCase();

        switch (serverType) {
            case "SFTP" -> uploadViaSftp(connection, decryptedPassword, filePath, content);
            case "FTP" -> uploadViaFtp(connection, decryptedPassword, filePath, content);
            case "WEBDAV" -> uploadViaWebDav(connection, decryptedPassword, filePath, content);
            default -> {
                log.error("Unsupported server type: {}", serverType);
                throw new GeneralException(FileMaskingErrorStatus.FILE_UPLOAD_FAILED);
            }
        }
    }

    public void delete(FileServerConnection connection, String decryptedPassword, String filePath) {
        String serverType = connection.getServerType().getName().toUpperCase();

        switch (serverType) {
            case "SFTP" -> deleteViaSftp(connection, decryptedPassword, filePath);
            case "FTP" -> deleteViaFtp(connection, decryptedPassword, filePath);
            case "WEBDAV" -> deleteViaWebDav(connection, decryptedPassword, filePath);
            default -> {
                log.error("Unsupported server type: {}", serverType);
                throw new GeneralException(FileMaskingErrorStatus.FILE_UPLOAD_FAILED);
            }
        }
    }

    private void uploadViaSftp(FileServerConnection connection, String password, String filePath, byte[] content) {
        Session session = null;
        ChannelSftp channel = null;

        try {
            JSch jsch = new JSch();
            session = jsch.getSession(connection.getUsername(), connection.getHost(), connection.getPort());
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(CONNECTION_TIMEOUT);

            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect(CONNECTION_TIMEOUT);

            try (InputStream is = new ByteArrayInputStream(content)) {
                channel.put(is, filePath);
            }

            log.info("SFTP upload successful: {}", filePath);

        } catch (Exception e) {
            log.error("SFTP upload failed for path {}: {}", filePath, e.getMessage());
            throw new GeneralException(FileMaskingErrorStatus.FILE_UPLOAD_FAILED);
        } finally {
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    private void deleteViaSftp(FileServerConnection connection, String password, String filePath) {
        Session session = null;
        ChannelSftp channel = null;

        try {
            JSch jsch = new JSch();
            session = jsch.getSession(connection.getUsername(), connection.getHost(), connection.getPort());
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(CONNECTION_TIMEOUT);

            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect(CONNECTION_TIMEOUT);

            channel.rm(filePath);
            log.info("SFTP delete successful: {}", filePath);

        } catch (Exception e) {
            log.error("SFTP delete failed for path {}: {}", filePath, e.getMessage());
            throw new GeneralException(FileMaskingErrorStatus.FILE_UPLOAD_FAILED);
        } finally {
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    private void uploadViaFtp(FileServerConnection connection, String password, String filePath, byte[] content) {
        FTPClient ftpClient = new FTPClient();

        try {
            ftpClient.setConnectTimeout(CONNECTION_TIMEOUT);
            ftpClient.connect(connection.getHost(), connection.getPort());
            ftpClient.login(connection.getUsername(), password);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);

            try (InputStream is = new ByteArrayInputStream(content)) {
                boolean success = ftpClient.storeFile(filePath, is);
                if (!success) {
                    throw new GeneralException(FileMaskingErrorStatus.FILE_UPLOAD_FAILED);
                }
            }

            log.info("FTP upload successful: {}", filePath);

        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            log.error("FTP upload failed for path {}: {}", filePath, e.getMessage());
            throw new GeneralException(FileMaskingErrorStatus.FILE_UPLOAD_FAILED);
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

    private void deleteViaFtp(FileServerConnection connection, String password, String filePath) {
        FTPClient ftpClient = new FTPClient();

        try {
            ftpClient.setConnectTimeout(CONNECTION_TIMEOUT);
            ftpClient.connect(connection.getHost(), connection.getPort());
            ftpClient.login(connection.getUsername(), password);
            ftpClient.enterLocalPassiveMode();

            boolean success = ftpClient.deleteFile(filePath);
            if (!success) {
                throw new GeneralException(FileMaskingErrorStatus.FILE_UPLOAD_FAILED);
            }

            log.info("FTP delete successful: {}", filePath);

        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            log.error("FTP delete failed for path {}: {}", filePath, e.getMessage());
            throw new GeneralException(FileMaskingErrorStatus.FILE_UPLOAD_FAILED);
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

    private void uploadViaWebDav(FileServerConnection connection, String password, String filePath, byte[] content) {
        Sardine sardine = null;

        try {
            sardine = SardineFactory.begin(connection.getUsername(), password);

            String fullUrl = buildWebDavUrl(connection) + filePath;
            log.debug("WebDAV uploading to: {}", fullUrl);

            sardine.put(fullUrl, content);
            log.info("WebDAV upload successful: {}", filePath);

        } catch (Exception e) {
            log.error("WebDAV upload failed for path {}: {}", filePath, e.getMessage());
            throw new GeneralException(FileMaskingErrorStatus.FILE_UPLOAD_FAILED);
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

    private void deleteViaWebDav(FileServerConnection connection, String password, String filePath) {
        Sardine sardine = null;

        try {
            sardine = SardineFactory.begin(connection.getUsername(), password);

            String fullUrl = buildWebDavUrl(connection) + filePath;
            log.debug("WebDAV deleting: {}", fullUrl);

            sardine.delete(fullUrl);
            log.info("WebDAV delete successful: {}", filePath);

        } catch (Exception e) {
            log.error("WebDAV delete failed for path {}: {}", filePath, e.getMessage());
            throw new GeneralException(FileMaskingErrorStatus.FILE_UPLOAD_FAILED);
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
