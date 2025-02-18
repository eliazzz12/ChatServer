package com.dam.elias.chat.server.exceptions;

public class HandlerNotFoundException extends RuntimeException {
    public HandlerNotFoundException(String message) {
        super(message);
    }
}
