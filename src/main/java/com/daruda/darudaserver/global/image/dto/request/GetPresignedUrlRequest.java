package com.daruda.darudaserver.global.image.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public record GetPresignedUrlRequest(
	@NotNull
	List<String> keyList
) {
}
