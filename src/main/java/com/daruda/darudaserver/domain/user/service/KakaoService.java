package com.daruda.darudaserver.domain.user.service;

import com.daruda.darudaserver.domain.user.dto.response.kakao.KakaoTokenResponse;
import com.daruda.darudaserver.domain.user.dto.response.kakao.KakaoUserDto;
import com.daruda.darudaserver.domain.user.dto.response.UserInfo;
import com.daruda.darudaserver.global.auth.client.KakaoAPiFeignClient;
import com.daruda.darudaserver.global.auth.client.KakaoFeignClient;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoService {
    private final KakaoFeignClient kakaoFeignClient;
    private final KakaoAPiFeignClient kakaoAPiFeignClient;

    @Value("${kakao.client_id}")
    private String clientId;

    @Value("${kakao.redirect_uri}")
    private String redirectUri;

    public String getAccessTokenFromKakao(String code){
        log.debug("redirect uri {}", redirectUri);
        log.info("인가코드 {}", code);
        try{
            KakaoTokenResponse kakaoTokenResponse = kakaoAPiFeignClient.getAccessToken(
                    "authorization_code",
                    clientId,
                    "https://daruda.shop",
                    code,
                    "application/x-www-form-urlencoded;charset=utf-8"
            );
            log.debug("카카오로부터 AccessToken을 성공적으로 받았습니다, {}", kakaoTokenResponse.getAccessToken());
            return kakaoTokenResponse.getAccessToken();
        } catch (Exception e){
            log.error("Error on: ", e);
            throw new BusinessException(ErrorCode.AUTHENTICATION_CODE_EXPIRED);
        }
    }
    public UserInfo getInfo(String code){
        String accessToken = getAccessTokenFromKakao(code);
        try{
            KakaoUserDto kakaoUserDto = kakaoFeignClient.getUserInformation(
                    "Bearer " + accessToken,
                    "application/x-www-form-urlencoded;charset=utf-8");
            log.debug("카카오로부터 사용자 정보를 성공적으로 불러왔습니다. Id:, {}", kakaoUserDto.id());
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
