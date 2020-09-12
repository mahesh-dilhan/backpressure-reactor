package com.reactor.flux.backpressureoverflow;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Log4j2
public class BackpressurewithZip {
    public static void main(String[] args) {
        Flux.range(1, 10)
                .zipWith(
                        Flux.interval(Duration.of(1, ChronoUnit.SECONDS)))
                .map(Tuple2::getT1)
                .toIterable()
                .forEach(i -> log.info("Received: {}", i));
    }


}
