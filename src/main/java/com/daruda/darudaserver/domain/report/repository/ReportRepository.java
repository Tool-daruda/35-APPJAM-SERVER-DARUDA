package com.daruda.darudaserver.domain.report.repository;

import com.daruda.darudaserver.domain.comment.entity.CommentEntity;
import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.domain.report.entity.ReportEntity;
import com.daruda.darudaserver.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<ReportEntity, Long> {

	boolean existsByReporterAndBoard(UserEntity reporter, Board board);

	boolean existsByReporterAndComment(UserEntity reporter, CommentEntity comment);
}
