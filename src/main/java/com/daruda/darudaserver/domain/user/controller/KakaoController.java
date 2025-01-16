package com.daruda.darudaserver.domain.user.controller;

import com.daruda.darudaserver.domain.user.dto.request.NicknameRequest;
import com.daruda.darudaserver.domain.user.dto.request.SignUpRequest;
import com.daruda.darudaserver.domain.user.dto.response.JwtTokenResponse;
import com.daruda.darudaserver.domain.user.dto.response.LoginResponse;
import com.daruda.darudaserver.domain.user.dto.response.SignUpSuccessResponse;
import com.daruda.darudaserver.domain.user.dto.response.UserInfo;
import com.daruda.darudaserver.domain.user.dto.response.kakao.KakaoTokenResponse;
import com.daruda.darudaserver.domain.user.service.KakaoService;
import com.daruda.darudaserver.domain.user.service.UserService;
import com.daruda.darudaserver.global.auth.UserId;
import com.daruda.darudaserver.global.common.response.ApiResponse;
import com.daruda.darudaserver.global.error.code.SuccessCode;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RequestMapping("/api/v1/users")
@RestController
@RequiredArgsConstructor
public class KakaoController {
    private final KakaoService kakaoService;
    private final UserService userService;

    @Value("${kakao.client_id}")
    private String clientId;

    @Value("${kakao.redirect_uri}")
    private String redirectUri;

    /*
    @GetMapping("/kakao/login-url")
    public void requestLogin(HttpServletResponse response) throws IOException {
        String location = "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=" + clientId + "&redirect_uri=" + redirectUri;
        response.sendRedirect(location);
    }*/

    @PostMapping("/token")
    public ResponseEntity<?> postAuthenticationCode(@RequestHeader("code") final String code){
        UserInfo userInfo = kakaoService.getInfo(code);
        LoginResponse loginResponse = userService.oAuthLogin(userInfo);
        return ResponseEntity.ok(ApiResponse.ofSuccessWithData(loginResponse,SuccessCode.SUCCESS_CREATE));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpRequest signUpRequest){
        SignUpSuccessResponse signUpSuccessResponse = userService.createUser(signUpRequest.email(), signUpRequest.nickname(),signUpRequest.positions());
        return ResponseEntity.ok(ApiResponse.ofSuccessWithData(signUpSuccessResponse,SuccessCode.SUCCESS_LOGIN));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logOut(@UserId Long userId){
        Long returnedUserId = userService.deleteUser(userId);
        return  ResponseEntity.ok(ApiResponse.ofSuccessWithData(returnedUserId,SuccessCode.SUCCESS_LOGUT));
    }

    @PostMapping("/nickname")
    public ResponseEntity<?> checkDuplicate(@NotNull(message = "닉네임은 필수입력값입니다") @RequestBody NicknameRequest nickName){
        boolean result = userService.isDuplicated(nickName.getNickName());
        return ResponseEntity.ok(ApiResponse.ofSuccessWithData(result,SuccessCode.SUCCESS_FETCH));
    }

    @PostMapping("/reissue")
    public ResponseEntity<?> regenerateToken(@UserId Long userId){
        JwtTokenResponse jwtTokenResponse = userService.reissueToken(userId);
        return ResponseEntity.ok(ApiResponse.ofSuccessWithData(jwtTokenResponse,SuccessCode.SUCCESS_REISSUE));
    }
}
