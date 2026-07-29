package com.daruda.darudaserver.domain.community.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daruda.darudaserver.domain.community.dto.res.BoardScrapRes;
import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.domain.community.entity.BoardScrap;
import com.daruda.darudaserver.domain.community.repository.BoardRepository;
import com.daruda.darudaserver.domain.community.repository.BoardScrapRepository;
import com.daruda.darudaserver.domain.community.repository.ScrapBoardProjection;
import com.daruda.darudaserver.domain.community.util.ValidateBoard;
import com.daruda.darudaserver.domain.search.repository.BoardSearchRepository;
import com.daruda.darudaserver.domain.user.dto.response.PagenationDto;
import com.daruda.darudaserver.domain.user.dto.response.ScrapBoardsResponse;
import com.daruda.darudaserver.domain.user.dto.response.ScrapBoardsRetrieveResponse;
import com.daruda.darudaserver.domain.user.entity.UserEntity;
import com.daruda.darudaserver.domain.user.repository.UserRepository;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.NotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BoardScrapService {

	private static final String TOOL_LOGO = "https://daruda.s3.ap-northeast-2.amazonaws.com/Cursor_logo.png";
	private static final String FREE = "자유";

	private final BoardScrapRepository boardScrapRepository;
	private final BoardRepository boardRepository;
	private final UserRepository userRepository;
	private final BoardSearchRepository boardSearchRepository;
	private final ValidateBoard validateBoard;
	private final BoardScrapInternalService boardScrapInternalService;

	// 스크랩 토글 (존재하면 삭제, 없으면 생성)
	public BoardScrapRes toggleScrap(final Long userId, final Long boardId) {
		UserEntity user = getUserById(userId);
		Board board = getBoardById(boardId);

		boolean exists = boardScrapRepository.existsByUserIdAndBoardId(userId, boardId);

		if (exists) {
			boardScrapRepository.deleteByUserIdAndBoardId(userId, boardId);
			updateSearchIndex(boardId, false);
			return BoardScrapRes.of(boardId, false);
		} else {
			BoardScrap boardScrap = BoardScrap.builder().user(user).board(board).build();
			boardScrapInternalService.saveIfAbsent(boardScrap); // 별도 트랜잭션에서 처리
			updateSearchIndex(boardId, true);
			return BoardScrapRes.of(boardId, true);
		}
	}

	// board별 스크랩 수 일괄 조회 (검색 등 타 도메인에서 스크랩 수를 표시할 때 사용)
	@Transactional(readOnly = true)
	public Map<Long, Long> getScrapCountMap(final List<Long> boardIds) {
		if (boardIds == null || boardIds.isEmpty()) {
			return Map.of();
		}
		return boardScrapRepository.countMapByBoardIds(boardIds);
	}

	// 스크랩 여부 확인 (UserEntity 기반)
	@Transactional(readOnly = true)
	public boolean isScraped(final UserEntity user, final Board board) {
		if (user == null) {
			log.info("** Board : {} 스크랩 여부 : false (비로그인 사용자)", board.getId());
			return false;
		}
		boolean isScrapped = boardScrapRepository.existsByUserIdAndBoardId(user.getId(), board.getId());
		log.info("** Board : {} 스크랩 여부 :{}", board.getId(), isScrapped);
		return isScrapped;
	}

	// 즐겨찾기 게시글 목록 조회
	@Transactional(readOnly = true)
	public ScrapBoardsRetrieveResponse getScrapBoards(final Long userId, final Pageable pageable) {
		validateBoard.validateUser(userId);

		Page<ScrapBoardProjection> boardScraps = boardScrapRepository.findScrapBoardsWithCount(userId, pageable);
		List<ScrapBoardsResponse> scrapBoardsResponses = boardScraps.getContent().stream()
			.map(projection -> ScrapBoardsResponse.of(
				projection.getBoardId(),
				projection.getTitle(),
				projection.getContent(),
				projection.getUpdatedAt(),
				projection.getToolName() != null ? projection.getToolName() : FREE,
				projection.getToolLogo() != null ? projection.getToolLogo() : TOOL_LOGO,
				true,
				projection.getScrapCount()
			))
			.toList();

		PagenationDto pageInfo = PagenationDto.of(pageable.getPageNumber(), pageable.getPageSize(),
			boardScraps.getTotalPages());
		return ScrapBoardsRetrieveResponse.of(userId, scrapBoardsResponses, pageInfo);
	}

	private void updateSearchIndex(final Long boardId, final boolean isScrapped) {
		boardSearchRepository.findById(boardId.toString()).ifPresent(boardDocument -> {
			boardDocument.updateScraped(isScrapped);
			boardSearchRepository.save(boardDocument);
		});
	}

	private UserEntity getUserById(final Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
	}

	private Board getBoardById(final Long boardId) {
		return boardRepository.findByIdAndDelYn(boardId, false)
			.orElseThrow(() -> new NotFoundException(ErrorCode.BOARD_NOT_FOUND));
	}
}
