package com.daruda.darudaserver.domain.user.service;

import com.daruda.darudaserver.domain.user.dto.response.kakao.KakaoTokenResponse;
import com.daruda.darudaserver.domain.user.dto.response.kakao.KakaoUserDto;
import com.daruda.darudaserver.domain.user.dto.response.UserInfo;
import com.daruda.darudaserver.global.auth.client.KakaoAPiFeignClient;
import com.daruda.darudaserver.global.auth.client.KakaoFeignClient;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.BusinessException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KakaoService {
    private final KakaoFeignClient kakaoFeignClient;
    private final KakaoAPiFeignClient kakaoAPiFeignClient;

    @Value("${kakao.client_id}")
    private String clientId;

    @Value("${kakao.redirect_uri}")
    private String redirectUri;

    private static final String KAUTH_TOKEN_URL_HOST = "https://kauth.kakao.com";
    private static final String KAUTH_USER_URL_HOST = "https://kapi.kakao.com";

    public String getAccessTokenFromKakao(String code){
        try{
            KakaoTokenResponse kakaoTokenResponse = kakaoAPiFeignClient.getAccessToken(
                    "authorization_code",
                    "65b8edb9273b17a701f937868170f95e",
                    "http://localhost:8080/api/v1/users/kakao/login-url",
                    code,
                    "application/x-www-form-urlencoded;charset=utf-8"
            );
            return kakaoTokenResponse.getAccessToken();
        } catch (Exception e){
            throw new BusinessException(ErrorCode.AUTHENTICATION_CODE_EXPIRED);
        }
    }
    public UserInfo getInfo(String code){
        String accessToken = getAccessTokenFromKakao(code);
        try{
            KakaoUserDto kakaoUserDto = kakaoFeignClient.getUserInformation(
                    "Bearer " + accessToken,
                    "application/x-www-form-urlencoded;charset=utf-8");
            return new UserInfo(
                    kakaoUserDto.id(),
                    kakaoUserDto.kakaoAccount().email(),
                    kakaoUserDto.kakaoAccount().kakaoUserProfile().nickname()
            );
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
