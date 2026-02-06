package com.lastcommit.piilot.domain.dbscan.service;

import com.lastcommit.piilot.domain.dbscan.entity.DbmsType;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@Component
public class DbConnectionTester {

    private static final int CONNECTION_TIMEOUT_SECONDS = 5;

    public boolean testConnection(DbmsType dbmsType, String host, Integer port,
                                  String dbName, String username, String password) {
        String jdbcUrl = dbmsType.getJdbcPrefix() + host + ":" + port + "/" + dbName;

        try {
            Class.forName(dbmsType.getDriverClassName());
            DriverManager.setLoginTimeout(CONNECTION_TIMEOUT_SECONDS);

            try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
                 Statement statement = connection.createStatement()) {
                statement.setQueryTimeout(CONNECTION_TIMEOUT_SECONDS);
                statement.execute(dbmsType.getTestQuery());
                return true;
            }
        } catch (ClassNotFoundException | SQLException e) {
            return false;
        }
    }
}
