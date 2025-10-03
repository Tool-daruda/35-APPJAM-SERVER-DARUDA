package com.daruda.darudaserver.global.image.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.daruda.darudaserver.global.image.entity.Image;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
	List<Image> findAllByImageUrlIn(Collection<String> imageUrls);

	boolean existsByImageUrl(String imageUrl);
}
