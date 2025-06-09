package com.daruda.darudaserver.domain.report.entity;

import java.time.LocalDateTime;

import com.daruda.darudaserver.domain.comment.entity.CommentEntity;
import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.domain.user.entity.UserEntity;
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
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "report")
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
	private ReportStatus status = ReportStatus.PENDING;

	@Column(name = "suspension_days")
	private Integer suspensionDays;

	@Column(name = "processed_at")
	private LocalDateTime processedAt;

	@Column(name = "processed_by_id")
	private Long processedById;

	@Column(name = "process_note", length = 500)
	private String processNote;

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
		this.board = board;
		this.comment = comment;
		this.reportType = reportType;
		this.detail = detail;
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

	public void updateProcessInfo(Long processedById, String processNote, LocalDateTime processedAt) {
		this.processedById = processedById;
		this.processNote = processNote;
		this.processedAt = processedAt;
	}

	public void updateSuspensionDays(Integer suspensionDays) {
		this.suspensionDays = suspensionDays;
	}

	public boolean isPending() {
		return status == ReportStatus.PENDING;
	}

	public boolean isCommentReport() {
		return comment != null;
	}

	public boolean isApproved() {
		return status == ReportStatus.APPROVED;
	}

	public boolean shouldApplySuspension() {
		return isApproved() && suspensionDays != null && suspensionDays > 0;
	}
}
