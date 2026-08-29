package com.daruda.darudaserver.domain.report.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daruda.darudaserver.domain.comment.entity.Comment;
import com.daruda.darudaserver.domain.comment.repository.CommentRepository;
import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.domain.community.repository.BoardRepository;
import com.daruda.darudaserver.domain.report.dto.request.CreateReportRequest;
import com.daruda.darudaserver.domain.report.dto.request.ProcessReportRequest;
import com.daruda.darudaserver.domain.report.dto.response.CreateReportResponse;
import com.daruda.darudaserver.domain.report.dto.response.ProcessReportResponse;
import com.daruda.darudaserver.domain.report.entity.Report;
import com.daruda.darudaserver.domain.report.repository.ReportRepository;
import com.daruda.darudaserver.domain.user.entity.User;
import com.daruda.darudaserver.domain.user.entity.enums.Positions;
import com.daruda.darudaserver.domain.user.repository.UserRepository;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.BusinessException;
import com.daruda.darudaserver.global.error.exception.ForbiddenException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {

	private final UserRepository userRepository;
	private final ReportRepository reportRepository;
	private final BoardRepository boardRepository;
	private final CommentRepository commentRepository;

	@Transactional
	public CreateReportResponse createReport(Long reporterId, CreateReportRequest request) {
		User reportedUser;
		User reporter = userRepository.findById(reporterId)
			.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

		Comment comment = null;
		Board board = null;

		if (request.isCommentReport()) {
			if (request.commentId() == null) {
				throw new BusinessException(ErrorCode.INVALID_FIELD_ERROR);
			}

			comment = commentRepository.findById(request.commentId())
				.orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

			board = comment.getBoard();
			reportedUser = comment.getUser();

			// 중복 신고 검증
			if (reportRepository.existsByReporterAndComment(reporter, comment)) {
				throw new BusinessException(ErrorCode.ALREADY_REPORTED);
			}
		} else {
			if (request.boardId() == null) {
				throw new BusinessException(ErrorCode.INVALID_FIELD_ERROR);
			}

			board = boardRepository.findById(request.boardId())
				.orElseThrow(() -> new BusinessException(ErrorCode.BOARD_NOT_FOUND));

			reportedUser = board.getUser();

			// 중복 신고 검증
			if (reportRepository.existsByReporterAndBoard(reporter, board)) {
				throw new BusinessException(ErrorCode.ALREADY_REPORTED);
			}
		}

		Report report = Report.of(
			reporter,
			reportedUser,
			board,
			comment,
			request.reportType(),
			request.title(),
			request.detail()
		);

		report = reportRepository.save(report);
		return CreateReportResponse.from(report);
	}

	@Transactional
	public ProcessReportResponse processReport(Long adminId, Long reportId, ProcessReportRequest request) {
		User admin = userRepository.findById(adminId)
			.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

		// 관리자 권한 검증
		if (admin.getPositions() != Positions.ADMIN) {
			throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
		}

		Report report = reportRepository.findById(reportId)
			.orElseThrow(() -> new BusinessException(ErrorCode.REPORT_NOT_FOUND));

		// 이미 처리된 신고인지 확인
		if (!report.isPending()) {
			throw new BusinessException(ErrorCode.ALREADY_PROCESSED_REPORT);
		}

		// 신고 상태 변경
		report.updateStatus(request.status());
		report.updateProcessInfo(admin.getId(), request.processNote(), LocalDateTime.now());
		report.updateSuspensionDays(request.getSuspensionDays());

		// 제재 적용
		if (report.shouldApplySuspension()) {
			User reportedUser = report.getReportedUser();
			reportedUser.suspend(
				LocalDateTime.now().plusDays(request.getSuspensionDays()),
				String.format("%s로 인한 %d일 활동 정지",
					report.getReportType().getDescription(),
					request.getSuspensionDays())
			);
		}

		return ProcessReportResponse.from(report);
	}
}
