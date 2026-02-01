package com.lastcommit.piilot.domain.dbscan.service;

import com.lastcommit.piilot.domain.dbscan.dto.request.EncryptionCheckRequestDTO;
import com.lastcommit.piilot.domain.dbscan.dto.request.PiiIdentificationRequestDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.EncryptionCheckResponseDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.PiiIdentificationResponseDTO;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Profile("local")
public class StubAiServerClient implements AiServerClient {

    private static final String[] STUB_PII_KEYWORDS = {"name", "email", "phone", "address", "ssn", "passport", "account"};
    private static final String[] STUB_PII_TYPES = {"NM", "EM", "PH", "ADD", "RRN", "PP", "ACN"};

    @Override
    public PiiIdentificationResponseDTO identifyPiiColumns(PiiIdentificationRequestDTO request) {
        List<PiiIdentificationResponseDTO.PiiColumnResult> results = new ArrayList<>();

        for (PiiIdentificationRequestDTO.TableColumns table : request.tables()) {
            for (String column : table.columns()) {
                String lowerColumn = column.toLowerCase();
                for (int i = 0; i < STUB_PII_KEYWORDS.length; i++) {
                    if (lowerColumn.contains(STUB_PII_KEYWORDS[i])) {
                        results.add(new PiiIdentificationResponseDTO.PiiColumnResult(
                                table.tableName(), column, STUB_PII_TYPES[i]));
                        break;
                    }
                }
            }
        }

        return new PiiIdentificationResponseDTO(results);
    }

    @Override
    public EncryptionCheckResponseDTO checkEncryption(EncryptionCheckRequestDTO request) {
        List<EncryptionCheckResponseDTO.EncryptionResult> results = new ArrayList<>();

        for (EncryptionCheckRequestDTO.PiiColumnInfo piiColumn : request.piiColumns()) {
            long totalRecords = 100L;
            long encRecords = 80L;
            // 실제 테스트 DB의 PK 값 범위에 맞춘 테스트 데이터
            // String 타입으로 변경하여 UUID/문자열 PK도 지원
            List<String> unencKeys = List.of(
                    "2000000", "2000001", "2000002", "2000003", "2000004",
                    "2000005", "2000006", "2000007", "2000008", "2000009",
                    "2000010", "2000011", "2000012", "2000013", "2000014",
                    "2000015", "2000016", "2000017", "2000018", "2000019"
            );

            results.add(new EncryptionCheckResponseDTO.EncryptionResult(
                    piiColumn.tableName(), piiColumn.columnName(), piiColumn.piiType(),
                    totalRecords, encRecords, unencKeys));
        }

        return new EncryptionCheckResponseDTO(results);
    }
}
