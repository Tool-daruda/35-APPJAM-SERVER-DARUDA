package com.daruda.darudaserver.global.image.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Image {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long imageId;

	@Column(name = "image_url", nullable = false)
	private String imageUrl;

	@Builder
	public Image(final Long imageId, final String imageUrl) {
		this.imageId = imageId;
		this.imageUrl = imageUrl;
	}

}
