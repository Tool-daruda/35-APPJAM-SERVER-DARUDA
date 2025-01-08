package com.daruda.darudaserver.global.image.service;

import com.daruda.darudaserver.global.error.exception.BusinessException;
import com.daruda.darudaserver.global.error.exception.InvalidValueException;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.NotFoundException;
import com.daruda.darudaserver.global.image.entity.Image;
import com.daruda.darudaserver.global.image.repository.ImageRepository;
import com.daruda.darudaserver.global.infra.S3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {
    private final S3Service s3Service;
    private final ImageRepository imageRepository;

    // 1. 이미지 업로드 - 이미지 아이디 리스트 반환
    @Transactional
    public List<Long> uploadImages(final List<MultipartFile> images, final String dirName)  {

        return images.stream()
                .map(image -> {
                    try {
                        String storedName = s3Service.uploadImage(dirName, image);
                        String originalName = image.getOriginalFilename();
                        log.info("Uploaded image Successful: originalName={}, storedName={}", originalName, storedName);
                        Image newImage = Image.builder()
                                .folder(dirName)
                                .originalName(originalName)
                                .storedName(storedName)
                                .build();
                         Image savedImage = imageRepository.save(newImage);
                        return savedImage.getImageId();
                    } catch (Exception e) {
                        log.error("Image upload failed: error={}", e.getMessage(), e);
                        throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL);
                    }
                })
                .toList();
    }

    // 2. 이미지 삭제
    @Transactional
    public void deleteImages(final List<Long> imageIds) {

        for (Long imageId : imageIds) {
            Image image = getImageById(imageId); // 이미지 조회
            String s3Key =  image.getFolder()+ "/" +image.getStoredName(); // S3 Key 생성

            try {
                // S3에서 삭제
                log.info("Delete image from S3: imageId={}, s3Key={}", imageId, s3Key);
                s3Service.deleteImage(s3Key);
                // DB에서 삭제
                image.delete();
                log.info("Image deleted successfully: imageId={}", imageId);
                imageRepository.save(image);
            } catch (InvalidValueException e) {
                log.error("Failed to delete image: imageId={}, error={}", imageId, e.getMessage(), e);
                throw new InvalidValueException(ErrorCode.FILE_DELETE_FAIL);
            }
        }
    }

    // 이미지 조회 메서드
    private Image getImageById(long imageId) {
        return imageRepository.findById(imageId)
                .filter(image -> !image.isDelYn())
                .orElseThrow(() -> new NotFoundException(ErrorCode.FILE_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public String getImageUrlById(final Long imageId) {
        Image image = getImageById(imageId);
        return s3Service.getImageUrl(image.getFolder(), image.getStoredName());
    }
}
