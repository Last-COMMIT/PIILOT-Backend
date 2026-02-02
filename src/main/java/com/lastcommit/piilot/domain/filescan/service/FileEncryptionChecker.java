package com.lastcommit.piilot.domain.filescan.service;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.lastcommit.piilot.domain.filescan.entity.FileServerConnection;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Component
public class FileEncryptionChecker {

    private static final int CONNECTION_TIMEOUT = 10000;
    private static final int MAX_FILE_SIZE_FOR_CHECK = 10 * 1024 * 1024; // 10MB

    public boolean isEncrypted(FileServerConnection connection, String decryptedPassword,
                                String filePath, String extension, Long fileSize) {
        if (fileSize != null && fileSize > MAX_FILE_SIZE_FOR_CHECK) {
            log.debug("File too large for encryption check: {}", filePath);
            return false;
        }

        // 암호화 확인 대상: pdf, docx (문서 파일 중 암호화 가능한 파일)
        // txt, 이미지, 오디오, 비디오는 암호화 확인 불필요
        return switch (extension.toLowerCase()) {
            case "pdf" -> isPdfEncrypted(connection, decryptedPassword, filePath);
            case "docx" -> isOoxmlEncrypted(connection, decryptedPassword, filePath);
            default -> false;
        };
    }

    private boolean isPdfEncrypted(FileServerConnection connection, String password, String filePath) {
        try (InputStream is = downloadFile(connection, password, filePath)) {
            if (is == null) {
                return false;
            }
            try (PDDocument doc = PDDocument.load(is)) {
                return doc.isEncrypted();
            }
        } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().contains("encrypted")) {
                return true;
            }
            log.debug("Failed to check PDF encryption for {}: {}", filePath, e.getMessage());
            return false;
        }
    }

    private boolean isOleEncrypted(FileServerConnection connection, String password, String filePath) {
        try (InputStream is = downloadFile(connection, password, filePath)) {
            if (is == null) {
                return false;
            }
            try (POIFSFileSystem fs = new POIFSFileSystem(is)) {
                return fs.getRoot().hasEntry("EncryptedPackage") ||
                       fs.getRoot().hasEntry("EncryptionInfo");
            }
        } catch (Exception e) {
            log.debug("Failed to check OLE encryption for {}: {}", filePath, e.getMessage());
            return false;
        }
    }

    private boolean isOoxmlEncrypted(FileServerConnection connection, String password, String filePath) {
        try (InputStream is = downloadFile(connection, password, filePath)) {
            if (is == null) {
                return false;
            }
            byte[] header = new byte[4];
            int bytesRead = is.read(header);
            if (bytesRead < 4) {
                return false;
            }
            // Check for OLE header (encrypted OOXML files are wrapped in OLE)
            // OLE magic number: D0 CF 11 E0
            return header[0] == (byte) 0xD0 &&
                   header[1] == (byte) 0xCF &&
                   header[2] == (byte) 0x11 &&
                   header[3] == (byte) 0xE0;
        } catch (Exception e) {
            log.debug("Failed to check OOXML encryption for {}: {}", filePath, e.getMessage());
            return false;
        }
    }

    private InputStream downloadFile(FileServerConnection connection, String password, String filePath) {
        String serverType = connection.getServerType().getName().toUpperCase();

        return switch (serverType) {
            case "SFTP" -> downloadFromSftp(connection, password, filePath);
            case "FTP" -> downloadFromFtp(connection, password, filePath);
            case "WEBDAV" -> downloadFromWebDav(connection, password, filePath);
            default -> null;
        };
    }

    private InputStream downloadFromSftp(FileServerConnection connection, String password, String filePath) {
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

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            channel.get(filePath, baos);

            return new ByteArrayInputStream(baos.toByteArray());
        } catch (Exception e) {
            log.debug("Failed to download file from SFTP {}: {}", filePath, e.getMessage());
            return null;
        } finally {
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    private InputStream downloadFromFtp(FileServerConnection connection, String password, String filePath) {
        FTPClient ftpClient = new FTPClient();

        try {
            ftpClient.setConnectTimeout(CONNECTION_TIMEOUT);
            ftpClient.connect(connection.getHost(), connection.getPort());
            ftpClient.login(connection.getUsername(), password);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            boolean success = ftpClient.retrieveFile(filePath, baos);

            if (success) {
                return new ByteArrayInputStream(baos.toByteArray());
            }
            return null;
        } catch (IOException e) {
            log.debug("Failed to download file from FTP {}: {}", filePath, e.getMessage());
            return null;
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
    }

    private InputStream downloadFromWebDav(FileServerConnection connection, String password, String filePath) {
        // WebDAV implementation pending
        log.debug("WebDAV download not yet implemented for {}", filePath);
        return null;
    }
}
