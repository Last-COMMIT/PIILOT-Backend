package com.lastcommit.piilot.domain.dbscan.dto.request;

import jakarta.validation.constraints.*;

public record DbConnectionRequestDTO(
        @NotNull(message = "DBMS 유형은 필수입니다.")
        Integer dbmsTypeId,

        @NotBlank(message = "연결 이름은 필수입니다.")
        @Size(max = 100, message = "연결 이름은 100자 이하여야 합니다.")
        String connectionName,

        @NotBlank(message = "호스트는 필수입니다.")
        @Size(max = 255, message = "호스트는 255자 이하여야 합니다.")
        String host,

        @NotNull(message = "포트는 필수입니다.")
        @Min(value = 1, message = "포트는 1 이상이어야 합니다.")
        @Max(value = 65535, message = "포트는 65535 이하여야 합니다.")
        Integer port,

        @NotBlank(message = "데이터베이스 이름은 필수입니다.")
        @Size(max = 100, message = "데이터베이스 이름은 100자 이하여야 합니다.")
        String dbName,

        @NotBlank(message = "사용자명은 필수입니다.")
        @Size(max = 100, message = "사용자명은 100자 이하여야 합니다.")
        String username,

        String password,  // 생성 시 필수, 수정 시 선택 (null이면 기존 유지)

        @NotBlank(message = "담당자 이름은 필수입니다.")
        @Size(max = 100, message = "담당자 이름은 100자 이하여야 합니다.")
        String managerName,

        @NotBlank(message = "담당자 이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String managerEmail
) {}
