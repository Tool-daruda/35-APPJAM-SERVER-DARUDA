package com.daruda.darudaserver.domain.tool.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import com.daruda.darudaserver.domain.tool.entity.Tool;
import com.daruda.darudaserver.domain.tool.entity.ToolPlatForm;

import jakarta.transaction.Transactional;

@Repository
public interface ToolPlatFormRepository extends JpaRepository<ToolPlatForm, Long> {
	List<ToolPlatForm> findAllByTool(final Tool tool);

	Optional<ToolPlatForm> findFirstByTool(final Tool tool);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	void deleteByTool(Tool tool);
}
