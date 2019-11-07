package com.micronaut.example.springawsos3.services.impl;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.micronaut.example.springawsos3.services.S3ClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.Optional;

@Service
public class S3ClientServiceImpl implements S3ClientService {
    private Logger logger = LoggerFactory.getLogger(S3ClientServiceImpl.class);

    private final AWSCredentialsProvider readCredentialProvider;
    private final String bucket;
    private final int expirationMinutes;
    private AmazonS3 s3client;

    public S3ClientServiceImpl(AWSCredentialsProvider readCredentialProvider,
                               @Value("${fp.aws.bucket}") final String bucket,
                               @Value("${fp.aws.expirationMinutes}") final int expirationMinutes,
                               AmazonS3 s3client) {
        this.readCredentialProvider = readCredentialProvider;
        this.bucket = bucket;
        this.expirationMinutes = expirationMinutes;
        this.s3client = s3client;
    }

    @Override
    public ByteArrayOutputStream downloadFile(String keyName) {
           try {
                S3Object s3object = s3client.getObject(new GetObjectRequest(bucket, keyName));

                InputStream is = s3object.getObjectContent();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int len;
                byte[] buffer = new byte[4096];
                while ((len = is.read(buffer, 0, buffer.length)) != -1) {
                    baos.write(buffer, 0, len);
                }

                return baos;
            } catch (IOException ioe) {
                logger.error("IOException: " + ioe.getMessage());
            } catch (AmazonServiceException ase) {
                logger.info("sCaught an AmazonServiceException from GET requests, rejected reasons:");
                logger.info("Error Message:    " + ase.getMessage());
                logger.info("HTTP Status Code: " + ase.getStatusCode());
                logger.info("AWS Error Code:   " + ase.getErrorCode());
                logger.info("Error Type:       " + ase.getErrorType());
                logger.info("Request ID:       " + ase.getRequestId());
                throw ase;
            } catch (AmazonClientException ace) {
                logger.info("Caught an AmazonClientException: ");
                logger.info("Error Message: " + ace.getMessage());
                throw ace;
            }
            return null;
    }

    @Override
    public void uploadFile(String keyName, MultipartFile file) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            s3client.putObject(bucket, keyName, file.getInputStream(), metadata);
        } catch(IOException ioe) {
            logger.error("IOException: " + ioe.getMessage());
        } catch (AmazonServiceException ase) {
            logger.info("Caught an AmazonServiceException from PUT requests, rejected reasons:");
            logger.info("Error Message:    " + ase.getMessage());
            logger.info("HTTP Status Code: " + ase.getStatusCode());
            logger.info("AWS Error Code:   " + ase.getErrorCode());
            logger.info("Error Type:       " + ase.getErrorType());
            logger.info("Request ID:       " + ase.getRequestId());
            throw ase;
        } catch (AmazonClientException ace) {
            logger.info("Caught an AmazonClientException: ");
            logger.info("Error Message: " + ace.getMessage());
            throw ace;
        }
    }

    @Override
    public void deleteFileFromS3Bucket(String docId) {
        try {
            s3client.deleteObject(new DeleteObjectRequest(bucket, docId));
        } catch (AmazonServiceException ex) {
            logger.error("error [" + ex.getMessage() + "] occurred while removing [" + docId + "] ");
        }
    }

    @Override
    public Optional<URL> generatePreassignedUrl(String docName) {
        if(!objectExists(docName)){
            return Optional.empty();
        }
        final Date expiration = new Date(System.currentTimeMillis() + (expirationMinutes * 60 * 1000));
        final GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucket, docName)
                .withExpiration(expiration)
                .withRequestCredentialsProvider(readCredentialProvider);
        final URL url;
        try {
            url = s3client.generatePresignedUrl(generatePresignedUrlRequest);
        } catch(Exception e){
            logger.error("Could not generatePreassigned URL");
            throw e;
        }
        return Optional.of(url);
    }
    private boolean objectExists(final String docName){
        boolean doesObjectExist = false;
        try {
            doesObjectExist = s3client.doesObjectExist(bucket, docName);
        } catch(Exception e){
            logger.error("could not check if object exist" , e);
        } finally {

        }
        return doesObjectExist;
    }
    //TODO download doc from url.
}
