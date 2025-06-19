package com.daruda.darudaserver.domain.user.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.daruda.darudaserver.domain.user.entity.enums.Positions;
import com.daruda.darudaserver.global.common.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@SQLDelete(sql = "UPDATE user SET is_deleted = true, deleted_at = NOW() WHERE user_id = ?")
@SQLRestriction("is_deleted = false")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user")
public class UserEntity extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long id;

	@Column(nullable = false)
	private String email;

	@Column(name = "nickname", nullable = false)
	private String nickname;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Positions positions;

	@Column(name = "is_deleted", nullable = false)
	private boolean isDeleted;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Builder
	private UserEntity(String email, String nickname, Positions positions) {
		this.email = email;
		this.nickname = nickname;
		this.positions = positions;
		this.isDeleted = false;
	}

	public static UserEntity of(String email, String nickname, Positions positions) {
		return UserEntity.builder()
			.email(email)
			.nickname(nickname)
			.positions(positions)
			.build();
	}

	public void updatePositions(Positions positions) {
		this.positions = positions;
	}

	public void updateNickname(String nickname) {
		this.nickname = nickname;
	}

}
