package com.lastcommit.piilot.domain.dbscan.controller;

import com.lastcommit.piilot.domain.dbscan.docs.DbPiiControllerDocs;
import com.lastcommit.piilot.domain.dbscan.dto.request.DbPiiSearchCondition;
import com.lastcommit.piilot.domain.dbscan.dto.response.DbPiiFilterOptionDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.DbPiiListResponseDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.DbPiiSummaryResponseDTO;
import com.lastcommit.piilot.domain.dbscan.service.DbPiiService;
import com.lastcommit.piilot.global.error.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/db-pii")
@RequiredArgsConstructor
public class DbPiiController implements DbPiiControllerDocs {

    private final DbPiiService dbPiiService;

    @Override
    @GetMapping("/summary")
    public CommonResponse<DbPiiSummaryResponseDTO> getSummary(
            @AuthenticationPrincipal Long userId
    ) {
        DbPiiSummaryResponseDTO result = dbPiiService.getSummary(userId);
        return CommonResponse.onSuccess(result);
    }

    @Override
    @GetMapping
    public CommonResponse<Slice<DbPiiListResponseDTO>> getPiiList(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long connectionId,
            @RequestParam(required = false) Long tableId,
            @RequestParam(required = false) String piiType,
            @RequestParam(required = false) Boolean encrypted,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) String keyword
    ) {
        DbPiiSearchCondition condition = new DbPiiSearchCondition(
                connectionId, tableId, piiType, encrypted, riskLevel, keyword
        );
        Slice<DbPiiListResponseDTO> result = dbPiiService.getPiiList(userId, condition, PageRequest.of(page, size));
        return CommonResponse.onSuccess(result);
    }

    @Override
    @GetMapping("/connections")
    public CommonResponse<List<DbPiiFilterOptionDTO>> getConnectionOptions(
            @AuthenticationPrincipal Long userId
    ) {
        List<DbPiiFilterOptionDTO> result = dbPiiService.getConnectionOptions(userId);
        return CommonResponse.onSuccess(result);
    }

    @Override
    @GetMapping("/tables")
    public CommonResponse<List<DbPiiFilterOptionDTO>> getTableOptions(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) Long connectionId
    ) {
        List<DbPiiFilterOptionDTO> result = dbPiiService.getTableOptions(userId, connectionId);
        return CommonResponse.onSuccess(result);
    }
}
