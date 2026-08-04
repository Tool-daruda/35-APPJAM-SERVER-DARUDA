package com.daruda.darudaserver.domain.report.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.daruda.darudaserver.domain.comment.entity.Comment;
import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.domain.report.entity.Report;
import com.daruda.darudaserver.domain.user.entity.User;

public interface ReportRepository extends JpaRepository<Report, Long> {

	boolean existsByReporterAndBoard(User reporter, Board board);

	boolean existsByReporterAndComment(User reporter, Comment comment);
}
