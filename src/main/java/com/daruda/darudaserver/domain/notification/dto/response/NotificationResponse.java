package com.daruda.darudaserver.domain.notification.dto.response;

import java.time.LocalDateTime;

import com.daruda.darudaserver.domain.comment.entity.Comment;
import com.daruda.darudaserver.domain.notification.entity.Notification;
import com.daruda.darudaserver.domain.notification.entity.enums.NotificationType;
import com.fasterxml.jackson.annotation.JsonFormat;

public record NotificationResponse(
	Long id,
	String title,
	String content,
	Long boardId,
	NotificationType type,
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd", timezone = "Asia/Seoul")
	LocalDateTime createdAt,
	boolean isRead,
	String url
) {
	public static NotificationResponse from(Notification notificationEntity) {
		return new NotificationResponse(
			notificationEntity.getId(),
			notificationEntity.getTitle(),
			notificationEntity.getContent(),
			getBoardId(notificationEntity.getComment()),
			notificationEntity.getType(),
			notificationEntity.getCreatedAt(),
			notificationEntity.isRead(),
			notificationEntity.getUrl()
		);
	}

	private static Long getBoardId(Comment comment) {
		if (comment != null) {
			return comment.getBoard().getId();
		}
		return null;
	}
}
