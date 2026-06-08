package com.daruda.darudaserver.domain.tool.entity;

import com.daruda.darudaserver.domain.user.entity.UserEntity;
import com.daruda.darudaserver.global.common.entity.BaseTimeEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
	name = "tool_like",
	uniqueConstraints = @UniqueConstraint(name = "uk_tool_like_user_tool", columnNames = {"user_id", "tool_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ToolLike extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long toolLikeId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tool_id", nullable = false)
	private Tool tool;

	@Builder
	private ToolLike(final Long toolLikeId, final UserEntity user, final Tool tool) {
		this.toolLikeId = toolLikeId;
		this.user = user;
		this.tool = tool;
	}

	public static ToolLike of(final UserEntity user, final Tool tool) {
		return ToolLike.builder()
			.user(user)
			.tool(tool)
			.build();
	}
}
