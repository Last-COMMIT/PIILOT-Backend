package com.lastcommit.piilot.domain.dashboard.controller;

import com.lastcommit.piilot.domain.dashboard.docs.DashboardControllerDocs;
import com.lastcommit.piilot.domain.dashboard.dto.response.DashboardSummaryResponseDTO;
import com.lastcommit.piilot.domain.dashboard.dto.response.DashboardTrendResponseDTO;
import com.lastcommit.piilot.domain.dashboard.service.DashboardService;
import com.lastcommit.piilot.global.error.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController implements DashboardControllerDocs {

    private final DashboardService dashboardService;

    @Override
    @GetMapping("/summary")
    public CommonResponse<DashboardSummaryResponseDTO> getSummary(
            @AuthenticationPrincipal Long userId
    ) {
        DashboardSummaryResponseDTO result = dashboardService.getSummary(userId);
        return CommonResponse.onSuccess(result);
    }

    @Override
    @GetMapping("/trends")
    public CommonResponse<DashboardTrendResponseDTO> getTrends(
            @AuthenticationPrincipal Long userId
    ) {
        DashboardTrendResponseDTO result = dashboardService.getTrends(userId);
        return CommonResponse.onSuccess(result);
    }
}
