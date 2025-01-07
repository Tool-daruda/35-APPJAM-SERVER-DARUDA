package com.daruda.darudaserver.global.infra.S3;



import com.daruda.darudaserver.global.exception.InvalidValueException;
import com.daruda.darudaserver.global.exception.code.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
public class S3Service {

    private final String bucketName;
    private final AWSConfig awsConfig;
    private static final List<String> IMAGE_EXTENSIONS = Arrays.asList("image/jpeg", "image/png", "image/jpg", "image/webp", "image/heic", "image/heif");
    private static final Long MAX_FILE_SIZE = 7 * 1024 * 1024L;

    public S3Service(@Value("${cloud.aws.s3.bucket}") String bucketName , AWSConfig awsConfig){
        this.bucketName = bucketName;
        this.awsConfig = awsConfig;
    }

    public String uploadImage(String directoryPath, MultipartFile image) throws IOException{
        final String imageName = generateImageFileName(image);
        final String key = directoryPath + "/"+ imageName;
        final S3Client s3Client = awsConfig.getS3Client();
        validateExtension(image);
        validateFileSize(image);
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(image.getContentType())
                .contentDisposition("inline")
                .build();
        RequestBody requestBody = RequestBody.fromBytes(image.getBytes()); //파일 바이트로 변환
        try{
            s3Client.putObject(request, requestBody);
        }catch(S3Exception e){
            throw new IOException("이미지 업로둥 중 오류가 발생했습니다.", e);
        }
        return imageName; //S3 에 저장된 경로 이미지 반환
    }

    public void deleteImage(String s3Key) {
        final S3Client s3Client = awsConfig.getS3Client();
        try{
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build()
            );
        }catch(S3Exception e){
            throw new InvalidValueException(ErrorCode.FILE_DELETE_FAIL);
        }
    }

    private String generateImageFileName(MultipartFile image){
        String extension = getExtension(Objects.requireNonNull(image.getContentType()));
        if(extension==null){
            throw new InvalidValueException(ErrorCode.INVALID_IMAGE_TYPE);
        }
        return UUID.randomUUID() + extension;
    }

    private String getExtension(String contentType){
        return switch (contentType){
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/heic" -> ".heic";
            case "image/heif" -> ".heif";
            default -> ".jpg";
        };
    }

    private void validateExtension(MultipartFile image){
        String contentType = image.getContentType();
        if(!IMAGE_EXTENSIONS.contains(contentType)){
            throw new InvalidValueException(ErrorCode.INVALID_IMAGE_TYPE);
        }
    }

    private void validateFileSize(MultipartFile image){
        if(image.getSize() > MAX_FILE_SIZE){
            throw new InvalidValueException(ErrorCode.INVALID_IMAGE_TYPE);
        }
    }

    public List<String> getAllImageKeys(String prefix){
        final S3Client s3Client = awsConfig.getS3Client();
        ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .build();
        ListObjectsV2Response listObjectsV2Response = s3Client.listObjectsV2(listObjectsRequest);
        return listObjectsV2Response.contents().stream()
                .map(S3Object::key)
                .toList();
    }
}
