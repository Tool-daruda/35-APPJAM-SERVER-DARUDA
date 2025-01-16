package com.daruda.darudaserver.domain.tool.repository;

import com.daruda.darudaserver.domain.tool.entity.Category;
import com.daruda.darudaserver.domain.tool.entity.Tool;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ToolRepository extends JpaRepository<Tool, Long> {

    // 1. popular 기준 (전체 조회)
    @Query("SELECT t FROM Tool t WHERE t.toolId < :cursor ORDER BY t.popular DESC, t.toolId DESC")
    List<Tool> findAllWithCursorOrderByPopular(@Param("cursor") Long cursor, Pageable pageable);

    // 1-2. 카테고리별 popular 조회
    @Query("SELECT t FROM Tool t WHERE t.category = :category AND t.toolId < :cursor ORDER BY t.popular DESC, t.toolId DESC")
    List<Tool> findByCategoryWithCursorOrderByPopular(@Param("category") Category category, @Param("cursor") Long cursor, Pageable pageable);

    // 2. createdAt 기준 (전체 조회)
    @Query("SELECT t FROM Tool t WHERE t.toolId < :cursor ORDER BY t.createdAt DESC, t.toolId DESC")
    List<Tool> findAllWithCursorOrderByCreatedAt(@Param("cursor") Long cursor, Pageable pageable);

    // 2-2. 카테고리별 createdAt 조회
    @Query("SELECT t FROM Tool t WHERE t.category = :category AND t.toolId < :cursor ORDER BY t.createdAt DESC, t.toolId DESC")
    List<Tool> findByCategoryWithCursorOrderByCreatedAt(@Param("category") Category category, @Param("cursor") Long cursor, Pageable pageable);


    @Query("SELECT COUNT(t) FROM Tool t WHERE t.category = :category AND t.toolId < :cursor")
    long countWithCursor(@Param("category") Category category, @Param("cursor") Long cursor);

    long count();
}
