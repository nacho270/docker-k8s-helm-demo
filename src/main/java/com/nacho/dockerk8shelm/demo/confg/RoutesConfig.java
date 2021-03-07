package com.nacho.dockerk8shelm.demo.confg;

import com.nacho.dockerk8shelm.demo.repository.impl.MongoBasedCounterRepository;
import com.nacho.dockerk8shelm.demo.service.CounterService;
import io.javalin.Javalin;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.File;
import java.util.Optional;

import static java.lang.String.valueOf;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
public class RoutesConfig {

    private static Configuration config;

    static {
        var profile = Optional.ofNullable(System.getenv("profile")).orElse("");
        log.info("Using profile: {}", profile);
        var fileName = "application" + (!isBlank(profile) ? "-" + profile : "");
        try {
            config = new Configurations().properties(new File(fileName) + ".properties");
        } catch (ConfigurationException cex) {
            log.error("Couldn't find {}.properties", fileName);
            System.exit(-1);
        }
        // Load the properties from the application.properties file and overriding those specified in the env.
        System.getenv().forEach((k, v) -> {
            Object property = config.getProperty(k);
            if (property != null) {
                log.info("Overriding property [{} -> {}] with new value: {}", k, property, v);
                config.setProperty(k, v);
            }
        });
    }

    private final CounterService counterService;

    public RoutesConfig() {
        counterService = new CounterService(new MongoBasedCounterRepository(
                config.getString("mongo.user"),
                config.getString("mongo.pass"),
                config.getString("mongo.host"),
                config.getInt("mongo.port")));
    }

    public void start() {
        var app = Javalin.create().start(config.getInt("server.port"));
        app.get("/ping", ctx -> ctx.result("pong"));
        app.get("/counter", ctx -> ctx.result(valueOf(counterService.getCurrent())));
        app.post("/counter", ctx -> ctx.result(valueOf(counterService.increment())));
    }
}
