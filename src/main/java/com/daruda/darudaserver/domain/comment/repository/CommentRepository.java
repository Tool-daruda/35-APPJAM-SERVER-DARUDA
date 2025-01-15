package com.daruda.darudaserver.domain.comment.repository;

import com.daruda.darudaserver.domain.comment.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<CommentEntity,Long> {
}
