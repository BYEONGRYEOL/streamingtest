package com.example.streamingtest;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest
// @TestPropertySource(properties = {"savepath=C:/Users/sbl/Desktop/streamingtest"})
public class AwsS3Test {
	@Autowired
	private S3AsyncClient s3AsyncClient;

	@Autowired
	private TestHandler testHandler;

	@Value("${savepath}")
	private String localSavePath;

	@Test
	void getObject_Sync() {
		String key = "lyulbyung/videos/lyulbyung.m3u8";
		CompletableFuture<GetObjectResponse> future = s3AsyncClient.getObject(getObjectRequest(key),
			PathUtils.getOrCreateSaveFilePath(localSavePath, key));
		future.thenAccept(object -> log.info("object : " + object)).join();
	}

	@Test
	void getObject_Async() throws InterruptedException {
		String[] timestamps = new String[]{
			"20231214222438",
			"20231214222447",
			"20231214222456",
			"20231214222504",
			"20231214222512",
		};

		Arrays.stream(timestamps).map(str -> "lyulbyung/videos/lyulbyung-" + str + ".ts")
			.forEach(key -> testHandler.downloadRequest(key));
		// String key = "lyulbyung/videos/lyulbyung-20231214222438.ts";
		// testHandler.downloadRequest(key);

		Thread.sleep(2000L);
	}

	public GetObjectRequest getObjectRequest(String key) {
		return GetObjectRequest.builder().bucket("lemonair-streaming").key(key).build();
	}

}
