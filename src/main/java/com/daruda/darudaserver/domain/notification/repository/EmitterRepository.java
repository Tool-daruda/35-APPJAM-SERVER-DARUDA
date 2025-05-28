package com.daruda.darudaserver.domain.notification.repository;

import java.util.Map;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface EmitterRepository {
	SseEmitter save(String emitterId, SseEmitter sseEmitter);

	void saveEventCache(String emitterId, Object event);

	Map<String, SseEmitter> findAllEmitterStartWithByUserId(String userId);

	Map<String, Object> findAllEventCacheStartWithByUserId(String userId);

	void deleteById(String id);

	void deleteAllEmitterStartWithId(String userId);

	void deleteAllEventCacheStartWithId(String userId);
}
