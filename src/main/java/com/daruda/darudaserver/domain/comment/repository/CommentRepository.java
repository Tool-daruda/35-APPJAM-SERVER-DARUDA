package com.daruda.darudaserver.domain.comment.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.daruda.darudaserver.domain.comment.entity.Comment;
import com.daruda.darudaserver.domain.user.entity.User;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

	@Query("SELECT c FROM Comment c "
		+ "WHERE c.board.id = :boardId "
		+ "AND c.id < :cursor "
		+ "ORDER BY c.createdAt DESC")
	List<Comment> findCommentsByBoardId(
		@Param("boardId") Long boardId,
		@Param("cursor") Long cursor,
		Pageable pageable
	);

	List<Comment> findCommentsByBoardId(Long boardId);

	@Modifying
	@Transactional
	void deleteCommentsByUserId(@Param("userId") Long userId);

	@Modifying
	@Transactional
	void deleteCommentsByBoardId(@Param("boardId") Long boardId);

	@Query("SELECT COUNT(c) FROM Comment c WHERE c.board.id = :boardId AND c.isDeleted = false")
	int countByBoardId(@Param("boardId") Long boardId);

	@Query("SELECT DISTINCT c.user FROM Comment c "
		+ "WHERE c.board.id = :boardId "
		+ "AND c.isDeleted = false ")
	List<User> findDistinctUserByBoardId(@Param("boardId") Long boardId);
}
