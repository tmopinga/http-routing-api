package com.tmop.exercise;

import com.fasterxml.jackson.databind.ser.std.IterableSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

@Component
public class RoundRobin implements LoadBalancer {

    @Value("${serverList}")
    private List<String> serverList;

    private static Integer index = 0;

    @Override
    public String getApplicationApi() {
        String server = null;
        synchronized (index) {
            if (index > serverList.size() - 1) {
                index = 0;
            }
            server = serverList.get(index);
            index++;
        }
        return server;
    }
}