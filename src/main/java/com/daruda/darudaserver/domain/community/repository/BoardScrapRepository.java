package com.daruda.darudaserver.domain.community.repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.daruda.darudaserver.domain.community.entity.BoardScrap;

import jakarta.transaction.Transactional;

@Repository
public interface BoardScrapRepository extends JpaRepository<BoardScrap, Long> {
	@Modifying
	@Transactional
	void deleteAllByUserId(@Param("userId") Long userId);

	boolean existsByUserIdAndBoardId(@Param("userId") Long userId, @Param("boardId") Long boardId);

	@Modifying
	@Transactional
	void deleteByUserIdAndBoardId(@Param("userId") Long userId, @Param("boardId") Long boardId);

	List<BoardScrap> findAllByBoardId(Long boardId);

	@Query("SELECT bs.board.id, COUNT(bs) FROM BoardScrap bs WHERE bs.board.id IN :boardIds GROUP BY bs.board.id")
	List<Object[]> countByBoardIds(@Param("boardIds") List<Long> boardIds);

	default Map<Long, Long> countMapByBoardIds(final List<Long> boardIds) {
		return countByBoardIds(boardIds).stream()
			.collect(Collectors.toMap(row -> (Long)row[0], row -> (Long)row[1]));
	}

	@Query(value = "SELECT b.id as boardId, b.title as title, b.content as content, b.updatedAt as updatedAt, "
		+ "t.toolMainName as toolName, t.toolLogo as toolLogo, "
		+ "(SELECT COUNT(s) FROM BoardScrap s WHERE s.board.id = b.id) as scrapCount "
		+ "FROM BoardScrap bs JOIN bs.board b LEFT JOIN b.tool t "
		+ "WHERE bs.user.id = :userId AND b.delYn = false",
		countQuery = "SELECT COUNT(bs) FROM BoardScrap bs WHERE bs.user.id = :userId AND bs.board.delYn = false")
	Page<ScrapBoardProjection> findScrapBoardsWithCount(@Param("userId") Long userId, Pageable pageable);
}
