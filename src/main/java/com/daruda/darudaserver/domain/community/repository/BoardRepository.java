package com.daruda.darudaserver.domain.community.repository;

import com.daruda.darudaserver.domain.community.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.daruda.darudaserver.domain.tool.entity.Tool;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.util.Optional;


import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board,Long> {

    List<Board> findByToolAndBoardIdLessThanOrderByBoardIdDesc(Tool tool, Long cursor, Pageable pageable);

    List<Board> findByIsFreeAndBoardIdLessThanOrderByBoardIdDesc(
            boolean isFree, Long boardId, Pageable pageable);

    List<Board> findByBoardIdLessThanOrderByBoardIdDesc(Long cursor, PageRequest pageRequest);
    Page<Board> findAllByUserId(Long userId, Pageable pageable);
}
