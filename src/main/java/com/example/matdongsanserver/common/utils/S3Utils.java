package com.example.matdongsanserver.common.utils;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.matdongsanserver.common.exception.BusinessException;
import com.example.matdongsanserver.common.exception.CommonErrorCode;
import com.example.matdongsanserver.domain.member.exception.MemberErrorCode;
import com.example.matdongsanserver.domain.member.exception.MemberException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Utils {

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    /**
     * 파일을 S3에 업로드하고 URL을 반환하는 메서드
     * @param folderName
     * @param fileName
     * @param inputStream
     * @param metadata
     * @return
     */
    private String uploadFile(String folderName, String fileName, InputStream inputStream, ObjectMetadata metadata) {
        String fullPath = folderName + fileName;
        try {
            amazonS3Client.putObject(new PutObjectRequest(bucketName, fullPath, inputStream, metadata));
            String fileUrl = amazonS3Client.getUrl(bucketName, fullPath).toString();
            log.info("S3 파일 업로드 성공: {}", fileUrl);
            return fileUrl;
        } catch (Exception e) {
            throw new BusinessException(CommonErrorCode.S3_FILE_UPLOAD_FAILED);
        }
    }

    /**
     * URL에서 이미지 파일을 다운로드하여 S3에 업로드하는 메서드
     * @param folderName
     * @param fileName
     * @param fileUrl
     * @return
     */
    public String uploadImageFromUrl(String folderName, String fileName, String fileUrl) {
        try (InputStream inputStream = new URL(fileUrl).openStream()) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("image/png");

            byte[] fileData = inputStream.readAllBytes();
            metadata.setContentLength(fileData.length);

            return uploadFile(folderName, fileName, new ByteArrayInputStream(fileData), metadata);
        } catch (Exception e) {
            throw new BusinessException(CommonErrorCode.S3_URL_IMAGE_UPLOAD_FAILED);
        }
    }

    /**
     * TTS 파일을 S3에 업로드하는 메서드
     * @param folderName
     * @param fileName
     * @param ttsData
     * @return
     */
    public String uploadTTSToS3(String folderName, String fileName, byte[] ttsData) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("audio/mpeg");
        metadata.setContentLength(ttsData.length);

        return uploadFile(folderName, fileName + ".mp3", new ByteArrayInputStream(ttsData), metadata);
    }

    /**
     * 파일을 S3에 업로드하는 메서드
     * @param file
     * @param fileName
     * @return
     */
    public String uploadFile(String folderName, String fileName, MultipartFile file) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            return uploadFile(folderName, fileName, file.getInputStream(), metadata);
        } catch (IOException e) {
            throw new MemberException(MemberErrorCode.PROFILE_IMAGE_UPLOAD_FAILED);
        }
    }
}
