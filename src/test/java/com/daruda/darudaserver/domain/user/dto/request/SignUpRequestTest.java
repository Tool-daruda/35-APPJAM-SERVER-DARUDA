package com.daruda.darudaserver.domain.user.dto.request;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daruda.darudaserver.domain.user.entity.enums.Positions;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Validator;
import jakarta.validation.metadata.ConstraintDescriptor;

@ExtendWith(MockitoExtension.class)
class SignUpRequestTest {

	@Mock
	private Validator validator;

	private AutoCloseable closeable;

	@BeforeEach
	void setUp() {
		closeable = MockitoAnnotations.openMocks(this);
	}

	@AfterEach
	void tearDown() throws Exception {
		closeable.close();
	}

	@Test
	@DisplayName("검증 성공")
	void validSignUpRequest() {
		// given
		SignUpRequest request = new SignUpRequest("tester", Positions.STUDENT, "test@example.com");
		when(validator.validate(request)).thenReturn(Set.of());

		// when
		Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(request);

		// then
		assertThat(violations).isEmpty();
		verify(validator).validate(request);
	}

	@Test
	@DisplayName("검증 실패 - 닉네임이 null")
	void invalidNickname_Null() {
		// given
		SignUpRequest request = new SignUpRequest(null, Positions.STUDENT, "test@example.com");
		ConstraintViolation<SignUpRequest> violation = createViolation(request, "닉네임은 필수 입력값입니다");
		when(validator.validate(request)).thenReturn(Set.of(violation));

		// when
		Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(request);

		// then
		assertThat(violations).isNotEmpty();
		assertThat(violations).anyMatch(v -> v.getMessage().equals("닉네임은 필수 입력값입니다"));
		verify(validator).validate(request);
	}

	@Test
	@DisplayName("검증 실패 - 닉네임이 10자를 초과")
	void invalidNickname_TooLong() {
		// given
		SignUpRequest request = new SignUpRequest("tester123456789", Positions.STUDENT, "test@example.com");
		ConstraintViolation<SignUpRequest> violation = createViolation(request, "닉네임은 최대 10자까지 가능합니다.");
		when(validator.validate(request)).thenReturn(Set.of(violation));

		// when
		Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(request);

		// then
		assertThat(violations).isNotEmpty();
		assertThat(violations).anyMatch(v -> v.getMessage().equals("닉네임은 최대 10자까지 가능합니다."));
		verify(validator).validate(request);
	}

	@Test
	@DisplayName("검증 실패 - 소속이 null")
	void invalidPosition_Null() {
		// given
		SignUpRequest request = new SignUpRequest("tester", null, "test@example.com");
		ConstraintViolation<SignUpRequest> violation = createViolation(request, "소속은 필수 입력값입니다");
		when(validator.validate(request)).thenReturn(Set.of(violation));

		// when
		Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(request);

		// then
		assertThat(violations).isNotEmpty();
		assertThat(violations).anyMatch(v -> v.getMessage().equals("소속은 필수 입력값입니다"));
		verify(validator).validate(request);
	}

	@Test
	@DisplayName("검증 실패 - 이메일이 null")
	void invalidEmail_Null() {
		// given
		SignUpRequest request = new SignUpRequest("tester", Positions.STUDENT, null);
		ConstraintViolation<SignUpRequest> violation = createViolation(request, "이메일은 필수 입력값입니다");
		when(validator.validate(request)).thenReturn(Set.of(violation));

		// when
		Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(request);

		// then
		assertThat(violations).isNotEmpty();
		assertThat(violations).anyMatch(v -> v.getMessage().equals("이메일은 필수 입력값입니다"));
		verify(validator).validate(request);
	}

	@Test
	@DisplayName("검증 실패 - 이메일 형식이 잘못됨")
	void invalidEmail_Format() {
		// given
		SignUpRequest request = new SignUpRequest("tester", Positions.STUDENT, "invalid-email");
		ConstraintViolation<SignUpRequest> violation = createViolation(request, "이메일 형식에 맞지 않습니다");
		when(validator.validate(request)).thenReturn(Set.of(violation));

		// when
		Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(request);

		// then
		assertThat(violations).isNotEmpty();
		assertThat(violations).anyMatch(v -> v.getMessage().equals("이메일 형식에 맞지 않습니다"));
		verify(validator).validate(request);
	}

	private ConstraintViolation<SignUpRequest> createViolation(SignUpRequest request, String message) {
		return new ConstraintViolation<>() {
			@Override
			public String getMessage() {
				return message;
			}

			@Override
			public String getMessageTemplate() {
				return "";
			}

			@Override
			public SignUpRequest getRootBean() {
				return request;
			}

			@Override
			public Class<SignUpRequest> getRootBeanClass() {
				return SignUpRequest.class;
			}

			@Override
			public Object getLeafBean() {
				return request;
			}

			@Override
			public Object[] getExecutableParameters() {
				return new Object[0];
			}

			@Override
			public Object getExecutableReturnValue() {
				return null;
			}

			@Override
			public Path getPropertyPath() {
				return null;
			}

			@Override
			public Object getInvalidValue() {
				return null;
			}

			@Override
			public ConstraintDescriptor<?> getConstraintDescriptor() {
				return null;
			}

			@Override
			public <U> U unwrap(Class<U> type) {
				return null;
			}
		};
	}
}
