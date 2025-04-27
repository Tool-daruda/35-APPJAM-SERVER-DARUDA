package com.daruda.darudaserver.domain.user.dto.response;

public record UserInformationResponse(
	Long id,
	String email,
	String nickname
) {
	public static UserInformationResponse of(Long id, String email, String nickname) {
		return new UserInformationResponse(id, email, nickname);
	}
}
