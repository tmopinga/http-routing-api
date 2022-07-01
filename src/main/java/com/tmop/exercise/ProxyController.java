package com.tmop.exercise;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
public class ProxyController {

    @Value("${serverList}")
    private List<String> serverList;

    private RestTemplate restTemplate = new RestTemplate();

    private LoadBalancer roundRobinBalancer;

    @PostConstruct
    private void setRoundRobinBalancer() {
        roundRobinBalancer = new RoundRobinBalancer(serverList);
    }

    @RequestMapping("/**")
    @ResponseBody
    public ResponseEntity request(@RequestBody(required = false) String body, HttpMethod method,
                                  HttpServletRequest request) throws URISyntaxException
    {
        String applicationApi = roundRobinBalancer.getApplicationApi();
        if (applicationApi == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }

        URI uri = new URI("http", applicationApi, request.getRequestURI(), request.getQueryString(), null);
        System.out.println(uri.toString());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> responseEntity =
                restTemplate.exchange(uri, method, new HttpEntity<>(body, headers), String.class);
        return responseEntity;
    }
}
