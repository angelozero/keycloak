package com.angelozero.keycloak.custom.spi.dto;

import java.util.List;

public record User(Integer id,
                   String firstName,
                   String lastName,
                   List<String> interests,
                   String email,
                   String password) {
}
