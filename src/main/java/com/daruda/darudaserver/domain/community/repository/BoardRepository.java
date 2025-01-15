package com.daruda.darudaserver.domain.community.repository;

import com.daruda.darudaserver.domain.community.entity.Board;
import feign.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.daruda.darudaserver.domain.tool.entity.Tool;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<Board,Long> {

    @Query("SELECT b FROM Board b " +
            "WHERE (:tool IS NULL OR b.tool = :tool) " +
            "AND (:isFree IS NULL OR b.isFree = :isFree) " +
            "AND b.id < :cursor " +
            "ORDER BY b.createdAt DESC")
    List<Board> findBoards(
            @Param("tool") Tool tool,
            @Param("isFree") Boolean isFree,
            @Param("cursor") Long cursor,
            Pageable pageable);


    Page<Board> findAllByUserId(Long userId, Pageable pageable);
}
