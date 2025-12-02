package com.bingbank.authService.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class AccountServiceClient {

    private static final String ACCOUNT_SERVICE_URL = "http://localhost:8082/api/accounts";
    
    private final RestTemplate restTemplate;
    
    @Autowired
    public AccountServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    public boolean accountExists(String accountNumber) {
        try {
            String url = ACCOUNT_SERVICE_URL + "/exists/" + accountNumber;
            @SuppressWarnings("unchecked")
            Map<String, Boolean> response = restTemplate.getForObject(url, Map.class);
            return response != null && Boolean.TRUE.equals(response.get("exists"));
        } catch (Exception e) {
            System.err.println("Error checking account existence: " + e.getMessage());
            return false;
        }
    }
}