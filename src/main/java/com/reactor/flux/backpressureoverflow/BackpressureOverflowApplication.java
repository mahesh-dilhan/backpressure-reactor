package com.reactor.flux.backpressureoverflow;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink.OverflowStrategy;

import java.time.Duration;

/**
 * OverflowStrategy.DROP
 */
@Log4j2
public class BackpressureOverflowApplication {

	public static void main(String[] args) throws InterruptedException {

		Flux<Object> sqsConsumer = Flux.create(emitter -> {

					// consume and publish 200 numbers
					for (int i = 0; i < 10; i++) {
						System.out.println(Thread.currentThread().getName() + " | Publishing = " + i);
						emitter.next(i);
					}
					// When all values or emitted, call complete.
					emitter.complete();
				//		}, OverflowStrategy.DROP)
				//				.onBackpressureDrop(i -> System.out.println(Thread.currentThread().getName() + " | DROPPED = " + i));
				},OverflowStrategy.BUFFER)
				//.repeat()
				.delaySequence(Duration.ofSeconds(60))
				;



		sqsConsumer
				//.subscribeOn(Schedulers.newSingle("sqsConsumer"))
				//.publishOn(Schedulers.elastic())
				.subscribe(i -> {
			// Process received value.
			System.out.println(Thread.currentThread().getName() + " | Received = " + i);
			// 500 mills delay to simulate slow subscriber
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		Thread.sleep(100000);
	}
}