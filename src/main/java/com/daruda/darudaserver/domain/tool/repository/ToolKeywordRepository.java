package com.daruda.darudaserver.domain.tool.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import com.daruda.darudaserver.domain.tool.entity.Tool;
import com.daruda.darudaserver.domain.tool.entity.ToolKeyword;

import jakarta.transaction.Transactional;

@Repository
public interface ToolKeywordRepository extends JpaRepository<ToolKeyword, Long> {
	List<ToolKeyword> findAllByTool(final Tool toolById);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	void deleteByTool(final Tool tool);
}
