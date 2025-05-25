package com.daruda.darudaserver.domain.report.service;

import com.daruda.darudaserver.domain.comment.entity.CommentEntity;
import com.daruda.darudaserver.domain.comment.repository.CommentRepository;
import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.domain.community.repository.BoardRepository;
import com.daruda.darudaserver.domain.report.dto.req.CreateReportRequest;
import com.daruda.darudaserver.domain.report.dto.res.CreateReportResponse;
import com.daruda.darudaserver.domain.report.entity.ReportEntity;
import com.daruda.darudaserver.domain.report.repository.ReportRepository;
import com.daruda.darudaserver.domain.user.entity.UserEntity;
import com.daruda.darudaserver.domain.user.repository.UserRepository;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ReportService {

	private final UserRepository userRepository;
	private final ReportRepository reportRepository;
	private final BoardRepository boardRepository;
	private final CommentRepository commentRepository;

	@Transactional
	public CreateReportResponse createReport(Long userId, CreateReportRequest request) {
		UserEntity reportedUser;
		UserEntity reporter = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

		CommentEntity comment = null;
		Board board = null;

		if (request.isCommentReport()) {
			comment = commentRepository.findById(Objects.requireNonNull(request.getCommentId()))
				.orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

			board = comment.getBoard();
			reportedUser = comment.getUser();

			// 중복 신고 검증
			if (reportRepository.existsByReporterAndComment(reporter, comment)) {
				throw new BusinessException(ErrorCode.ALREADY_REPORTED);
			}
		} else {
			board = boardRepository.findById(Objects.requireNonNull(request.getBoardId()))
				.orElseThrow(() -> new BusinessException(ErrorCode.BOARD_NOT_FOUND));

			reportedUser = board.getUser();

			// 중복 신고 검증
			if (reportRepository.existsByReporterAndBoard(reporter, board)) {
				throw new BusinessException(ErrorCode.ALREADY_REPORTED);
			}
		}

		ReportEntity report = ReportEntity.of(
			reporter,
			reportedUser,
			board,
			comment,
			request.getReportType(),
			request.getDetail()
		);

		report = reportRepository.save(report);
		return CreateReportResponse.from(report);
	}
}
