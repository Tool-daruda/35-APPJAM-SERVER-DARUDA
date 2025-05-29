package com.daruda.darudaserver.domain.community.util;

import org.springframework.stereotype.Component;

import com.daruda.darudaserver.domain.user.repository.UserRepository;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ValidateBoard {
	private final UserRepository userRepository;

	public void validateUser(Long userId) {
		userRepository.findById(userId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
	}

}
