package com.daruda.darudaserver.domain.comment.service;

import com.daruda.darudaserver.domain.comment.dto.request.CreateCommentRequest;
import com.daruda.darudaserver.domain.comment.dto.response.CreateCommentResponse;
import com.daruda.darudaserver.domain.comment.entity.CommentEntity;
import com.daruda.darudaserver.domain.comment.repository.CommentRepository;
import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.domain.community.repository.BoardRepository;
import com.daruda.darudaserver.domain.user.entity.UserEntity;
import com.daruda.darudaserver.domain.user.repository.UserRepository;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    public CreateCommentResponse postComment(Long userId, Long boardId, CreateCommentRequest createCommentRequest){
        //게시글과 사용자 존재 여부 검사
        Board board = boardRepository.findById(boardId)
                .orElseThrow(()->new NotFoundException(ErrorCode.DATA_NOT_FOUND));
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(()->new NotFoundException(ErrorCode.USER_NOT_FOUND));

        //댓글 entity 생성
        CommentEntity commentEntity = CommentEntity.builder()
                .userEntity(userEntity)
                .board(board)
                .photoUrl(createCommentRequest.image())
                .content(createCommentRequest.content())
                .build();

        //댓글 entity 생성 및 댓글 ID 추출
        Long commentId = commentRepository.save(commentEntity).getId();

        //ResponseDto 변환
        CreateCommentResponse createCommentResponse = CreateCommentResponse.of(commentId, commentEntity.getContent(), commentEntity.getUpdatedAt(),commentEntity.getPhotoUrl(),userEntity.getNickname());

        return createCommentResponse;
    }
}
