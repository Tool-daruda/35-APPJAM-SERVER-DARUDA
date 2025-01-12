package com.daruda.darudaserver.global.auth.client;

import com.daruda.darudaserver.domain.user.dto.response.kakao.KakaoTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "kakaoApiFeignClient", url = "https://kauth.kakao.com")
public interface KakaoAPiFeignClient {
    @PostMapping("/oauth/token")
    KakaoTokenResponse getAccessToken(
            @RequestParam("grant_type") String grantType,
            @RequestParam("client_id") String clientId,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam("code") String code,
            @RequestHeader("Content-Type") String contentType
    );
}
