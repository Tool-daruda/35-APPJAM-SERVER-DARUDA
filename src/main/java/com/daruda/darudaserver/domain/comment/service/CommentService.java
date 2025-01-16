package com.daruda.darudaserver.domain.comment.service;

import com.daruda.darudaserver.domain.comment.dto.request.CreateCommentRequest;
import com.daruda.darudaserver.domain.comment.dto.response.CreateCommentResponse;
import com.daruda.darudaserver.domain.comment.dto.response.GetCommentResponse;
import com.daruda.darudaserver.domain.comment.dto.response.GetCommentRetrieveResponse;
import com.daruda.darudaserver.domain.comment.entity.CommentEntity;
import com.daruda.darudaserver.domain.comment.repository.CommentRepository;
import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.domain.community.repository.BoardRepository;
import com.daruda.darudaserver.domain.user.entity.UserEntity;
import com.daruda.darudaserver.domain.user.repository.UserRepository;
import com.daruda.darudaserver.global.common.response.ScrollPaginationCollection;
import com.daruda.darudaserver.global.common.response.ScrollPaginationDto;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.NotFoundException;
import com.daruda.darudaserver.global.infra.S3.S3Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {
    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    public CreateCommentResponse postComment(Long userId, Long boardId, CreateCommentRequest createCommentRequest, MultipartFile image) throws IOException {
        //게시글과 사용자 존재 여부 검사
        Board board = boardRepository.findById(boardId)
                .orElseThrow(()->new NotFoundException(ErrorCode.BOARD_NOT_FOUND));
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(()->new NotFoundException(ErrorCode.USER_NOT_FOUND));

        String photoUrl = null;

        if(!(image == null || image.isEmpty())){
            //S3에 이미지 저장
            String imageName = s3Service.uploadImage(image);
            photoUrl = s3Service.getImageUrl(imageName);
        }
        //댓글 entity 생성
        CommentEntity commentEntity = CommentEntity.builder()
                .userEntity(userEntity)
                .board(board)
                .photoUrl(photoUrl)
                .content(createCommentRequest.content())
                .build();

        //댓글 entity 생성 및 댓글 ID 추출
        Long commentId = commentRepository.save(commentEntity).getId();

        //ResponseDto 변환
        CreateCommentResponse createCommentResponse = CreateCommentResponse.of(commentId, commentEntity.getContent(), commentEntity.getUpdatedAt(),commentEntity.getPhotoUrl(),userEntity.getNickname());

        return createCommentResponse;
    }

    public void deleteComment(Long userId, Long commentId){
        //사용자 존재 여부 검사
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
        //댓글 존재 여부 검사 및 entity 반환
        CommentEntity commentEntity = commentRepository.findById(commentId)
                .orElseThrow(()-> new NotFoundException(ErrorCode.COMMENT_NOT_FOUND));
        //댓글 삭제
        commentRepository.delete(commentEntity);
    }

    public GetCommentRetrieveResponse getComments(Long boardId, int size, Long lastCommentId) {
        List<CommentEntity> commentList;
        Long cursor = (lastCommentId == null) ? Long.MAX_VALUE : lastCommentId;
        PageRequest pageRequest = PageRequest.of(0, size + 1);

        List<CommentEntity> commentEntityList = commentRepository.findAllByBoardId(boardId, cursor, pageRequest);

        ScrollPaginationCollection<CommentEntity> commentCursor= ScrollPaginationCollection.of(commentEntityList,size);

        List<GetCommentResponse> commentResponse = commentCursor.getCurrentScrollItems().stream()
                .map(commentEntity -> GetCommentResponse.builder()
                        .content(commentEntity.getContent())
                        .image(commentEntity.getPhotoUrl())
                        .commentId(commentEntity.getId())
                        .nickname(commentEntity.getUserEntity().getNickname())
                        .updatedAt(commentEntity.getUpdatedAt())
                        .build())
                .toList();

        // ScrollPaginationCollection을 이용한 페이지네이션 처리
        // 다음 페이지를 위한 커서 계산
        long nextCursor = commentCursor.isLastScroll() ? -1L : commentCursor.getNextCursor().getId();

        // ScrollPaginationDto 생성
        ScrollPaginationDto scrollPaginationDto = ScrollPaginationDto.of(commentCursor.getTotalElements(), nextCursor);

        // 최종 결과 반환
        return new GetCommentRetrieveResponse(commentResponse, scrollPaginationDto);
    }


}
