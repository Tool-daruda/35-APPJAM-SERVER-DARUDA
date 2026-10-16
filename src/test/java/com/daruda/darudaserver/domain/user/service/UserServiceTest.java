package com.daruda.darudaserver.domain.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daruda.darudaserver.domain.tool.entity.Category;
import com.daruda.darudaserver.domain.tool.entity.License;
import com.daruda.darudaserver.domain.tool.entity.PlanType;
import com.daruda.darudaserver.domain.tool.entity.Tool;
import com.daruda.darudaserver.domain.tool.entity.ToolScrap;
import com.daruda.darudaserver.domain.tool.repository.ToolScrapRepository;
import com.daruda.darudaserver.domain.tool.service.ToolService;
import com.daruda.darudaserver.domain.user.dto.response.FavoriteToolsResponse;
import com.daruda.darudaserver.domain.user.dto.response.MyProfileResponse;
import com.daruda.darudaserver.domain.user.dto.response.UpdateMyResponse;
import com.daruda.darudaserver.domain.user.entity.User;
import com.daruda.darudaserver.domain.user.entity.enums.Positions;
import com.daruda.darudaserver.domain.user.repository.UserRepository;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.BadRequestException;
import com.daruda.darudaserver.global.error.exception.BusinessException;
import com.daruda.darudaserver.global.error.exception.ConflictException;
import com.daruda.darudaserver.global.error.exception.NotFoundException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private ToolScrapRepository toolScrapRepository;

	@Mock
	private ToolService toolService;

	@InjectMocks
	private UserService userService;

	@Test
	@DisplayName("중복된 닉네임 체크 성공")
	void isDuplicatedNickname_success() {
		// given
		String nickname = "tester";
		when(userRepository.existsByNickname(nickname)).thenReturn(true);

		// when
		boolean result = userService.isDuplicatedNickname(nickname);

		// then
		assertTrue(result);
	}

	@Test
	@DisplayName("닉네임 중복 확인 - 중복되지 않은 경우")
	void isDuplicatedNickname_notDuplicated() {
		// given
		String nickname = "tester";
		when(userRepository.existsByNickname(nickname)).thenReturn(false);

		// when
		boolean result = userService.isDuplicatedNickname(nickname);

		// then
		assertFalse(result);
	}

	@Test
	@DisplayName("프로필 조회 성공")
	void getMyProfile_success() {
		// given
		Long userId = 1L;
		String email = "test@example.com";
		String nickname = "tester";
		Positions positions = Positions.STUDENT;
		User userEntity = User.of(email, nickname, positions);

		when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

		// when
		MyProfileResponse response = userService.getMyProfile(userId);

		// then
		assertEquals(nickname, response.nickname());
		assertEquals(positions, response.positions());
	}

	@Test
	@DisplayName("프로필 조회 실패 - 사용자 없음")
	void getMyProfile_userNotFound() {
		// given
		Long userId = 1L;
		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		// when & then
		NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.getMyProfile(userId));
		assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
	}

	@Test
	@DisplayName("좋아하는 툴 목록 조회 성공")
	void getFavoriteTools_success() {
		// given
		Long userId = 1L;
		String email = "test@example.com";
		String nickname = "tester";
		Positions positions = Positions.STUDENT;
		User userEntity = User.of(email, nickname, positions);

		Tool tool = Tool.builder()
			.toolMainName("tool1")
			.toolLogo("logo1")
			.description("desc1")
			.license(License.FREE)
			.category(Category.AI)
			.build();

		ToolScrap toolScrap = ToolScrap.of(userEntity, tool);

		when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
		when(toolScrapRepository.findAllByUserId(userId)).thenReturn(List.of(toolScrap));
		when(toolScrapRepository.findByUserAndTool(userEntity, tool)).thenReturn(Optional.of(toolScrap));
		when(toolService.convertToKeywordRes(tool)).thenReturn(List.of());

		// when
		FavoriteToolsResponse response = userService.getFavoriteTools(userId);

		// then
		assertNotNull(response);
		assertFalse(response.toolList().isEmpty());
	}

	@Test
	@DisplayName("프로필 업데이트 성공 - 닉네임만 변경")
	void updateProfile_onlyNickname() {
		// given
		Long userId = 1L;
		String email = "test@example.com";
		String oldNickname = "tester";
		String newNickname = "newTester";
		Positions positions = Positions.STUDENT;
		User userEntity = User.of(email, oldNickname, positions);

		when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
		when(userRepository.existsByNickname(newNickname)).thenReturn(false);

		// when
		UpdateMyResponse response = userService.updateProfile(userId, newNickname, null);

		// then
		assertEquals(newNickname, response.nickname());
		assertEquals(positions, response.positions());
	}

	@Test
	@DisplayName("프로필 업데이트 성공 - 직무만 변경")
	void updateProfile_onlyPosition() {
		// given
		Long userId = 1L;
		String email = "test@example.com";
		String nickname = "tester";
		Positions oldPositions = Positions.STUDENT;
		Positions newPositions = Positions.WORKER;
		User userEntity = User.of(email, nickname, oldPositions);

		when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

		// when
		UpdateMyResponse response = userService.updateProfile(userId, null, newPositions.getName());

		// then
		assertEquals(nickname, response.nickname());
		assertEquals(newPositions, response.positions());
	}

	@Test
	@DisplayName("프로필 업데이트 성공 - 닉네임과 직무 모두 변경")
	void updateProfile_bothNicknameAndPosition() {
		// given
		Long userId = 1L;
		String email = "test@example.com";
		String oldNickname = "tester";
		String newNickname = "newTester";
		Positions oldPositions = Positions.STUDENT;
		Positions newPositions = Positions.WORKER;
		User userEntity = User.of(email, oldNickname, oldPositions);

		when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
		when(userRepository.existsByNickname(newNickname)).thenReturn(false);

		// when
		UpdateMyResponse response = userService.updateProfile(userId, newNickname, newPositions.getName());

		// then
		assertEquals(newNickname, response.nickname());
		assertEquals(newPositions, response.positions());
	}

	@Test
	@DisplayName("프로필 업데이트 실패 - 파라미터 없음")
	void updateProfile_noParameter() {
		// given
		Long userId = 1L;

		// when
		BadRequestException exception = assertThrows(BadRequestException.class,
			() -> userService.updateProfile(userId, null, null));

		// then
		assertEquals(ErrorCode.MISSING_PARAMETER, exception.getErrorCode());
	}

	@Test
	@DisplayName("프로필 업데이트 실패 - 사용자 없음")
	void updateProfile_userNotFound() {
		// given
		Long userId = 1L;
		String nickname = "tester";
		String positionStr = Positions.STUDENT.getName();

		// when
		when(userRepository.findById(userId)).thenReturn(Optional.empty());
		NotFoundException exception = assertThrows(NotFoundException.class,
			() -> userService.updateProfile(userId, nickname, positionStr));

		// then
		assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
	}

	@Test
	@DisplayName("프로필 업데이트 실패 - 중복된 닉네임")
	void updateProfile_duplicatedNickname() {
		// given
		Long userId = 1L;
		String email = "test@example.com";
		String nickname = "tester";
		Positions positions = Positions.STUDENT;
		User userEntity = User.of(email, nickname, positions);

		when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
		when(userRepository.existsByNickname(nickname)).thenReturn(true);

		// when
		ConflictException exception = assertThrows(ConflictException.class,
			() -> userService.updateProfile(userId, nickname, positions.getName()));

		// then
		assertEquals(ErrorCode.DUPLICATED_NICKNAME, exception.getErrorCode());
	}
}
