package com.dam.elias.chat.server;

import com.dam.elias.chat.api.model.Message;
import com.dam.elias.chat.api.model.User;
import com.dam.elias.chat.server.exceptions.ListNotSentException;
import com.dam.elias.chat.server.exceptions.MessageNotSentException;
import com.dam.elias.chat.server.exceptions.StatusNotSentException;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

public class Sender {
    private ObjectOutputStream out;

    public Sender(ObjectOutputStream out) {
        setOut(out);
    }

    public void sendMessage(Message message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            throw new MessageNotSentException(e);
        }
        System.out.println("SERVER: mensaje enviado: "+message.getText());
    }

    public void sendLoginStatus(boolean status) {
        try {
            out.writeBoolean(status);
            out.flush();
        } catch (IOException e) {
            throw new StatusNotSentException(e);
        }
    }

    public void sendUserList(List<User> list) {
        try{
            out.writeObject(list);
        } catch (IOException e){
            throw new ListNotSentException(e);
        }
    }

    public void setOut(ObjectOutputStream out) {
        if(out == null) {
            throw new IllegalArgumentException("Object output stream cannot be null");
        }
        this.out = out;
    }
}
