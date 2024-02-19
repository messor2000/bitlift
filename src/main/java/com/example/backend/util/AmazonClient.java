package com.example.backend.util;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AmazonClient {
    private AmazonS3 s3client;
//    @Value("${amazon.endpointUrl}")
//    private String endpointUrl;
    @Value("${amazon.bucketName}")
    private String bucketName;
    @Value("${amazon.accessKey}")
    private String accessKey;
    @Value("${amazon.secretKey}")
    private String secretKey;

//    @PostConstruct
//    private void initializeAmazon() {
////        AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
////        this.s3client = new AmazonS3Client(credentials);
//        BasicAWSCredentials creds = new BasicAWSCredentials(this.accessKey, this.secretKey);
//
//        this.s3client = AmazonS3ClientBuilder
//                .standard()
//                .withCredentials(new AWSStaticCredentialsProvider(creds))
//                .build();
//    }
}