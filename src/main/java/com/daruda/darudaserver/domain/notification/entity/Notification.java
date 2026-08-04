package com.daruda.darudaserver.domain.notification.entity;

import java.time.LocalDateTime;

import com.daruda.darudaserver.domain.comment.entity.Comment;
import com.daruda.darudaserver.domain.notification.entity.enums.NotificationType;
import com.daruda.darudaserver.domain.user.entity.User;
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
public class Notification extends BaseTimeEntity {

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
	private Comment comment;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User receiver;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private NotificationType type;

	@Column(nullable = false)
	private boolean isRead;

	@Column
	private String url;

	@Builder
	public Notification(String title, String content, Comment comment, User receiver,
		NotificationType type, boolean isRead, String url) {
		this.title = title;
		this.content = content;
		this.comment = comment;
		this.receiver = receiver;
		this.type = type;
		this.isRead = isRead;
		this.url = url;
	}

	public static Notification of(User receiver, NotificationType type, String title, String content,
		Comment comment) {
		return Notification.builder()
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

	public LocalDateTime getCreatedAt() {
		return super.getCreatedAt();
	}
}
