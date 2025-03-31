package com.daruda.darudaserver.domain.community.entity;

import com.daruda.darudaserver.domain.tool.entity.Tool;
import com.daruda.darudaserver.domain.user.entity.UserEntity;
import com.daruda.darudaserver.global.common.entity.BaseTimeEntity;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.BadRequestException;

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

@Getter
@Entity
@Builder
@Table(name = "board")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Board extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "board_id")
	private Long id;

	@NotNull
	private String title;

	@NotNull
	private String content;

	@NotNull
	@Builder.Default
	private boolean delYn = false;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tool_id")
	private Tool tool;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity user;

	@Builder.Default
	private boolean isFree = true;

	@Builder
	public Board(final String title, final String content, final Tool tool, final UserEntity user, final boolean delYn,
		final boolean isFree) {
		this.title = title;
		this.content = content;
		this.tool = tool;
		this.user = user;
		this.delYn = delYn;
		this.isFree = isFree;
	}

	public static Board create(final Tool tool, final UserEntity user, final String title, final String content) {
		if (tool == null) {
			throw new BadRequestException(ErrorCode.BAD_REQUEST_DATA);
		}
		return Board.builder()
			.tool(tool)
			.user(user)
			.title(title)
			.content(content)
			.isFree(false)
			.build();
	}

	public static Board createFree(final UserEntity user, final String title, final String content) {
		return Board.builder()
			.tool(null)
			.user(user)
			.title(title)
			.content(content)
			.isFree(true)
			.build();
	}

	public void update(final Tool tool, final UserEntity user, final String title, final String content,
		final boolean isFree) {
		this.tool = tool;
		this.user = user;
		this.title = title;
		this.content = content;
		this.isFree = isFree;
	}

	public void delete() {
		this.delYn = true;
	}
}
