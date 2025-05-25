package com.daruda.darudaserver.domain.report.entity;

import com.daruda.darudaserver.domain.comment.entity.CommentEntity;
import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.domain.user.entity.UserEntity;
import com.daruda.darudaserver.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "report")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReportEntity extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "report_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reporter_id", nullable = false)
	private UserEntity reporter;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reported_user_id", nullable = false)
	private UserEntity reportedUser;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "board_id", nullable = true)
	private Board board;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "comment_id", nullable = true)
	private CommentEntity comment;

	@Enumerated(EnumType.STRING)
	@Column(name = "report_type", nullable = false, length = 50)
	private ReportType reportType;

	@Column(name = "detail", nullable = true, length = 1_000)
	private String detail;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private ReportStatus status;

	@Builder
	private ReportEntity(
		UserEntity reporter,
		UserEntity reportedUser,
		Board board,
		CommentEntity comment,
		ReportType reportType,
		String detail
	) {
		this.reporter = reporter;
		this.reportedUser = reportedUser;
		this.reportType = reportType;
		this.board = board;
		this.comment = comment;
		this.detail = detail;
		this.status = ReportStatus.PENDING;
	}

	public static ReportEntity of(
		UserEntity reporter,
		UserEntity reportedUser,
		Board board,
		CommentEntity comment,
		ReportType reportType,
		String detail
	) {
		return ReportEntity.builder()
			.reporter(reporter)
			.reportedUser(reportedUser)
			.board(board)
			.comment(comment)
			.reportType(reportType)
			.detail(detail)
			.build();
	}

	public void updateStatus(ReportStatus status) {
		this.status = status;
	}

	public boolean isCommentReport() {
		return comment != null;
	}
}
