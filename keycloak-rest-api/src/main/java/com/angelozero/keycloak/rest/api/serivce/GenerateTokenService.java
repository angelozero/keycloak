package com.angelozero.keycloak.rest.api.serivce;

import com.angelozero.keycloak.rest.api.config.JwtAuthProperties;
import com.angelozero.keycloak.rest.api.rest.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class GenerateTokenService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final JwtAuthProperties jwtAuthProperties;

    public TokenResponse execute(String username, String password) {
        var url = jwtAuthProperties.getTokenUrl();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/x-www-form-urlencoded");

        String body = String.format(
                "username=%s&password=%s&client_id=%s&client_secret=%s&grant_type=password",
                username,
                password,
                jwtAuthProperties.getClientId(),
                jwtAuthProperties.getClientSecret());


        var response = restTemplate.postForEntity(url, new HttpEntity<>(body, headers), TokenResponse.class);

        return response.getBody();
    }
}
