package com.daruda.darudaserver.domain.notification.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
public class EmitterRepositoryImpl implements EmitterRepository {

	private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
	private final Map<String, Object> eventCache = new ConcurrentHashMap<>();

	@Override
	public SseEmitter save(String emitterId, SseEmitter sseEmitter) {
		emitters.put(emitterId, sseEmitter);
		return sseEmitter;
	}

	@Override
	public void saveEventCache(String emitterId, Object event) {
		eventCache.put(emitterId, event);
	}

	@Override
	public Map<String, SseEmitter> findAllEmitterStartWithByUserId(String userId) {
		return emitters.entrySet().stream()
			.filter(entry -> entry.getKey().startsWith(userId))
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	@Override
	public Map<String, Object> findAllEventCacheStartWithByUserId(String userId) {
		return eventCache.entrySet().stream()
			.filter(entry -> entry.getKey().startsWith(userId))
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	@Override
	public void deleteById(String emitterId) {
		emitters.remove(emitterId);
	}

	@Override
	public void deleteAllEmitterStartWithId(String userId) {
		emitters.forEach(
			(key, emitter) -> {
				if (key.startsWith(userId)) {
					emitters.remove(key);
				}
			}
		);
	}

	@Override
	public void deleteAllEventCacheStartWithId(String userId) {
		eventCache.forEach(
			(key, value) -> {
				if (key.startsWith(userId)) {
					eventCache.remove(key);
				}
			}
		);
	}
}
