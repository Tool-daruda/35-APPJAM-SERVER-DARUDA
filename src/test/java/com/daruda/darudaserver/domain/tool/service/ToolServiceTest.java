package com.daruda.darudaserver.domain.tool.service;


import com.daruda.darudaserver.domain.user.entity.UserEntity;
import com.daruda.darudaserver.domain.user.repository.UserRepository;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

    @ExtendWith(MockitoExtension.class)
    class ToolServiceTest {

        @Mock
        private UserRepository userRepository;

        @InjectMocks
        private ToolService toolService;

        @DisplayName("유저 조회 성공")
        @Test
        void getUserById_유저조회성공() {
            // given
            Long userId = 1L;
            UserEntity mockUser = UserEntity.builder()
                    .email("test@example.com")
                    .nickname("tester")
                    .positions(null)
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

            // when
            UserEntity result = toolService.getUserById(userId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("test@example.com");
            assertThat(result.getNickname()).isEqualTo("tester");
        }

        @Test
        void getUserById_유저조회실패() {
            // given
            Long userId = 1L;

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> toolService.getUserById(userId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage(ErrorCode.DATA_NOT_FOUND.getMessage());
        }
    }
