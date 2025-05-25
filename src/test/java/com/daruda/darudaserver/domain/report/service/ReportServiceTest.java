package com.daruda.darudaserver.domain.report.service;

import com.daruda.darudaserver.domain.comment.entity.CommentEntity;
import com.daruda.darudaserver.domain.comment.repository.CommentRepository;
import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.domain.community.repository.BoardRepository;
import com.daruda.darudaserver.domain.report.dto.req.CreateReportRequest;
import com.daruda.darudaserver.domain.report.dto.res.CreateReportResponse;
import com.daruda.darudaserver.domain.report.entity.ReportEntity;
import com.daruda.darudaserver.domain.report.entity.ReportType;
import com.daruda.darudaserver.domain.report.repository.ReportRepository;
import com.daruda.darudaserver.domain.tool.entity.Tool;
import com.daruda.darudaserver.domain.user.entity.UserEntity;
import com.daruda.darudaserver.domain.user.entity.enums.Positions;
import com.daruda.darudaserver.domain.user.repository.UserRepository;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

	@Mock
	private ReportRepository reportRepository;

	@Mock
	private BoardRepository boardRepository;

	@Mock
	private CommentRepository commentRepository;

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private ReportService reportService;

	private UserEntity reporter;
	private Board board;
	private CommentEntity comment;
	private ReportEntity report;

	@BeforeEach
	void setUp() {
		reporter = UserEntity.builder()
			.email("reporter@test.com")
			.nickname("신고자")
			.positions(Positions.WORKER)
			.build();
		ReflectionTestUtils.setField(reporter, "id", 1L);

		UserEntity reportedUser = UserEntity.builder()
			.email("target@test.com")
			.nickname("대상자")
			.positions(Positions.WORKER)
			.build();
		ReflectionTestUtils.setField(reportedUser, "id", 2L);

		Tool tool = Tool.builder().toolMainName("툴").build();

		board = Board.create(tool, reportedUser, "제목", "본문");
		ReflectionTestUtils.setField(board, "id", 10L);

		comment = CommentEntity.of("댓글 내용", null, reportedUser, board);
		ReflectionTestUtils.setField(comment, "id", 100L);
	}

	@Nested
	@DisplayName("신고 생성")
	class Create {

		@Test
		@DisplayName("게시글 신고 성공")
		void createBoardReport_success() {
			// given
			given(userRepository.findById(reporter.getId())).willReturn(Optional.of(reporter));
			given(boardRepository.findById(board.getId())).willReturn(Optional.of(board));
			given(reportRepository.existsByReporterAndBoard(reporter, board))
				.willReturn(false);
			given(reportRepository.save(any(ReportEntity.class)))
				.willAnswer(inv -> {
					ReportEntity r = inv.getArgument(0);
					ReflectionTestUtils.setField(r, "id", 1000L);
					return r;
				});

			CreateReportRequest request = CreateReportRequest.builder()
				.boardId(board.getId())
				.reportType(ReportType.SPAM)
				.detail("스팸 게시글입니다")
				.build();

			// when
			CreateReportResponse response = reportService.createReport(reporter.getId(), request);

			// then
			Long reportId = response.getId();
			assertThat(reportId).isEqualTo(1000L);
		}

		@Test
		@DisplayName("댓글 신고 성공")
		void createCommentReport_success() {
			// given
			given(userRepository.findById(reporter.getId())).willReturn(Optional.of(reporter));
			given(commentRepository.findById(comment.getId())).willReturn(Optional.of(comment));
			given(reportRepository.existsByReporterAndComment(reporter, comment))
				.willReturn(false);
			given(reportRepository.save(any(ReportEntity.class)))
				.willAnswer(inv -> {
					ReportEntity r = inv.getArgument(0);
					ReflectionTestUtils.setField(r, "id", 1000L);
					return r;
				});

			CreateReportRequest request = CreateReportRequest.builder()
				.commentId(comment.getId())
				.reportType(ReportType.SPAM)
				.detail("스팸 댓글입니다")
				.build();

			// when
			CreateReportResponse response = reportService.createReport(reporter.getId(), request);

			// then
			Long reportId = response.getId();
			assertThat(reportId).isEqualTo(1000L);
		}

		@Test
		@DisplayName("신고자가 존재하지 않으면 BusinessException")
		void create_reporter_not_found() {
			// given
			given(userRepository.findById(anyLong())).willReturn(Optional.empty());

			CreateReportRequest request = CreateReportRequest.builder()
				.boardId(board.getId())
				.reportType(ReportType.SPAM)
				.build();

			// when & then
			assertThatThrownBy(() -> reportService.createReport(99L, request))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
		}

		@Test
		@DisplayName("게시글이 존재하지 않으면 BusinessException")
		void create_board_not_found() {
			// given
			given(userRepository.findById(reporter.getId())).willReturn(Optional.of(reporter));
			given(boardRepository.findById(anyLong())).willReturn(Optional.empty());

			CreateReportRequest request = CreateReportRequest.builder()
				.boardId(99L)
				.reportType(ReportType.SPAM)
				.build();

			// when & then
			assertThatThrownBy(() -> reportService.createReport(reporter.getId(), request))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.BOARD_NOT_FOUND);
		}

		@Test
		@DisplayName("댓글이 존재하지 않으면 BusinessException")
		void create_comment_not_found() {
			// given
			given(userRepository.findById(reporter.getId())).willReturn(Optional.of(reporter));
			given(commentRepository.findById(anyLong())).willReturn(Optional.empty());

			CreateReportRequest request = CreateReportRequest.builder()
				.commentId(99L)
				.reportType(ReportType.SPAM)
				.build();

			// when & then
			assertThatThrownBy(() -> reportService.createReport(reporter.getId(), request))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_NOT_FOUND);
		}

		@Test
		@DisplayName("이미 신고한 게시글을 다시 신고하면 BusinessException")
		void create_duplicate_board_report() {
			// given
			given(userRepository.findById(reporter.getId())).willReturn(Optional.of(reporter));
			given(boardRepository.findById(board.getId())).willReturn(Optional.of(board));
			given(reportRepository.existsByReporterAndBoard(reporter, board))
				.willReturn(true);

			CreateReportRequest request = CreateReportRequest.builder()
				.boardId(board.getId())
				.reportType(ReportType.SPAM)
				.build();

			// when & then
			assertThatThrownBy(() -> reportService.createReport(reporter.getId(), request))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_REPORTED);
		}

		@Test
		@DisplayName("이미 신고한 댓글을 다시 신고하면 BusinessException")
		void create_duplicate_comment_report() {
			// given
			given(userRepository.findById(reporter.getId())).willReturn(Optional.of(reporter));
			given(commentRepository.findById(comment.getId())).willReturn(Optional.of(comment));
			given(reportRepository.existsByReporterAndComment(reporter, comment))
				.willReturn(true);

			CreateReportRequest request = CreateReportRequest.builder()
				.commentId(comment.getId())
				.reportType(ReportType.SPAM)
				.build();

			// when & then
			assertThatThrownBy(() -> reportService.createReport(reporter.getId(), request))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_REPORTED);
		}
	}
}
