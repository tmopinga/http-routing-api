package com.tmop.exercise;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableScheduling
public class AppConfiguration {
    @Value("${healthCheck.connectionTimeoutInMs}")
    private int healthCheckConnectionTimeoutInMs;

    @Value("${healthCheck.readTimeoutInMs}")
    private int healthCheckReadTimeoutInMs;

    @Value("${healthCheck.connectionTimeoutInMs}")
    private int proxyConnectionTimeoutInMs;

    @Value("${healthCheck.readTimeoutInMs}")
    private int proxyReadTimeoutInMs;

    @Value("${serverList}")
    private List<String> servers;

    @Bean
    public RestTemplate healthCheckRestTemplate() {
        return new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofMillis(healthCheckConnectionTimeoutInMs))
                .setReadTimeout(Duration.ofMillis(healthCheckReadTimeoutInMs))
                .build();
    }

    @Bean
    public RestTemplate proxyRestTemplate() {
        return new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofMillis(proxyConnectionTimeoutInMs))
                .setReadTimeout(Duration.ofMillis(proxyReadTimeoutInMs))
                .build();
    }

    @Bean
    public RoundRobinBalancer roundRobinBalancer() {
        return new RoundRobinBalancer(servers, healthCheckRestTemplate());
    }
}
