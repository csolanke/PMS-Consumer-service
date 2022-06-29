package com.cds.org.consumer.controller;

import com.cds.org.consumer.BadUserCredentialsException;
import com.cds.org.consumer.model.AuthenticationRequest;
import com.cds.org.consumer.model.ClientDetails;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class PMSConsumerController {

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    RestTemplate restTemplate;

    @GetMapping("/fetch-clients/{id}")
    public ResponseEntity<ClientDetails> fetchClient(@PathVariable int id) {

        ResponseEntity<ClientDetails> response = null;

        try {
            response = restTemplate.getForEntity("http://portfolio-manager-service/api/v1/client"+id, ClientDetails.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    @GetMapping("/fetch-all-clients")
    public ResponseEntity<List<ClientDetails>> fetchAllClients() throws JsonProcessingException {

        HttpHeaders autheticationAPIHeaders = new HttpHeaders();
        autheticationAPIHeaders.setContentType(MediaType.APPLICATION_JSON);

        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        authenticationRequest.setUsername("chandra");
        authenticationRequest.setPassword("welcome");

        ObjectMapper mapper = new ObjectMapper();
        String inputJsonFormattedObject = mapper.writeValueAsString(authenticationRequest);

        HttpEntity<String> authenticationEntity = new HttpEntity<>(inputJsonFormattedObject, autheticationAPIHeaders);

        String jwt = null;
        List<ClientDetails> clientDetails = null;
        try {
            ResponseEntity<String> exchange = restTemplate.exchange(
                    "http://portfolio-manager-service/api/v1/authenticate",
                    HttpMethod.POST,
                    authenticationEntity,
                    String.class);



            if ( exchange.getStatusCode().is2xxSuccessful() && exchange.getBody()!=null) {
                jwt = exchange.getBody();

                String finalToken = null;
                if(null!=jwt)
                {
                    String jWtToken = jwt.substring(29);
                    finalToken = jWtToken.substring(0, jWtToken.length() - 31);
                }

                HttpHeaders headersForClientAPI = new HttpHeaders();
                headersForClientAPI.set("Authorization", "Bearer " + finalToken);

                HttpEntity<String> clientEntity = new HttpEntity<>("body", headersForClientAPI);

                ResponseEntity<ClientDetails[]> clientExchange = restTemplate.exchange(
                        "http://portfolio-manager-service/api/v1/client",
                        HttpMethod.GET, clientEntity, ClientDetails[].class);
                clientDetails = Arrays.stream(clientExchange.getBody()).toList();
            }
            else{
                throw new BadUserCredentialsException("Bad user credentials");
            }

        } catch (Exception e) {
            e.getMessage();
        }

        return new ResponseEntity<>(clientDetails, HttpStatus.OK);
    }

}