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
import com.daruda.darudaserver.domain.user.dto.response.FavoriteBoardsRetrieveResponse;
import com.daruda.darudaserver.domain.user.dto.response.FavoriteToolsResponse;
import com.daruda.darudaserver.domain.user.dto.response.MyProfileResponse;
import com.daruda.darudaserver.domain.user.dto.response.UpdateMyResponse;
import com.daruda.darudaserver.domain.user.service.UserService;
import com.daruda.darudaserver.global.annotation.DisableSwaggerSecurity;
import com.daruda.darudaserver.global.common.response.ApiResponse;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.code.SuccessCode;
import com.daruda.darudaserver.global.error.exception.BusinessException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
@Tag(name = "user 컨트롤러", description = "사용자와 관련된 API를 처리합니다.")
public class UserController {
	private final UserService userService;
	private final BoardService boardService;

	@PatchMapping("/profile")
	@Operation(summary = "프로필 수정", description = "사용자의 프로필을 수정합니다.")
	public ResponseEntity<ApiResponse<UpdateMyResponse>> patchMy(@AuthenticationPrincipal Long userId,
		@Valid @RequestBody UpdateMyRequest updateMyRequest) {
		if (updateMyRequest.positions() == null && updateMyRequest.nickname() == null) {
			throw new BusinessException(ErrorCode.MISSING_PARAMETER);
		}
		UpdateMyResponse updateMyResponse = userService.updateMy(userId, updateMyRequest.nickname(),
			updateMyRequest.positions());
		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(updateMyResponse, SuccessCode.SUCCESS_UPDATE));
	}

	@GetMapping("/scrap-tools")
	@Operation(summary = "찜한 툴 목록 조회", description = "사용자의 찜한 툴 목록을 조회합니다.")
	public ResponseEntity<ApiResponse<FavoriteToolsResponse>> getFavoriteTools(@AuthenticationPrincipal Long userId) {
		FavoriteToolsResponse favoriteToolsResponse = userService.getFavoriteTools(userId);

		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(favoriteToolsResponse, SuccessCode.SUCCESS_FETCH));
	}

	@GetMapping("/boards")
	@Operation(summary = "작성한 게시글 목록 조회", description = "사용자가 작성한 게시글 목록을 조회합니다.")
	public ResponseEntity<ApiResponse<BoardListResponse>> getMyBoards(@AuthenticationPrincipal Long userIdOrNull,
		@Parameter(description = "조회할 페이지", example = "1")
		@RequestParam(defaultValue = "1", value = "page") int pageNo,
		@Parameter(description = "조회할 게시글 개수", example = "5")
		@RequestParam(defaultValue = "5", value = "size") int size,
		@Parameter(description = "정렬 기준", example = "createdAt")
		@RequestParam(defaultValue = "createdAt", value = "criteria") String criteria) {
		Pageable pageable = PageRequest.of(pageNo - 1, size, Sort.by(Sort.Direction.DESC, criteria));
		BoardListResponse boardListResponse = boardService.getMyBoards(userIdOrNull, pageable);
		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(boardListResponse, SuccessCode.SUCCESS_FETCH));
	}

	@GetMapping("/profile")
	@Operation(summary = "프로필 조회", description = "사용자의 프로필을 조회합니다.")
	public ResponseEntity<ApiResponse<MyProfileResponse>> getMyProfile(@AuthenticationPrincipal Long userId) {
		MyProfileResponse myProfileResponse = userService.getMyInfo(userId);
		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(myProfileResponse, SuccessCode.SUCCESS_FETCH));
	}

	@DisableSwaggerSecurity
	@GetMapping("/nickname")
	@Operation(summary = "닉네임 중복 확인", description = "닉네임 중복을 검사합니다.")
	public ResponseEntity<ApiResponse<Boolean>> checkDuplicate(
		@Parameter(description = "닉네임", example = "test")
		@NotNull(message = "닉네임은 필수 입력값입니다.") @RequestParam("nickname") String nickName) {
		boolean result = userService.isDuplicated(nickName);
		return ResponseEntity.ok(ApiResponse.ofSuccessWithData(result, SuccessCode.SUCCESS_FETCH));
	}

	@GetMapping("/scrap-tools")
	@Operation(summary = "스크랩 글 목록 조회", description = "스크랩 글 목록을 조회합니다.")
	public ResponseEntity<?> getFavoriteBoards(@AuthenticationPrincipal Long userIdOrNull,
		@Parameter(description = "조회할 페이지", example = "1")
		@RequestParam(value = "page", defaultValue = "1") int pageNo,
		@Parameter(description = "조회할 게시글 개수", example = "5")
		@RequestParam(value = "size", defaultValue = "5") int size,
		@Parameter(description = "정렬 기준", example = "createdAt")
		@RequestParam(value = "criteria", defaultValue = "createdAt") String criteria) {
		Pageable pageable = PageRequest.of(pageNo - 1, size, Sort.by(Sort.Direction.DESC, criteria));
		FavoriteBoardsRetrieveResponse favoriteBoardsRetrieveResponse = boardService.getFavoriteBoards(userIdOrNull,
			pageable);

		return ResponseEntity.ok(
			ApiResponse.ofSuccessWithData(favoriteBoardsRetrieveResponse, SuccessCode.SUCCESS_FETCH));
	}
}
