package com.tmop.exercise;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

public class RoundRobinBalancerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoundRobinBalancerTest.class);

    private List<String> serverIPs;
    private RestTemplate restTemplate;
    private RoundRobinBalancer lb;

    @Before
    public void setup() {
        serverIPs = new ArrayList<>();
        serverIPs.add("localhost:4000");
        serverIPs.add("localhost:4001");
        serverIPs.add("localhost:4002");

        restTemplate = mock(RestTemplate.class);
    }

    @Test
    public void testConstructor() {
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(ResponseEntity.ok().body("OK"));
        lb = new RoundRobinBalancer(serverIPs, restTemplate);
        Assert.assertEquals(serverIPs.size(), lb.getActiveServers().size());
        verify(restTemplate, times(serverIPs.size())).getForEntity(anyString(), eq(String.class));
    }

    @Test
    public void testEmptyActiveServers() {
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(ResponseEntity.internalServerError().build());
        lb = new RoundRobinBalancer(serverIPs, restTemplate);
        Assert.assertNull(lb.getApplicationApi());
        Assert.assertEquals(0, lb.getIndex());
    }

    @Test
    public void testSyncGetApplicationApiAllActive() {
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(ResponseEntity.ok().body("OK"));
        lb = new RoundRobinBalancer(serverIPs, restTemplate);
        for (int i = 0; i < 16; i++) {
            String ip = lb.getApplicationApi();
            LOGGER.info("{} {}", i, ip);
            Assert.assertEquals(ip, serverIPs.get(i % serverIPs.size()));
        }
    }

    @Test
    public void testUnhealthyServer() {
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(ResponseEntity.ok().body("OK"));
        lb = new RoundRobinBalancer(serverIPs, restTemplate);
        Assert.assertEquals(lb.getActiveServers().get(0), lb.getApplicationApi());

        when(restTemplate.getForEntity(eq("http://" + serverIPs.get(1) + "/health"), eq(String.class)))
                .thenReturn(ResponseEntity.internalServerError().build());
        lb.checkHealth();
        Assert.assertEquals(serverIPs.size() - 1, lb.getActiveServers().size());
        Assert.assertEquals(lb.getActiveServers().get(1), lb.getApplicationApi());
        Assert.assertEquals(lb.getActiveServers().get(0), lb.getApplicationApi());

        when(restTemplate.getForEntity(eq("http://" + serverIPs.get(1) + "/health"), eq(String.class)))
                .thenReturn(ResponseEntity.ok().body("OK"));
        lb.checkHealth();
        Assert.assertEquals(serverIPs.size(), lb.getActiveServers().size());
        Assert.assertEquals(lb.getActiveServers().get(1), lb.getApplicationApi());
    }

    @Test
    public void testAsyncGetApplicationApiAllActive() {
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(ResponseEntity.ok().body("OK"));
        lb = new RoundRobinBalancer(serverIPs, restTemplate);
        IntStream
                .range(0, 15)
                .parallel()
                .forEach(i ->
                        System.out.println(Thread.currentThread().getName() + " Request " + i + " = " +  lb.getApplicationApi())
                );
        Assert.assertEquals(3, lb.getIndex());
        lb.getApplicationApi();
        Assert.assertEquals(1, lb.getIndex());
        }
}
