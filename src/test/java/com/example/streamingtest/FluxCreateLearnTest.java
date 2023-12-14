package com.example.streamingtest;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@Slf4j
public class FluxCreateLearnTest {

	/**
	 * Flux.create() 메서드는 사실 복잡하지만
	 * 조건따위 없이 그냥 절차적으로 다음 요소와 언제 생성을 종료할지를 지정할 수도 있다.
	 */
	@Test
	void createWithNext() {

		Flux<Integer> flux = Flux.create(fluxSink -> {
			fluxSink.next(1);
			fluxSink.next(2);
			fluxSink.complete();
		});

		StepVerifier.create(flux)
			.expectNext(1)
			.expectNext(2)
			.verifyComplete();
	}

	/**
	 * Flux.create() 메서드에 전달하는 Consumer 내부에서 for loop을 이용한 버전
	 * 이전의 절차적 create와 다를바 없다.
	 */
	@Test
	void forLoopInConsumer(){
		Flux<Integer> flux = Flux.create(fluxSink ->{
			for (int i = 0; i < 10; i++) {
				fluxSink.next(i);
			}
			fluxSink.complete();
		});

		StepVerifier.create(flux)
			.expectNext(0,1,2,3,4,5,6,7,8,9)
			.verifyComplete();
	}

	/**
	 * Flux.take() : 생산 조절 함수
	 * Stream.limit()과 동일한 역할을 함
	 */
	@Test
	void takeOperator(){
		Flux<Integer> flux = Flux.range(1,10)
			.log()
			.take(3)
			.log();

		StepVerifier.create(flux)
			.expectNextCount(3)
			.verifyComplete();

		/**
		 * 실행결과 3개를 요청한 뒤에 subscriber가 publisher에게 구독 관계를 중지를 요청한다.
		 * sub -> pub cancel() 요청
		 * pub -> sub onComplete() 시그널 요청
		 * request(3)
		 * 12:08:00.822 [Test worker] INFO reactor.Flux.Range.1 -- | onNext(1)
		 * 12:08:00.822 [Test worker] INFO reactor.Flux.LimitRequest.2 -- onNext(1)
		 * 12:08:00.822 [Test worker] INFO reactor.Flux.Range.1 -- | onNext(2)
		 * 12:08:00.822 [Test worker] INFO reactor.Flux.LimitRequest.2 -- onNext(2)
		 * 12:08:00.823 [Test worker] INFO reactor.Flux.Range.1 -- | onNext(3)
		 * 12:08:00.823 [Test worker] INFO reactor.Flux.LimitRequest.2 -- onNext(3)
		 * 12:08:00.823 [Test worker] INFO reactor.Flux.Range.1 -- | cancel()
		 * 12:08:00.823 [Test worker] INFO reactor.Flux.LimitRequest.2 -- onComplete()
		 */
	}


}
