package com.daruda.darudaserver.domain.user.repository;

import com.daruda.darudaserver.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity,Long> {
    Optional<UserEntity> findByEmail(String email);
    Boolean existsByNickname(String nickname);
    Boolean existsByEmail(String email);
}
