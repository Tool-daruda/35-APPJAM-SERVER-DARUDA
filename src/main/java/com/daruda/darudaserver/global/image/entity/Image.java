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
	@Column(name = "image_id")
	private Long id;

	@Column(name = "image_url", nullable = false, length = 500)
	private String imageUrl;

	@Builder
	public Image(final Long id, final String imageUrl) {
		this.id = id;
		this.imageUrl = imageUrl;
	}

}
