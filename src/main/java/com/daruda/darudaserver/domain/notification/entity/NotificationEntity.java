package com.daruda.darudaserver.domain.notification.entity;

import java.sql.Timestamp;

import com.daruda.darudaserver.domain.comment.entity.CommentEntity;
import com.daruda.darudaserver.domain.notification.entity.enums.NotificationType;
import com.daruda.darudaserver.domain.user.entity.UserEntity;
import com.daruda.darudaserver.global.common.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity(name = "notification")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationEntity extends BaseTimeEntity {

	@Id
	@Column(name = "notification_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private String content;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "comment_id")
	private CommentEntity comment;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity receiver;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private NotificationType type;

	@Column(nullable = false)
	private boolean isRead;

	@Builder
	private NotificationEntity(String title, String content, CommentEntity comment, UserEntity receiver,
		NotificationType type, boolean isRead) {
		this.title = title;
		this.content = content;
		this.comment = comment;
		this.receiver = receiver;
		this.type = type;
		this.isRead = isRead;
	}

	public static NotificationEntity of(UserEntity receiver, NotificationType type, String title, String content,
		CommentEntity comment) {
		return NotificationEntity.builder()
			.receiver(receiver)
			.type(type)
			.title(title)
			.content(content)
			.comment(comment)
			.isRead(false)
			.build();
	}

	public void markAsRead() {
		this.isRead = true;
	}

	public Timestamp getCreatedAt() {
		return super.getCreatedAt();
	}
}
