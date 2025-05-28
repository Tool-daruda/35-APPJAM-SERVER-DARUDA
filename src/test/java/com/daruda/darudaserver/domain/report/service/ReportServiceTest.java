package com.daruda.darudaserver.domain.report.service;

import com.daruda.darudaserver.domain.comment.entity.CommentEntity;
import com.daruda.darudaserver.domain.comment.repository.CommentRepository;
import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.domain.community.repository.BoardRepository;
import com.daruda.darudaserver.domain.report.dto.req.CreateReportRequest;
import com.daruda.darudaserver.domain.report.dto.req.ProcessReportRequest;
import com.daruda.darudaserver.domain.report.dto.res.CreateReportResponse;
import com.daruda.darudaserver.domain.report.dto.res.ProcessReportResponse;
import com.daruda.darudaserver.domain.report.entity.ReportEntity;
import com.daruda.darudaserver.domain.report.entity.ReportStatus;
import com.daruda.darudaserver.domain.report.entity.ReportType;
import com.daruda.darudaserver.domain.report.entity.SuspensionDuration;
import com.daruda.darudaserver.domain.report.repository.ReportRepository;
import com.daruda.darudaserver.domain.tool.entity.Tool;
import com.daruda.darudaserver.domain.user.entity.UserEntity;
import com.daruda.darudaserver.domain.user.entity.enums.Positions;
import com.daruda.darudaserver.domain.user.repository.UserRepository;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.BusinessException;
import com.daruda.darudaserver.global.error.exception.ForbiddenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

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
	private UserEntity admin;
	private UserEntity reportedUser;
	private Board board;
	private CommentEntity comment;
	private ReportEntity report;
	private Tool tool;

	@BeforeEach
	void setUp() {
		reporter = UserEntity.builder()
			.email("reporter@test.com")
			.nickname("신고자")
			.positions(Positions.WORKER)
			.build();
		ReflectionTestUtils.setField(reporter, "id", 1L);

		admin = UserEntity.builder()
			.email("admin@test.com")
			.nickname("관리자")
			.positions(Positions.ADMIN)
			.build();
		ReflectionTestUtils.setField(admin, "id", 99L);

		reportedUser = UserEntity.builder()
			.email("target@test.com")
			.nickname("대상자")
			.positions(Positions.WORKER)
			.build();
		ReflectionTestUtils.setField(reportedUser, "id", 2L);

		tool = Tool.builder().toolMainName("툴").build();

		board = Board.create(tool, reportedUser, "제목", "본문");
		ReflectionTestUtils.setField(board, "id", 10L);

		comment = CommentEntity.of("댓글 내용", null, reportedUser, board);
		ReflectionTestUtils.setField(comment, "id", 100L);

		report = ReportEntity.of(reporter, reportedUser, board, null, ReportType.SPAM, "스팸 게시글");
		ReflectionTestUtils.setField(report, "id", 1000L);
	}

	@Nested
	@DisplayName("신고 생성")
	class CreateReport {

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
			assertThat(response.getId()).isEqualTo(1000L);
			then(reportRepository).should().save(any(ReportEntity.class));
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
			assertThat(response.getId()).isEqualTo(1000L);
			then(reportRepository).should().save(any(ReportEntity.class));
		}

		@Test
		@DisplayName("신고자가 존재하지 않으면 BusinessException")
		void createReport_reporter_not_found() {
			// given
			given(userRepository.findById(anyLong())).willReturn(Optional.empty());

			CreateReportRequest request = CreateReportRequest.builder()
				.boardId(board.getId())
				.reportType(ReportType.SPAM)
				.detail("스팸 게시글")
				.build();

			// when & then
			assertThatThrownBy(() -> reportService.createReport(99L, request))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
		}

		@Test
		@DisplayName("게시글이 존재하지 않으면 BusinessException")
		void createReport_board_not_found() {
			// given
			given(userRepository.findById(reporter.getId())).willReturn(Optional.of(reporter));
			given(boardRepository.findById(anyLong())).willReturn(Optional.empty());

			CreateReportRequest request = CreateReportRequest.builder()
				.boardId(99L)
				.reportType(ReportType.SPAM)
				.detail("스팸 게시글")
				.build();

			// when & then
			assertThatThrownBy(() -> reportService.createReport(reporter.getId(), request))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.BOARD_NOT_FOUND);
		}

		@Test
		@DisplayName("댓글이 존재하지 않으면 BusinessException")
		void createReport_comment_not_found() {
			// given
			given(userRepository.findById(reporter.getId())).willReturn(Optional.of(reporter));
			given(commentRepository.findById(anyLong())).willReturn(Optional.empty());

			CreateReportRequest request = CreateReportRequest.builder()
				.commentId(99L)
				.reportType(ReportType.SPAM)
				.detail("스팸 댓글")
				.build();

			// when & then
			assertThatThrownBy(() -> reportService.createReport(reporter.getId(), request))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_NOT_FOUND);
		}

		@Test
		@DisplayName("이미 신고한 게시글을 다시 신고하면 BusinessException")
		void createReport_duplicate_board_report() {
			// given
			given(userRepository.findById(reporter.getId())).willReturn(Optional.of(reporter));
			given(boardRepository.findById(board.getId())).willReturn(Optional.of(board));
			given(reportRepository.existsByReporterAndBoard(reporter, board))
				.willReturn(true);

			CreateReportRequest request = CreateReportRequest.builder()
				.boardId(board.getId())
				.reportType(ReportType.SPAM)
				.detail("스팸 게시글")
				.build();

			// when & then
			assertThatThrownBy(() -> reportService.createReport(reporter.getId(), request))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_REPORTED);
		}

		@Test
		@DisplayName("이미 신고한 댓글을 다시 신고하면 BusinessException")
		void createReport_duplicate_comment_report() {
			// given
			given(userRepository.findById(reporter.getId())).willReturn(Optional.of(reporter));
			given(commentRepository.findById(comment.getId())).willReturn(Optional.of(comment));
			given(reportRepository.existsByReporterAndComment(reporter, comment))
				.willReturn(true);

			CreateReportRequest request = CreateReportRequest.builder()
				.commentId(comment.getId())
				.reportType(ReportType.SPAM)
				.detail("스팸 댓글")
				.build();

			// when & then
			assertThatThrownBy(() -> reportService.createReport(reporter.getId(), request))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_REPORTED);
		}
	}

	@Nested
	@DisplayName("신고 처리")
	class ProcessReport {

		@Test
		@DisplayName("신고 처리 성공 - 제재 적용")
		void processReport_success_with_suspension() {
			// given
			given(userRepository.findById(admin.getId())).willReturn(Optional.of(admin));
			given(reportRepository.findById(report.getId())).willReturn(Optional.of(report));

			ProcessReportRequest request = ProcessReportRequest.builder()
				.status(ReportStatus.APPROVED)
				.suspensionDuration(SuspensionDuration.SEVEN)
				.processNote("불건전한 게시글로 인한 제재")
				.build();

			// when
			ProcessReportResponse response = reportService.processReport(admin.getId(), report.getId(), request);

			// then
			assertThat(response.getId()).isEqualTo(report.getId());
			assertThat(response.getStatus()).isEqualTo(ReportStatus.APPROVED);
		}

		@Test
		@DisplayName("신고 처리 성공 - 제재 없음")
		void processReport_success_without_suspension() {
			// given
			given(userRepository.findById(admin.getId())).willReturn(Optional.of(admin));
			given(reportRepository.findById(report.getId())).willReturn(Optional.of(report));

			ProcessReportRequest request = ProcessReportRequest.builder()
				.status(ReportStatus.REJECTED)
				.processNote("신고 내용이 부적절함")
				.build();

			// when
			ProcessReportResponse response = reportService.processReport(admin.getId(), report.getId(), request);

			// then
			assertThat(response.getId()).isEqualTo(report.getId());
			assertThat(response.getStatus()).isEqualTo(ReportStatus.REJECTED);
		}

		@Test
		@DisplayName("관리자가 존재하지 않으면 BusinessException")
		void processReport_admin_not_found() {
			// given
			given(userRepository.findById(anyLong())).willReturn(Optional.empty());

			ProcessReportRequest request = ProcessReportRequest.builder()
				.status(ReportStatus.APPROVED)
				.suspensionDuration(SuspensionDuration.SEVEN)
				.processNote("처리")
				.build();

			// when & then
			assertThatThrownBy(() -> reportService.processReport(99L, report.getId(), request))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
		}

		@Test
		@DisplayName("관리자가 아닌 사용자가 처리 시도하면 ForbiddenException")
		void processReport_not_admin() {
			// given
			given(userRepository.findById(reporter.getId())).willReturn(Optional.of(reporter));

			ProcessReportRequest request = ProcessReportRequest.builder()
				.status(ReportStatus.APPROVED)
				.suspensionDuration(SuspensionDuration.SEVEN)
				.processNote("처리")
				.build();

			// when & then
			assertThatThrownBy(() -> reportService.processReport(reporter.getId(), report.getId(), request))
				.isInstanceOf(ForbiddenException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED_ACCESS);
		}

		@Test
		@DisplayName("신고가 존재하지 않으면 BusinessException")
		void processReport_report_not_found() {
			// given
			given(userRepository.findById(admin.getId())).willReturn(Optional.of(admin));
			given(reportRepository.findById(anyLong())).willReturn(Optional.empty());

			ProcessReportRequest request = ProcessReportRequest.builder()
				.status(ReportStatus.APPROVED)
				.suspensionDuration(SuspensionDuration.SEVEN)
				.processNote("처리")
				.build();

			// when & then
			assertThatThrownBy(() -> reportService.processReport(admin.getId(), 999L, request))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.REPORT_NOT_FOUND);
		}

		@Test
		@DisplayName("이미 처리된 신고를 다시 처리하면 BusinessException")
		void processReport_already_processed() {
			// given
			given(userRepository.findById(admin.getId())).willReturn(Optional.of(admin));
			
			// 이미 처리된 신고로 설정
			ReportEntity processedReport = ReportEntity.of(reporter, reportedUser, board, null, ReportType.SPAM, "스팸");
			processedReport.updateStatus(ReportStatus.APPROVED);
			processedReport.updateProcessInfo(admin.getId(), "처리 완료");
			processedReport.updateSuspensionDays(7);
			given(reportRepository.findById(anyLong())).willReturn(Optional.of(processedReport));

			ProcessReportRequest request = ProcessReportRequest.builder()
				.status(ReportStatus.APPROVED)
				.suspensionDuration(SuspensionDuration.SEVEN)
				.processNote("재처리 시도")
				.build();

			// when & then
			assertThatThrownBy(() -> reportService.processReport(admin.getId(), 1L, request))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_PROCESSED_REPORT);
		}
	}

	@Nested
	@DisplayName("사용자 제재 상태 확인")
	class SuspensionCheck {

		@Test
		@DisplayName("제재되지 않은 사용자는 isSuspended가 false")
		void user_not_suspended() {
			// given
			UserEntity user = UserEntity.builder()
				.email("test@test.com")
				.nickname("테스트")
				.positions(Positions.WORKER)
				.build();

			// when & then
			assertThat(user.isSuspended()).isFalse();
		}

		@Test
		@DisplayName("제재 기간이 지난 사용자는 isSuspended가 false")
		void user_suspension_expired() {
			// given
			UserEntity user = UserEntity.builder()
				.email("test@test.com")
				.nickname("테스트")
				.positions(Positions.WORKER)
				.build();
			
			// 어제까지 제재
			user.suspend(LocalDateTime.now().minusDays(1), "테스트 제재");

			// when & then
			assertThat(user.isSuspended()).isFalse();
		}

		@Test
		@DisplayName("제재 기간 중인 사용자는 isSuspended가 true")
		void user_currently_suspended() {
			// given
			UserEntity user = UserEntity.builder()
				.email("test@test.com")
				.nickname("테스트")
				.positions(Positions.WORKER)
				.build();
			
			// 내일까지 제재
			user.suspend(LocalDateTime.now().plusDays(1), "테스트 제재");

			// when & then
			assertThat(user.isSuspended()).isTrue();
		}
	}

	@Nested
	@DisplayName("제재 기간 enum 테스트")
	class SuspensionDurationTest {

		@Test
		@DisplayName("SuspensionDuration enum 값 확인")
		void suspensionDuration_values() {
			// when & then
			assertThat(SuspensionDuration.ONE.getDays()).isEqualTo(1);
			assertThat(SuspensionDuration.THREE.getDays()).isEqualTo(3);
			assertThat(SuspensionDuration.SEVEN.getDays()).isEqualTo(7);
			assertThat(SuspensionDuration.THIRTY.getDays()).isEqualTo(30);
			assertThat(SuspensionDuration.NINETY.getDays()).isEqualTo(90);
			assertThat(SuspensionDuration.ONE_YEAR.getDays()).isEqualTo(365);
		}

		@Test
		@DisplayName("description으로 enum 찾기")
		void suspensionDuration_fromString() {
			// when & then
			assertThat(SuspensionDuration.fromString("1일")).isEqualTo(SuspensionDuration.ONE);
			assertThat(SuspensionDuration.fromString("7일")).isEqualTo(SuspensionDuration.SEVEN);
			assertThat(SuspensionDuration.fromString("1년")).isEqualTo(SuspensionDuration.ONE_YEAR);
		}

		@Test
		@DisplayName("잘못된 description으로 enum 찾기 시 예외 발생")
		void suspensionDuration_fromString_invalid() {
			// when & then
			assertThatThrownBy(() -> SuspensionDuration.fromString("잘못된 기간"))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_FIELD_ERROR);
		}
	}
}
