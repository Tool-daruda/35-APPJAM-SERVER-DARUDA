package com.daruda.darudaserver.domain.notification.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.daruda.darudaserver.domain.comment.entity.Comment;
import com.daruda.darudaserver.domain.notification.entity.Notification;
import com.daruda.darudaserver.domain.user.entity.User;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
	List<Notification> findAllByReceiver(User receiver);

	void deleteAllByReceiver(User receiver);

	void deleteAllByComment(Comment comment);

	List<Notification> findTop3ByReceiverOrderByCreatedAtDesc(User receiver);
}
