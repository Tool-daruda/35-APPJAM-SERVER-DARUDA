package com.daruda.darudaserver.domain.tool.repository;

import com.daruda.darudaserver.domain.tool.entity.ToolScrap;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ToolScrapRepository extends JpaRepository<ToolScrap,Long> {


    @Query("SELECT CASE WHEN COUNT(ts) > 0 THEN true ELSE false END " +
            "FROM ToolScrap ts WHERE ts.user.id = :userId AND ts.tool.id = :toolId")
    boolean existsByUserIdAndToolId(Long userId, Long toolId);

    @Transactional
    @Modifying
    @Query("DELETE FROM ToolScrap ts WHERE ts.user.id = :userId AND ts.tool.id = :toolId")
    void deleteByUserIdAndToolId(Long userId, Long toolId);
}
