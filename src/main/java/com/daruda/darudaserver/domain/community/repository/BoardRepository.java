package com.daruda.darudaserver.domain.community.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.domain.tool.entity.Tool;
import com.daruda.darudaserver.domain.user.entity.UserEntity;

import jakarta.transaction.Transactional;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {

	@Query("SELECT b FROM Board b "
		+ "WHERE (:tool IS NULL OR b.tool = :tool) "
		+ "AND (:isFree IS NULL OR b.isFree = :isFree) "
		+ "AND b.id < :cursor "
		+ "AND b.delYn = false "
		+ "ORDER BY b.createdAt DESC")
	List<Board> findBoards(
		@Param("tool") Tool tool,
		@Param("isFree") Boolean isFree,
		@Param("cursor") Long cursor,
		Pageable pageable);

	@Modifying
	@Transactional
	void deleteAllByUserId(@Param("userId") Long userId);

	List<Board> findAllByUserId(Long userId);

	Page<Board> findAllByUserIdAndDelYnFalse(Long userId, Pageable pageable);

	Optional<Board> findByIdAndDelYn(Long boardId, boolean delYn);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query(value = "UPDATE board SET tool_id = 0, updated_at = CURRENT_TIMESTAMP WHERE tool_id = :#{#tool.toolId}", nativeQuery = true)
	void clearTool(Tool tool);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query(value = "UPDATE board SET user_id = 0, updated_at = CURRENT_TIMESTAMP WHERE user_id = :#{#userEntity.id}", nativeQuery = true)
	void clearUser(UserEntity userEntity);
}
