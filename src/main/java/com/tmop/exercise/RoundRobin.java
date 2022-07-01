package com.tmop.exercise;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Iterator;
import java.util.List;

@Component
public class RoundRobin implements LoadBalancer {

    @Value("${serverList}")
    private List<String> serverList;

    private Iterator<String> serverIterator;

    @PostConstruct
    private void initIterator() {
        serverIterator = serverList.listIterator();
    }

    @Override
    public String getApplicationApi() {
        String server = null;
        synchronized (serverIterator) {
            if (!serverIterator.hasNext()) {
                serverIterator = serverList.listIterator();
            }
            server = serverIterator.next();
        }
        return server;
    }
}