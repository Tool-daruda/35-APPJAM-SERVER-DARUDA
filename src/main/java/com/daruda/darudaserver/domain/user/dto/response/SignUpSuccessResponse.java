package com.daruda.darudaserver.domain.user.dto.response;

public record SignUpSuccessResponse(
        String nickname,
        String positions,
        String email,
        JwtTokenResponse jwtTokenResponse
) {
    public static SignUpSuccessResponse of(String nickname, String positions, String email, JwtTokenResponse jwtTokenResponse){
        return new SignUpSuccessResponse(nickname, positions, email, jwtTokenResponse);
    }
}
