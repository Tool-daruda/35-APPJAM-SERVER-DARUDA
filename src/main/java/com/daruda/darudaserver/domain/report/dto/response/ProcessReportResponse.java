package com.daruda.darudaserver.domain.report.dto.response;

import java.time.LocalDateTime;

import com.daruda.darudaserver.domain.report.entity.Report;
import com.daruda.darudaserver.domain.report.entity.ReportStatus;
import com.daruda.darudaserver.domain.report.entity.SuspensionDuration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "신고 처리 응답")
public record ProcessReportResponse(
	@Schema(description = "신고 ID", example = "1")
	Long id,

	@Schema(description = "처리 상태", example = "APPROVED")
	ReportStatus status,

	@Schema(description = "제재 기간(일)", example = "7")
	Integer suspensionDays,

	@Schema(description = "제재 기간", example = "SEVEN")
	SuspensionDuration suspensionDuration,

	@Schema(description = "처리자 ID", example = "1")
	Long processedById,

	@Schema(description = "처리 일시")
	LocalDateTime processedAt,

	@Schema(description = "처리 메모", example = "불건전한 게시글로 인한 제재")
	String processNote
) {

	public static ProcessReportResponse from(Report report) {
		SuspensionDuration suspensionDuration = null;
		if (report.getSuspensionDays() != null) {
			// suspensionDays로부터 해당하는 enum 찾기
			for (SuspensionDuration duration : SuspensionDuration.values()) {
				if (duration.getDays() == report.getSuspensionDays()) {
					suspensionDuration = duration;
					break;
				}
			}
		}

		return ProcessReportResponse.builder()
			.id(report.getId())
			.status(report.getStatus())
			.suspensionDays(report.getSuspensionDays())
			.suspensionDuration(suspensionDuration)
			.processedById(report.getProcessedById())
			.processedAt(report.getProcessedAt())
			.processNote(report.getProcessNote())
			.build();
	}
}
