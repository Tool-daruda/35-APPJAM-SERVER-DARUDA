package com.daruda.darudaserver.domain.notification.service;

import static com.daruda.darudaserver.domain.notification.entity.enums.NotificationFormat.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.daruda.darudaserver.domain.comment.entity.CommentEntity;
import com.daruda.darudaserver.domain.comment.repository.CommentRepository;
import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.domain.notification.dto.request.CommunityBlockNoticeRequest;
import com.daruda.darudaserver.domain.notification.dto.request.NoticeRequest;
import com.daruda.darudaserver.domain.notification.dto.response.NotificationResponse;
import com.daruda.darudaserver.domain.notification.entity.NotificationEntity;
import com.daruda.darudaserver.domain.notification.entity.enums.BlockDurationInDay;
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
@RequiredArgsConstructor
public class NotificationService {

	// 한 시간 동안 연결
	private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;

	private final UserRepository userRepository;
	private final EmitterRepository emitterRepository;
	private final NotificationRepository notificationRepository;
	private final CommentRepository commentRepository;

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

	@Transactional
	public void sendCommentNotification(CommentEntity commentEntity) {
		Board board = commentEntity.getBoard();
		UserEntity commenter = commentEntity.getUser();

		String title = createCommentNotificationTitle(commentEntity);
		String content = COMMENT_CONTENT_BOARD_TITLE.format(board.getTitle());

		// 1. 게시글 작성자에게 알림 (내가 쓴 글에 내가 댓글 다는 것 제외)
		if (!board.getUser().getId().equals(commenter.getId())) {
			send(board.getUser(), NotificationType.COMMENT, title, content, commentEntity, null);
		}

		// 2. 다른 댓글 작성자들에게 알림
		List<UserEntity> otherCommenters = commentRepository.findDistinctUserByBoardId(board.getId());
		for (UserEntity receiver : otherCommenters) {
			// 알림 받을 사람이 댓글 작성자 본인이 아니고, 게시글 작성자도 아닐 경우 (게시글 작성자는 위에서 보냄)
			if (!receiver.getId().equals(commenter.getId()) && !receiver.getId().equals(board.getUser().getId())) {
				send(receiver, NotificationType.COMMENT, title, content, commentEntity, null);
			}
		}
	}

	private String createCommentNotificationTitle(CommentEntity commentEntity) {
		boolean hasPhoto = commentEntity.getPhotoUrl() != null && !commentEntity.getPhotoUrl().isBlank();
		boolean hasText = commentEntity.getContent() != null && !commentEntity.getContent().isBlank();

		if (hasPhoto && hasText) {
			return COMMENT_TITLE_IMAGE_WITH_TEXT.format(commentEntity.getContent().trim());
		}
		if (hasPhoto) {
			return COMMENT_TITLE_IMAGE_ONLY.getMessageFormat();
		}
		return hasText
			? COMMENT_TITLE_TEXT_ONLY.format(commentEntity.getContent().trim())
			: COMMENT_TITLE_TEXT_ONLY.format("(내용 없음)");
	}

	@Transactional
	public void sendNotice(NoticeRequest noticeRequest) {
		int batchSize = 1000;
		int page = 0;
		Pageable pageable;
		Page<UserEntity> users;
		do {
			pageable = PageRequest.of(page, batchSize);
			users = userRepository.findAll(pageable);
			for (UserEntity userEntity : users.getContent()) {
				send(userEntity,
					NotificationType.NOTICE,
					NOTICE_TITLE.format(noticeRequest.title()),
					NOTICE_CONTENT.getMessageFormat(),
					null,
					noticeRequest.url());
			}
			page++;
		} while (users.hasNext());
	}

	public void sendBlockNotice(CommunityBlockNoticeRequest communityBlockNoticeRequest) {
		UserEntity receiver = userRepository.findById(communityBlockNoticeRequest.userId())
			.orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

		BlockDurationInDay blockDurationInDay = BlockDurationInDay.fromString(
			communityBlockNoticeRequest.blockDurationInDay());

		LocalDate now = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
		String formattedDate = now.format(formatter);

		String title = COMMUNITY_BLOCK_NOTICE_TITLE.format(receiver.getNickname());
		String content = COMMUNITY_BLOCK_NOTICE_CONTENT.format(formattedDate, blockDurationInDay.getDays());

		send(receiver, NotificationType.NOTICE, title, content, null, null);
	}

	@Transactional
	public void delete(Long userId) {
		String userIdString = String.valueOf(userId);
		UserEntity userEntity = userRepository.findById(userId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
		emitterRepository.deleteAllEmitterStartWithId(userIdString);
		emitterRepository.deleteAllEventCacheStartWithId(userIdString);
		notificationRepository.deleteAllByReceiver(userEntity);
	}

	@Transactional
	public void readNotification(Long userId, Long notificationId) {
		NotificationEntity notificationEntity = notificationRepository.findById(notificationId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.NOTIFICATION_NOT_FOUND));
		if (!notificationEntity.getReceiver().getId().equals(userId)) {
			throw new BadRequestException(ErrorCode.NOTIFICATION_READ_FORBIDDEN);
		}
		notificationEntity.markAsRead();
	}

	@Transactional
	public List<NotificationResponse> getNotifications(Long userId) {
		UserEntity userEntity = userRepository.findById(userId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

		return notificationRepository.findAllByReceiver(userEntity).stream()
			.map(NotificationResponse::from)
			.toList();
	}

	@Transactional
	public List<NotificationResponse> getRecentNotifications(Long userId) {
		UserEntity userEntity = userRepository.findById(userId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

		return notificationRepository.findTop3ByReceiverOrderByCreatedAtDesc(userEntity).stream()
			.map(NotificationResponse::from)
			.toList();
	}

	private void send(UserEntity receiver, NotificationType notificationType, String title, String content,
		CommentEntity commentEntity, String url) {
		NotificationEntity notificationEntity = NotificationEntity.builder()
			.receiver(receiver)
			.type(notificationType)
			.title(title)
			.content(content)
			.comment(commentEntity)
			.url(url)
			.isRead(false)
			.build();

		notificationRepository.save(notificationEntity);

		TransactionSynchronizationManager.registerSynchronization(
			new TransactionSynchronization() {
				@Override
				public void afterCommit() {
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
