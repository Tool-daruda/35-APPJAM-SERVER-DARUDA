package com.daruda.darudaserver.domain.tool.entity;

import com.daruda.darudaserver.domain.user.entity.User;
import com.daruda.darudaserver.global.common.entity.BaseTimeEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tool_scrap")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ToolScrap extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long toolScrapId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tool_id", nullable = false)
	private Tool tool;

	@NotNull
	private boolean delYn = false;

	@Builder
	private ToolScrap(final Long toolScrapId, final User user, final Tool tool) {
		this.toolScrapId = toolScrapId;
		this.user = user;
		this.tool = tool;
	}

	public static ToolScrap of(final User user, final Tool tool) {
		return ToolScrap.builder()
			.user(user)
			.tool(tool)
			.build();
	}

	public void update() {
		this.delYn = !this.delYn;
	}
}
