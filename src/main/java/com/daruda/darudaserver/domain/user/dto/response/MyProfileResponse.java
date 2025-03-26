package com.daruda.darudaserver.domain.user.dto.response;

public record MyProfileResponse(
	String nickname,
	String positions
) {
	public static MyProfileResponse of(String nickname, String positions) {
		return new MyProfileResponse(nickname, positions);
	}
}