package com.example.streamingtest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import jakarta.annotation.PostConstruct;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.identity.spi.AwsCredentialsIdentity;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AwsConfig {

	@Value("${aws.s3.credentials.accessKey}")
	private String accessKey;

	@Value("${aws.s3.credentials.secretKey}")
	private String secretKey;

	@Value("${aws.s3.region}")
	private String region;

	private AwsCredentials credentials;

	@PostConstruct
	void init() {
		credentials = AwsBasicCredentials.create(accessKey, secretKey);
	}

	/**
	 * spring-cloud-starter-aws 의존성을 통해 빌드할 수 있는 AmazonS3 객체를 생성합니다.
	 * @return AmazonS3
	 */
	@Bean
	public AmazonS3 amazonS3Client() {
		AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
		return AmazonS3ClientBuilder.standard()
			.withCredentials(new AWSStaticCredentialsProvider(credentials))
			.withRegion(region)
			.build();
	}

	/**
	 * awssdk 의존성을 통해 빌드할 수 있는 S3AsyncClient 객체를 생성합니다.
	 * @return S3AsyncClient
	 */
	@Bean
	public S3AsyncClient s3AsyncClient() {
		return S3AsyncClient.builder().region(Region.of(region))
			// .endpointOverride(URI.create(endPoint))
			.credentialsProvider(StaticCredentialsProvider.create(credentials)).build();
	}

	/**
	 * awssdk 의존성을 통해 빌드할 수 있는 S3Client 객체를 생성합니다.
	 * @return S3Client
	 */
	@Bean
	public S3Client s3Client() {
		return S3Client.builder().region(Region.of(region))
			// .endpointOverride(URI.create(endPoint))
			.credentialsProvider(StaticCredentialsProvider.create(credentials)).build();
	}

}
