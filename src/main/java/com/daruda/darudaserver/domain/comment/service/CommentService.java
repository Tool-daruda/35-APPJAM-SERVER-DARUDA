package com.daruda.darudaserver.domain.comment.service;

import com.daruda.darudaserver.domain.comment.dto.request.CreateCommentRequest;
import com.daruda.darudaserver.domain.comment.dto.response.CreateCommentResponse;
import com.daruda.darudaserver.domain.comment.entity.CommentEntity;
import com.daruda.darudaserver.domain.comment.repository.CommentRepository;
import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.domain.community.repository.BoardRepository;
import com.daruda.darudaserver.domain.user.entity.UserEntity;
import com.daruda.darudaserver.domain.user.repository.UserRepository;
import com.daruda.darudaserver.domain.user.service.UserService;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.BadRequestException;
import com.daruda.darudaserver.global.error.exception.BusinessException;
import com.daruda.darudaserver.global.error.exception.NotFoundException;
import com.daruda.darudaserver.global.infra.S3.S3Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {
    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    public CreateCommentResponse postComment(Long userId, Long boardId, CreateCommentRequest createCommentRequest) throws IOException {
        //게시글과 사용자 존재 여부 검사
        Board board = boardRepository.findById(boardId)
                .orElseThrow(()->new NotFoundException(ErrorCode.DATA_NOT_FOUND));
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(()->new NotFoundException(ErrorCode.USER_NOT_FOUND));

        //S3에 이미지 저장
        String imageName = s3Service.uploadImage(createCommentRequest.image());
        String imageUrl = s3Service.getImageUrl(imageName);

        //댓글 entity 생성
        CommentEntity commentEntity = CommentEntity.builder()
                .userEntity(userEntity)
                .board(board)
                .photoUrl(imageUrl)
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


}
