package com.daruda.darudaserver.domain.report.dto.res;

import java.time.LocalDateTime;

import com.daruda.darudaserver.domain.report.entity.ReportEntity;
import com.daruda.darudaserver.domain.report.entity.ReportType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(description = "신고 생성 응답")
public class CreateReportResponse {
	@Schema(description = "신고 ID")
	private final Long id;

	@Schema(description = "신고자 ID")
	private final Long reporterId;

	@Schema(description = "신고자 닉네임")
	private final String reporterNickname;

	@Schema(description = "신고 대상자 ID")
	private final Long reportedUserId;

	@Schema(description = "신고 대상자 닉네임")
	private final String reportedUserNickname;

	@Schema(description = "신고된 게시글 ID")
	private final Long boardId;

	@Schema(description = "신고된 댓글 ID (댓글 신고인 경우)")
	private final Long commentId;

	@Schema(description = "신고 유형")
	private final ReportType reportType;

	@Schema(description = "신고 제목")
	private final String title;

	@Schema(description = "신고 상세 내용")
	private final String detail;

	@Schema(description = "생성 일시")
	private final LocalDateTime createdAt;

	@Schema(description = "수정 일시")
	private final LocalDateTime updatedAt;

	@Builder
	private CreateReportResponse(
		Long id,
		Long reporterId,
		String reporterNickname,
		Long reportedUserId,
		String reportedUserNickname,
		Long boardId,
		Long commentId,
		ReportType reportType,
		String title,
		String detail,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
	) {
		this.id = id;
		this.reporterId = reporterId;
		this.reporterNickname = reporterNickname;
		this.reportedUserId = reportedUserId;
		this.reportedUserNickname = reportedUserNickname;
		this.boardId = boardId;
		this.commentId = commentId;
		this.reportType = reportType;
		this.title = title;
		this.detail = detail;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static CreateReportResponse from(ReportEntity report) {
		return CreateReportResponse.builder()
			.id(report.getId())
			.reporterId(report.getReporter().getId())
			.reporterNickname(report.getReporter().getNickname())
			.reportedUserId(report.getReportedUser().getId())
			.reportedUserNickname(report.getReportedUser().getNickname())
			.boardId(report.getBoard() != null ? report.getBoard().getId() : null)
			.commentId(report.getComment() != null ? report.getComment().getId() : null)
			.reportType(report.getReportType())
			.title(report.getTitle())
			.detail(report.getDetail())
			.createdAt(report.getCreatedAt().toLocalDateTime())
			.updatedAt(report.getUpdatedAt().toLocalDateTime())
			.build();
	}
}
