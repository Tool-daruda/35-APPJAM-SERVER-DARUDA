package com.daruda.darudaserver.domain.tool.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.daruda.darudaserver.domain.tool.entity.Tool;
import com.daruda.darudaserver.domain.tool.entity.ToolBlog;

@Repository
public interface ToolBlogRepository extends JpaRepository<ToolBlog, Long> {
	List<ToolBlog> findAllByTool(final Tool tool);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	void deleteByTool(Tool tool);
}
