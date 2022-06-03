package com.cds.org.consumer.controller;

import com.cds.org.consumer.model.ClientDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class PMSConsumerController {

    @Autowired
    private DiscoveryClient discoveryClient;

    @GetMapping("/fetch-clients/{id}")
    public ResponseEntity<ClientDetails> fetchClient(@PathVariable int id) {

        String baseUri = getbaseUri();
        StringBuilder urlBuilder = new StringBuilder();

         urlBuilder.append(baseUri)
                       .append("/api/v1/client/")
                               .append(id);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<ClientDetails> response = null;

        try {
            response = restTemplate.getForEntity(urlBuilder.toString() , ClientDetails.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    @GetMapping("/fetch-all-clients")
    public ResponseEntity<List<ClientDetails>> fetchAllClients() {

        String baseUri = getbaseUri();

        StringBuilder urlBuilder = new StringBuilder();
         urlBuilder.append(baseUri)
                .append("/api/v1/client");

        List<ClientDetails> clientList = new ArrayList<>();
        ClientDetails[] responseArray = new ClientDetails[10];

        try {
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<ClientDetails[]> responseEntity = restTemplate.getForEntity(urlBuilder.toString(), ClientDetails[].class);
            responseArray = responseEntity.getBody();
            clientList.addAll(List.of(responseArray));

        } catch (Exception e) {
            e.getMessage();
        }
        return new ResponseEntity<>(clientList, HttpStatus.OK);
    }

    private String getbaseUri() {
        List<ServiceInstance> instances = discoveryClient.getInstances("portfolio-manager-service");
        ServiceInstance serviceInstance = instances.get(0);

        return serviceInstance.getUri().toString();
    }

}