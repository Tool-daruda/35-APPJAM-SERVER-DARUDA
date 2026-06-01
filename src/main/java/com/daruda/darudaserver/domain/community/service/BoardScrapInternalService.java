package com.daruda.darudaserver.domain.community.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.daruda.darudaserver.domain.community.entity.BoardScrap;
import com.daruda.darudaserver.domain.community.repository.BoardScrapRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class BoardScrapInternalService {

	private final BoardScrapRepository boardScrapRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean saveIfAbsent(BoardScrap scrap) {
		try {
			boardScrapRepository.save(scrap);
			return true;
		} catch (DataIntegrityViolationException e) {
			log.warn("스크랩 중복 삽입 시도 감지 (userId={}, boardId={})",
				scrap.getUser().getId(), scrap.getBoard().getId());
			return false; // 이미 존재 → 중복으로 간주
		}
	}
}
