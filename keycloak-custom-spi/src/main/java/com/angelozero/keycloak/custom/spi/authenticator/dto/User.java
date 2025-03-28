package com.angelozero.keycloak.custom.spi.authenticator.dto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public record User(Integer id, String firstName, String lastName, String email, String password) {

    public String getHashPassword() {
        if (this.password != null && !this.password.isEmpty()) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");

                byte[] hash = digest.digest(this.password.getBytes());

                StringBuilder hexString = new StringBuilder();
                for (byte b : hash) {
                    String hex = Integer.toHexString(0xff & b);
                    if (hex.length() == 1) {
                        hexString.append('0');
                    }
                    hexString.append(hex);
                }
                return hexString.toString();

            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("Error creating user password hash - Error: " + e.getMessage(), e);
            }
        }
        return this.password;
    }
}
