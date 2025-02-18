package com.dam.elias.chat.server.exceptions;

public class ListNotSentException extends RuntimeException {
    public ListNotSentException(String message) {
        super(message);
    }
    public ListNotSentException(Exception e) {super(e);}
}
