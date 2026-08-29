package com.daruda.darudaserver.domain.tool.entity;

import com.daruda.darudaserver.domain.user.entity.User;
import com.daruda.darudaserver.global.common.entity.BaseTimeEntity;

import jakarta.persistence.Column;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tool_scrap")
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ToolScrap extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "tool_scrap_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tool_id", nullable = false)
	private Tool tool;

	@NotNull
	@Builder.Default
	private boolean delYn = false;

	@Builder
	private ToolScrap(final Long id, final User user, final Tool tool) {
		this.id = id;
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
