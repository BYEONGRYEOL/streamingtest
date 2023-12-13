package com.example.streamingtest;

import static org.springframework.web.reactive.function.server.ServerResponse.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.amazonaws.client.builder.AwsAsyncClientBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.CopyObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;


// @Slf4j
@Component
@RequiredArgsConstructor
public class TestHandler {

	@Value("${aws.s3.bucket}")
	private String bucket;

	private final S3AsyncClient s3AsyncClient;

	@Value("${savepath.m3u8}")
	private String m3u8SaveFilePath;

	public Mono<ServerResponse> test(ServerRequest request) {
		return ok().bodyValue("Hello world");
	}

	public GetObjectRequest getM3U8Request(String key){
		return GetObjectRequest.builder()
			.bucket(bucket)
			.key(key)
			.build();
	}



	public Mono<ServerResponse> stream(ServerRequest request) {
		Flux<ByteBuffer>  byteBufferFlux = downloadFromS3("lyulbyung/videos/lyulbyung.m3u8");

		byteBufferFlux
			.log()
			.collectList()
			.map(TestHandler::concatenateBuffers)
			.log()
			.map(bytes -> new String(bytes, StandardCharsets.UTF_8))
			.doOnNext(m3u8Content -> writeToFile(m3u8SaveFilePath, m3u8Content))
			.subscribe();

		return Mono.just(Objects.requireNonNull(ok().build().block()));
	}


	public Flux<ByteBuffer> downloadFromS3(String key) {
		return Flux.create(sink ->
			// 비동기식으로 HTTP응답을 처리하고 응답 본문을 바이트 배열로 변환한다.
			s3AsyncClient.getObject(getM3U8Request(key), AsyncResponseTransformer.toBytes())
				.whenComplete((response, exception) -> {
					if (response != null) {
						sink.next(ByteBuffer.wrap(response.asByteArray()));
					} else {
						sink.error(exception);
					}
				}));
	}
	private static void writeToFile(String filePath, String content) {
		try (FileOutputStream fos = new FileOutputStream(filePath)) {
			fos.write(content.getBytes(StandardCharsets.UTF_8));
			System.out.println("M3U8 file saved successfully at: " + filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static byte[] concatenateBuffers(List<ByteBuffer> byteBuffers) {
		int totalSize = byteBuffers.stream().mapToInt(ByteBuffer::remaining).sum();
		ByteBuffer concatenatedBuffer = ByteBuffer.allocate(totalSize);

		byteBuffers.forEach(concatenatedBuffer::put);

		return concatenatedBuffer.array();
	}

}
