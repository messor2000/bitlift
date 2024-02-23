package com.example.backend.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Log4j2
@Service
public class AmazonService {

    private final AmazonS3 s3client;
    @Value("${amazon.bucketName}")
    private String bucketName;

    public AmazonService(AmazonS3 s3client) {
        this.s3client = s3client;
    }

    public void uploadFile(String folderName, int fileNumber, MultipartFile file) throws IOException {
        String fileName = fileNumber + "-" + file.getOriginalFilename();
        String keyName = folderName + "/" + fileName;

        // Check if folder exists, create if not
        if (!s3client.doesObjectExist(bucketName, keyName)) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());

            // Upload file to S3 bucket
            s3client.putObject(bucketName, keyName, file.getInputStream(), metadata);
            log.info("File uploaded successfully: {}", keyName);
        } else {
            log.error("File already exists: {}", keyName);
        }
    }

    public S3Object getFile(String folderName, String fileName) {
        String keyName = folderName + "/" + fileName;
        return s3client.getObject(bucketName, keyName);
    }

}
