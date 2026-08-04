package com.daruda.darudaserver.domain.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.daruda.darudaserver.domain.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);

	Optional<User> findByNickname(String nickname);

	Boolean existsByEmail(String email);

	Boolean existsByNickname(String nickname);
}
