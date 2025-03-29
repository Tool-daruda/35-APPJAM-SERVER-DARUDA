package com.daruda.darudaserver.domain.user.controller;

import static com.daruda.darudaserver.global.error.code.SuccessCode.*;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
		// String location = "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=" + clientId + "&redirect_uri=" + redirectUri;
		String location = "http://localhost:5173/oauth/kakao";

		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(location, SUCCESS_REDIRECT));
	}

	@PostMapping(value = "/token")
	public ResponseEntity<ApiResponse<LoginResponse>> postAuthenticationCode(
		@RequestHeader("Authorization") String code) {
		log.debug("CODE = {}", code);
		UserInfo userInfo = kakaoService.getInfo(code);
		LoginResponse loginResponse = userService.oauthlogin(userInfo);
		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(loginResponse, SuccessCode.SUCCESS_CREATE));
	}

	@PostMapping("/signup")
	public ResponseEntity<ApiResponse<SignUpSuccessResponse>> signUp(@Valid @RequestBody SignUpRequest signUpRequest) {
		SignUpSuccessResponse signUpSuccessResponse = userService.createUser(signUpRequest.email(),
			signUpRequest.nickname(), signUpRequest.positions());
		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(signUpSuccessResponse, SuccessCode.SUCCESS_LOGIN));
	}

	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<Long>> logOut(@UserId Long userId) {
		Long returnedUserId = userService.deleteUser(userId);
		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(returnedUserId, SuccessCode.SUCCESS_LOGOUT));
	}

	@PostMapping("/nickname")
	public ResponseEntity<ApiResponse<Boolean>> checkDuplicate(
		@NotNull(message = "닉네임은 필수입력값입니다") @RequestParam("nickname") String nickName) {
		boolean result = userService.isDuplicated(nickName);
		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(result, SuccessCode.SUCCESS_FETCH));
	}

	@PostMapping("/reissue")
	public ResponseEntity<ApiResponse<JwtTokenResponse>> regenerateToken(@UserId Long userId) {
		JwtTokenResponse jwtTokenResponse = userService.reissueToken(userId);
		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(jwtTokenResponse, SuccessCode.SUCCESS_REISSUE));
	}

	@DeleteMapping("/withdraw")
	public ResponseEntity<ApiResponse<?>> withdrawUser(@UserId Long userId) {
		userService.withdrawMe(userId);
		return ResponseEntity.ok(ApiResponse.ofSuccess(SuccessCode.SUCCESS_WITHDRAW));
	}

}
