package com.daruda.darudaserver.global.image.service;

import com.daruda.darudaserver.global.exception.BusinessException;
import com.daruda.darudaserver.global.exception.InvalidValueException;
import com.daruda.darudaserver.global.exception.code.ErrorCode;
import com.daruda.darudaserver.global.image.entity.Image;
import com.daruda.darudaserver.global.image.repository.ImageRepository;
import com.daruda.darudaserver.global.infra.S3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final S3Service s3Service;
    private final ImageRepository imageRepository;

    // 1. 단일 이미지 업로드 하는 메서드
    @Transactional
    public Image uploadImage(final MultipartFile image, final String dirName)  {
        if (image == null || image.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }
        try {
            String storedName = s3Service.uploadImage(dirName, image);
            String originalName = image.getOriginalFilename();
            Image newImage = Image.builder()
                    .folder(dirName)
                    .originalName(originalName)
                    .storedName(storedName)
                    .build();
            return imageRepository.save(newImage);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL);
        }
    }

    // 2. 이미지 리스트 업로드 하는 메서드
    @Transactional
    public List<Image> uploadImages(final List<MultipartFile> images, final String dirName)  {
        if (images == null || images.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }
        return images.stream()
                .map(image -> {
                    try {
                        return uploadImage(image, dirName);
                    } catch (Exception e) {
                        throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL);
                    }
                })
                .toList();
    }

    // 3. 이미지 삭제
    @Transactional
    public void deleteImage(final long imageId) {

        Image image = getImageById(imageId); // 이미지 조회

        if(image.isDelYn()){
            throw new InvalidValueException(ErrorCode.FILE_NOT_FOUND);
        }
        String s3Key =  image.getFolder()+ "/" +image.getStoredName(); // S3 Key 생성

        try {
            // S3에서 삭제
            s3Service.deleteImage(s3Key);
            // DB에서 삭제
            image.delete();
            imageRepository.save(image);
        } catch (InvalidValueException e) {
            throw new InvalidValueException(ErrorCode.FILE_DELETE_FAIL);
        }
    }

    // 4. 다중 이미지 삭제
    @Transactional
    public void deleteImages(final List<Long> imageIds) {
        List<Image> images = imageRepository.findAllById(imageIds);
        if (images.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }
        for (Image image : images) {
            deleteImage(image.getImageId());
        }
    }

    // 공통 메서드: ID로 이미지 조회
    private Image getImageById(long imageId) {
        return imageRepository.findById(imageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FILE_NOT_FOUND));
    }
}


