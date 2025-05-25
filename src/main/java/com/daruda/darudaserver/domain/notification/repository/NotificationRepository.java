package com.daruda.darudaserver.domain.notification.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.daruda.darudaserver.domain.comment.entity.CommentEntity;
import com.daruda.darudaserver.domain.notification.entity.NotificationEntity;
import com.daruda.darudaserver.domain.user.entity.UserEntity;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
	List<NotificationEntity> findAllByReceiver(UserEntity receiver);

	void deleteAllByReceiver(UserEntity receiver);

	void deleteAllByComment(CommentEntity comment);

	List<NotificationEntity> findTop3ByReceiverOrderByCreatedAtDesc(UserEntity receiver);
}
