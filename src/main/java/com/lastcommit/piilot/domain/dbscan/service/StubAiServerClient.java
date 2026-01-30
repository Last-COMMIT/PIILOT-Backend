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
            List<Long> unencKeys = List.of(1L, 5L, 23L, 42L, 57L, 68L, 73L, 81L, 90L, 95L,
                    101L, 115L, 128L, 142L, 155L, 168L, 181L, 194L, 207L, 220L);

            results.add(new EncryptionCheckResponseDTO.EncryptionResult(
                    piiColumn.tableName(), piiColumn.columnName(), piiColumn.piiType(),
                    totalRecords, encRecords, unencKeys));
        }

        return new EncryptionCheckResponseDTO(results);
    }
}
