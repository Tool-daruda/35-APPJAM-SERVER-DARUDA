package com.daruda.darudaserver.domain.tool.repository;

import com.daruda.darudaserver.domain.tool.entity.Category;
import com.daruda.darudaserver.domain.tool.entity.Tool;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ToolRepository extends JpaRepository<Tool,Long> {

    @Transactional
    @Modifying
    @Query("update Tool t set t.viewCount = t.viewCount + 1 where t.toolId = :id")
    int updateView(Long id);

    @Query("SELECT t FROM Tool t WHERE ( :category = 'ALL' OR t.category = :category)")
    Page<Tool> findAllWithFilter(@Param("category") Category category, Pageable pageable);

}
