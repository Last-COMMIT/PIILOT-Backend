package com.lastcommit.piilot.domain.regulation.client;

import com.lastcommit.piilot.domain.regulation.dto.request.AiRegulationSearchRequestDTO;
import com.lastcommit.piilot.domain.regulation.dto.response.AiRegulationSearchResponseDTO;
import com.lastcommit.piilot.domain.regulation.dto.response.AiRegulationSearchResponseDTO.AiReferenceDTO;
import com.lastcommit.piilot.domain.regulation.dto.response.PrincipleDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Profile("local")
public class StubRegulationSearchAiClient implements RegulationSearchAiClient {

    @Override
    public AiRegulationSearchResponseDTO search(AiRegulationSearchRequestDTO request) {
        log.info("[STUB] AI 서버 법령/내규 검색 요청: query={}", request.query());

        List<PrincipleDTO> principles = List.of(
                new PrincipleDTO("관련 원칙", List.of(
                        "목적 달성 시 파기: 개인정보 동의서를 받은 목적이 달성되면 즉시 파기하는 것이 원칙입니다.",
                        "법령상 근거: 다른 법령(전자상거래법 등)에 따라 보존해야 할 의무가 있는 경우, 해당 법령이 정한 기간(예: 5년) 동안 보관할 수 있습니다.",
                        "정보주체 동의 기간: 정보주체에게 동의받은 보유·이용 기간이 있다면 그 기간까지 보관합니다."
                ))
        );

        List<AiReferenceDTO> references = List.of(
                new AiReferenceDTO(1L, "개인정보처리방침_내부규정.pdf",
                        "본 규정은 회사가 처리하는 개인정보에 대하여 「개인정보 보호법」 및 관계 법령을 준수하기 위한 내부 기준을 정하고, 개인정보의 수집·이용, 보관 및 파기 절차를 명확히 함을 목적으로 합니다.", 0.95),
                new AiReferenceDTO(2L, "개인정보보호_관리지침.pdf",
                        "개인정보의 안전한 처리를 위하여 필요 관리적 부여 기준, 비밀번호 관리, 로그 기록 및 점검 등 기술적·관리적 보호조치를 수립하고 이를 준영함에 있어 필요한 사항을 규정합니다.", 0.90),
                new AiReferenceDTO(3L, "개인정보_수집-이용_보관_파기_기준.pdf",
                        "회사는 개인정보를 수집 시 명시한 목적이 달성된 때까지 해당 정보를 보유하는 것을 원칙으로 하며, 관련 법령에 따라 일정 기간 보존이 필요한 경우에는 그 기간 동안 안전하게 보관한 후 파기합니다.", 0.85),
                new AiReferenceDTO(4L, "개인정보 보호법.pdf",
                        "개인정보처리자의 정보인 이용목달성하기 위하여 필요한 경우로서 명확하게 정보주체에게 원리적다 지정하는 경우, 이 경우 개인정보처리자의 정당한 이익과 상당한 관련이 있고 합리적 범위를 초과하지 않습니다.", 0.80),
                new AiReferenceDTO(5L, "개인정보보호위원회 법령 목록(원문).pdf",
                        "개인정보보호위원회에서 제공하는 개인정보 관련 법령 전문 목록입니다.", 0.75)
        );

        String context = "개인정보 동의서 보관 주기는 '개인정보보호법' 및 '각 법령', 그리고 '개인정보 수집 시 정보주체에게 동의받은 기간'에 따라 달라지며, " +
                "일반적으로는 이용 목적 달성 시 즉시 파기를 원칙으로 하지만, 다른 법령에 근거하여 별도 보관하거나, " +
                "이용자 해지 시 고객 정보를 최대 6개월 보관하는 등 사안별로 정해진 기간이 다르므로 해당 기관의 개인정보처리방침을 확인해야 합니다.";

        log.info("[STUB] AI 서버 법령/내규 검색 완료: {} 참고문서", references.size());

        return new AiRegulationSearchResponseDTO(true, context, principles, references);
    }
}
