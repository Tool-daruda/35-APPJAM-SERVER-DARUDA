package com.daruda.darudaserver.domain.community.service;

import com.daruda.darudaserver.domain.community.dto.req.BoardCreateAndUpdateReq;
import com.daruda.darudaserver.domain.community.dto.res.BoardRes;
import com.daruda.darudaserver.domain.community.dto.res.BoardScrapRes;
import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.domain.community.entity.BoardImage;
import com.daruda.darudaserver.domain.community.entity.BoardScrap;
import com.daruda.darudaserver.domain.community.repository.BoardImageRepository;
import com.daruda.darudaserver.domain.community.repository.BoardRepository;
import com.daruda.darudaserver.domain.community.repository.BoardScrapRepository;
import com.daruda.darudaserver.domain.tool.entity.Tool;
import com.daruda.darudaserver.domain.tool.repository.ToolRepository;
import com.daruda.darudaserver.domain.user.entity.UserEntity;
import com.daruda.darudaserver.domain.user.repository.UserRepository;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.NotFoundException;
import com.daruda.darudaserver.global.error.exception.UnauthorizedException;
import com.daruda.darudaserver.global.image.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.daruda.darudaserver.domain.community.entity.Board.createFree;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardImageService boardImageService;
    private final BoardImageRepository boardImageRepository;
    private final ImageService imageService;
    private final ToolRepository toolRepository;
    private final UserRepository userRepository;
    private final BoardScrapRepository boardScrapRepository ;

    public BoardRes createBoard(
            final Long userId,
            final BoardCreateAndUpdateReq boardCreateAndUpdateReq,
            List<MultipartFile> images) {

        //UserId 검증
        UserEntity user = getUserById(userId);
        Board board;

        // 자유 게시판일 경우
        if(boardCreateAndUpdateReq.isFree()){
            board = createFreeBoard(user, boardCreateAndUpdateReq);
        }
        // 툴 기반 게시판 생성
        else {
            Tool tool = getToolById(boardCreateAndUpdateReq.toolId());
            board = createToolBoard(tool.getToolId(), user, boardCreateAndUpdateReq);
        }
        // 이미지가 없으면 바로 응답 반환
        if (images == null || images.isEmpty() || images.stream().allMatch(MultipartFile::isEmpty)) {
            return BoardRes.of(board);
        }
        // imageURL 반환
        List<Long> imageIds =imageService.uploadImages(images);
        // BoardImage 저장
        boardImageService.saveBoardImages(board.getBoardId(), imageIds);
        List<String> imageUrls= boardImageService.getBoardImageUrls(board.getBoardId());
        return BoardRes.of(board,imageUrls);
    }

    public BoardRes updateBoard(
            final Long userId,
            final Long boardId,
            final BoardCreateAndUpdateReq boardCreateAndUpdateReq,
            List<MultipartFile> images) {
        //userId 검증
        UserEntity user = getUserById(userId);
        //Board 객체 검증
        Board board = getBoardById(boardId);
        validateUser(user.getId(), board.getUser().getId());
        //Board 정보 Update
        board.update(boardCreateAndUpdateReq.toolId(), user, boardCreateAndUpdateReq.title(), boardCreateAndUpdateReq.content());

        // 1. 들어온 이미지가 없을 경우 -> 기존 이미지 삭제
        if (images == null || images.isEmpty() || images.stream().allMatch(MultipartFile::isEmpty)) {
            deleteOriginImages(boardId);
            return BoardRes.of(board);
        }
        //2. 들어온 이미지가 있을 경우 -> 기존 이미지의 존재 여부 확인
        List<BoardImage> boardImages = boardImageRepository.findAllByBoardId(boardId);
        //2-1. 기존 이미지가 존재할 경우 -> 삭제
        if(!boardImages.isEmpty()){
            deleteOriginImages(boardId);
        }
        //2-2. 기존 이미지가 존재하지 않을 경우 + 2-1 진행 후 이미지 업로드
        // imageURL 업로드
        List<Long> imageIds =imageService.uploadImages(images);
        // BoardImage 저장
        boardImageService.saveBoardImages(board.getBoardId(), imageIds);
        List<String> imageUrls= boardImageService.getBoardImageUrls(board.getBoardId());
        return BoardRes.of(board,imageUrls);
    }

    public void deleteBoard(final Long userId,final Long boardId) {
        UserEntity user = getUserById(userId);
        Board board = getBoardById(boardId);
        validateUser(user.getId(), board.getBoardId());
        // Image 제거
        List<BoardImage> boardImages = boardImageRepository.findAllByBoardId(boardId);
        //2-1. 기존 이미지가 존재할 경우 -> 삭제
        if(!boardImages.isEmpty()){
            deleteOriginImages(boardId);
        }
        //Board 제거
        board.delete();
    }

    public BoardScrapRes postScrap(final Long userId, final Long boardId) {
        Board board = getBoardById(boardId);
        UserEntity user = getUserById(userId);
        boolean boardExists = boardScrapRepository.existsByUserAndBoard(user, board);

        if(boardExists){
            boardScrapRepository.deleteByUserAndBoard(user, board);
            return BoardScrapRes.of(boardId, false);
        }else{
            BoardScrap boardScrap = BoardScrap.builder()
                    .user(user)
                    .board(board)
                    .build();
            boardScrapRepository.save(boardScrap);
            return BoardScrapRes.of(boardId, true);
        }

    }

    public BoardRes getBoard(final Long boardId) {
        Board board = getBoardById(boardId);
        List<String> imageUrls= boardImageService.getBoardImageUrls(board.getBoardId());
        return BoardRes.of(board,imageUrls);
    }

    private Board createToolBoard(final Long toolId, final UserEntity user, final BoardCreateAndUpdateReq boardCreateAndUpdateReq) {
        Board board = Board.create(toolId, user, boardCreateAndUpdateReq.title(), boardCreateAndUpdateReq.content());
        return boardRepository.save(board);
    }

    private Board createFreeBoard(final UserEntity user, final BoardCreateAndUpdateReq boardCreateAndUpdateReq) {
        Board board = createFree(user, boardCreateAndUpdateReq.title(), boardCreateAndUpdateReq.content());
        return boardRepository.save(board);
    }

    private Board getBoardById(final Long boardId){
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.DATA_NOT_FOUND));

        // 삭제된 보드인지 확인
        if (board.isDelYn()) {
            log.error("삭제된 보드입니다. boardId={}", boardId);
            throw new NotFoundException(ErrorCode.DATA_NOT_FOUND);
        }
        return board;
    }

    private Tool getToolById(final Long toolId) {
        log.debug("툴을 조회합니다. toolId={}", toolId);
        return toolRepository.findById(toolId)
                .orElseThrow(() -> {
                    log.error("툴을 찾을 수 없습니다. toolId={}", toolId);
                    return new NotFoundException(ErrorCode.DATA_NOT_FOUND);
                });
    }

    public UserEntity getUserById(final Long userId) {
        log.debug("유저를 조회합니다. userId={}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("유저를 찾을 수 없습니다. userId={}", userId);
                    return new NotFoundException(ErrorCode.DATA_NOT_FOUND);
                });
    }

    // 기존 이미지 삭제
    public void deleteOriginImages(final Long boardId){
        List<BoardImage> boardImages = boardImageRepository.findAllByBoardId(boardId);
        List<Long> deleteImages = boardImages.stream()
                .map(BoardImage::getImageId)
                .toList();
        boardImageRepository.deleteAll(boardImages);
        //Image Repository 의 이미지 삭제
        imageService.deleteImages(deleteImages);
    }

    public void validateUser(final Long userId, final Long boardUserId){
        if(!boardUserId.equals(userId)){
            throw new UnauthorizedException(ErrorCode.BOARD_FORBIDDEN);
        }
    }


}
