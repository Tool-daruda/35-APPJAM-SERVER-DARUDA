package com.daruda.darudaserver.domain.tool.repository;

import com.daruda.darudaserver.domain.tool.entity.Tool;
import com.daruda.darudaserver.domain.tool.entity.ToolScrap;
import com.daruda.darudaserver.domain.user.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ToolScrapRepository extends JpaRepository<ToolScrap,Long> {


    List<ToolScrap> findAllByUserId(@Param("userId") final Long userId);


    Optional<ToolScrap>  findByUserAndTool(final UserEntity user,final Tool tool);

    @Query("SELECT COUNT(ts) FROM ToolScrap ts WHERE ts.tool.toolId = :toolId AND ts.delYn = false")
    int countByToolId(@Param("toolId")final  Long toolId);

    // toolId와 isDelYn이 false인 경우의 스크랩 수 계산
    int countByTool_ToolIdAndDelYnFalse(final Long toolId);

    boolean existsByUserAndTool(UserEntity user, Tool tool);
}
