package com.angelozero.keycloak.custom.spi.exception;

public class CustomAuthenticatorException extends RuntimeException {

    public CustomAuthenticatorException(final String message){
        super(message);
    }
}
