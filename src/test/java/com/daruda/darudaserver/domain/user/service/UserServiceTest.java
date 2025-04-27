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
import com.daruda.darudaserver.domain.tool.entity.Tool;
import com.daruda.darudaserver.domain.tool.entity.ToolScrap;
import com.daruda.darudaserver.domain.tool.repository.ToolScrapRepository;
import com.daruda.darudaserver.domain.tool.service.ToolService;
import com.daruda.darudaserver.domain.user.dto.response.FavoriteToolsResponse;
import com.daruda.darudaserver.domain.user.dto.response.MyProfileResponse;
import com.daruda.darudaserver.domain.user.dto.response.UpdateMyResponse;
import com.daruda.darudaserver.domain.user.entity.UserEntity;
import com.daruda.darudaserver.domain.user.entity.enums.Positions;
import com.daruda.darudaserver.domain.user.repository.UserRepository;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.BusinessException;
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
		verify(userRepository).existsByNickname(nickname);
	}

	@Test
	@DisplayName("프로필 조회 성공")
	void getMyProfile_success() {
		// given
		Long userId = 1L;
		String email = "test@example.com";
		String nickname = "tester";
		Positions positions = Positions.STUDENT;
		UserEntity userEntity = UserEntity.of(email, nickname, positions);

		when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

		// when
		MyProfileResponse response = userService.getMyProfile(userId);

		// then
		assertNotNull(response);
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
	@DisplayName("찜한 도구 목록 조회 성공")
	void getFavoriteTools_success() {
		// given
		Long userId = 1L;
		String email = "test@example.com";
		String nickname = "tester";
		Positions positions = Positions.STUDENT;
		UserEntity userEntity = UserEntity.of(email, nickname, positions);
		Tool tool = Tool.of("ToolName", "toolSubName", Category.ALL, "toolLink", "toolDescription", License.FREE, true,
			"toolDetailDescription", "toolPlanLink", "toolBgColor", true, "toolLogo");
		ToolScrap toolScrap = ToolScrap.of(userEntity, tool);

		when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
		when(toolScrapRepository.findAllByUserId(userId)).thenReturn(List.of(toolScrap));
		when(toolService.convertToKeywordRes(tool)).thenReturn(List.of("keyword"));

		// when
		FavoriteToolsResponse response = userService.getFavoriteTools(userId);

		// then
		assertNotNull(response);
		assertFalse(response.toolList().isEmpty());
	}

	@Test
	@DisplayName("프로필 업데이트 성공 - 닉네임과 포지션 모두 변경")
	void updateProfile_success() {
		// given
		Long userId = 1L;
		String email = "test@example.com";
		String nickname = "tester";
		Positions positions = Positions.STUDENT;
		String newNickname = "newTester";
		Positions newPositions = Positions.NORMAL;
		UserEntity userEntity = UserEntity.of(email, nickname, positions);

		when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
		when(userRepository.existsByNickname(newNickname)).thenReturn(false);

		// when
		UpdateMyResponse response = userService.updateProfile(userId, newNickname, newPositions);

		// then
		assertNotNull(response);
		assertEquals(newNickname, response.nickname());
		assertEquals(newPositions, response.positions());
	}

	@Test
	@DisplayName("프로필 업데이트 성공 - 닉네임 변경")
	void updateProfile_nickname_success() {
		// given
		Long userId = 1L;
		String email = "test@example.com";
		String nickname = "tester";
		Positions positions = Positions.STUDENT;
		String newNickname = "newTester";
		UserEntity userEntity = UserEntity.of(email, nickname, positions);

		when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
		when(userRepository.existsByNickname(newNickname)).thenReturn(false);

		// when
		UpdateMyResponse response = userService.updateProfile(userId, newNickname, null);

		// then
		assertNotNull(response);
		assertEquals(newNickname, response.nickname());
	}

	@Test
	@DisplayName("프로필 업데이트 성공 - 포지션 변경")
	void updateProfile_position_success() {
		// given
		Long userId = 1L;
		String email = "test@example.com";
		String nickname = "tester";
		Positions positions = Positions.STUDENT;
		Positions newPositions = Positions.NORMAL;
		UserEntity userEntity = UserEntity.of(email, nickname, positions);

		when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

		// when
		UpdateMyResponse response = userService.updateProfile(userId, null, newPositions);

		// then
		assertNotNull(response);
		assertEquals(newPositions, response.positions());
	}

	@Test
	@DisplayName("프로필 업데이트 실패 - 파라미터 없음")
	void updateProfile_noParameter() {
		// given
		Long userId = 1L;

		// when
		BusinessException exception = assertThrows(BusinessException.class,
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
		Positions positions = Positions.STUDENT;

		// when
		BusinessException exception = assertThrows(BusinessException.class,
			() -> userService.updateProfile(userId, nickname, positions));

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
		UserEntity userEntity = UserEntity.of(email, nickname, positions);

		when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
		when(userRepository.existsByNickname(nickname)).thenReturn(true);

		// when
		BusinessException exception = assertThrows(BusinessException.class,
			() -> userService.updateProfile(userId, nickname, positions));

		// then
		assertEquals(ErrorCode.DUPLICATED_NICKNAME, exception.getErrorCode());
	}
}
