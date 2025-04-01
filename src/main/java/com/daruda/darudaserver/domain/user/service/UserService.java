package com.daruda.darudaserver.domain.user.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.daruda.darudaserver.domain.comment.repository.CommentRepository;
import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.domain.community.repository.BoardRepository;
import com.daruda.darudaserver.domain.community.repository.BoardScrapRepository;
import com.daruda.darudaserver.domain.tool.dto.res.ToolDtoGetRes;
import com.daruda.darudaserver.domain.tool.entity.Tool;
import com.daruda.darudaserver.domain.tool.entity.ToolScrap;
import com.daruda.darudaserver.domain.tool.repository.ToolRepository;
import com.daruda.darudaserver.domain.tool.repository.ToolScrapRepository;
import com.daruda.darudaserver.domain.tool.service.ToolService;
import com.daruda.darudaserver.domain.user.dto.response.FavoriteToolsResponse;
import com.daruda.darudaserver.domain.user.dto.response.JwtTokenResponse;
import com.daruda.darudaserver.domain.user.dto.response.LoginResponse;
import com.daruda.darudaserver.domain.user.dto.response.MyProfileResponse;
import com.daruda.darudaserver.domain.user.dto.response.SignUpSuccessResponse;
import com.daruda.darudaserver.domain.user.dto.response.UpdateMyResponse;
import com.daruda.darudaserver.domain.user.dto.response.UserInfo;
import com.daruda.darudaserver.domain.user.entity.UserEntity;
import com.daruda.darudaserver.domain.user.entity.enums.Positions;
import com.daruda.darudaserver.domain.user.repository.UserRepository;
import com.daruda.darudaserver.global.auth.jwt.provider.JwtTokenProvider;
import com.daruda.darudaserver.global.auth.jwt.provider.JwtValidationType;
import com.daruda.darudaserver.global.auth.jwt.service.TokenService;
import com.daruda.darudaserver.global.auth.security.UserAuthentication;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.BadRequestException;
import com.daruda.darudaserver.global.error.exception.BusinessException;
import com.daruda.darudaserver.global.error.exception.NotFoundException;
import com.daruda.darudaserver.global.error.exception.UnauthorizedException;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {
	private final UserRepository userRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final TokenService tokenService;
	private final BoardScrapRepository boardScrapRepository;
	private final ToolRepository toolRepository;
	private final ToolScrapRepository toolScrapRepository;
	private final ToolService toolService;
	private final BoardRepository boardRepository;
	private final CommentRepository commentRepository;

	public LoginResponse oauthlogin(final UserInfo userInfo) {
		String email = userInfo.email();
		Optional<UserEntity> userEntity = userRepository.findByEmail(email);
		//등록된 회원이 아닌 경우
		if (userEntity.isEmpty()) {
			return LoginResponse.of(false, email, null);
		} else { //등록된 회원인 경우
			Long userId = userEntity.get().getId();
			log.debug("유저 아이디를 성공적으로 조회했습니다. userId : {}", userId);
			UserAuthentication userAuthentication = UserAuthentication.createUserAuthentication(userId);

			//토큰 생성 및 refreshToken db에 저장
			String accessToken = jwtTokenProvider.generateAccessToken(userAuthentication);
			String refreshToken = tokenService.updateRefreshTokenByUserId(userId);
			log.info("토큰을 정상적으로 생성하였습니다");
			tokenService.saveRefreshtoken(userId, refreshToken);
			JwtTokenResponse jwtTokenResponse = JwtTokenResponse.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.build();

			return LoginResponse.of(true, jwtTokenResponse, userEntity.get().getNickname());
		}
	}

	@Transactional
	public SignUpSuccessResponse createUser(final String email, final String nickname, final String positions) {
		if (userRepository.existsByEmail(email)) {
			throw new BusinessException(ErrorCode.DUPLICATED_EMAIL);
		}
		UserEntity userEntity = UserEntity.builder()
			.email(email)
			.nickname(nickname)
			.positions(Positions.fromString(positions))
			.build();
		Long userId = userRepository.save(userEntity).getId();
		log.debug("유저 아이디를 성공적으로 조회했습니다. userId : {}", userId);
		UserAuthentication userAuthentication = UserAuthentication.createUserAuthentication(userId);
		String accessToken = jwtTokenProvider.generateAccessToken(userAuthentication);
		String refreshToken = jwtTokenProvider.generateRefreshToken(userAuthentication);
		log.info("토큰을 정상적으로 생성하였습니다");
		tokenService.saveRefreshtoken(userId, refreshToken);

		JwtTokenResponse jwtTokenResponse = JwtTokenResponse.of(accessToken, refreshToken);

		return SignUpSuccessResponse.of(nickname, positions, email, jwtTokenResponse);
	}

	public Long deleteUser(final Long userId) {
		tokenService.deleteRefreshToken(userId);
		return userId;
	}

	public boolean isDuplicated(final String nickname) {
		return userRepository.existsByNickname(nickname);
	}

	@Transactional
	public JwtTokenResponse reissueToken(String refreshToken) {
		validateRefreshToken(refreshToken);

		Long userId = jwtTokenProvider.getUserIdFromJwt(refreshToken);

		verifyUserIdWithStoredToken(userId, refreshToken);

		UserAuthentication userAuthentication = UserAuthentication.createUserAuthentication(userId);

		//새 토큰 생성
		String newAccessToken = jwtTokenProvider.generateAccessToken(userAuthentication);
		log.debug("AccessToken을 정상적으로 생성하였습니다, {}", newAccessToken);

		String newRefreshToken = jwtTokenProvider.generateRefreshToken(userAuthentication);
		log.debug("RefreshToken을 정상적으로 생성하였습니다, {}", newRefreshToken);
		tokenService.saveRefreshtoken(userId, newRefreshToken);

		return JwtTokenResponse.builder()
			.accessToken(newAccessToken)
			.refreshToken(newRefreshToken)
			.build();
	}

	public MyProfileResponse getMyInfo(Long userId) {
		UserEntity userEntity = userRepository.findById(userId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
		MyProfileResponse myProfileResponse = MyProfileResponse.of(userEntity.getNickname(),
			userEntity.getPositions().toString());
		return myProfileResponse;
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
				tool.getToolMainName();
				tool.getToolLogo();
				toolService.convertToKeywordRes(tool);
				boolean isScrapped = getToolScrap(userEntity, tool);
				return ToolDtoGetRes.from(tool, toolService.convertToKeywordRes(tool), isScrapped);
			})
			.toList();

		return FavoriteToolsResponse.of(toolDtoGetResList);

	}

	public Boolean getToolScrap(final UserEntity user, final Tool tool) {
		return (user != null
			&& toolScrapRepository.findByUserAndTool(user, tool)
			.map(toolScrap -> !toolScrap.isDelYn())
			.orElse(false));
	}

	public UpdateMyResponse updateMy(Long userId, String nickname, String positions) {
		UserEntity userEntity = userRepository.findById(userId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
		log.debug("사용자를 성공적으로 조회하였습니다., {}", userId);
		if (isDuplicated(nickname)) {
			throw new BusinessException(ErrorCode.DUPLICATED_NICKNAME);
		}
		if (nickname == null) {
			userEntity.updatePositions(Positions.fromString(positions));
			return UpdateMyResponse.of(userEntity.getNickname(), positions);
		}
		if (positions == null) {
			userEntity.updateNickname(nickname);
			return UpdateMyResponse.of(nickname, userEntity.getPositions().getName());
		}

		userEntity.updateNickname(nickname);
		userEntity.updatePositions(Positions.fromString(positions));
		log.debug("사용자 정보를 성공적으로 업데이트 했습니다., {} {}", nickname, positions);
		return UpdateMyResponse.of(nickname, positions);
	}

	public void withdrawMe(Long userId) {
		//사용자 찾기
		UserEntity userEntity = userRepository.findById(userId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

		//FK로 묶여있는 toolScrap삭제
		toolScrapRepository.deleteAllByUserId(userId);
		log.info("toolScrap을 성공적으로 삭제하였습니다");

		commentRepository.deleteAllByUserId(userId);

		List<Board> boardList = boardRepository.findAllByUserId(userId);

		boardList.stream()
			.forEach(board -> commentRepository.deleteByBoardId(board.getId()));

		//FK로 묶여있는 boardScrap 삭제
		boardScrapRepository.deleteAllByUserId(userId);
		log.info("boardScrap을 성공적으로 삭제하였습니다");

		//FK로 묶여있는 board 삭제
		boardRepository.deleteAllByUserId(userId);
		log.info("board를 성공적으로 삭제하였습니다");

		//User 탈퇴
		userRepository.delete(userEntity);
		log.info("정상적으로 탈퇴되었습니다");

		//토큰 삭제
		tokenService.deleteRefreshToken(userId);
		log.info("Refresh 토큰을 정상적으로 삭제하였습니다");
	}

	private void validateRefreshToken(String refreshToken) {
		JwtValidationType validationType = jwtTokenProvider.validateToken(refreshToken);

		if (!validationType.equals(JwtValidationType.VALID_JWT)) {
			throw switch (validationType) {
				case EXPIRED_JWT_TOKEN -> new UnauthorizedException(ErrorCode.REFRESH_TOKEN_EXPIRED_ERROR);
				case INVALID_JWT_TOKEN -> new BadRequestException(ErrorCode.INVALID_REFRESH_TOKEN_ERROR);
				case INVALID_JWT_SIGNATURE -> new BadRequestException(ErrorCode.REFRESH_TOKEN_SIGNATURE_ERROR);
				case UNSUPPORTED_JWT_TOKEN -> new BadRequestException(ErrorCode.UNSUPPORTED_REFRESH_TOKEN_ERROR);
				case EMPTY_JWT -> new BadRequestException(ErrorCode.REFRESH_TOKEN_EMPTY_ERROR);
				default -> new BusinessException(ErrorCode.UNKNOWN_REFRESH_TOKEN_ERROR);
			};
		}
	}

	private void verifyUserIdWithStoredToken(final Long userId, final String refreshToken) {
		Long storedUserId = tokenService.findIdByRefreshToken(refreshToken);

		if (!storedUserId.equals(userId)) {
			throw new BadRequestException(ErrorCode.REFRESH_TOKEN_USER_ID_MISMATCH_ERROR);
		}
	}

	private Tool getTool(final Long toolId) {
		Tool tool = toolRepository.findById(toolId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.DATA_NOT_FOUND));

		return tool;
	}

}
