package com.daruda.darudaserver.domain.notification.service;

import static com.daruda.darudaserver.domain.notification.entity.enums.NotificationFormat.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.daruda.darudaserver.domain.comment.entity.CommentEntity;
import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.domain.notification.dto.request.CommunityBlockNoticeRequest;
import com.daruda.darudaserver.domain.notification.dto.request.NoticeRequest;
import com.daruda.darudaserver.domain.notification.dto.response.NotificationResponse;
import com.daruda.darudaserver.domain.notification.entity.NotificationEntity;
import com.daruda.darudaserver.domain.notification.entity.enums.BlockDurationInDay;
import com.daruda.darudaserver.domain.notification.entity.enums.NotificationFormat;
import com.daruda.darudaserver.domain.notification.entity.enums.NotificationType;
import com.daruda.darudaserver.domain.notification.repository.EmitterRepository;
import com.daruda.darudaserver.domain.notification.repository.NotificationRepository;
import com.daruda.darudaserver.domain.tool.entity.Tool;
import com.daruda.darudaserver.domain.user.entity.UserEntity;
import com.daruda.darudaserver.domain.user.entity.enums.Positions;
import com.daruda.darudaserver.domain.user.repository.UserRepository;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.BadRequestException;
import com.daruda.darudaserver.global.error.exception.NotFoundException;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private EmitterRepository emitterRepository;

	@Mock
	private NotificationRepository notificationRepository;

	@InjectMocks
	private NotificationService notificationService;

	@Test
	@DisplayName("SSE 연결 성공 - 신규")
	void subscribe_ShouldReturnNewSseEmitter() {
		// given
		Long userId = 1L;
		String lastEventId = "";
		SseEmitter emitter = mock(SseEmitter.class);

		// when
		when(emitterRepository.save(anyString(), any(SseEmitter.class))).thenReturn(emitter);

		// then
		SseEmitter result = notificationService.subscribe(userId, lastEventId);
		assertNotNull(result);

		ArgumentCaptor<String> emitterIdCaptor = ArgumentCaptor.forClass(String.class);
		verify(emitterRepository).save(emitterIdCaptor.capture(), any(SseEmitter.class));

		String capturedEmitterId = emitterIdCaptor.getValue();
		assertTrue(capturedEmitterId.startsWith(userId + "_"));
	}

	@Test
	@DisplayName("SSE 연결 성공 - 기존")
	void subscribe_ShouldReturnSseEmitter() {
		// given
		Long userId = 1L;
		String lastEventId = userId + "_12345";
		SseEmitter emitter = mock(SseEmitter.class);
		Map<String, Object> cachedEvents = Map.of("1_12345", "Event 1");

		// when
		when(emitterRepository.save(anyString(), any(SseEmitter.class))).thenReturn(emitter);
		when(emitterRepository.findAllEventCacheStartWithByUserId(String.valueOf(userId))).thenReturn(cachedEvents);

		// then
		SseEmitter result = notificationService.subscribe(userId, lastEventId);
		assertNotNull(result);

		verify(emitterRepository).findAllEventCacheStartWithByUserId(String.valueOf(userId));
		cachedEvents.entrySet().stream()
			.filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
			.forEach(entry -> verify(emitterRepository).saveEventCache(entry.getKey(), entry.getValue()));

		ArgumentCaptor<String> emitterIdCaptor = ArgumentCaptor.forClass(String.class);
		verify(emitterRepository).save(emitterIdCaptor.capture(), any(SseEmitter.class));

		String capturedEmitterId = emitterIdCaptor.getValue();
		assertTrue(capturedEmitterId.startsWith(userId + "_"));
	}

	@Test
	@DisplayName("SSE 연결 실패 - 초기 이벤트 전송 실패")
	void subscribe_ShouldHandleInitialEventSendFailure() throws IOException {
		// given
		Long userId = 1L;
		String lastEventId = "";
		SseEmitter emitter = mock(SseEmitter.class);

		// when
		when(emitterRepository.save(anyString(), any(SseEmitter.class))).thenReturn(emitter);
		doThrow(new IOException("이벤트 전송 실패")).when(emitter).send(any(SseEmitter.SseEventBuilder.class));

		// then
		SseEmitter result = notificationService.subscribe(userId, lastEventId);
		assertNotNull(result);

		ArgumentCaptor<String> emitterIdCaptor = ArgumentCaptor.forClass(String.class);
		verify(emitterRepository).save(emitterIdCaptor.capture(), any(SseEmitter.class));

		String capturedEmitterId = emitterIdCaptor.getValue();
		assertTrue(capturedEmitterId.startsWith(userId + "_"));
		verify(emitterRepository).deleteById(capturedEmitterId);
	}

	@Test
	@DisplayName("댓글 알림 전송 성공")
	void sendCommentNotification_ShouldSendNotification() {
		// given
		Long userId = 1L;
		String userIdString = "1";
		String email = "test@example.com";
		String nickname = "tester";
		Positions positions = Positions.STUDENT;
		UserEntity userEntity = UserEntity.of(email, nickname, positions);
		ReflectionTestUtils.setField(userEntity, "id", userId); // id 설정

		String toolName = "test";
		Tool tool = Tool.builder().toolMainName(toolName).build();

		String communityTitle = "title";
		String communityContent = "content";
		Board board = Board.create(tool, userEntity, communityTitle, communityContent);

		String photoUrl = "http://test.test";
		CommentEntity comment = CommentEntity.of(communityContent, photoUrl, userEntity, board);

		String title = String.format(NotificationFormat.COMMENT_NOTIFICATION_TITLE.getMessageFormat(),
			comment.getContent());
		String content = String.format(NotificationFormat.COMMENT_NOTIFICATION_CONTENT.getMessageFormat(),
			board.getTitle());

		NotificationEntity notification = NotificationEntity.of(userEntity, NotificationType.COMMENT, title, content,
			comment);

		SseEmitter emitter = mock(SseEmitter.class);
		String emitterId = "1_12345";

		// when
		when(emitterRepository.findAllEmitterStartWithByUserId(userIdString)).thenReturn(Map.of(emitterId, emitter));
		when(notificationRepository.save(any(NotificationEntity.class))).thenReturn(notification);

		notificationService.sendCommentNotification(comment);

		// then
		ArgumentCaptor<NotificationEntity> notificationCaptor = ArgumentCaptor.forClass(NotificationEntity.class);
		verify(notificationRepository).save(notificationCaptor.capture());
		NotificationEntity capturedNotification = notificationCaptor.getValue();

		assertEquals(notification.getTitle(), capturedNotification.getTitle());
		assertEquals(notification.getContent(), capturedNotification.getContent());
		assertEquals(notification.getType(), capturedNotification.getType());

		verify(emitterRepository).findAllEmitterStartWithByUserId(userIdString);
		verify(emitterRepository).saveEventCache(eq(emitterId), any(NotificationEntity.class));
		verifyNoMoreInteractions(emitterRepository);
	}

	@Test
	@DisplayName("공지 알림 전송 성공")
	void sendNotice_ShouldSendNotificationsToAllUsers() {
		// given
		String email = "test@example.com";
		String nickname = "tester";
		Positions positions = Positions.STUDENT;
		UserEntity user1 = UserEntity.of(email, nickname, positions);
		UserEntity user2 = UserEntity.of(email, nickname, positions);

		String title = "title";
		String content = "content";

		// when
		when(userRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(user1, user2)));
		when(emitterRepository.findAllEmitterStartWithByUserId(anyString())).thenReturn(Map.of());

		NoticeRequest noticeRequest = new NoticeRequest(title, content);
		notificationService.sendNotice(noticeRequest);

		// then
		verify(userRepository).findAll(any(Pageable.class));
		verify(emitterRepository, times(2)).findAllEmitterStartWithByUserId(anyString());
		verifyNoMoreInteractions(emitterRepository);
	}

	@Test
	@DisplayName("제한 알림 전송 성공")
	void sendBlockNotification_ShouldSendNotification() {
		// given
		Long userId = 1L;
		String userIdString = "1";
		String email = "test@example.com";
		String nickname = "tester";
		Positions positions = Positions.STUDENT;
		UserEntity userEntity = UserEntity.of(email, nickname, positions);
		ReflectionTestUtils.setField(userEntity, "id", userId); // id 설정

		String blockDurationInDayString = "1일";
		BlockDurationInDay blockDurationInDay = BlockDurationInDay.fromString(blockDurationInDayString);

		LocalDate now = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
		String formattedDate = now.format(formatter);

		String title = String.format(NotificationFormat.COMMUNITY_BLOCK_NOTICE_TITLE.getMessageFormat(), nickname);
		String content = String.format(NotificationFormat.COMMUNITY_BLOCK_NOTICE_CONTENT.getMessageFormat(),
			formattedDate, blockDurationInDay.getDays());

		NotificationEntity expectedNotification = NotificationEntity.of(userEntity, NotificationType.NOTICE, title,
			content, null);

		SseEmitter emitter = mock(SseEmitter.class);
		String emitterId = "1_12345";

		CommunityBlockNoticeRequest communityBlockNoticeRequest = new CommunityBlockNoticeRequest(userId,
			blockDurationInDayString);

		// when
		when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
		when(emitterRepository.findAllEmitterStartWithByUserId(userIdString)).thenReturn(Map.of(emitterId, emitter));
		when(notificationRepository.save(any(NotificationEntity.class))).thenReturn(expectedNotification);

		notificationService.sendBlockNotice(communityBlockNoticeRequest);

		// then
		ArgumentCaptor<NotificationEntity> notificationCaptor = ArgumentCaptor.forClass(NotificationEntity.class);
		verify(notificationRepository).save(notificationCaptor.capture());
		NotificationEntity capturedNotification = notificationCaptor.getValue();

		assertEquals(expectedNotification.getTitle(), capturedNotification.getTitle());
		assertEquals(expectedNotification.getContent(), capturedNotification.getContent());
		assertEquals(expectedNotification.getType(), capturedNotification.getType());

		verify(emitterRepository).findAllEmitterStartWithByUserId(userIdString);
		verify(emitterRepository).saveEventCache(eq(emitterId), any(NotificationEntity.class));
		verifyNoMoreInteractions(emitterRepository);
	}

	@Test
	@DisplayName("회원가입 알림 전송 성공")
	void sendRegisterNotification_ShouldSendNotification() {
		// given
		Long userId = 1L;
		String userIdString = "1";
		String email = "test@example.com";
		String nickname = "tester";
		Positions positions = Positions.STUDENT;
		UserEntity userEntity = UserEntity.of(email, nickname, positions);
		ReflectionTestUtils.setField(userEntity, "id", userId); // id 설정

		String title = REGISTER_NOTICE_TITLE.getMessageFormat();
		String content = REGISTER_NOTICE_CONTENT.getMessageFormat();

		NotificationEntity expectedNotification = NotificationEntity.of(userEntity, NotificationType.NOTICE, title,
			content, null);

		SseEmitter emitter = mock(SseEmitter.class);
		String emitterId = "1_12345";

		// when
		when(emitterRepository.findAllEmitterStartWithByUserId(userIdString)).thenReturn(Map.of(emitterId, emitter));
		when(notificationRepository.save(any(NotificationEntity.class))).thenReturn(expectedNotification);

		notificationService.sendRegisterNotice(userEntity);

		// then
		ArgumentCaptor<NotificationEntity> notificationCaptor = ArgumentCaptor.forClass(NotificationEntity.class);
		verify(notificationRepository).save(notificationCaptor.capture());
		NotificationEntity capturedNotification = notificationCaptor.getValue();

		assertEquals(expectedNotification.getTitle(), capturedNotification.getTitle());
		assertEquals(expectedNotification.getContent(), capturedNotification.getContent());
		assertEquals(expectedNotification.getType(), capturedNotification.getType());

		verify(emitterRepository).findAllEmitterStartWithByUserId(userIdString);
		verify(emitterRepository).saveEventCache(eq(emitterId), any(NotificationEntity.class));
		verifyNoMoreInteractions(emitterRepository);
	}

	@Test
	@DisplayName("회원가입 알림 전송 실패")
	void sendRegisterNotification_FailSendNotification() throws IOException {
		// given
		Long userId = 1L;
		String userIdString = "1";
		String email = "test@example.com";
		String nickname = "tester";
		Positions positions = Positions.STUDENT;
		UserEntity userEntity = UserEntity.of(email, nickname, positions);
		ReflectionTestUtils.setField(userEntity, "id", userId);

		SseEmitter emitter = mock(SseEmitter.class);
		String emitterId = "1_12345";

		// when
		when(emitterRepository.findAllEmitterStartWithByUserId(userIdString)).thenReturn(Map.of(emitterId, emitter));
		doThrow(new IOException("알림 전송 실패"))
			.when(emitter).send(any(SseEmitter.SseEventBuilder.class));

		// then
		assertDoesNotThrow(() -> notificationService.sendRegisterNotice(userEntity));

		verify(emitterRepository).findAllEmitterStartWithByUserId(userIdString);
		verify(emitterRepository).saveEventCache(eq(emitterId), any(NotificationEntity.class));
		verify(emitterRepository).deleteById(emitterId);
		verifyNoMoreInteractions(emitterRepository);
	}

	@Test
	@DisplayName("알림 삭제 성공")
	void delete_ShouldDeleteEmittersAndNotifications() {
		// given
		Long userId = 1L;
		String email = "test@example.com";
		String nickname = "tester";
		Positions positions = Positions.STUDENT;
		UserEntity userEntity = UserEntity.of(email, nickname, positions);

		// when
		when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

		notificationService.delete(userId);

		// then
		verify(emitterRepository).deleteAllEmitterStartWithId(String.valueOf(userId));
		verify(notificationRepository).deleteAllByReceiver(userEntity);
	}

	@Test
	@DisplayName("알림 목록 조회 성공")
	void getNotifications_ShouldReturnNotificationResponses() {
		// given
		Long userId = 1L;
		String email = "test@example.com";
		String nickname = "tester";
		Positions positions = Positions.STUDENT;
		UserEntity userEntity = UserEntity.of(email, nickname, positions);

		String toolName = "test";
		Tool tool = Tool.builder().toolMainName(toolName).build();

		String title = "title";
		String content = "content";
		Board board = Board.create(tool, userEntity, title, content);

		String photoUrl = "http://test.test";
		CommentEntity comment = CommentEntity.of(content, photoUrl, userEntity, board);

		NotificationEntity notification = NotificationEntity.of(userEntity, NotificationType.COMMENT, title, content,
			comment);

		// when
		when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
		when(notificationRepository.findAllByReceiver(userEntity)).thenReturn(List.of(notification));

		List<NotificationResponse> responses = notificationService.getNotifications(userId);

		// then
		assertEquals(1, responses.size());
		verify(notificationRepository).findAllByReceiver(userEntity);
	}

	@Test
	@DisplayName("최근 알림 목록 조회 성공")
	void getNotifications_ShouldReturnRecentNotificationList() {
		// given
		Long userId = 1L;
		String email = "test@example.com";
		String nickname = "tester";
		Positions positions = Positions.STUDENT;
		UserEntity userEntity = UserEntity.of(email, nickname, positions);

		String toolName = "test";
		Tool tool = Tool.builder().toolMainName(toolName).build();

		String title = "title";
		String content = "content";
		Board board = Board.create(tool, userEntity, title, content);

		String photoUrl = "http://test.test";
		CommentEntity comment = CommentEntity.of(content, photoUrl, userEntity, board);

		NotificationEntity notification = NotificationEntity.of(userEntity, NotificationType.COMMENT, title, content,
			comment);

		// when
		when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
		when(notificationRepository.findTop3ByReceiverOrderByCreatedAtDesc(userEntity)).thenReturn(
			List.of(notification));
		List<NotificationResponse> responses = notificationService.getRecentNotifications(userId);

		// then
		assertEquals(1, responses.size());
		verify(notificationRepository).findTop3ByReceiverOrderByCreatedAtDesc(userEntity);
	}

	@Test
	@DisplayName("알림 읽음 성공")
	void readNotification_ShouldMarkNotificationAsRead() {
		// given
		Long userId = 1L;
		String email = "test@example.com";
		String nickname = "tester";
		Positions positions = Positions.STUDENT;
		UserEntity userEntity = UserEntity.of(email, nickname, positions);
		ReflectionTestUtils.setField(userEntity, "id", userId);

		String toolName = "test";
		Tool tool = Tool.builder().toolMainName(toolName).build();

		String title = "title";
		String content = "content";
		Board board = Board.create(tool, userEntity, title, content);

		String photoUrl = "http://test.test";
		CommentEntity comment = CommentEntity.of(content, photoUrl, userEntity, board);

		Long notificationId = 1L;
		NotificationEntity notification = NotificationEntity.of(userEntity, NotificationType.COMMENT, title, content,
			comment);

		// when
		when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

		notificationService.readNotification(userId, notificationId);

		// then
		assertTrue(notification.isRead());
		verify(notificationRepository).findById(notificationId);
	}

	@Test
	@DisplayName("알림 읽음 실패 - 알림이 없음")
	void readNotification_ShouldThrowException_WhenNotificationNotFound() {
		// given
		Long userId = 1L;
		Long notificationId = 1L;

		// when
		when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

		NotFoundException exception = assertThrows(NotFoundException.class,
			() -> notificationService.readNotification(userId, notificationId));

		// then
		assertEquals(ErrorCode.NOTIFICATION_NOT_FOUND, exception.getErrorCode());
	}

	@Test
	@DisplayName("알림 읽음 실패 - 다른 사람 알림 읽기 시도")
	void readNotification_ShouldThrowException_WhenNotificationIsNotMine() {
		// given
		Long user1Id = 1L;
		Long user2Id = 2L;
		String email = "test@example.com";
		String nickname = "tester";
		Positions positions = Positions.STUDENT;
		UserEntity userEntity = UserEntity.of(email, nickname, positions);
		ReflectionTestUtils.setField(userEntity, "id", user1Id);

		String toolName = "test";
		Tool tool = Tool.builder().toolMainName(toolName).build();

		String title = "title";
		String content = "content";
		Board board = Board.create(tool, userEntity, title, content);

		String photoUrl = "http://test.test";
		CommentEntity comment = CommentEntity.of(content, photoUrl, userEntity, board);

		Long notificationId = 1L;
		NotificationEntity notification = NotificationEntity.of(userEntity, NotificationType.COMMENT, title, content,
			comment);

		// when
		when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

		BadRequestException exception = assertThrows(BadRequestException.class,
			() -> notificationService.readNotification(user2Id, notificationId));

		// then
		assertEquals(ErrorCode.NOTIFICATION_READ_FORBIDDEN, exception.getErrorCode());
	}
}
