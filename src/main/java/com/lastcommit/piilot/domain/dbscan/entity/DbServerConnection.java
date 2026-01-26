package com.lastcommit.piilot.domain.dbscan.entity;

import com.lastcommit.piilot.domain.shared.BaseEntity;
import com.lastcommit.piilot.domain.shared.ConnectionStatus;
import com.lastcommit.piilot.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "db_server_connections")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DbServerConnection extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dbms_type_id", nullable = false)
    private DbmsType dbmsType;

    @Column(name = "connection_name", nullable = false, length = 100)
    private String connectionName;

    @Column(nullable = false)
    private String host;

    @Column(nullable = false)
    private Integer port;

    @Column(name = "db_name", nullable = false, length = 100)
    private String dbName;

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

    @Builder
    private DbServerConnection(User user, DbmsType dbmsType, String connectionName,
                               String host, Integer port, String dbName,
                               String username, String encryptedPassword,
                               String managerName, String managerEmail, ConnectionStatus status) {
        this.user = user;
        this.dbmsType = dbmsType;
        this.connectionName = connectionName;
        this.host = host;
        this.port = port;
        this.dbName = dbName;
        this.username = username;
        this.encryptedPassword = encryptedPassword;
        this.managerName = managerName;
        this.managerEmail = managerEmail;
        this.status = status;
    }

    public void updateConnectionInfo(DbmsType dbmsType, String connectionName,
                                     String host, Integer port, String dbName,
                                     String username, String encryptedPassword,
                                     String managerName, String managerEmail, ConnectionStatus status) {
        this.dbmsType = dbmsType;
        this.connectionName = connectionName;
        this.host = host;
        this.port = port;
        this.dbName = dbName;
        this.username = username;
        this.encryptedPassword = encryptedPassword;
        this.managerName = managerName;
        this.managerEmail = managerEmail;
        this.status = status;
    }

    public void updateStatus(ConnectionStatus status) {
        this.status = status;
    }
}
