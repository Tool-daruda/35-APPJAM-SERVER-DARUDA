package com.daruda.darudaserver.domain.community.repository;

import com.daruda.darudaserver.domain.community.entity.BoardImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BoardImageRepository extends JpaRepository<BoardImage,Long> {
    List<BoardImage> findAllByBoardId(Long boardId);
}
