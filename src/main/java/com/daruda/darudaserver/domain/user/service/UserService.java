package com.daruda.darudaserver.domain.user.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.daruda.darudaserver.domain.tool.dto.res.ToolDtoGetRes;
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

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {

	private final UserRepository userRepository;
	private final ToolScrapRepository toolScrapRepository;
	private final ToolService toolService;

	public boolean isDuplicatedNickname(final String nickname) {
		return userRepository.existsByNickname(nickname);
	}

	public MyProfileResponse getMyProfile(Long userId) {
		UserEntity userEntity = userRepository.findById(userId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
		return MyProfileResponse.of(userEntity.getNickname(), userEntity.getPositions());
	}

	public FavoriteToolsResponse getFavoriteTools(Long userId) {
		UserEntity userEntity = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

		List<ToolScrap> toolScrapList = toolScrapRepository.findAllByUserId(userId);
		List<Tool> tools = toolScrapList.stream()
			.filter(toolScrap -> !toolScrap.isDelYn())
			.map(ToolScrap::getTool)
			.toList();

		List<ToolDtoGetRes> toolDtoGetResList = tools.stream()
			.map(tool -> {
				toolService.convertToKeywordRes(tool);
				boolean isScrapped = getToolScrap(userEntity, tool);
				return ToolDtoGetRes.from(tool, toolService.convertToKeywordRes(tool), isScrapped);
			})
			.toList();

		return FavoriteToolsResponse.of(toolDtoGetResList);

	}

	public UpdateMyResponse updateProfile(Long userId, String nickname, String positionStr) {
		if (positionStr == null && nickname == null) {
			throw new BusinessException(ErrorCode.MISSING_PARAMETER);
		}

		UserEntity userEntity = userRepository.findById(userId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
		log.debug("사용자를 성공적으로 조회하였습니다., {}", userId);

		if (isDuplicatedNickname(nickname)) {
			throw new BusinessException(ErrorCode.DUPLICATED_NICKNAME);
		}

		Positions positions = positionStr != null ? Positions.fromString(positionStr) : null;

		if (nickname == null) {
			userEntity.updatePositions(positions);
			return UpdateMyResponse.of(userEntity.getNickname(), positions);
		}

		if (positions == null) {
			userEntity.updateNickname(nickname);
			return UpdateMyResponse.of(nickname, userEntity.getPositions());
		}

		userEntity.updateNickname(nickname);
		userEntity.updatePositions(positions);
		log.debug("사용자 정보를 성공적으로 업데이트 했습니다., {} {}", nickname, positions);

		return UpdateMyResponse.of(nickname, positions);
	}

	private Boolean getToolScrap(final UserEntity user, final Tool tool) {
		return toolScrapRepository.findByUserAndTool(user, tool)
			.map(toolScrap -> !toolScrap.isDelYn())
			.orElse(false);
	}
}
