package com.lastcommit.piilot.domain.dbscan.controller;

import com.lastcommit.piilot.domain.dbscan.docs.DbConnectionControllerDocs;
import com.lastcommit.piilot.domain.dbscan.dto.request.DbConnectionRequestDTO;
import com.lastcommit.piilot.domain.dbscan.dto.response.DbConnectionResponseDTO;
import com.lastcommit.piilot.domain.dbscan.service.DbConnectionService;
import com.lastcommit.piilot.global.error.response.CommonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
