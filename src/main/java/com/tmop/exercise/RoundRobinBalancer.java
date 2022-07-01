package com.tmop.exercise;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;

public class RoundRobinBalancer implements LoadBalancer {

    private List<String> serverList;
    private RestTemplate restTemplate = new RestTemplate();

    private static Integer index = 0;

    public RoundRobinBalancer(List<String> serverList) {
        this.serverList = serverList;
    }

    @Override
    public String getApplicationApi() {
        String server = null;
        synchronized (index) {
            if (serverList.size() == 1) {
                return serverList.get(0);
            }

            int nextIndex = index % serverList.size();
            server = serverList.get(nextIndex);
            while(!isServerHealthy(server)) {
                index = nextIndex + 1;
                nextIndex = index % serverList.size();
                server = serverList.get(nextIndex);
            }
            index = nextIndex + 1;
        }
        return server;
    }

    private boolean isServerHealthy(String server) {
        String uri = new StringBuilder("http://").append(server).append("/health").toString();
        try {
           ResponseEntity<String> responseEntity =
                   restTemplate.getForEntity(uri, String.class);
           System.out.println("Health Check: " + uri + " " + responseEntity.getStatusCode().toString());
           return responseEntity.getStatusCode().is2xxSuccessful();
       } catch (Exception e) {
            System.out.println("Health Check: " + uri + " Error");
            return false;
        }
    }
}