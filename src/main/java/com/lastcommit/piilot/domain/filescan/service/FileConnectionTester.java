package com.lastcommit.piilot.domain.filescan.service;

import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
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
            case "WEBDAV" -> testWebDavConnection(host, port, username, password, defaultPath);
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

    private boolean testWebDavConnection(String host, Integer port, String username,
                                          String password, String defaultPath) {
        Sardine sardine = null;
        try {
            sardine = SardineFactory.begin(username, password);

            // WebDAV URL 구성
            String baseUrl = buildWebDavUrl(host, port);
            String path = (defaultPath == null || defaultPath.isEmpty()) ? "/" : defaultPath;
            String fullUrl = baseUrl + (path.startsWith("/") ? path : "/" + path);

            if (!fullUrl.endsWith("/")) {
                fullUrl += "/";
            }

            // 디렉토리 목록 조회로 연결 테스트 (Depth: 1 명시)
            sardine.list(fullUrl, 1);
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            if (sardine != null) {
                try {
                    sardine.shutdown();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private String buildWebDavUrl(String host, int port) {
        // 이미 URL 형태인 경우
        if (host.startsWith("http://") || host.startsWith("https://")) {
            // 포트가 기본값이 아니고, URL에 포트가 없으면 추가
            if (port != 80 && port != 443 && !host.matches(".*:\\d+.*")) {
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
}
