package com.lastcommit.piilot.domain.dbscan.service;

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

        List<String> unencRecordKeys = parseUnencRecordKeys(rawKeys);
        if (unencRecordKeys.isEmpty()) {
            log.warn("No unencrypted record keys found for column '{}'. Raw value: {}", piiColumn.getName(), rawKeys);
            return Collections.emptyList();
        }

        log.debug("Parsed {} unencrypted record keys for column '{}'", unencRecordKeys.size(), piiColumn.getName());

        int effectiveLimit = limit > 0 ? limit : DEFAULT_LIMIT;
        List<String> limitedKeys = unencRecordKeys.stream()
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

            List<UnencryptedRecordDTO> records = fetchRecords(conn, dbmsType, table.getName(), pkColumn, piiColumn.getName(), limitedKeys);
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

    /**
     * unenc_records_key JSON 배열을 파싱합니다.
     * JSON 숫자/문자열 모두 지원하기 위해 List<?>로 파싱 후 String 변환합니다.
     */
    private List<String> parseUnencRecordKeys(String unencRecordsKey) {
        if (unencRecordsKey == null || unencRecordsKey.isBlank()) {
            return Collections.emptyList();
        }

        try {
            // JSON 숫자([1,2,3])와 문자열(["1","2","3"]) 모두 지원
            List<?> rawKeys = objectMapper.readValue(unencRecordsKey, List.class);
            return rawKeys.stream()
                    .map(String::valueOf)
                    .toList();
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

    /**
     * 비암호화 레코드를 조회합니다.
     * PK 컬럼 타입을 감지하여 적절한 Java 타입으로 변환 후 바인딩합니다.
     */
    private List<UnencryptedRecordDTO> fetchRecords(
            Connection conn,
            DbmsType dbmsType,
            String tableName,
            String pkColumn,
            String piiColumn,
            List<String> keys
    ) throws SQLException {
        if (keys.isEmpty()) {
            return Collections.emptyList();
        }

        // SQL 인젝션 방지: 식별자 검증
        validateIdentifier(tableName, "tableName");
        validateIdentifier(pkColumn, "pkColumn");
        validateIdentifier(piiColumn, "piiColumn");

        // PK 컬럼 타입 감지
        int pkColumnType = getPkColumnType(conn, tableName, pkColumn);
        log.debug("PK column '{}' type: {}", pkColumn, pkColumnType);

        String placeholders = keys.stream()
                .map(k -> "?")
                .collect(Collectors.joining(","));

        // DBMS별 식별자 인용 문자: MySQL=백틱(`), PostgreSQL/Oracle=쌍따옴표(")
        String q = getIdentifierQuote(dbmsType);
        String sql = String.format(
                "SELECT %s%s%s, %s%s%s FROM %s%s%s WHERE %s%s%s IN (%s)",
                q, pkColumn, q, q, piiColumn, q, q, tableName, q, q, pkColumn, q, placeholders
        );

        List<UnencryptedRecordDTO> records = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setQueryTimeout(QUERY_TIMEOUT_SECONDS);
            for (int i = 0; i < keys.size(); i++) {
                bindPkValue(stmt, i + 1, keys.get(i), pkColumnType);
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

    /**
     * PK 컬럼의 SQL 타입을 조회합니다.
     */
    private int getPkColumnType(Connection conn, String tableName, String pkColumn) {
        try {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getColumns(null, null, tableName, pkColumn)) {
                if (rs.next()) {
                    return rs.getInt("DATA_TYPE");
                }
            }
        } catch (SQLException e) {
            log.warn("Failed to get PK column type for {}.{}: {}", tableName, pkColumn, e.getMessage());
        }
        // 기본값: VARCHAR (문자열로 처리)
        return Types.VARCHAR;
    }

    /**
     * PK 컬럼 타입에 맞게 값을 변환하여 바인딩합니다.
     */
    private void bindPkValue(PreparedStatement stmt, int paramIndex, String value, int sqlType) throws SQLException {
        switch (sqlType) {
            case Types.BIGINT:
            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.TINYINT:
                // 숫자 타입: Long으로 변환
                try {
                    stmt.setLong(paramIndex, Long.parseLong(value));
                } catch (NumberFormatException e) {
                    log.warn("Failed to parse '{}' as Long, using String", value);
                    stmt.setString(paramIndex, value);
                }
                break;
            case Types.NUMERIC:
            case Types.DECIMAL:
                // 정밀 숫자 타입: BigDecimal로 변환
                try {
                    stmt.setBigDecimal(paramIndex, new java.math.BigDecimal(value));
                } catch (NumberFormatException e) {
                    log.warn("Failed to parse '{}' as BigDecimal, using String", value);
                    stmt.setString(paramIndex, value);
                }
                break;
            default:
                // VARCHAR, UUID, 기타: 문자열로 처리
                stmt.setString(paramIndex, value);
                break;
        }
    }

    /**
     * SQL 식별자(테이블명, 컬럼명)를 검증합니다.
     * SQL 인젝션 방지를 위해 허용된 문자만 포함되어 있는지 확인합니다.
     *
     * @param identifier 검증할 식별자
     * @param fieldName 로그용 필드명
     * @throws IllegalArgumentException 유효하지 않은 식별자인 경우
     */
    private void validateIdentifier(String identifier, String fieldName) {
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or blank");
        }

        // 허용 패턴: 알파벳, 숫자, 언더스코어, 한글 (일부 DB는 한글 테이블명 허용)
        // 인용 문자(", `)는 SQL에서 특별한 의미를 가지므로 허용하지 않음
        if (!identifier.matches("^[a-zA-Z0-9_가-힣]+$")) {
            log.error("Invalid SQL identifier detected: {} = '{}'", fieldName, identifier);
            throw new IllegalArgumentException("Invalid SQL identifier: " + fieldName);
        }
    }

    /**
     * DBMS별 식별자 인용 문자를 반환합니다.
     * - MySQL: 백틱(`)
     * - PostgreSQL, Oracle: 쌍따옴표(")
     */
    private String getIdentifierQuote(DbmsType dbmsType) {
        if (dbmsType != null && dbmsType.getName().toLowerCase().contains("mysql")) {
            return "`";
        }
        // PostgreSQL, Oracle 등은 SQL 표준 쌍따옴표 사용
        return "\"";
    }
}
