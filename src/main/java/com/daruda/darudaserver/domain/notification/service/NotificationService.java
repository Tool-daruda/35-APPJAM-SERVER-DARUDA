package com.daruda.darudaserver.domain.notification.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.daruda.darudaserver.domain.comment.entity.CommentEntity;
import com.daruda.darudaserver.domain.community.entity.Board;
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
		sendToClient(emitter, emitterId, "EventStream Created. [userId=" + userId + "]");

		// lastEventId가 남아있다면 남은 데이터를 전송
		if (!lastEventId.isEmpty()) {
			Map<String, Object> events = emitterRepository.findAllEventCacheStartWithByUserId(
				String.valueOf(userId));
			events.entrySet().stream()
				.filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
				.forEach(entry -> {
					try {
						sendToClient(emitter, entry.getKey(), entry.getValue());
					} catch (Exception e) {
						log.error("알림 전송을 실패하였습니다. - emitterId: {}", entry.getKey());
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
				sendToClient(emitter, key, NotificationResponse.from(notificationEntity));
			}
		);
	}

	private void sendToClient(SseEmitter emitter, String emitterId, Object data) {
		try {
			emitter.send(SseEmitter.event()
				.id(emitterId)
				.data(data));
		} catch (IOException exception) {
			emitterRepository.deleteById(emitterId);
			throw new BadRequestException(ErrorCode.SEND_NOTIFICATION_FAIL);
		}
	}
}
