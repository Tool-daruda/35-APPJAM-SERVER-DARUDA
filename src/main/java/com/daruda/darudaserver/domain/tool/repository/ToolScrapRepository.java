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

import java.util.Optional;

import java.util.List;

@Repository
public interface ToolScrapRepository extends JpaRepository<ToolScrap,Long> {




    @Query("SELECT ts FROM ToolScrap ts WHERE ts.user.id = :userId")
    Page<ToolScrap> findAllByUserId(@Param("userId") Long userId, Pageable pageable);


    Optional<ToolScrap>  findByUserAndTool(UserEntity user, Tool tool);
}
