package com.lastcommit.piilot.domain.dbscan.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lastcommit.piilot.domain.dbscan.dto.response.UnencryptedRecordDTO;
import com.lastcommit.piilot.domain.dbscan.entity.DbPiiColumn;
import com.lastcommit.piilot.domain.dbscan.entity.DbServerConnection;
import com.lastcommit.piilot.domain.dbscan.entity.DbTable;
import com.lastcommit.piilot.domain.dbscan.entity.DbmsType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class UnencryptedDataFetcher {

    private static final int CONNECTION_TIMEOUT_SECONDS = 5;
    private static final int QUERY_TIMEOUT_SECONDS = 30;
    private static final int DEFAULT_LIMIT = 100;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<UnencryptedRecordDTO> fetch(
            DbServerConnection connection,
            DbTable table,
            DbPiiColumn piiColumn,
            String decryptedPassword,
            int limit
    ) {
        String rawKeys = piiColumn.getUnencRecordsKey();
        log.debug("unenc_records_key for column '{}': {}", piiColumn.getName(), rawKeys);

        List<Long> unencRecordKeys = parseUnencRecordKeys(rawKeys);
        if (unencRecordKeys.isEmpty()) {
            log.warn("No unencrypted record keys found for column '{}'. Raw value: {}", piiColumn.getName(), rawKeys);
            return Collections.emptyList();
        }

        log.debug("Parsed {} unencrypted record keys for column '{}'", unencRecordKeys.size(), piiColumn.getName());

        int effectiveLimit = limit > 0 ? limit : DEFAULT_LIMIT;
        List<Long> limitedKeys = unencRecordKeys.stream()
                .limit(effectiveLimit)
                .toList();

        DbmsType dbmsType = connection.getDbmsType();
        String jdbcUrl = dbmsType.getJdbcPrefix() + connection.getHost() + ":" + connection.getPort() + "/" + connection.getDbName();

        try {
            Class.forName(dbmsType.getDriverClassName());
            DriverManager.setLoginTimeout(CONNECTION_TIMEOUT_SECONDS);
        } catch (ClassNotFoundException e) {
            log.error("JDBC driver not found: {}", dbmsType.getDriverClassName(), e);
            return Collections.emptyList();
        }

        try (Connection conn = DriverManager.getConnection(jdbcUrl, connection.getUsername(), decryptedPassword)) {
            String pkColumn = getPrimaryKeyColumn(conn, table.getName(), connection.getDbName(), dbmsType);
            if (pkColumn == null) {
                // PK를 찾지 못한 경우 일반적인 패턴 시도
                pkColumn = findFallbackPkColumn(conn, table.getName());
            }

            if (pkColumn == null) {
                log.error("Could not determine primary key column for table '{}'. Cannot fetch unencrypted records.", table.getName());
                return Collections.emptyList();
            }

            List<UnencryptedRecordDTO> records = fetchRecords(conn, table.getName(), pkColumn, piiColumn.getName(), limitedKeys);
            log.debug("Fetched {} unencrypted records for column '{}'", records.size(), piiColumn.getName());
            return records;
        } catch (SQLException e) {
            log.error("Failed to fetch unencrypted data: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public List<UnencryptedRecordDTO> fetch(
            DbServerConnection connection,
            DbTable table,
            DbPiiColumn piiColumn,
            String decryptedPassword
    ) {
        return fetch(connection, table, piiColumn, decryptedPassword, DEFAULT_LIMIT);
    }

    private List<Long> parseUnencRecordKeys(String unencRecordsKey) {
        if (unencRecordsKey == null || unencRecordsKey.isBlank()) {
            return Collections.emptyList();
        }

        try {
            return objectMapper.readValue(unencRecordsKey, new TypeReference<List<Long>>() {});
        } catch (Exception e) {
            log.error("Failed to parse unencRecordsKey: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private String getPrimaryKeyColumn(Connection conn, String tableName, String dbName, DbmsType dbmsType) {
        try {
            DatabaseMetaData metaData = conn.getMetaData();

            // JDBC 표준 API로 PK 조회 (DB 종류와 관계없이 동작)
            try (ResultSet rs = metaData.getPrimaryKeys(dbName, null, tableName)) {
                if (rs.next()) {
                    String pkColumn = rs.getString("COLUMN_NAME");
                    log.debug("Found primary key column '{}' for table '{}'", pkColumn, tableName);
                    return pkColumn;
                }
            }

            // PostgreSQL의 경우 catalog가 null이어야 할 수 있음
            if (dbmsType.getName().toLowerCase().contains("postgres")) {
                try (ResultSet rs = metaData.getPrimaryKeys(null, "public", tableName)) {
                    if (rs.next()) {
                        String pkColumn = rs.getString("COLUMN_NAME");
                        log.debug("Found primary key column '{}' for table '{}' (public schema)", pkColumn, tableName);
                        return pkColumn;
                    }
                }
            }

            log.warn("No primary key found for table '{}', will use fallback", tableName);
        } catch (SQLException e) {
            log.warn("Failed to get primary key column for table {}: {}", tableName, e.getMessage());
        }

        return null;
    }

    private String findFallbackPkColumn(Connection conn, String tableName) {
        // 일반적인 PK 컬럼명 패턴들
        List<String> commonPkNames = List.of(
                "id",
                tableName + "_id",
                tableName.replace("_", "") + "_id",
                "seq",
                "no",
                "idx"
        );

        try {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getColumns(null, null, tableName, null)) {
                List<String> actualColumns = new ArrayList<>();
                while (rs.next()) {
                    actualColumns.add(rs.getString("COLUMN_NAME").toLowerCase());
                }

                // 일반적인 패턴 중 실제 존재하는 컬럼 찾기
                for (String pkName : commonPkNames) {
                    if (actualColumns.contains(pkName.toLowerCase())) {
                        log.info("Using fallback PK column '{}' for table '{}'", pkName, tableName);
                        return pkName;
                    }
                }

                // 첫 번째 컬럼을 PK로 가정 (보통 PK가 첫 번째 컬럼)
                if (!actualColumns.isEmpty()) {
                    String firstColumn = actualColumns.get(0);
                    log.info("Using first column '{}' as fallback PK for table '{}'", firstColumn, tableName);
                    return firstColumn;
                }
            }
        } catch (SQLException e) {
            log.warn("Failed to find fallback PK column for table {}: {}", tableName, e.getMessage());
        }

        return null;
    }

    private List<UnencryptedRecordDTO> fetchRecords(
            Connection conn,
            String tableName,
            String pkColumn,
            String piiColumn,
            List<Long> keys
    ) throws SQLException {
        if (keys.isEmpty()) {
            return Collections.emptyList();
        }

        String placeholders = keys.stream()
                .map(k -> "?")
                .collect(Collectors.joining(","));

        String sql = String.format(
                "SELECT %s, %s FROM %s WHERE %s IN (%s)",
                pkColumn, piiColumn, tableName, pkColumn, placeholders
        );

        List<UnencryptedRecordDTO> records = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setQueryTimeout(QUERY_TIMEOUT_SECONDS);
            for (int i = 0; i < keys.size(); i++) {
                stmt.setLong(i + 1, keys.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String pk = rs.getString(1);
                    String value = rs.getString(2);
                    records.add(new UnencryptedRecordDTO(pk, value));
                }
            }
        }

        return records;
    }
}
