package com.daruda.darudaserver.domain.report.dto.req;

import com.daruda.darudaserver.domain.report.entity.ReportStatus;
import com.daruda.darudaserver.domain.report.entity.SuspensionDuration;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "신고 처리 요청")
public class ProcessReportRequest {

	@NotNull(message = "처리 상태는 필수입니다.")
	@Schema(description = "처리 상태", example = "APPROVED", required = true)
	private ReportStatus status;

	@Schema(description = "제재 기간", example = "SEVEN")
	private SuspensionDuration suspensionDuration;

	@Size(max = 500, message = "처리 메모는 500자를 초과할 수 없습니다.")
	@Schema(description = "처리 메모", example = "불건전한 게시글로 인한 제재", maxLength = 500)
	private String processNote;

	@Builder
	public ProcessReportRequest(ReportStatus status, SuspensionDuration suspensionDuration, String processNote) {
		this.status = status;
		this.suspensionDuration = suspensionDuration;
		this.processNote = processNote;
	}

	public Integer getSuspensionDays() {
		return suspensionDuration != null ? suspensionDuration.getDays() : null;
	}
}
