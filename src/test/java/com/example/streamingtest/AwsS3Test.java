package com.example.streamingtest;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;


@Slf4j
@SpringBootTest
public class AwsS3Test {
	@Autowired
	private S3AsyncClient s3AsyncClient;


	@Value("${savepath.m3u8}")
	private String m3u8SaveFilePath;
	@Test
	void getObject_Sync(){
		String key = "lyulbyung/videos/lyulbyung.m3u8";
		CompletableFuture<GetObjectResponse> future =  s3AsyncClient.getObject(getM3U8Request(key), getOrCreateSaveFilePath(key));
		future.thenAccept(object-> log.info("object : " + object)).join();
	}
	//
	// @Test
	// void getObject_Async(){
	// 	String key = "lyulbyung/videos/lyulbyung.m3u8";
	//
	// 	s3AsyncClient.getObject(getM3U8Request(key),)
	// }

	private Path getOrCreateSaveFilePath(String key) {
		Path filePath = FileSystems.getDefault().getPath(m3u8SaveFilePath + "/" + key);
		try {
			Files.createDirectories(filePath.getParent());
		} catch (IOException e) {
			log.info(" 부모 디렉토리 생성 실패");
			throw new RuntimeException(e);
		}
		return filePath;
	}
	public GetObjectRequest getM3U8Request(String key){
		return GetObjectRequest.builder()
			.bucket("lemonair-streaming")
			.key(key)
			.build();
	}

}
