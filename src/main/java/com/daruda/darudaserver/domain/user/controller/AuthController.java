package com.daruda.darudaserver.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daruda.darudaserver.domain.user.dto.request.LoginRequest;
import com.daruda.darudaserver.domain.user.dto.request.SignUpRequest;
import com.daruda.darudaserver.domain.user.dto.response.JwtTokenResponse;
import com.daruda.darudaserver.domain.user.dto.response.LoginResponse;
import com.daruda.darudaserver.domain.user.dto.response.LoginSuccessResponse;
import com.daruda.darudaserver.domain.user.dto.response.SignUpResponse;
import com.daruda.darudaserver.domain.user.dto.response.SignUpSuccessResponse;
import com.daruda.darudaserver.domain.user.dto.response.TokenResponse;
import com.daruda.darudaserver.domain.user.dto.response.UserInformationResponse;
import com.daruda.darudaserver.domain.user.entity.enums.SocialType;
import com.daruda.darudaserver.domain.user.service.AuthService;
import com.daruda.darudaserver.domain.user.service.SocialService;
import com.daruda.darudaserver.global.annotation.DisableSwaggerSecurity;
import com.daruda.darudaserver.global.auth.cookie.CookieProvider;
import com.daruda.darudaserver.global.auth.jwt.service.TokenService;
import com.daruda.darudaserver.global.common.response.ApiResponse;
import com.daruda.darudaserver.global.error.code.SuccessCode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "auth 컨트롤러", description = "로그인과 관련된 API를 처리합니다.")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final TokenService tokenService;
	private final CookieProvider cookieProvider;

	@DisableSwaggerSecurity
	@GetMapping("/login-url")
	@Operation(summary = "소셜 로그인 URL 반환", description = "소셜 로그인 URL을 반환합니다.")
	public ResponseEntity<ApiResponse<String>> requestLoginUrl(
		@Parameter(description = "소셜 로그인 타입", example = "KAKAO")
		@RequestParam SocialType socialType
	) {
		SocialService socialService = authService.findSocialService(socialType);
		String redirectUrl = socialService.getLoginUrl();
		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(redirectUrl, SuccessCode.SUCCESS_REDIRECT));
	}

	@DisableSwaggerSecurity
	@PostMapping(value = "/login")
	@Operation(summary = "소셜 로그인",
		description = "소셜 로그인에서 발급받은 Authorization Code를 통해, 로그인을 진행합니다.")
	public ResponseEntity<ApiResponse<LoginResponse>> login(
		@Parameter(description = "Authorization Code", example = "1234")
		@RequestParam("code") String code,
		@RequestBody LoginRequest loginRequest,
		HttpServletResponse httpServletResponse
	) {
		log.debug("소셜 로그인 요청 수신 (socialType = {})", loginRequest.socialType());
		SocialService socialService = authService.findSocialService(loginRequest.socialType());
		UserInformationResponse userInformationResponse = socialService.getInfo(code);
		LoginSuccessResponse loginSuccessResponse = authService.login(userInformationResponse);

		JwtTokenResponse jwtTokenResponse = loginSuccessResponse.jwtTokenResponse();

		if (jwtTokenResponse != null) {
			cookieProvider.setTokenCookies(httpServletResponse,
				jwtTokenResponse.accessToken(),
				jwtTokenResponse.refreshToken()
			);
		}

		LoginResponse loginResponse = LoginResponse.from(loginSuccessResponse);

		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(loginResponse, SuccessCode.SUCCESS_LOGIN));
	}

	@DisableSwaggerSecurity
	@PostMapping("/sign-up")
	@Operation(summary = "회원 가입", description = "회원 가입을 진행합니다.")
	public ResponseEntity<ApiResponse<SignUpResponse>> register(
		@Valid @RequestBody SignUpRequest signUpRequest,
		HttpServletResponse httpServletResponse
	) {
		SignUpSuccessResponse signUpSuccessResponse = authService.register(signUpRequest.email(),
			signUpRequest.nickname(), signUpRequest.positions());

		JwtTokenResponse jwtTokenResponse = signUpSuccessResponse.jwtTokenResponse();

		if (jwtTokenResponse != null) {
			cookieProvider.setTokenCookies(httpServletResponse,
				jwtTokenResponse.accessToken(),
				jwtTokenResponse.refreshToken()
			);
		}

		SignUpResponse signUpResponse = SignUpResponse.from(signUpSuccessResponse);

		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(signUpResponse, SuccessCode.SUCCESS_CREATE));
	}

	@PostMapping("/logout")
	@Operation(summary = "로그아웃", description = "로그아웃을 진행합니다.")
	public ResponseEntity<ApiResponse<Long>> logout(
		@AuthenticationPrincipal Long userId,
		HttpServletResponse httpServletResponse
	) {
		cookieProvider.deleteTokenCookies(httpServletResponse);

		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(authService.logout(userId), SuccessCode.SUCCESS_LOGOUT));
	}

	@DisableSwaggerSecurity
	@PostMapping("/reissue")
	@Operation(summary = "Access Token 재발급", description = "Refresh Token을 통해 Access Token을 재발급합니다.")
	public ResponseEntity<ApiResponse<TokenResponse>> reissueToken(HttpServletRequest httpServletRequest,
		HttpServletResponse httpServletResponse) {

		JwtTokenResponse tokenResponse = tokenService.reissueToken(httpServletRequest);

		cookieProvider.setTokenCookies(httpServletResponse,
			tokenResponse.accessToken(),
			tokenResponse.refreshToken()
		);

		return ResponseEntity.ok(ApiResponse.ofSuccess(SuccessCode.SUCCESS_REISSUE));
	}

	@DeleteMapping("/withdraw")
	@Operation(summary = "회원 탈퇴", description = "회원 탈퇴를 진행합니다.")
	public ResponseEntity<ApiResponse<Void>> withdraw(
		@AuthenticationPrincipal Long userId,
		HttpServletResponse httpServletResponse
	) {
		cookieProvider.deleteTokenCookies(httpServletResponse);
		authService.withdraw(userId);
		return ResponseEntity.ok(ApiResponse.ofSuccess(SuccessCode.SUCCESS_WITHDRAW));
	}
}
