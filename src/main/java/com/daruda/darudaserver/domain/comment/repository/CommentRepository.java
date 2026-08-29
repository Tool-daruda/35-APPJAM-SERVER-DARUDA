package com.daruda.darudaserver.domain.comment.repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

	@Query("SELECT c.board.id, COUNT(c) FROM Comment c "
		+ "WHERE c.board.id IN :boardIds AND c.isDeleted = false GROUP BY c.board.id")
	List<Object[]> countByBoardIds(@Param("boardIds") List<Long> boardIds);

	default Map<Long, Long> countMapByBoardIds(final List<Long> boardIds) {
		return countByBoardIds(boardIds).stream()
			.collect(Collectors.toMap(row -> (Long)row[0], row -> (Long)row[1]));
	}

	@Query("SELECT DISTINCT c.user FROM Comment c "
		+ "WHERE c.board.id = :boardId "
		+ "AND c.isDeleted = false ")
	List<User> findDistinctUserByBoardId(@Param("boardId") Long boardId);
}
