package com.angelozero.keycloak.custom.spi.authenticator.exception;

public class CustomAuthenticatorException extends RuntimeException {

    public CustomAuthenticatorException(final String message){
        super(message);
    }
}
