package com.angelozero.keycloak.custom.spi.exception;

public class UserRepositoryException extends RuntimeException {

    public UserRepositoryException(final String message){
        super(message);
    }
}
