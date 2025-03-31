package com.daruda.darudaserver.domain.comment.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.daruda.darudaserver.domain.comment.entity.CommentEntity;

import jakarta.transaction.Transactional;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

	@Query("SELECT c FROM CommentEntity c "
		+ "WHERE c.board.id = :boardId "
		+ "AND c.id < :cursor "
		+ "ORDER BY c.createdAt DESC")
	List<CommentEntity> findAllByBoardId(
		@Param("boardId") Long boardId,
		@Param("cursor") Long cursor,
		Pageable pageable);

	List<CommentEntity> findAllByBoardId(Long boardId);

	@Modifying
	@Transactional
	void deleteAllByUserId(@Param("userId") Long userId);

	@Modifying
	@Transactional
	void deleteByBoardId(@Param("boardId") Long boardId);

}
