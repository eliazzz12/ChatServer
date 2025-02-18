package com.dam.elias.chat.server.exceptions;

import java.io.IOException;

public class StatusNotSentException extends RuntimeException {
    public StatusNotSentException(IOException e) {super(e);}
}
