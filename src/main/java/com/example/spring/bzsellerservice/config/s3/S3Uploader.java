package com.example.spring.bzsellerservice.config.s3;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class S3Uploader {
    @Autowired
    private AmazonS3Client amazonS3Client;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * 로컬 경로에 저장
     */
    public String uploadFileToS3(MultipartFile multipartFile, String filePath) {
        // MultipartFile -> File 로 변환
        File uploadFile = null;

        if (multipartFile.isEmpty()) {
            throw new IllegalArgumentException("File is empty.");
        } try {
            // S3 업로드 로직
            // 파일 저장 및 경로 리턴
            uploadFile = convert(multipartFile)
                    .orElseThrow(() -> new IllegalArgumentException("[error]: MultipartFile -> 파일 변환 실패"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to S3", e);
        }

        // S3에 저장된 파일 이름
        String fileName = filePath + UUID.randomUUID();

        // s3로 업로드 후 로컬 파일 삭제
        String uploadImageUrl = putS3(uploadFile, fileName);
        removeNewFile(uploadFile);
        return uploadImageUrl;
    }


    /**
     * S3로 업로드
     * @param uploadFile : 업로드할 파일
     * @param fileName : 업로드할 파일 이름
     * @return 업로드 경로
     */
    public String putS3(File uploadFile, String fileName) {
        amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, uploadFile).withCannedAcl(
                CannedAccessControlList.PublicRead));
        return amazonS3Client.getUrl(bucket, fileName).toString();
    }

    /**
     * S3에 있는 파일 삭제
     * 영어 파일만 삭제 가능 -> 한글 이름 파일은 안됨
     */
    public void deleteS3(String filePath) throws Exception {
        try {
            // S3 URL에서 버킷 이름과 경로 제거
            String key = filePath.replaceFirst("https://s3.ap-northeast-2.amazonaws.com/" + bucket + "/", "");
            amazonS3Client.deleteObject(bucket, key);
            log.info("S3 파일 삭제 성공: " + filePath);
        } catch (AmazonServiceException e) {
            log.error("S3 파일 삭제 실패: " + filePath, e);
        }
        log.info("[S3Uploader] : S3에 있는 파일 삭제");
    }

    /**
     * 로컬에 저장된 파일 지우기
     * @param targetFile : 저장된 파일
     */
    private void removeNewFile(File targetFile) {
        if (targetFile.delete()) {
            log.info("[파일 업로드] : 파일 삭제 성공");
            return;
        }
        log.info("[파일 업로드] : 파일 삭제 실패");
    }

    /**
     * 로컬에 파일 업로드 및 변환
     * @param file : 업로드할 파일
     */
    private Optional<File> convert(MultipartFile file) throws IOException {
        // 로컬에서 저장할 파일 경로 : user.dir => 현재 디렉토리 기준
        String dirPath = System.getProperty("user.dir") + "/" + file.getOriginalFilename();
        File convertFile = new File(dirPath);

        if (convertFile.createNewFile()) {
            // FileOutputStream 데이터를 파일에 바이트 스트림으로 저장
            try (FileOutputStream fos = new FileOutputStream(convertFile)) {
                fos.write(file.getBytes());
            }
            return Optional.of(convertFile);
        }

        return Optional.empty();
    }

    public void ensureDirectoryExists(String folderPath) {
        String key = folderPath.substring(bucket.length() + 1); // remove bucket name from key
        if (!amazonS3Client.doesObjectExist(bucket, key)) {
            amazonS3Client.putObject(bucket, key + "/", new ByteArrayInputStream(new byte[0]), new ObjectMetadata());
            log.info("S3 폴더 생성 완료: " + folderPath);
        }
    }
}