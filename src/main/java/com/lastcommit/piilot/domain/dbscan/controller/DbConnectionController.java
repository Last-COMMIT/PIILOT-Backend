package com.lastcommit.piilot.domain.dbscan.controller;

import com.lastcommit.piilot.domain.dbscan.docs.DbConnectionControllerDocs;
import com.lastcommit.piilot.domain.dbscan.dto.request.DbConnectionRequestDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.DbConnectionDetailResponseDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.DbConnectionListResponseDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.DbConnectionResponseDTO;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import com.lastcommit.piilot.domain.dbscan.service.DbConnectionService;
import com.lastcommit.piilot.global.error.response.CommonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/db-connections")
@RequiredArgsConstructor
public class DbConnectionController implements DbConnectionControllerDocs {

    private final DbConnectionService dbConnectionService;

    @Override
    @PostMapping
    public CommonResponse<DbConnectionResponseDTO> createConnection(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody DbConnectionRequestDTO request
    ) {
        DbConnectionResponseDTO result = dbConnectionService.createConnection(userId, request);
        return CommonResponse.onCreated(result);
    }

    @Override
    @PutMapping("/{connectionId}")
    public CommonResponse<DbConnectionResponseDTO> updateConnection(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long connectionId,
            @Valid @RequestBody DbConnectionRequestDTO request
    ) {
        DbConnectionResponseDTO result = dbConnectionService.updateConnection(userId, connectionId, request);
        return CommonResponse.onSuccess(result);
    }

    @Override
    @DeleteMapping("/{connectionId}")
    public CommonResponse<Void> deleteConnection(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long connectionId
    ) {
        dbConnectionService.deleteConnection(userId, connectionId);
        return CommonResponse.onSuccess(null);
    }

    @Override
    @GetMapping("/{connectionId}")
    public CommonResponse<DbConnectionDetailResponseDTO> getConnectionDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long connectionId
    ) {
        DbConnectionDetailResponseDTO result = dbConnectionService.getConnectionDetail(userId, connectionId);
        return CommonResponse.onSuccess(result);
    }

    @Override
    @GetMapping
    public CommonResponse<Slice<DbConnectionListResponseDTO>> getConnectionList(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Slice<DbConnectionListResponseDTO> result = dbConnectionService.getConnectionList(userId, PageRequest.of(page, size));
        return CommonResponse.onSuccess(result);
    }
}
