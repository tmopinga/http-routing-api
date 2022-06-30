package com.tmop.exercise;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;

@RestController
public class ProxyController {

    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private RoundRobin roundRobinBalancer;

    @RequestMapping("/**")
    @ResponseBody
    public ResponseEntity request(@RequestBody(required = false) String body, HttpMethod method,
                                  HttpServletRequest request) throws URISyntaxException
    {
        URI uri = new URI("http", roundRobinBalancer.getApplicationApi(), request.getRequestURI(), request.getQueryString(), null);
        System.out.println(uri.toString());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> responseEntity =
                restTemplate.exchange(uri, method, new HttpEntity<>(body, headers), String.class);
        return responseEntity;
    }
}
