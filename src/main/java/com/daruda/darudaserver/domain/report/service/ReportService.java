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
import com.daruda.darudaserver.global.error.exception.BadRequestException;
import com.daruda.darudaserver.global.error.exception.ForbiddenException;
import com.daruda.darudaserver.global.error.exception.InvalidValueException;
import com.daruda.darudaserver.global.error.exception.NotFoundException;

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
			.orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

		Comment comment = null;
		Board board = null;

		if (request.isCommentReport()) {
			if (request.getCommentId() == null) {
				throw new InvalidValueException(ErrorCode.INVALID_FIELD_ERROR);
			}

			comment = commentRepository.findById(request.getCommentId())
				.orElseThrow(() -> new NotFoundException(ErrorCode.COMMENT_NOT_FOUND));

			board = comment.getBoard();
			reportedUser = comment.getUser();

			// 중복 신고 검증
			if (reportRepository.existsByReporterAndComment(reporter, comment)) {
				throw new BadRequestException(ErrorCode.ALREADY_REPORTED);
			}
		} else {
			if (request.getBoardId() == null) {
				throw new InvalidValueException(ErrorCode.INVALID_FIELD_ERROR);
			}

			board = boardRepository.findById(request.getBoardId())
				.orElseThrow(() -> new NotFoundException(ErrorCode.BOARD_NOT_FOUND));

			reportedUser = board.getUser();

			// 중복 신고 검증
			if (reportRepository.existsByReporterAndBoard(reporter, board)) {
				throw new BadRequestException(ErrorCode.ALREADY_REPORTED);
			}
		}

		Report report = Report.of(
			reporter,
			reportedUser,
			board,
			comment,
			request.getReportType(),
			request.getTitle(),
			request.getDetail()
		);

		report = reportRepository.save(report);
		return CreateReportResponse.from(report);
	}

	@Transactional
	public ProcessReportResponse processReport(Long adminId, Long reportId, ProcessReportRequest request) {
		User admin = userRepository.findById(adminId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

		// 관리자 권한 검증
		if (admin.getPositions() != Positions.ADMIN) {
			throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
		}

		Report report = reportRepository.findById(reportId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.REPORT_NOT_FOUND));

		// 이미 처리된 신고인지 확인
		if (!report.isPending()) {
			throw new BadRequestException(ErrorCode.ALREADY_PROCESSED_REPORT);
		}

		// 신고 상태 변경
		report.updateStatus(request.getStatus());
		report.updateProcessInfo(admin.getId(), request.getProcessNote(), LocalDateTime.now());
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
