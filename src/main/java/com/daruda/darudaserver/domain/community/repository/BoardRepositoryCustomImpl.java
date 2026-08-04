package com.daruda.darudaserver.domain.community.repository;

import static com.daruda.darudaserver.domain.community.entity.QBoard.*;
import static com.daruda.darudaserver.domain.community.entity.QBoardScrap.*;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.daruda.darudaserver.domain.community.entity.Board;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BoardRepositoryCustomImpl implements BoardRepositoryCustom {

	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public long countBoards(final Boolean noTopic, final Long toolId) {
		return Optional.ofNullable(jpaQueryFactory
			.select(board.count())
			.from(board)
			.where(
				board.delYn.eq(false),
				noTopic != null ? board.isFree.eq(noTopic) : null,
				toolId != null ? board.tool.toolId.eq(toolId) : null
			)
			.fetchFirst()).orElse(0L);
	}

	@Override
	public List<BoardScrapCountRow> findBoardsByScrapCountDesc(
		final Boolean noTopic, final Long toolId, final Long lastScrapCount, final Long lastBoardId,
		final long limit) {

		NumberExpression<Long> scrapCountExpr = Expressions.asNumber(
			JPAExpressions
				.select(boardScrap.count())
				.from(boardScrap)
				.where(boardScrap.board.id.eq(board.id))
		);

		BooleanBuilder where = new BooleanBuilder();
		where.and(board.delYn.eq(Boolean.FALSE));
		if (noTopic != null) {
			where.and(board.isFree.eq(noTopic));
		}
		if (toolId != null) {
			where.and(board.tool.toolId.eq(toolId));
		}
		if (lastScrapCount != null && lastBoardId != null) {
			where.and(
				scrapCountExpr.lt(lastScrapCount)
					.or(scrapCountExpr.eq(lastScrapCount).and(board.id.lt(lastBoardId)))
			);
		}

		List<Tuple> results = jpaQueryFactory
			.select(board, scrapCountExpr)
			.from(board)
			.where(where)
			.orderBy(scrapCountExpr.desc(), board.id.desc())
			.limit(limit)
			.fetch();

		return results.stream()
			.map(tuple -> {
				Long scrapCount = tuple.get(scrapCountExpr);
				return new BoardScrapCountRow(tuple.get(board), scrapCount == null ? 0L : scrapCount);
			})
			.toList();
	}

	@Override
	public List<Board> findBoardsByIdDesc(final Boolean noTopic, final Long toolId, final Long cursor,
		final long limit) {

		return jpaQueryFactory
			.selectFrom(board)
			.where(
				noTopic != null ? board.isFree.eq(noTopic) : null,
				toolId != null ? board.tool.toolId.eq(toolId) : null,
				board.delYn.eq(Boolean.FALSE),
				board.id.lt(cursor)
			)
			.orderBy(board.id.desc())
			.limit(limit)
			.fetch();
	}
}
