package com.reactor.flux.backpressureoverflow;

import lombok.extern.log4j.Log4j2;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * 1 second, 10 calls
 */
@Log4j2
public class BackpressureWebClient {
    public static void main(String[] args) throws InterruptedException {
        WebClient webClient = WebClient.create("https://httpbin.org/get");

        Flux.interval(Duration.ofSeconds(1L))
                .take(1)
                .flatMap(batch ->
                        Flux.range(1, 10)
                        .flatMap(i ->
                                Mono.fromSupplier(()->
                                webClient
                                        .get()
                                        .retrieve()
                                        .bodyToMono(String.class).subscribe(s -> log.info("{}",s))
                            )
                        )
                ).subscribe(stringMono -> log.info("{}",stringMono));

        Thread.sleep(10000);
    }


}
