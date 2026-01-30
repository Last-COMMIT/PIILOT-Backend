package com.lastcommit.piilot.domain.dbscan.controller;

import com.lastcommit.piilot.domain.dbscan.docs.DbPiiControllerDocs;
import com.lastcommit.piilot.domain.dbscan.dto.response.DbPiiColumnListResponseDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.DbPiiConnectionResponseDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.DbPiiTableResponseDTO;
import com.lastcommit.piilot.domain.dbscan.service.DbPiiService;
import com.lastcommit.piilot.domain.shared.PiiCategory;
import com.lastcommit.piilot.domain.shared.RiskLevel;
import com.lastcommit.piilot.global.error.response.CommonResponse;import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/db-pii")
@RequiredArgsConstructor
@Validated
public class DbPiiController implements DbPiiControllerDocs {

    private final DbPiiService dbPiiService;

    @Override
    @GetMapping("/connections")
    public CommonResponse<List<DbPiiConnectionResponseDTO>> getConnections(
            @AuthenticationPrincipal Long userId
    ) {
        List<DbPiiConnectionResponseDTO> result = dbPiiService.getConnections(userId);
        return CommonResponse.onSuccess(result);
    }

    @Override
    @GetMapping("/connections/{connectionId}/tables")
    public CommonResponse<List<DbPiiTableResponseDTO>> getTables(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long connectionId
    ) {
        List<DbPiiTableResponseDTO> result = dbPiiService.getTables(userId, connectionId);
        return CommonResponse.onSuccess(result);
    }

    @Override
    @GetMapping("/columns")
    public CommonResponse<DbPiiColumnListResponseDTO> getPiiColumns(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) Long connectionId,
            @RequestParam(required = false) Long tableId,
            @RequestParam(required = false) PiiCategory piiType,
            @RequestParam(required = false) Boolean encrypted,
            @RequestParam(required = false) RiskLevel riskLevel,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        DbPiiColumnListResponseDTO result = dbPiiService.getPiiColumns(
                userId, connectionId, tableId, piiType, encrypted, riskLevel, keyword,
                PageRequest.of(page, size)
        );
        return CommonResponse.onSuccess(result);
    }
}
