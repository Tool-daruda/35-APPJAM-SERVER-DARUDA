package com.daruda.darudaserver.domain.user.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NicknameRequest {
    @NotBlank(message = "닉네임은 필수 입력값입니다.")
    private String nickName;
}
