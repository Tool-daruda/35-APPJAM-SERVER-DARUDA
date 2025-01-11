package com.daruda.darudaserver.domain.tool.repository;

import com.daruda.darudaserver.domain.tool.entity.Tool;
import com.daruda.darudaserver.domain.tool.entity.ToolPlatForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ToolPlatFormRepository extends JpaRepository<ToolPlatForm,Long> {
    List<ToolPlatForm> findAllByTool(final Tool toolId);
}
