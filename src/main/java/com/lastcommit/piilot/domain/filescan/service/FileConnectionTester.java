package com.lastcommit.piilot.domain.filescan.service;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class FileConnectionTester {

    private static final int CONNECTION_TIMEOUT_MS = 5000;

    public boolean testConnection(String serverTypeName, String host, Integer port,
                                   String username, String password, String defaultPath) {
        return switch (serverTypeName.toUpperCase()) {
            case "FTP" -> testFtpConnection(host, port, username, password);
            case "SFTP" -> testSftpConnection(host, port, username, password, defaultPath);
            default -> false;
        };
    }

    private boolean testFtpConnection(String host, Integer port, String username, String password) {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.setConnectTimeout(CONNECTION_TIMEOUT_MS);
            ftpClient.connect(host, port);

            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                return false;
            }

            boolean loginSuccess = ftpClient.login(username, password);
            if (loginSuccess) {
                ftpClient.logout();
            }
            return loginSuccess;
        } catch (IOException e) {
            return false;
        } finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private boolean testSftpConnection(String host, Integer port, String username,
                                        String password, String defaultPath) {
        Session session = null;
        ChannelSftp channelSftp = null;

        try {
            JSch jsch = new JSch();
            session = jsch.getSession(username, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setTimeout(CONNECTION_TIMEOUT_MS);
            session.connect(CONNECTION_TIMEOUT_MS);

            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect(CONNECTION_TIMEOUT_MS);

            channelSftp.cd(defaultPath);

            return true;
        } catch (Exception e) {
            return false;
        } finally {
            if (channelSftp != null && channelSftp.isConnected()) {
                channelSftp.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }
}
