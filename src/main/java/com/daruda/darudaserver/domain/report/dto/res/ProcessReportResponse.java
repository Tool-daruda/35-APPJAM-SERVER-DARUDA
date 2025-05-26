package com.daruda.darudaserver.domain.report.dto.res;

import com.daruda.darudaserver.domain.report.entity.ReportEntity;
import com.daruda.darudaserver.domain.report.entity.ReportStatus;
import com.daruda.darudaserver.domain.report.entity.SuspensionDuration;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Schema(description = "신고 처리 응답")
public class ProcessReportResponse {

    @Schema(description = "신고 ID", example = "1")
    private final Long id;

    @Schema(description = "처리 상태", example = "APPROVED")
    private final ReportStatus status;

    @Schema(description = "제재 기간(일)", example = "7")
    private final Integer suspensionDays;

    @Schema(description = "제재 기간", example = "SEVEN")
    private final SuspensionDuration suspensionDuration;

    @Schema(description = "처리자 ID", example = "1")
    private final Long processedById;

    @Schema(description = "처리 일시")
    private final LocalDateTime processedAt;

    @Schema(description = "처리 메모", example = "불건전한 게시글로 인한 제재")
    private final String processNote;

    @Builder
    private ProcessReportResponse(
            Long id,
            ReportStatus status,
            Integer suspensionDays,
            SuspensionDuration suspensionDuration,
            Long processedById,
            LocalDateTime processedAt,
            String processNote
    ) {
        this.id = id;
        this.status = status;
        this.suspensionDays = suspensionDays;
        this.suspensionDuration = suspensionDuration;
        this.processedById = processedById;
        this.processedAt = processedAt;
        this.processNote = processNote;
    }

    public static ProcessReportResponse from(ReportEntity report) {
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