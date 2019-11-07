package com.micronaut.example.springawsos3.config;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Config {
    @Bean
    public AmazonS3 s3client(@Value("${fp.aws.read.accessKey}") final String accessKey, @Value("${fp.aws.read.secretKey}") final String secretKey, @Value("${fp.aws.region}") final String region) {
        return buildAmazonS3(accessKey, secretKey, region);
    }

    @Bean
    public AmazonS3 ownerClient(@Value("${fp.aws.owner.accessKey}") final String accessKey, @Value("${fp.aws.owner.secretKey}") final String secretKey, @Value("${fp.aws.region}") final String region) {
        return buildAmazonS3(accessKey, secretKey, region);
    }

    @Bean
    public AWSCredentialsProvider readCredentialProvider(@Value("${fp.aws.owner.accessKey}") final String accessKey,
                                                         @Value("${fp.aws.owner.secretKey}") final String secretKey){
        return new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey));
    }

    private static AmazonS3 buildAmazonS3(final String accessKey, final String secretKey, final String region){
        return AmazonS3ClientBuilder.standard()
                .withPathStyleAccessEnabled(true)
                .withPayloadSigningEnabled(true).withRegion(Regions.fromName(region))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
                .build();
    }

}
