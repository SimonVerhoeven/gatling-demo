package dev.simonverhoeven.gatlingdemo;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class GreetingSimulation extends Simulation {
    final HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080")
            .acceptHeader("application/json")
            .userAgentHeader("Gatling performance Test");

    ScenarioBuilder sampleScenario = scenario("Load Test greeting")
            .exec(http("get greeting")
                    .get(session -> "/greet/" + UUID.randomUUID())
                    .check(status().is(200))
            )
            .pause(5)
            .exec(http("Randomly slow")
                    .get("/slow")
                    .check(status().is(200))
            );

    public GreetingSimulation() {
        this.setUp(sampleScenario.injectOpen(constantUsersPerSec(100).during(Duration.ofSeconds(60))))
                .assertions(forAll().failedRequests().percent().lte(1D))
                .protocols(httpProtocol);
    }

    ChainBuilder greeting = exec(http("get greeting")
            .get(session -> "/greet/" + UUID.randomUUID())
            .check(status().is(200))
    )
            .pause(5);

    ChainBuilder slowcall = exec(http("Randomly slow")
            .get("/slow")
            .check(status().is(200))
    );

    ScenarioBuilder sampleScenario2 = scenario("Load test greeting").exec(greeting, slowcall);

    ChainBuilder sessionStep = exec(session -> {
        return session.set("someField", "value");
    });

    ChainBuilder inverseState = exec(session -> {
        boolean failed = session.isFailed();
        if (failed) {
            session.markAsSucceeded();
        } else {
            session.markAsFailed();
        }
        exitHereIfFailed()
        return session;
    });

    Iterator<Map<String, Object>> feeder = Stream.generate((Supplier<Map<String, Object>>) () -> Collections.singletonMap("dieRoll", ThreadLocalRandom.current().nextInt(1, 7))).iterator();

// The below code is to showcase a deeper usage of checks, to also validate data, and use it later on.
//    ScenarioBuilder sampleScenario = scenario("Load Test greeting")
//            .exec(http("get greeting")
//                    .get(session -> "/greet/" + UUID.randomUUID())
//                    .check(
//                            status().is(200),
//                            bodyString()
//                                    .transform(String::toUpperCase)
//                                    .validate("Contains HELLO validation", (value, session) -> {
//                                        if (value.startsWith("HELLO")) {
//                                            return value;
//                                        } else {
//                                            throw new IllegalStateException("Value " + value + " should start with HELLO");
//                                        }
//                                    })
//                                    .name("Greeting message check")
//                                    .saveAs("loudMessage")
//                    )
//            )
//            .pause(5)
//            .exec(http("Randomly slow")
//                    .get("/slow")
//                    .check(status().is(200))
//                    .checkIf(session -> session.getString("loudMessage") != null).then(status().not(404))
//            );
}
