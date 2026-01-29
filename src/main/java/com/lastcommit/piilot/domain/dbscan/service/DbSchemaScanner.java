package com.lastcommit.piilot.domain.dbscan.service;

import com.lastcommit.piilot.domain.dbscan.dto.internal.SchemaTableInfoDTO;
import com.lastcommit.piilot.domain.dbscan.entity.DbServerConnection;
import com.lastcommit.piilot.domain.dbscan.entity.DbmsType;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class DbSchemaScanner {

    private static final int CONNECTION_TIMEOUT_SECONDS = 5;
    private static final int QUERY_TIMEOUT_SECONDS = 30;

    public List<SchemaTableInfoDTO> scanSchema(DbServerConnection connection, String decryptedPassword) {
        DbmsType dbmsType = connection.getDbmsType();
        String jdbcUrl = dbmsType.getJdbcPrefix() + connection.getHost() + ":" + connection.getPort() + "/" + connection.getDbName();

        try {
            Class.forName(dbmsType.getDriverClassName());
            DriverManager.setLoginTimeout(CONNECTION_TIMEOUT_SECONDS);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("JDBC driver not found: " + dbmsType.getDriverClassName(), e);
        }

        try (Connection conn = DriverManager.getConnection(jdbcUrl, connection.getUsername(), decryptedPassword)) {
            Map<String, List<String>> tableColumns = collectColumns(conn, connection.getDbName(), dbmsType);
            Map<String, String> changeSignatures = collectChangeSignatures(conn, connection.getDbName(), dbmsType);

            List<SchemaTableInfoDTO> results = new ArrayList<>();
            for (Map.Entry<String, List<String>> entry : tableColumns.entrySet()) {
                String tableName = entry.getKey();
                List<String> columns = entry.getValue();
                String signature = changeSignatures.getOrDefault(tableName, "");
                results.add(new SchemaTableInfoDTO(tableName, columns, columns.size(), signature));
            }
            return results;
        } catch (SQLException e) {
            throw new RuntimeException("Schema scan failed: " + e.getMessage(), e);
        }
    }

    private Map<String, List<String>> collectColumns(Connection conn, String dbName, DbmsType dbmsType) throws SQLException {
        Map<String, List<String>> tableColumns = new LinkedHashMap<>();
        boolean isPostgres = dbmsType.getName().toLowerCase().contains("postgres");
        String schema = isPostgres ? "public" : null;

        String sql = """
                SELECT t.table_name, c.column_name
                FROM information_schema.tables t
                JOIN information_schema.columns c ON t.table_name = c.table_name AND t.table_schema = c.table_schema
                WHERE t.table_schema = ? AND t.table_type = 'BASE TABLE'
                ORDER BY t.table_name, c.ordinal_position
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setQueryTimeout(QUERY_TIMEOUT_SECONDS);
            stmt.setString(1, isPostgres ? "public" : dbName);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String tableName = rs.getString("table_name");
                    String columnName = rs.getString("column_name");
                    tableColumns.computeIfAbsent(tableName, k -> new ArrayList<>()).add(columnName);
                }
            }
        }
        return tableColumns;
    }

    private Map<String, String> collectChangeSignatures(Connection conn, String dbName, DbmsType dbmsType) throws SQLException {
        Map<String, String> signatures = new LinkedHashMap<>();
        boolean isPostgres = dbmsType.getName().toLowerCase().contains("postgres");

        if (isPostgres) {
            String sql = """
                    SELECT relname AS table_name,
                           COALESCE(n_tup_ins, 0) + COALESCE(n_tup_upd, 0) + COALESCE(n_tup_del, 0) AS dml_count
                    FROM pg_stat_user_tables
                    WHERE schemaname = 'public'
                    """;
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setQueryTimeout(QUERY_TIMEOUT_SECONDS);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        signatures.put(rs.getString("table_name"), String.valueOf(rs.getLong("dml_count")));
                    }
                }
            }
        } else {
            String sql = """
                    SELECT table_name, COALESCE(update_time, create_time) AS change_time
                    FROM information_schema.tables
                    WHERE table_schema = ? AND table_type = 'BASE TABLE'
                    """;
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setQueryTimeout(QUERY_TIMEOUT_SECONDS);
                stmt.setString(1, dbName);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Timestamp changeTime = rs.getTimestamp("change_time");
                        signatures.put(rs.getString("table_name"),
                                changeTime != null ? changeTime.toString() : "");
                    }
                }
            }
        }
        return signatures;
    }
}
