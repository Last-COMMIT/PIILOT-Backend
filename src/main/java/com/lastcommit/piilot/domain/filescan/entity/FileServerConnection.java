package com.lastcommit.piilot.domain.filescan.entity;

import com.lastcommit.piilot.domain.shared.BaseEntity;
import com.lastcommit.piilot.domain.shared.ConnectionStatus;
import com.lastcommit.piilot.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "file_server_connections",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "connection_name"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileServerConnection extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_type_id", nullable = false)
    private FileServerType serverType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "connection_name", nullable = false, length = 100)
    private String connectionName;

    @Column(nullable = false)
    private String host;

    @Column(nullable = false)
    private Integer port;

    @Column(name = "default_path", nullable = false, length = 255)
    private String defaultPath;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(name = "encrypted_password", nullable = false, length = 255)
    private String encryptedPassword;

    @Column(name = "manager_name", nullable = false, length = 100)
    private String managerName;

    @Column(name = "manager_email", nullable = false, length = 255)
    private String managerEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConnectionStatus status;

    @Column(name = "retention_period_months", nullable = false)
    private Integer retentionPeriodMonths;

    @Column(name = "is_scanning", nullable = false)
    private Boolean isScanning = false;

    @Builder
    private FileServerConnection(FileServerType serverType, User user, String connectionName,
                                  String host, Integer port, String defaultPath,
                                  String username, String encryptedPassword,
                                  String managerName, String managerEmail,
                                  ConnectionStatus status, Integer retentionPeriodMonths) {
        this.serverType = serverType;
        this.user = user;
        this.connectionName = connectionName;
        this.host = host;
        this.port = port;
        this.defaultPath = defaultPath;
        this.username = username;
        this.encryptedPassword = encryptedPassword;
        this.managerName = managerName;
        this.managerEmail = managerEmail;
        this.status = status;
        this.retentionPeriodMonths = retentionPeriodMonths;
    }

    public void updateConnectionInfo(FileServerType serverType, String connectionName,
                                      String host, Integer port, String defaultPath,
                                      String username, String encryptedPassword,
                                      String managerName, String managerEmail,
                                      ConnectionStatus status, Integer retentionPeriodMonths) {
        this.serverType = serverType;
        this.connectionName = connectionName;
        this.host = host;
        this.port = port;
        this.defaultPath = defaultPath;
        this.username = username;
        this.encryptedPassword = encryptedPassword;
        this.managerName = managerName;
        this.managerEmail = managerEmail;
        this.status = status;
        this.retentionPeriodMonths = retentionPeriodMonths;
    }

    public void updateStatus(ConnectionStatus status) {
        this.status = status;
    }

    public void startScanning() {
        this.isScanning = true;
    }

    public void stopScanning() {
        this.isScanning = false;
    }
}
