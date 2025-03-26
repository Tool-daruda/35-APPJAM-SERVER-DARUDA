package com.daruda.darudaserver.domain.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.daruda.darudaserver.domain.user.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
	Optional<UserEntity> findByEmail(String email);

	Optional<UserEntity> findByNickname(String nickname);

	Boolean existsByEmail(String email);

	Boolean existsByNickname(String nickname);
}
