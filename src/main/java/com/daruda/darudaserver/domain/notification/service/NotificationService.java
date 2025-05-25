package com.daruda.darudaserver.domain.notification.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.daruda.darudaserver.domain.comment.entity.CommentEntity;
import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.domain.notification.dto.request.CommunityBlockNoticeRequest;
import com.daruda.darudaserver.domain.notification.dto.request.NoticeRequest;
import com.daruda.darudaserver.domain.notification.dto.response.NotificationResponse;
import com.daruda.darudaserver.domain.notification.entity.NotificationEntity;
import com.daruda.darudaserver.domain.notification.entity.enums.NotificationType;
import com.daruda.darudaserver.domain.notification.repository.EmitterRepository;
import com.daruda.darudaserver.domain.notification.repository.NotificationRepository;
import com.daruda.darudaserver.domain.user.entity.UserEntity;
import com.daruda.darudaserver.domain.user.repository.UserRepository;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.BadRequestException;
import com.daruda.darudaserver.global.error.exception.NotFoundException;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class NotificationService {

	// 한 시간 동안 연결
	private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;
	private static final String COMMENT_NOTIFICATION_TITLE_FORMAT = "내가 쓴 글에 댓글이 달렸어요: %s";
	private static final String COMMENT_NOTIFICATION_CONTENT_FORMAT = "제목: %s";
	private static final String COMMUNITY_BLOCK_NOTICE_TITLE_FORMAT = "[공지] %s님의 신고가 접수되었습니다.";
	private static final String COMMUNITY_BLOCK_NOTICE_CONTENT_FORMAT = "회원님께서는 \n"
		+ "커뮤니티이용규칙을 위반하여\n"
		+ "%s부로 %d일간 커뮤니티의 일부 활동을 제한합니다. \n"
		+ "문의사항이 있다면 홈 화면 우측 상단의 ‘문의하기’를 이용해 주시기 바랍니다.\n"
		+ "감사합니다.";

	private final UserRepository userRepository;
	private final EmitterRepository emitterRepository;
	private final NotificationRepository notificationRepository;

	public SseEmitter subscribe(Long userId, String lastEventId) {
		String emitterId = userId + "_" + System.currentTimeMillis();
		SseEmitter emitter = emitterRepository.save(emitterId, new SseEmitter(DEFAULT_TIMEOUT));

		// 시간 초과나 비동기 요청이 안되면 자동으로 emitter 삭제
		emitter.onCompletion(() -> {
			emitterRepository.deleteById(emitterId);
			emitterRepository.deleteAllEventCacheStartWithId(emitterId);
		});
		emitter.onTimeout(() -> {
			emitterRepository.deleteById(emitterId);
			emitterRepository.deleteAllEventCacheStartWithId(emitterId);
		});

		// 최초 연결시 발생하는 503 오류에 대응하기 위한 더미 데이터
		if (isSendFailedToClient(emitter, emitterId, "EventStream Created. [userId=" + userId + "]")) {
			log.warn("초기 연결 이벤트 전송 실패 - userId: {}", userId);
		}

		// lastEventId가 남아있다면 남은 데이터를 전송
		if (lastEventId != null && !lastEventId.isEmpty()) {
			Map<String, Object> events = emitterRepository.findAllEventCacheStartWithByUserId(
				String.valueOf(userId));
			events.entrySet().stream()
				.filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
				.forEach(entry -> {
					if (isSendFailedToClient(emitter, entry.getKey(), entry.getValue())) {
						log.error("캐시된 이벤트 전송 실패 - key: {}", entry.getKey());
					}
				});
		}
		return emitter;

	}

	public void sendCommentNotification(CommentEntity commentEntity) {
		Board board = commentEntity.getBoard();

		String title = String.format(COMMENT_NOTIFICATION_TITLE_FORMAT, commentEntity.getContent());
		String content = String.format(COMMENT_NOTIFICATION_CONTENT_FORMAT, board.getTitle());

		send(board.getUser(), NotificationType.COMMENT, title, content, commentEntity);
	}

	public void sendNotice(NoticeRequest noticeRequest) {
		for (UserEntity userEntity : userRepository.findAll()) {
			send(userEntity, NotificationType.NOTICE, noticeRequest.title(), noticeRequest.content(), null);
		}
	}

	public void sendBlockNotice(CommunityBlockNoticeRequest communityBlockNoticeRequest) {
		UserEntity receiver = userRepository.findById(communityBlockNoticeRequest.userId())
			.orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

		LocalDate now = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
		String formattedDate = now.format(formatter);

		String title = String.format(COMMUNITY_BLOCK_NOTICE_TITLE_FORMAT, receiver.getNickname());
		String content = String.format(COMMUNITY_BLOCK_NOTICE_CONTENT_FORMAT, formattedDate,
			communityBlockNoticeRequest.blockDurationInDays());

		send(receiver, NotificationType.NOTICE, title, content, null);
	}

	public void delete(Long userId) {
		String userIdString = String.valueOf(userId);
		UserEntity userEntity = userRepository.findById(userId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
		emitterRepository.deleteAllEmitterStartWithId(userIdString);
		emitterRepository.deleteAllEventCacheStartWithId(userIdString);
		notificationRepository.deleteAllByReceiver(userEntity);
	}

	public void readNotification(Long userId, Long notificationId) {
		NotificationEntity notificationEntity = notificationRepository.findById(notificationId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.NOTIFICATION_NOT_FOUND));
		if (!notificationEntity.getReceiver().getId().equals(userId)) {
			throw new BadRequestException(ErrorCode.NOTIFICATION_READ_FORBIDDEN);
		}
		notificationEntity.markAsRead();
	}

	public List<NotificationResponse> getNotifications(Long userId) {
		UserEntity userEntity = userRepository.findById(userId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

		return notificationRepository.findAllByReceiver(userEntity).stream()
			.map(NotificationResponse::from)
			.toList();
	}

	public List<NotificationResponse> getRecentNotifications(Long userId) {
		UserEntity userEntity = userRepository.findById(userId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

		return notificationRepository.findTop3ByReceiverOrderByCreatedAtDesc(userEntity).stream()
			.map(NotificationResponse::from)
			.toList();
	}

	private void send(UserEntity receiver, NotificationType notificationType, String title, String content,
		CommentEntity commentEntity) {
		NotificationEntity notificationEntity = notificationRepository.save(
			NotificationEntity.of(receiver, notificationType, title, content, commentEntity));
		String userId = String.valueOf(receiver.getId());

		Map<String, SseEmitter> sseEmitters = emitterRepository.findAllEmitterStartWithByUserId(userId);
		sseEmitters.forEach(
			(key, emitter) -> {
				emitterRepository.saveEventCache(key, notificationEntity);
				if (isSendFailedToClient(emitter, key, NotificationResponse.from(notificationEntity))) {
					log.warn("알림 전송 실패 - emitterId: {}, 수신자: {}", key, receiver.getEmail());
				}
			}
		);
	}

	private boolean isSendFailedToClient(SseEmitter emitter, String emitterId, Object data) {
		try {
			emitter.send(SseEmitter.event()
				.id(emitterId)
				.data(data));
			return false;
		} catch (IOException exception) {
			emitterRepository.deleteById(emitterId);
			return true;
		}
	}
}
