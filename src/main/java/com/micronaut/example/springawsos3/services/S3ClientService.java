package com.micronaut.example.springawsos3.services;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.Optional;

public interface S3ClientService {
    public ByteArrayOutputStream downloadFile(String keyName);
    public void uploadFile(String keyName, MultipartFile file);
    public void deleteFileFromS3Bucket(String docId);
    public Optional<URL> generatePreassignedUrl(final String fileName);
}
