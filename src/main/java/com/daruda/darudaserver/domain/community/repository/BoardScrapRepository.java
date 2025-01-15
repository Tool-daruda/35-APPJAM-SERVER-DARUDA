package com.daruda.darudaserver.domain.community.repository;

import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.domain.community.entity.BoardScrap;
import com.daruda.darudaserver.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BoardScrapRepository extends JpaRepository<BoardScrap,Long> {


    Optional<BoardScrap> findByUserAndBoard(UserEntity user, Board board);
}
