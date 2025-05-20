package com.daruda.darudaserver.domain.user.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.daruda.darudaserver.domain.comment.repository.CommentRepository;
import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.domain.community.repository.BoardRepository;
import com.daruda.darudaserver.domain.community.repository.BoardScrapRepository;
import com.daruda.darudaserver.domain.notification.service.NotificationService;
import com.daruda.darudaserver.domain.tool.repository.ToolScrapRepository;
import com.daruda.darudaserver.domain.user.dto.response.JwtTokenResponse;
import com.daruda.darudaserver.domain.user.dto.response.LoginResponse;
import com.daruda.darudaserver.domain.user.dto.response.SignUpSuccessResponse;
import com.daruda.darudaserver.domain.user.dto.response.UserInformationResponse;
import com.daruda.darudaserver.domain.user.entity.UserEntity;
import com.daruda.darudaserver.domain.user.entity.enums.Positions;
import com.daruda.darudaserver.domain.user.entity.enums.SocialType;
import com.daruda.darudaserver.domain.user.repository.UserRepository;
import com.daruda.darudaserver.global.auth.jwt.service.TokenService;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.BusinessException;
import com.daruda.darudaserver.global.error.exception.NotFoundException;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

	private final KakaoService kakaoService;
	private final UserRepository userRepository;
	private final TokenService tokenService;
	private final BoardRepository boardRepository;
	private final BoardScrapRepository boardScrapRepository;
	private final CommentRepository commentRepository;
	private final ToolScrapRepository toolScrapRepository;
	private final NotificationService notificationService;

	@Transactional
	public SignUpSuccessResponse register(final String email, final String nickname, final String positionStr) {
		if (userRepository.existsByEmail(email)) {
			throw new BusinessException(ErrorCode.DUPLICATED_EMAIL);
		}

		Positions positions = Positions.fromString(positionStr);

		UserEntity userEntity = UserEntity.of(email, nickname, positions);

		Long userId = userRepository.save(userEntity).getId();
		log.debug("유저 아이디를 성공적으로 조회했습니다. userId : {}", userId);

		JwtTokenResponse jwtTokenResponse = tokenService.createToken(userId);

		return SignUpSuccessResponse.of(nickname, positions, email, jwtTokenResponse);
	}

	public LoginResponse login(final UserInformationResponse userInformationResponse) {
		String email = userInformationResponse.email();
		Optional<UserEntity> userEntity = userRepository.findByEmail(email);
		//등록된 회원이 아닌 경우
		if (userEntity.isEmpty()) {
			return LoginResponse.ofNonRegisteredUser(email);
		} else { //등록된 회원인 경우
			Long userId = userEntity.get().getId();
			log.debug("유저 아이디를 성공적으로 조회했습니다. userId : {}", userId);

			JwtTokenResponse jwtTokenResponse = tokenService.createToken(userId);

			return LoginResponse.ofRegisteredUser(jwtTokenResponse, userEntity.get().getNickname());
		}
	}

	public Long logout(final Long userId) {
		tokenService.deleteRefreshToken(userId);
		return userId;
	}

	@Transactional
	public void withdraw(Long userId) {
		//사용자 찾기
		UserEntity userEntity = userRepository.findById(userId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

		//FK로 묶여있는 toolScrap삭제
		toolScrapRepository.deleteAllByUserId(userId);
		log.info("toolScrap을 성공적으로 삭제하였습니다");

		commentRepository.deleteCommentsByUserId(userId);

		List<Board> boardList = boardRepository.findAllByUserId(userId);

		boardList.forEach(board -> commentRepository.deleteCommentsByBoardId(board.getId()));

		//FK로 묶여있는 boardScrap 삭제
		boardScrapRepository.deleteAllByUserId(userId);
		log.info("boardScrap을 성공적으로 삭제하였습니다");

		//FK로 묶여있는 board 삭제
		boardRepository.deleteAllByUserId(userId);
		log.info("board를 성공적으로 삭제하였습니다");

		// 알림 삭제
		notificationService.delete(userId);
		log.info("알림을 성공적으로 삭제하였습니다");

		//토큰 삭제
		tokenService.deleteRefreshToken(userId);
		log.info("Refresh 토큰을 정상적으로 삭제하였습니다");

		//User 탈퇴
		userRepository.delete(userEntity);
		log.info("정상적으로 탈퇴되었습니다");
	}

	public SocialService findSocialService(SocialType socialType) {
		return switch (socialType) {
			case KAKAO -> kakaoService;
			// case GOOGLE -> googleSocialService;
			// default -> throw new BadRequestException(ErrorCode.SOCIAL_TYPE_BAD_REQUEST);
		};
	}
}
