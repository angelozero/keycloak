package com.angelozero.keycloak.custom.spi.authenticator.exception;

public class UserRepositoryException extends RuntimeException {

    public UserRepositoryException(final String message){
        super(message);
    }
}
