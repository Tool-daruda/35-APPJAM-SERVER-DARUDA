package com.daruda.darudaserver.domain.comment.entity;

import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.domain.user.entity.UserEntity;
import com.daruda.darudaserver.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "comment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql =
	"UPDATE comment SET is_deleted = true, deleted_at = NOW()" +
	"WHERE comment_id = ?")
@SQLRestriction("is_deleted = false")
public class CommentEntity extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "comment_id")
	private Long id;

	@Column(nullable = false, length = 1_000)
	private String content;

	@Column(name = "comment_photo_url")
	private String photoUrl;

	@Column(name = "is_deleted", nullable = false)
	private boolean isDeleted = false;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "board_id", nullable = false)
	private Board board;

	@Builder
	private CommentEntity(
		String content, String photoUrl, UserEntity user, Board board
	) {
		this.content = content;
		this.photoUrl = photoUrl;
		this.user = user;
		this.board = board;

		this.isDeleted = false;
		this.deletedAt = null;
	}

	public static CommentEntity of(
		String content, String photoUrl, UserEntity user, Board board
	) {
		return CommentEntity.builder()
			.content(content)
			.photoUrl(photoUrl)
			.user(user)
			.board(board)
			.build();
	}

	public void updateContent(String content) {
		this.content = content;
	}

	public void updatePhotoUrl(String photoUrl) {
		this.photoUrl = photoUrl;
	}
}
