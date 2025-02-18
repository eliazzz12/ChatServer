package com.dam.elias.chat.server;

import com.dam.elias.chat.api.model.GroupChat;
import com.dam.elias.chat.api.model.Message;
import com.dam.elias.chat.api.model.User;
import com.dam.elias.chat.api.model.exceptions.UsernameBeingUsedException;
import com.dam.elias.chat.server.exceptions.StatusNotSentException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

public class ServerMain {
    private static User USER = new User("SERVER");
    private static GroupChat ALL = new GroupChat("ALL", new ArrayList<>(List.of(USER)));
    private static final Map<User, Sender> users = new ConcurrentHashMap<>();
    private static BlockingQueue<Message> messages = new LinkedBlockingDeque<>();

    public static void main(String[] args) {
        final int port = 10101;
        new Thread(new MessageSenderRunnable(users, messages)).start();
        try(ServerSocket serverSocket = new ServerSocket(port)){
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    Socket client = serverSocket.accept();
                    if(acceptClient(client)){
                        System.out.println("Client connected");
                    } else {
                        System.out.println("Client connection denied");
                    }
                } catch (IOException e) {
                    System.out.println("Client connection error");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean acceptClient(Socket client) throws IOException {
        System.out.println("Accepting client");
        boolean isValidUser=false;
        try {
            ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(client.getInputStream());
            User user = (User) in.readObject();
            System.out.println("SERVER: User = " + user.getUsername());
            try{
                addUser(user, out, in);
                isValidUser = true;
                users.get(user).sendLoginStatus(isValidUser);
            } catch (UsernameBeingUsedException _) {
                rejectUserConnection(out);
            }
            System.out.println("SERVER: isValidUser = " + isValidUser);
            if(isValidUser){
                Message welcomeMessage = new Message(USER, ALL, "Welcome to the chat "+user.getUsername()+" :D");
                ALL.addUser(user);
                messages.add(welcomeMessage);
            }
        } catch (ClassNotFoundException e) {
            System.out.println("SERVER: Clase User no encontrada");
        }
        return isValidUser;
    }

    private static void rejectUserConnection(ObjectOutputStream out) {
        try {
            out.writeBoolean(false);
            out.flush();
        } catch (IOException e) {
            throw new StatusNotSentException(e);
        }
    }

    private static void addUser(User user, ObjectOutputStream out, ObjectInputStream in) throws UsernameBeingUsedException {
        if(users.containsKey(user)) {
            throw new UsernameBeingUsedException();
        }
        users.put(user, new Sender(out));
        new Thread(new ReceivingRunnable(user, users, messages, in)).start();
        System.out.println("SERVER: User added "+user.getUsername());
    }
}
