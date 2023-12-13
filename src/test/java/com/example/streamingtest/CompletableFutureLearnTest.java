package com.example.streamingtest;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CompletableFutureLearnTest {
	// CompletabgleFuture는
	// 1. 비동기 작업 실행 2. 작업 콜백 3. 작업 조합 4. 예외 처리

	/**
	 * 1. 비동기 작업 실행
	 * runAsync(), supplyAsync()
	 */
	@Test
	void runAsync() throws ExecutionException, InterruptedException {
		// given
		AtomicReference<String> inRunAsyncThreadName = new AtomicReference<>("");
		String testWorkerThreadName = "";
		CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
			inRunAsyncThreadName.set(Thread.currentThread().getName());
			log.info("Thread: " + Thread.currentThread().getName());
		});

		// when
		future.get();

		// then
		testWorkerThreadName = Thread.currentThread().getName();
		log.info("Thread: " + testWorkerThreadName);

		assertThat(inRunAsyncThreadName.get()).isNotEqualTo(testWorkerThreadName);
	}

	@Test
	void supplyAsync() throws ExecutionException, InterruptedException {
		// given
		CompletableFuture<String> whoWillPerformTheFuture = CompletableFuture.supplyAsync(() -> {
			String threadName = Thread.currentThread().getName();
			log.info("Thread: " + Thread.currentThread().getName());
			return threadName;
		});
		// when
		String futureWorkerThreadName = whoWillPerformTheFuture.get();
		String testWorkerThreadName =Thread.currentThread().getName();
		log.info("Thread: " + testWorkerThreadName);

		// then
		assertThat(futureWorkerThreadName).isNotEqualTo(testWorkerThreadName);
	}

	/**
	 * 2. 작업 콜백
	 * CompletableFuture에서 비동기 실행이 끝난 후에 적용할 작업 콜백
	 * <p>
	 * thenApply() : 반환 값을 받아서 처리한 후 다른 값을 반환함,
	 *
	 * @parameter : Function
	 * <p>
	 * thenAccept() : 반환 값을 받아서 처리한 후 값을 반환하지 않음
	 * @parameter : Consumer
	 * <p>
	 * thenRun() : 반환 값을 받지 않고 다른 작업을 실행함
	 * @parameter : Runnable
	 */

	@Test
	void thenApply() throws ExecutionException, InterruptedException {
		// given
		CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
			return 12345;
		}).thenApply(String::valueOf);

		// when then
		assertThat(future.get()).isEqualTo("12345");
	}

	@Test
	void thenAccept() throws ExecutionException, InterruptedException {
		// given
		AtomicInteger sumOneToTen = new AtomicInteger();
		CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> IntStream.rangeClosed(1, 10))
			.thenAccept(stream -> {
				sumOneToTen.set(stream.sum());
			});

		int before = sumOneToTen.get();
		log.info("before : " + before);

		// when
		future.get();

		// then
		int after = sumOneToTen.get();
		log.info("after : " + after);
		assertThat(after).isEqualTo(55);
	}

	@Test
	void thenRun() throws ExecutionException, InterruptedException {
	    // given
		AtomicBoolean isSupplyRun = new AtomicBoolean(false);

		CompletableFuture<Void> future = CompletableFuture.supplyAsync(()->{
			isSupplyRun.set(true);
			return IntStream.rangeClosed(1,1000).boxed().collect(Collectors.toList());
		}).thenRun(()->{
			System.out.println("1부터 1000까지 List 생성 작업 끝남");
		});

	    // when
		assert !isSupplyRun.get();
		var result = future.get();

	    // then
		assert isSupplyRun.get();
		assert result == null;
	}

}
