package com.daruda.darudaserver.domain.user.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daruda.darudaserver.domain.community.service.BoardService;
import com.daruda.darudaserver.domain.user.dto.request.UpdateMyRequest;
import com.daruda.darudaserver.domain.user.dto.response.BoardListResponse;
import com.daruda.darudaserver.domain.user.dto.response.FavoriteToolsResponse;
import com.daruda.darudaserver.domain.user.dto.response.MyProfileResponse;
import com.daruda.darudaserver.domain.user.dto.response.UpdateMyResponse;
import com.daruda.darudaserver.domain.user.service.UserService;
import com.daruda.darudaserver.global.auth.UserId;
import com.daruda.darudaserver.global.common.response.ApiResponse;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.code.SuccessCode;
import com.daruda.darudaserver.global.error.exception.BusinessException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users/profile")
@RequiredArgsConstructor
public class MyPageController {
	private final UserService userService;
	private final BoardService boardService;

	@PatchMapping
	public ResponseEntity<ApiResponse<UpdateMyResponse>> patchMy(@UserId Long userId,
		@Valid @RequestBody UpdateMyRequest updateMyRequest) {
		if (updateMyRequest.positions() == null && updateMyRequest.nickname() == null) {
			throw new BusinessException(ErrorCode.MISSING_PARAMETER);
		}
		UpdateMyResponse updateMyResponse = userService.updateMy(userId, updateMyRequest.nickname(),
			updateMyRequest.positions());
		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(updateMyResponse, SuccessCode.SUCCESS_UPDATE));
	}

	@GetMapping("/tools")
	public ResponseEntity<ApiResponse<FavoriteToolsResponse>> getFavoriteTools(@UserId Long userId) {
		FavoriteToolsResponse favoriteToolsResponse = userService.getFavoriteTools(userId);

		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(favoriteToolsResponse, SuccessCode.SUCCESS_FETCH));
	}

	@GetMapping("/boards")
	public ResponseEntity<ApiResponse<BoardListResponse>> getMyBoards(@AuthenticationPrincipal Long userIdOrNull,
		@RequestParam(defaultValue = "1", value = "page") int pageNo,
		@RequestParam(defaultValue = "5", value = "size") int size,
		@RequestParam(defaultValue = "createdAt", value = "criteria") String criteria) {
		Pageable pageable = PageRequest.of(pageNo - 1, size, Sort.by(Sort.Direction.DESC, criteria));
		BoardListResponse boardListResponse = boardService.getMyBoards(userIdOrNull, pageable);
		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(boardListResponse, SuccessCode.SUCCESS_FETCH));
	}

	@GetMapping("/me")
	public ResponseEntity<ApiResponse<MyProfileResponse>> getMyProfile(@UserId Long userId) {
		MyProfileResponse myProfileResponse = userService.getMyInfo(userId);
		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(myProfileResponse, SuccessCode.SUCCESS_FETCH));
	}
}
