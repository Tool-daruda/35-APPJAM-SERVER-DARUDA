package com.daruda.darudaserver.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record SignUpRequest(
        @NotNull(message = "닉네임은 필수 입력값입니다")
        String nickname,

        @NotNull(message = "소속은 필수 입력값입니다")
        String positions,

        @NotNull(message = "이메일은 필수 입력값입니다")
        @Email(message = "이메일 형식에 맞지 않습니다")
        String email
) {
}
