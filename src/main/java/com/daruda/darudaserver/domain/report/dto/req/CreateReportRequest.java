package com.daruda.darudaserver.domain.report.dto.req;

import com.daruda.darudaserver.domain.report.entity.ReportType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "신고 생성 요청")
public class CreateReportRequest {

	@Nullable
	@Schema(description = "신고할 게시글의 ID (댓글 ID가 없을 경우 필수)", example = "1")
	private Long boardId;

	@Nullable
	@Schema(description = "신고할 댓글의 ID (게시글 ID가 없을 경우 필수)", example = "1")
	private Long commentId;

	@NotNull(message = "신고 유형은 필수입니다.")
	@Schema(description = "신고 유형", example = "SPAM", required = true)
	private ReportType reportType;

	@Nullable
	@Size(max = 1_000, message = "신고 상세 내용은 1000자를 초과할 수 없습니다.")
	@Schema(description = "신고 상세 내용 (선택)", example = "광고성 게시글입니다.", required = false, maxLength = 1000)
	private String detail;

	@NotNull(message = "신고 제목은 필수입니다.")
	@Schema(description = "신고 제목", example = "광고성 게시로 인한 신고.", required = true, maxLength = 100)
	private String title;

	@Builder
	public CreateReportRequest(
		Long boardId,
		Long commentId,
		ReportType reportType,
		String title,
		String detail
	) {
		this.boardId = boardId;
		this.commentId = commentId;
		this.reportType = reportType;
		this.title = title;
		this.detail = detail;
	}

	public boolean isCommentReport() {
		return commentId != null;
	}

	@AssertTrue(message = "게시글 ID 또는 댓글 ID 중 하나만 입력해야 합니다.")
	private boolean isValidTarget() {
		// 정확히 하나만 있어야 함
		return (boardId != null) ^ (commentId != null);
	}
}
