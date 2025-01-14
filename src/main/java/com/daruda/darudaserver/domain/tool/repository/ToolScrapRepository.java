package com.daruda.darudaserver.domain.tool.repository;

import com.daruda.darudaserver.domain.tool.entity.Tool;
import com.daruda.darudaserver.domain.tool.entity.ToolScrap;
import com.daruda.darudaserver.domain.user.entity.UserEntity;
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
public interface ToolScrapRepository extends JpaRepository<ToolScrap,Long> {


    @Query("SELECT CASE WHEN COUNT(ts) > 0 THEN true ELSE false END " +
            "FROM ToolScrap ts WHERE ts.user.id = :userId AND ts.tool.id = :toolId")
    boolean existsByUserIdAndToolId(@Param("userId") Long userId, @Param("toolId") Long toolId);

    boolean existsByUserAndTool(final UserEntity user, final Tool tool);

    void deleteByUserAndTool(final UserEntity user, final Tool tool);
    @Transactional
    @Modifying
    @Query("DELETE FROM ToolScrap ts WHERE ts.user.id = :userId AND ts.tool.id = :toolId")
    void deleteByUserIdAndToolId(@Param("userId") Long userId, @Param("toolId") Long toolId);

    @Query("SELECT ts FROM ToolScrap ts WHERE ts.user.id = :userId")
    Page<ToolScrap> findAllByUserId(@Param("userId") Long userId, Pageable pageable);

}
