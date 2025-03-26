package com.daruda.darudaserver.global.image.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.daruda.darudaserver.global.image.entity.Image;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
}
