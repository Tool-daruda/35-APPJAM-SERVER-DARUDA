package com.daruda.darudaserver.domain.community.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daruda.darudaserver.domain.community.dto.res.BoardScrapRes;
import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.domain.community.entity.BoardScrap;
import com.daruda.darudaserver.domain.community.repository.BoardRepository;
import com.daruda.darudaserver.domain.community.repository.BoardScrapRepository;
import com.daruda.darudaserver.domain.community.util.ValidateBoard;
import com.daruda.darudaserver.domain.search.repository.BoardSearchRepository;
import com.daruda.darudaserver.domain.user.dto.response.FavoriteBoardsResponse;
import com.daruda.darudaserver.domain.user.dto.response.FavoriteBoardsRetrieveResponse;
import com.daruda.darudaserver.domain.user.dto.response.PagenationDto;
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
			try {
				BoardScrap boardScrap = BoardScrap.builder().user(user).board(board).build();
				boardScrapRepository.save(boardScrap);
				updateSearchIndex(boardId, true);
				return BoardScrapRes.of(boardId, true);
			} catch (DataIntegrityViolationException e) {
				// 동시성 이슈로 중복 삽입 시도된 경우 — 이미 스크랩된 상태로 처리
				log.warn("스크랩 중복 삽입 시도 감지 (userId={}, boardId={})", userId, boardId);
				return BoardScrapRes.of(boardId, true);
			}
		}
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
	public FavoriteBoardsRetrieveResponse getFavoriteBoards(final Long userId, final Pageable pageable) {
		validateBoard.validateUser(userId);

		Page<BoardScrap> boardScraps = boardScrapRepository.findAllActiveByUserId(userId, pageable);
		List<FavoriteBoardsResponse> favoriteBoardsResponses = boardScraps.getContent().stream()
			.map(boardScrap -> {
				Board board = boardScrap.getBoard();
				return FavoriteBoardsResponse.of(
					board.getId(),
					board.getTitle(),
					board.getContent(),
					board.getUpdatedAt(),
					board.getTool() != null ? board.getTool().getToolMainName() : FREE,
					board.getTool() != null ? board.getTool().getToolLogo() : TOOL_LOGO,
					true
				);
			})
			.toList();

		PagenationDto pageInfo = PagenationDto.of(pageable.getPageNumber(), pageable.getPageSize(),
			boardScraps.getTotalPages());
		return new FavoriteBoardsRetrieveResponse(userId, favoriteBoardsResponses, pageInfo);
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
