package com.daruda.darudaserver.domain.tool.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.daruda.darudaserver.domain.tool.entity.ToolLike;
import com.daruda.darudaserver.domain.tool.repository.ToolLikeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ToolLikeInternalService {

	private final ToolLikeRepository toolLikeRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean saveIfAbsent(ToolLike toolLike) {
		try {
			toolLikeRepository.save(toolLike);
			return true;
		} catch (DataIntegrityViolationException e) {
			log.warn("툴 좋아요 중복 삽입 시도 감지 (userId={}, toolId={})",
				toolLike.getUser().getId(), toolLike.getTool().getId());
			return false; // 이미 존재 → 중복으로 간주
		}
	}
}
