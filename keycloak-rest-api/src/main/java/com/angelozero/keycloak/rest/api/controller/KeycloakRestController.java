package com.angelozero.keycloak.rest.api.controller;

import com.angelozero.keycloak.rest.api.rest.TokenRequest;
import com.angelozero.keycloak.rest.api.serivce.GenerateTokenService;
import com.angelozero.keycloak.rest.api.rest.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/keycloak")
@RequiredArgsConstructor
public class KeycloakRestController {

    private final GenerateTokenService generateTokenService;

    @GetMapping("/user")
    @PreAuthorize("hasRole('client_user')")
    public ResponseEntity<String> helloKeycloakRestApi() {
        return ResponseEntity.ok("Hello Keycloak Rest API for USER !");
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('client_admin')")
    public ResponseEntity<String> helloKeycloakRestApiAdmin() {
        return ResponseEntity.ok("Hello Keycloak Rest API for ADMIN!");
    }

    @PostMapping("/token")
    public ResponseEntity<TokenResponse> getToken(@RequestBody TokenRequest tokenRequest) {
        var response = generateTokenService.execute(tokenRequest.username(), tokenRequest.password());
        return ResponseEntity.ok(response);
    }
}
