package com.example.streamingtest;

import static org.springframework.web.reactive.function.server.ServerResponse.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class TestHandler {

	@Value("${aws.s3.bucket}")
	private String bucket;

	private final S3AsyncClient s3AsyncClient;

	@Value("${savepath}")
	private String localSavePath;

	public Mono<ServerResponse> test(ServerRequest request) {
		return ok().bodyValue("Hello world");
	}


	public Mono<ServerResponse> stream(ServerRequest request) {
		String streamerName = request.pathVariable("streamername");
		String key = streamerName + "/videos/" + streamerName + ".m3u8";
		return downloadRequest(key);

	}
	public Mono<ServerResponse> downloadRequest(String key){
		Path saveFilePath = PathUtils.getOrCreateSaveFilePath(localSavePath, key);
		Flux<ByteBuffer>  byteBufferFlux = downloadFromS3(key);
		byteBufferFlux
			.log()
			.collectList()
			.map(TestHandler::concatenateBuffers)
			.doOnNext(contentByteArray -> writeToFile(saveFilePath, contentByteArray)).subscribe();

		return Mono.just(Objects.requireNonNull(ok().build().block()));
	}
	
	public Flux<ByteBuffer> downloadFromS3(String key) {
		return Flux.create(sink ->
			// 비동기식으로 HTTP응답을 처리하고 응답 본문을 바이트 배열로 변환한다.
			s3AsyncClient.getObject(getM3U8Request(key), AsyncResponseTransformer.toBytes())
				.whenComplete((response, exception) -> { // 비동기 다운로드 작업이 단계별로 종료될 때 마다.
					if (response != null) {
						// 응답이 byte[] 형식으로 정상적으로 온 경우 계속해서 Flux에 값을 넣는다.
						sink.next(ByteBuffer.wrap(response.asByteArray()));
						sink.complete();
					} else {
						// 응답이 오지 않는 경우
						sink.error(exception);
					}
				}));
	}

	private static void writeToFile(Path filePath, byte[] contentByteArray) {
		try {
			Files.write(filePath, contentByteArray);
		} catch (IOException e) {
			log.info("파일 작성할때 오류 발생");
			throw new RuntimeException(e);
		}
	}

	private static byte[] concatenateBuffers(List<ByteBuffer> byteBuffers) {
		int totalSize = byteBuffers.stream().mapToInt(ByteBuffer::remaining).sum();
		ByteBuffer concatenatedBuffer = ByteBuffer.allocate(totalSize);
		byteBuffers.forEach(concatenatedBuffer::put);
		log.info("concatenateBuffers 실행");
		return concatenatedBuffer.array();
	}

	private GetObjectRequest getM3U8Request(String key){
		return GetObjectRequest.builder()
			.bucket(bucket)
			.key(key)
			.build();
	}

}
