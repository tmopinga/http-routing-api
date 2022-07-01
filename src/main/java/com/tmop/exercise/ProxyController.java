package com.tmop.exercise;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;

@RestController
@Slf4j
public class ProxyController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyController.class);

    @Autowired
    @Qualifier("proxyRestTemplate")
    private RestTemplate restTemplate;

    @Autowired
    private RoundRobinBalancer roundRobinBalancer;

    @RequestMapping("/**")
    @ResponseBody
    public ResponseEntity request(@RequestBody(required = false) String body, HttpMethod method,
                                  HttpServletRequest request) throws URISyntaxException {
        String applicationApi = roundRobinBalancer.getApplicationApi();
        if (applicationApi == null) {
            LOGGER.info("No active server.");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }

        URI uri = new URI("http", applicationApi, request.getRequestURI(), request.getQueryString(), null);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        LOGGER.info("Sending request to: {}", uri.toString());
        ResponseEntity<String> responseEntity = restTemplate.exchange(uri, method, new HttpEntity<>(body, headers), String.class);
        return responseEntity;
    }
}
