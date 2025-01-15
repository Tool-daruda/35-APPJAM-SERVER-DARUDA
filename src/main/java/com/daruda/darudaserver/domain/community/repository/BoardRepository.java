package com.daruda.darudaserver.domain.community.repository;

import com.daruda.darudaserver.domain.community.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board,Long> {
    Page<Board> findAllByUserId(Long userId, Pageable pageable);
}
