package com.daruda.darudaserver.domain.tool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import com.daruda.darudaserver.domain.tool.entity.Tool;
import com.daruda.darudaserver.domain.tool.entity.ToolLike;
import com.daruda.darudaserver.domain.user.entity.UserEntity;

@Repository
public interface ToolLikeRepository extends JpaRepository<ToolLike, Long> {

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	void deleteAllByUserId(Long userId);

	boolean existsByUserAndTool(final UserEntity user, final Tool tool);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	void deleteByUserAndTool(final UserEntity user, final Tool tool);

	int countByTool_ToolId(final Long toolId);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	void deleteByTool(Tool tool);
}
