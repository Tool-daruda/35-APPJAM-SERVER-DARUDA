package com.daruda.darudaserver.domain.user.controller;

import com.daruda.darudaserver.domain.user.dto.request.AuthCodeRequest;
import com.daruda.darudaserver.domain.user.dto.request.NicknameRequest;
import com.daruda.darudaserver.domain.user.dto.request.SignUpRequest;
import com.daruda.darudaserver.domain.user.dto.response.JwtTokenResponse;
import com.daruda.darudaserver.domain.user.dto.response.LoginResponse;
import com.daruda.darudaserver.domain.user.dto.response.SignUpSuccessResponse;
import com.daruda.darudaserver.domain.user.dto.response.UserInfo;
import com.daruda.darudaserver.domain.user.service.KakaoService;
import com.daruda.darudaserver.domain.user.service.UserService;
import com.daruda.darudaserver.global.auth.UserId;
import com.daruda.darudaserver.global.common.response.ApiResponse;
import com.daruda.darudaserver.global.error.code.SuccessCode;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;

import static com.daruda.darudaserver.global.error.code.SuccessCode.SUCCESS_REDIRECT;

@Slf4j
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


    @GetMapping("/kakao/login-url")
    public ResponseEntity<ApiResponse<String>> requestLogin(HttpServletResponse response) throws IOException {
        String location = "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=" + clientId + "&redirect_uri=" + redirectUri;


        return ResponseEntity.ok(ApiResponse.ofSuccessWithData(location, SUCCESS_REDIRECT));
    }

    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<LoginResponse>> postAuthenticationCode(@RequestBody AuthCodeRequest authCodeRequest){
        log.debug("CODE = {}", authCodeRequest.getCode());
        UserInfo userInfo = kakaoService.getInfo(authCodeRequest.getCode());
        LoginResponse loginResponse = userService.oAuthLogin(userInfo);
        return ResponseEntity.ok(ApiResponse.ofSuccessWithData(loginResponse,SuccessCode.SUCCESS_CREATE));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignUpSuccessResponse>> signUp(@Valid @RequestBody SignUpRequest signUpRequest){
        SignUpSuccessResponse signUpSuccessResponse = userService.createUser(signUpRequest.email(), signUpRequest.nickname(),signUpRequest.positions());
        return ResponseEntity.ok(ApiResponse.ofSuccessWithData(signUpSuccessResponse,SuccessCode.SUCCESS_LOGIN));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Long>> logOut(@UserId Long userId){
        Long returnedUserId = userService.deleteUser(userId);
        return  ResponseEntity.ok(ApiResponse.ofSuccessWithData(returnedUserId,SuccessCode.SUCCESS_LOGOUT));
    }

    @PostMapping("/nickname")
    public ResponseEntity<ApiResponse<Boolean>> checkDuplicate(@NotNull(message = "닉네임은 필수입력값입니다") @RequestParam("nickname")String nickName){
        boolean result = userService.isDuplicated(nickName);
        return ResponseEntity.ok(ApiResponse.ofSuccessWithData(result,SuccessCode.SUCCESS_FETCH));
    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<JwtTokenResponse>> regenerateToken(@UserId Long userId){
        JwtTokenResponse jwtTokenResponse = userService.reissueToken(userId);
        return ResponseEntity.ok(ApiResponse.ofSuccessWithData(jwtTokenResponse,SuccessCode.SUCCESS_REISSUE));
    }

    @DeleteMapping("/withdraw")
    public ResponseEntity<ApiResponse<?>> withdrawUser(@UserId Long userId){
        userService.withdrawMe(userId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(SuccessCode.SUCCESS_WITHDRAW));
    }

}
