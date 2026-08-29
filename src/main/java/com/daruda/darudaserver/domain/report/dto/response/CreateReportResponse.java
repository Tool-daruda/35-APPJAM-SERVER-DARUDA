package com.daruda.darudaserver.domain.report.dto.response;

import java.time.LocalDateTime;

import com.daruda.darudaserver.domain.report.entity.Report;
import com.daruda.darudaserver.domain.report.entity.ReportType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "신고 생성 응답")
public record CreateReportResponse(
	@Schema(description = "신고 ID")
	Long id,

	@Schema(description = "신고자 ID")
	Long reporterId,

	@Schema(description = "신고자 닉네임")
	String reporterNickname,

	@Schema(description = "신고 대상자 ID")
	Long reportedUserId,

	@Schema(description = "신고 대상자 닉네임")
	String reportedUserNickname,

	@Schema(description = "신고된 게시글 ID")
	Long boardId,

	@Schema(description = "신고된 댓글 ID (댓글 신고인 경우)")
	Long commentId,

	@Schema(description = "신고 유형")
	ReportType reportType,

	@Schema(description = "신고 제목")
	String title,

	@Schema(description = "신고 상세 내용")
	String detail,

	@Schema(description = "생성 일시")
	LocalDateTime createdAt,

	@Schema(description = "수정 일시")
	LocalDateTime updatedAt
) {

	public static CreateReportResponse from(Report report) {
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
			.createdAt(report.getCreatedAt())
			.updatedAt(report.getUpdatedAt())
			.build();
	}
}
