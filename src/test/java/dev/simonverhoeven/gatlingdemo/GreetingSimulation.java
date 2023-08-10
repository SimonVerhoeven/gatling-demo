package dev.simonverhoeven.gatlingdemo;

import io.gatling.javaapi.core.CoreDsl;
import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;
import java.util.UUID;

import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class GreetingSimulation extends Simulation {
    final HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080")
            .acceptHeader("application/json")
            .userAgentHeader("Gatling performance Test");

    ScenarioBuilder scenario = CoreDsl.scenario("Load Test greeting")
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
        this.setUp(scenario.injectOpen(constantUsersPerSec(100).during(Duration.ofSeconds(60))))
                .protocols(httpProtocol);
    }
}
