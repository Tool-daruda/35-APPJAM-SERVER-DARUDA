package com.daruda.darudaserver.domain.tool.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.daruda.darudaserver.domain.tool.entity.Tool;
import com.daruda.darudaserver.domain.tool.entity.ToolLike;
import com.daruda.darudaserver.domain.user.entity.UserEntity;

import jakarta.transaction.Transactional;

@Repository
public interface ToolLikeRepository extends JpaRepository<ToolLike, Long> {

	@Modifying
	@Transactional
	void deleteAllByUserId(@Param("userId") Long userId);

	List<ToolLike> findAllByUserId(@Param("userId") final Long userId);

	Optional<ToolLike> findByUserAndTool(final UserEntity user, final Tool tool);

	// toolId와 delYn이 false인 경우의 좋아요 수 계산
	int countByTool_ToolIdAndDelYnFalse(final Long toolId);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	void deleteByTool(Tool tool);

	@Query("""
			select tl.tool.toolId
			from ToolLike tl
			where tl.user.id = :userId
			and tl.delYn = false
			and tl.tool.toolId in :toolIds
		""")
	Set<Long> findLikedToolIds(
		@Param("userId") Long userId,
		@Param("toolIds") List<Long> toolIds
	);
}
