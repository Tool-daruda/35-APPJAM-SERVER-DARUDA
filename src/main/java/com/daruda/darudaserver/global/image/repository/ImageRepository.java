package com.daruda.darudaserver.global.image.repository;

import com.daruda.darudaserver.global.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<Image,Long> {
}
