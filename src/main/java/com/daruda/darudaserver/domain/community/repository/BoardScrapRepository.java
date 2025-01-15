package com.daruda.darudaserver.domain.community.repository;

import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.domain.community.entity.BoardScrap;
import com.daruda.darudaserver.domain.user.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardScrapRepository extends JpaRepository<BoardScrap,Long> {

    void deleteByUserAndBoard(UserEntity user, Board board);

    boolean existsByUserAndBoard(UserEntity user, Board board);

    Page<BoardScrap> findAllByUserId(Long userId, Pageable pageable);
}
