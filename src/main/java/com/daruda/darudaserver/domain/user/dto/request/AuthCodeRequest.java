package com.daruda.darudaserver.domain.user.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AuthCodeRequest {
    @NotNull(message = "인가코드가 비었습니다")
    String code;
}
