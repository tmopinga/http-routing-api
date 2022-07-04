package com.tmop.exercise;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class RoundRobinBalancer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoundRobinBalancer.class);

    private RestTemplate restTemplate;
    private List<String> serverIPs;
    private List<String> activeServers;

    private static int index;
    private final ReentrantLock lock;

    public RoundRobinBalancer(List<String> serverIPs, RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.serverIPs = serverIPs;
        this.activeServers = serverIPs.stream().filter(this::isServerHealthy).collect(Collectors.toList());
        this.index = 0;
        this.lock = new ReentrantLock();
    }

    public List<String> getActiveServers() {
        return new ArrayList<>(activeServers);
    }

    public int getIndex() {
        return index;
    }

    public String getApplicationApi() {
        lock.lock();
        try {
            if (activeServers.isEmpty()) {
                return null;
            }

            if (activeServers.size() == 1) {
                return activeServers.get(0);
            }

            if (index >= activeServers.size()) {
                index = 0;
            }

            String server = activeServers.get(index);
            LOGGER.debug("INDEX {} {} {}", index, server, Thread.currentThread().getName());
            index++;

            return server;
        } finally {
            lock.unlock();
        }
    }

    @Scheduled(fixedDelayString = "${healthCheck.fixedDelayInMs}")
    public void checkHealth() {
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