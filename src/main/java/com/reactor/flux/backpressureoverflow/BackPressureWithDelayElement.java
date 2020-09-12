package com.reactor.flux.backpressureoverflow;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;


@Log4j2
public class BackPressureWithDelayElement {
    public static void main(String[] args) throws InterruptedException {



        Flux<Integer> flux = Flux.range(0, 1000)
                .log()
                .delaySubscription(Duration.ofSeconds(1))
                .delaySequence(Duration.ofSeconds(1))
        ;


        flux
                .subscribeOn(Schedulers.elastic())
                .subscribe(new Subscriber<Integer>() {
            private Subscription subscription;
            private int dataCounter;
            @Override
            public void onSubscribe(Subscription s) {
                this.subscription = s;
                s.request(3);
            }

            @SneakyThrows
            @Override
            public void onNext(Integer value) {

                log.info("API Call --------" + value);
               // Thread.sleep(500);
                this.dataCounter++;

                if(this.dataCounter % 3 == 0) {
                    this.subscription.request(3);
                }
            }

            @Override
            public void onError(Throwable t) {
                log.error("Cannot obtain value cause: {}", t);
            }

            @Override
            public void onComplete() {
                log.info("Completed");
            }
        });

        Thread.sleep(1000000);
    }
}
