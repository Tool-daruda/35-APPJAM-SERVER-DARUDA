package com.daruda.darudaserver.domain.tool.repository;


import com.daruda.darudaserver.domain.tool.entity.RelatedTool;
import com.daruda.darudaserver.domain.tool.entity.Tool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RelatedToolRepository extends JpaRepository<RelatedTool,Long> {
    List<RelatedTool> findAllByTool(Tool tool);
}
