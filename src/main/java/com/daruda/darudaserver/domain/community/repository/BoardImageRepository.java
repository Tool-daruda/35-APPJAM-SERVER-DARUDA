package com.daruda.darudaserver.domain.community.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.daruda.darudaserver.domain.community.entity.BoardImage;

@Repository
public interface BoardImageRepository extends JpaRepository<BoardImage, Long> {
	List<BoardImage> findAllByBoardId(Long boardId);
}
