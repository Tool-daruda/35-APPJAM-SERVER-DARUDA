package com.daruda.darudaserver.domain.user.entity.enums;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.BusinessException;

@ExtendWith(MockitoExtension.class)
class PositionsTest {

	@Test
	@DisplayName("형 변환 성공")
	void fromString_ValidName_ReturnsPosition() {
		// given
		String validName = "학생";

		// when
		Positions result = Positions.fromString(validName);

		// then
		assertThat(result).isEqualTo(Positions.STUDENT);
	}

	@Test
	@DisplayName("형 변환 실패 - 유효하지 않는 값")
	void fromString_InvalidName_ThrowsException() {
		// given
		String invalidName = "없는값";

		// when & then
		BusinessException exception = assertThrows(BusinessException.class,
			() -> Positions.fromString(invalidName));
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_FIELD_ERROR);
	}
}
