package com.daruda.darudaserver.domain.notification.repository;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@ExtendWith(MockitoExtension.class)
class EmitterRepositoryTest {

	@InjectMocks
	private EmitterRepositoryImpl emitterRepository;

	@Test
	@DisplayName("Emitter 저장 성공")
	void saveEmitter_Success() {
		// given
		String emitterId = "1_12345";
		SseEmitter emitter = mock(SseEmitter.class);

		// when
		SseEmitter result = emitterRepository.save(emitterId, emitter);

		// then
		assertThat(result).isEqualTo(emitter);
		assertThat(emitterRepository.findAllEmitterStartWithByUserId("1")).containsKey(emitterId);
	}

	@Test
	@DisplayName("Event Cache 저장 성공")
	void saveEventCache_Success() {
		// given
		String emitterId = "1_12345";
		String event = "testEvent";

		// when
		emitterRepository.saveEventCache(emitterId, event);

		// then
		assertThat(emitterRepository.findAllEventCacheStartWithByUserId("1")).containsKey(emitterId);
		assertThat(emitterRepository.findAllEventCacheStartWithByUserId("1").get(emitterId)).isEqualTo(event);
	}

	@Test
	@DisplayName("UserId로 시작하는 모든 Emitter 조회")
	void findAllEmitterStartWithByUserId_Success() {
		// given
		String emitterId1 = "1_12345";
		String emitterId2 = "1_67890";
		SseEmitter sseEmitter1 = mock(SseEmitter.class);
		SseEmitter sseEmitter2 = mock(SseEmitter.class);
		emitterRepository.save(emitterId1, sseEmitter1);
		emitterRepository.save(emitterId2, sseEmitter2);

		// when
		Map<String, SseEmitter> result = emitterRepository.findAllEmitterStartWithByUserId("1");

		// then
		assertThat(result).hasSize(2);
		assertThat(result).containsKeys(emitterId1, emitterId2);
	}

	@Test
	@DisplayName("Emitter ID로 삭제 성공")
	void deleteById_Success() {
		// given
		String emitterId = "1_12345";
		SseEmitter emitter = mock(SseEmitter.class);
		emitterRepository.save(emitterId, emitter);

		// when
		emitterRepository.deleteById(emitterId);

		// then
		assertThat(emitterRepository.findAllEmitterStartWithByUserId("1")).doesNotContainKey(emitterId);
	}

	@Test
	@DisplayName("UserId로 시작하는 모든 Emitter 삭제")
	void deleteAllEmitterStartWithId_Success() {
		// given
		String emitterId1 = "1_12345";
		String emitterId2 = "2_67890";
		SseEmitter emitter1 = mock(SseEmitter.class);
		SseEmitter emitter2 = mock(SseEmitter.class);
		emitterRepository.save(emitterId1, emitter1);
		emitterRepository.save(emitterId2, emitter2);

		// when
		emitterRepository.deleteAllEmitterStartWithId("1");

		// then
		assertThat(emitterRepository.findAllEmitterStartWithByUserId("1")).isEmpty();
	}

	@Test
	@DisplayName("UserId로 시작하는 모든 Event Cache 삭제")
	void deleteAllEventCacheStartWithId_Success() {
		// given
		String emitterId1 = "1_12345";
		String emitterId2 = "2_67890";
		emitterRepository.saveEventCache(emitterId1, "event1");
		emitterRepository.saveEventCache(emitterId2, "event2");

		// when
		emitterRepository.deleteAllEventCacheStartWithId("1");

		// then
		assertThat(emitterRepository.findAllEventCacheStartWithByUserId("1")).isEmpty();
	}
}
