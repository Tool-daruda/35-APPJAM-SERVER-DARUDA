package com.daruda.darudaserver.domain.community.repository;

import com.daruda.darudaserver.domain.community.entity.Board;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.daruda.darudaserver.domain.tool.entity.Tool;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board,Long> {

    @Query("SELECT b FROM Board b " +
            "WHERE (:tool IS NULL OR b.tool = :tool) " +
            "AND (:isFree IS NULL OR b.isFree = :isFree) " +
            "AND b.id < :cursor " +
            "AND b.delYn = false " +
            "ORDER BY b.createdAt DESC")
    List<Board> findBoards(
            @Param("tool") Tool tool,
            @Param("isFree") Boolean isFree,
            @Param("cursor") Long cursor,
            Pageable pageable);


    @Modifying
    @Transactional
    void deleteAllByUserId(@Param("userId") Long userId);

    List<Board> findAllByUserId(Long userId);


    Page<Board> findAllByUserIdAndDelYnFalse(Long userId, Pageable pageable);

    Optional<Board> findByIdAndDelYn(Long boardId, boolean delYn);

    @Query("SELECT COUNT(b) FROM Board b WHERE b.id >= :lastBoardId")
    long countBoardsAfterCursor(@Param("lastBoardId") Long lastBoardId);


}
