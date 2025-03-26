package com.daruda.darudaserver.domain.tool.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.daruda.darudaserver.domain.tool.entity.Tool;
import com.daruda.darudaserver.domain.tool.entity.ToolVideo;

@Repository
public interface ToolVideoRepository extends JpaRepository<ToolVideo, Long> {
	List<ToolVideo> findAllByTool(final Tool toolId);
}
