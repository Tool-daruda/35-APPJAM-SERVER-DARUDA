package com.daruda.darudaserver.domain.tool.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.daruda.darudaserver.domain.tool.entity.Tool;
import com.daruda.darudaserver.domain.tool.entity.ToolCore;

@Repository
public interface ToolCoreRepository extends JpaRepository<ToolCore, Long> {
	List<ToolCore> findAllByTool(Tool tool);
}
