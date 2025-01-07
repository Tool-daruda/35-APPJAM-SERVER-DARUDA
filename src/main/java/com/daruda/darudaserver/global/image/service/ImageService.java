package com.daruda.darudaserver.global.image.service;

import com.daruda.darudaserver.global.error.exception.BusinessException;
import com.daruda.darudaserver.global.error.exception.InvalidValueException;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.NotFoundException;
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

    // 1. 이미지 업로드
    @Transactional
    public List<Image> uploadImages(final List<MultipartFile> images, final String dirName)  {

        return images.stream()
                .map(image -> {
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
                s3Service.deleteImage(s3Key);
                // DB에서 삭제
                image.delete();
                imageRepository.save(image);
            } catch (InvalidValueException e) {
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
}
