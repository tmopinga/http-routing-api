package com.tmop.exercise;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

public class RoundRobinBalancer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoundRobinBalancer.class);

    private RestTemplate restTemplate;
    private List<String> serverIPs;
    private List<String> activeServers;

    public RoundRobinBalancer(List<String> serverIPs, RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.serverIPs = serverIPs;
        this.activeServers = serverIPs.stream().filter(this::isServerHealthy).collect(Collectors.toList());
    }

    private static Integer index = 0;

    public String getApplicationApi() {
        String server = null;
        synchronized (index) {
            if (activeServers.isEmpty()) {
                return null;
            }

            if (activeServers.size() == 1) {
                return activeServers.get(0);
            }

            int nextIndex = index % activeServers.size();
            server = activeServers.get(nextIndex);
            index = nextIndex + 1;
        }
        return server;
    }

    @Scheduled(fixedDelayString = "${healthCheck.fixedDelayInMs}")
    private void checkHealth() {
        LOGGER.info("Running health check for target servers [{}]", serverIPs.size());
        serverIPs.forEach(ip -> {
            if (!isServerHealthy(ip)) {
                activeServers.remove(ip);
            } else if (!activeServers.contains(ip)) {
                activeServers.add(ip);
            }
        });
        LOGGER.info("Active servers after health check [{}]", activeServers.size());
    }

    private boolean isServerHealthy(String server) {
        String uri = new StringBuilder("http://").append(server).append("/health").toString();
        try {
            ResponseEntity<String> responseEntity =
                    restTemplate.getForEntity(uri, String.class);
            LOGGER.info("Health check response: {} - {}", uri, responseEntity.getStatusCode().toString());
            return responseEntity.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            LOGGER.info("Health check error: {} - {}", uri, e.getLocalizedMessage());
            return false;
        }
    }
}