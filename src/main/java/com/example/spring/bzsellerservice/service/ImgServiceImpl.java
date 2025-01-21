package com.example.spring.bzsellerservice.service;

import com.example.spring.bzsellerservice.config.s3.S3Uploader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class ImgServiceImpl implements ImgService {
    private final S3Uploader s3Uploader;

    public ImgServiceImpl(S3Uploader s3Uploader) {
        this.s3Uploader = s3Uploader;
    }

    @Override
    @Transactional
    public String uploadImg(String name, MultipartFile file) {
        String url = "";
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("MultipartFile must not be null or empty.");
        } else {
            url = s3Uploader.uploadFileToS3(file, "static/bz-product/");
            log.info("[ImgServiceImpl] Uploaded image URL: {}", url); // 로그 추가
            return url;
        }
    }
}
