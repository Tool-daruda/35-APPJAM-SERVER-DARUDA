package com.daruda.darudaserver.domain.tool.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.daruda.darudaserver.domain.tool.entity.RelatedTool;
import com.daruda.darudaserver.domain.tool.entity.Tool;

@Repository
public interface RelatedToolRepository extends JpaRepository<RelatedTool, Long> {
	List<RelatedTool> findAllByTool(Tool tool);
}
