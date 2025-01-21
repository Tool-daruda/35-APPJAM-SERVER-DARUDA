package com.daruda.darudaserver.domain.community.repository;

import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.domain.community.entity.BoardScrap;
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

@Repository
public interface BoardScrapRepository extends JpaRepository<BoardScrap,Long> {
    @Modifying
    @Transactional
    void deleteAllByUserId(@Param("userId") Long userId);

    @Query("SELECT bs FROM BoardScrap bs WHERE bs.user.id = :userId AND bs.delYn = false")
    Page<BoardScrap> findAllActiveByUserId(@Param("userId") Long userId, Pageable pageable);

    Optional<BoardScrap> findByUserAndBoard(UserEntity user, Board board);
}
