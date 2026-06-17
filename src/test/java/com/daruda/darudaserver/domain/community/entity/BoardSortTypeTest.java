package com.daruda.darudaserver.domain.community.entity;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.daruda.darudaserver.global.error.exception.InvalidValueException;

class BoardSortTypeTest {

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "   "})
	@DisplayName("from: null/공백 입력 시 기본값 LATEST 반환")
	void from_blankOrNull_returnsLatest(final String input) {
		assertThat(BoardSortType.from(input)).isEqualTo(BoardSortType.LATEST);
	}

	@ParameterizedTest
	@ValueSource(strings = {"SCRAP", "scrap", "Scrap"})
	@DisplayName("from: 대소문자 무관하게 SCRAP 파싱")
	void from_scrapCaseInsensitive(final String input) {
		assertThat(BoardSortType.from(input)).isEqualTo(BoardSortType.SCRAP);
	}

	@ParameterizedTest
	@ValueSource(strings = {"LATEST", "latest", "Latest"})
	@DisplayName("from: 대소문자 무관하게 LATEST 파싱")
	void from_latestCaseInsensitive(final String input) {
		assertThat(BoardSortType.from(input)).isEqualTo(BoardSortType.LATEST);
	}

	@Test
	@DisplayName("from: 알 수 없는 값은 InvalidValueException 발생")
	void from_unknownValue_throws() {
		assertThatThrownBy(() -> BoardSortType.from("unknown"))
			.isInstanceOf(InvalidValueException.class);
	}
}
