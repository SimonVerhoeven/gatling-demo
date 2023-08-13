package dev.simonverhoeven.gatlingdemo;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;
import java.util.UUID;

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

    ChainBuilder greeting = exec(http("get greeting")
            .get(session -> "/greet/" + UUID.randomUUID())
            .check(status().is(200))
    )
            .pause(5);

    ChainBuilder slowcall = exec(http("Randomly slow")
            .get("/slow")
            .check(status().is(200))
    );

    ScenarioBuilder sampleScenario2 = scenario("Loead test greeting").exec(greeting, slowcall);

    public GreetingSimulation() {
        this.setUp(sampleScenario.injectOpen(constantUsersPerSec(100).during(Duration.ofSeconds(60))))
                .protocols(httpProtocol);
    }
}
