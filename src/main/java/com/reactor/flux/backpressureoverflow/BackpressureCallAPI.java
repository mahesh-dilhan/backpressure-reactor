package com.reactor.flux.backpressureoverflow;

import lombok.extern.log4j.Log4j2;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.text.MessageFormat;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 1 miniture , 200 request
 */
@Log4j2
public class BackpressureCallAPI {
    public static void main(String[] args) {

        int COUNT = 1;

        RestTemplate client = new RestTemplate();

        var exit = new AtomicBoolean(false);
        var lock = new ReentrantLock();
        var condition = lock.newCondition();

        MessageFormat message = new MessageFormat("#batch: {0}, #req: {1}, resultLength: {2}");
        Flux.interval(Duration.ofSeconds(1L))
                .take(COUNT) // this is just for limiting the test running period otherwise you don't need it
                .doOnNext(batch -> log.debug("#batch", batch)) // just for debugging
                .flatMap(batch -> Flux.range(1, 1) // 10 requests per 1 second
                        .flatMap(i -> Mono.fromSupplier(() ->
                                        client
                                                .getForEntity("http://httpbin.org/ip", String.class)
                                                .getBody()


                                ) // your request goes here (1 of 10)
                                        .map(s -> message.format(new Object[]{batch, i, s.length()})) // here the request's result will be the output of message.format(...)
                                        .doOnSubscribe(s -> log.debug("doOnSubscribe: #batch = " + batch + ", i = " + i)) // just for debugging
                                     //   .subscribe()
                                        .subscribeOn(Schedulers.elastic()) // one I/O thread per request

                        )
                )
                .subscribe(
                        s -> log.debug("received") // do something with the above request's result
                        ,e -> {
                            log.error("error", e.getMessage());
                            signalAll(exit, condition, lock);
                        },
                        () -> {
                            log.debug("done");
                            signalAll(exit, condition, lock);
                        }
                );

        await(exit, condition, lock);
    }

    private static void await(AtomicBoolean exit, Condition condition, Lock lock) {
        lock.lock();
        while (!exit.get()) {
            try {
                condition.await();
            } catch (InterruptedException e) {
                // maybe spurious wakeup
                e.printStackTrace();
            }
        }
        lock.unlock();
        log.debug("exit");
    }

    private static void signalAll(AtomicBoolean exit, Condition condition, Lock lock) {
        exit.set(true);
        try {
            lock.lock();
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
