package com.lastcommit.piilot.domain.dbscan.controller;

import com.lastcommit.piilot.domain.dbscan.docs.DbScanControllerDocs;
import com.lastcommit.piilot.domain.dbscan.dto.response.DbScanResponseDTO;
import com.lastcommit.piilot.domain.dbscan.service.DbScanService;
import com.lastcommit.piilot.global.error.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/db-connections")
@RequiredArgsConstructor
public class DbScanController implements DbScanControllerDocs {

    private final DbScanService dbScanService;

    @Override
    @PostMapping("/{connectionId}/scan")
    public CommonResponse<DbScanResponseDTO> scanConnection(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long connectionId
    ) {
        DbScanResponseDTO result = dbScanService.scanConnection(connectionId);
        return CommonResponse.onCreated(result);
    }
}
