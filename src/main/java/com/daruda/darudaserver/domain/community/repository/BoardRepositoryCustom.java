package com.daruda.darudaserver.domain.community.repository;

import java.util.List;

import com.daruda.darudaserver.domain.community.entity.Board;

public interface BoardRepositoryCustom {

	long countBoards(Boolean noTopic, Long toolId);

	List<BoardScrapCountRow> findBoardsByScrapCountDesc(
		Boolean noTopic, Long toolId, Long lastScrapCount, Long lastBoardId, long limit);

	List<Board> findBoardsByIdDesc(Boolean noTopic, Long toolId, Long cursor, long limit);
}
