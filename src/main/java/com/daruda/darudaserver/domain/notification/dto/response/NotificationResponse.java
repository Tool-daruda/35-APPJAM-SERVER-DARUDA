package com.daruda.darudaserver.domain.notification.dto.response;

import java.sql.Timestamp;

import com.daruda.darudaserver.domain.notification.entity.NotificationEntity;
import com.daruda.darudaserver.domain.notification.entity.enums.NotificationType;
import com.fasterxml.jackson.annotation.JsonFormat;

public record NotificationResponse(
	Long id,
	String title,
	String content,
	Long boardId,
	NotificationType type,
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd", timezone = "Asia/Seoul")
	Timestamp createdAt,
	boolean isRead
) {
	public static NotificationResponse from(NotificationEntity notificationEntity) {
		return new NotificationResponse(
			notificationEntity.getId(),
			notificationEntity.getTitle(),
			notificationEntity.getContent(),
			notificationEntity.getComment() != null
				? notificationEntity.getComment().getBoard().getId() : null,
			notificationEntity.getType(),
			notificationEntity.getCreatedAt(),
			notificationEntity.isRead()
		);
	}
}
