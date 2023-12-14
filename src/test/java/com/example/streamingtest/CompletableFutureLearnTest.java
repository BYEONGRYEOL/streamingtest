package com.example.streamingtest;

import static org.assertj.core.api.Assertions.*;

import java.nio.ByteBuffer;
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
import reactor.core.publisher.Flux;

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

	/**
	 * 작업 조합
	 *
	 * thenCompose() : 두 작업이 이어서 실행됨, 앞선 작업의 결과를 받아 새로운 CompletableFuture를 생성한다.
	 * 
	 * thenCombine() : 두 작업을 독립적으로 실행하고, 둘 다 완료되었을 때 실행할 콜백을 지정한다.
	 * 
	 * allOf() : 여러 작업들을 동시에 실행하고, 모든 작업 결과에 콜백을 실행함
	 * 
	 * anyOf() : 여러 작업들 중 가장 빨리 끝난 하나의 결과에 콜백을 실행함
	 */
	@Test
	void thenCompose() throws ExecutionException, InterruptedException {
		CompletableFuture<String> future = 중학교진학("서병렬").thenCompose(this::고등학교진학);
		log.info("future.get() : " + future.get());
	}

	@Test
	void thenCombine() throws ExecutionException, InterruptedException {
		CompletableFuture<String> future = 중학교진학("서병렬")
			.thenCompose(this::고등학교진학)
			.thenCombine(보습학원등록("서병렬"), this::명문대진학);

		System.out.println(future.get());
	}


	private CompletableFuture<String> 보습학원등록(String name){
		return CompletableFuture.supplyAsync(()-> name + "은 하교 후에도 학원에서 열공");
	}
	private String 명문대진학(String graduate, String academy){
		return  graduate + "하면서 " + academy + "하니까 명문대 진학";
	}
	private CompletableFuture<String> 중학교진학(String name){
		return CompletableFuture.supplyAsync(()-> name + "의 중학교 졸업장");
	}
	
	private CompletableFuture<String> 고등학교진학(String 중학교졸업장){
		return CompletableFuture.supplyAsync(()-> 중학교졸업장 + " 이 있으므로 고등학교 진학 후 고등학교 졸업");
	}


	public Flux<ByteBuffer> processAsyncData() {
		return Flux.create(sink -> {
			// Simulating an asynchronous operation using CompletableFuture
			CompletableFuture<Void> asyncOperation = simulateAsyncOperation();

			// whenComplete is used to handle the result or exception of the async operation
			asyncOperation.whenComplete((result, exception) -> {
				if (exception == null) {
					// If the async operation is successful, emit the result to the Flux
					sink.next(ByteBuffer.wrap("Async Operation Result".getBytes()));
					sink.complete(); // Complete the Flux after emitting the value
				} else {
					// If there is an exception, emit an error to the Flux
					sink.error(exception);
				}
			});
		});
	}

	private CompletableFuture<Void> simulateAsyncOperation() {
		// Simulating an asynchronous operation with CompletableFuture
		return CompletableFuture.runAsync(() -> {
			// Simulate some async work
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
	}

}
