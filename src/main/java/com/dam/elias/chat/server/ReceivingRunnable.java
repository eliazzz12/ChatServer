package com.dam.elias.chat.server;

import com.dam.elias.chat.api.model.Message;
import com.dam.elias.chat.api.model.User;
import com.dam.elias.chat.server.exceptions.HandlerNotFoundException;

import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class ReceivingRunnable implements Runnable {
    private User user;
    private static Map<User, Sender> users;
    private static BlockingQueue<Message> messageQueue;
    private ObjectInputStream in;

    public ReceivingRunnable(User user, Map<User, Sender> users, BlockingQueue<Message> messageQueue,
                             ObjectInputStream in) {
        setUser(user);
        setUsers(users);
        setMessageQueue(messageQueue);
        setIn(in);
    }

    @Override
    public void run() {
        boolean userIsOnline = true;
        while(userIsOnline) {
            try {
                handle(in.readObject());
            } catch (Exception _) {
                users.remove(user);
                userIsOnline = false;
            }
        }
    }

    private void setUsers(Map<User, Sender> users_) {
        if(users_ == null) {
            throw new IllegalArgumentException("users map cannot be null");
        }
        users = users_;
    }

    private void setMessageQueue(BlockingQueue<Message> messageQueue_) {
        if(messageQueue_ == null) {
            throw new IllegalArgumentException("Messages queue cannot be null");
        }
        messageQueue = messageQueue_;
    }

    private void setIn(ObjectInputStream in) {
        if(in == null) {
            throw new IllegalArgumentException("Object input stream cannot be null");
        }
        this.in = in;
    }

    private interface Handler {
        void handle(Object o);
    }

    private static final Map<Class,Handler> dispatch = Map.ofEntries(
        Map.entry(Message.class, o -> handleMessage((Message) o)),
        Map.entry(User.class, o -> handleUser((User) o)),
        Map.entry(ArrayList.class, o -> handleList((ArrayList<User>) o))
    );

    private static void handle(Object o) {
        Handler h = dispatch.get(o.getClass());
        if (h == null) {
            throw new HandlerNotFoundException("Handler not found for " + o.getClass()+ " class");
        }
        h.handle(o);
    }

    private static void handleList(List<User> o) {
        User askingUser = o.getFirst();
        List<User> list = new ArrayList<>();
        list.addAll(users.keySet());
        list.remove(askingUser);
        users.get(askingUser).sendUserList(list);
    }

    static void handleMessage(Message message){
        System.out.println("SERVER: mensaje recibido: "+message.getText());
        messageQueue.add(message);
    }

    static void handleUser(User userToUpdate){
        throw new UnsupportedOperationException("Not implemented yet");
        //TODO updateUser(): actualizar datos en todos los clientes que tengan conversación con el user
    }

    public void setUser(User user) {
        if(user == null) {
            throw new IllegalArgumentException("user cannot be null");
        }
        this.user = user;
    }
}
