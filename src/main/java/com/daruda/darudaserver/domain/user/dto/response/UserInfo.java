package com.daruda.darudaserver.domain.user.dto.response;

public record UserInfo(Long id, String email, String nickname) {
    public static UserInfo of(Long id, String email, String nickname){
        return new UserInfo(id, email, nickname);
    }
}
