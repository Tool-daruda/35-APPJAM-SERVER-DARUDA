package com.daruda.darudaserver.domain.tool.repository;

import com.daruda.darudaserver.domain.tool.entity.Tool;
import com.daruda.darudaserver.domain.tool.entity.ToolScrap;
import com.daruda.darudaserver.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ToolScrapRepository extends JpaRepository<ToolScrap,Long> {

    void deleteByUserAndTool(final UserEntity user, final Tool tool);

    Optional<ToolScrap>  findByUserAndTool(UserEntity user, Tool tool);
}
