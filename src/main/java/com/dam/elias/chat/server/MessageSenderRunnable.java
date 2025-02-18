package com.dam.elias.chat.server;

import com.dam.elias.chat.api.model.*;
import com.dam.elias.chat.api.model.exceptions.UserNotInThisChatException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class MessageSenderRunnable implements Runnable {
    private Map<User, Sender> users;
    private BlockingQueue<Message> queue;

    public MessageSenderRunnable(Map<User, Sender> users, BlockingQueue<Message> queue) {
        setUsers(users);
        setQueue(queue);
    }

    @Override
    public void run() {
        while(true) {
            try {
                Message message = queue.take();
                Chat chat = message.getChat();
                if(chat.isPrivate()) {
                    PrivateChat privateChat = (PrivateChat) chat;
                    User receiver = null;
                    try {
                        receiver = privateChat.getOtherUser(message.getSender());
                        users.get(receiver).sendMessage(message);
                    } catch (UserNotInThisChatException _) {
                        //TODO hacer algo?
                    } catch (NullPointerException _) {
                        //TODO notificar no existe el usuario
                        /*
                        No existiría el usuario si estaba online y se ha desconectado
                         */
                    }
                } else if(chat.getName().equals("ALL")) {
                    removeDisconnectedUsers((GroupChat) chat);
                    if(message.getSender().getUsername().equals("SERVER")) {
                        String userName = message.getText().split(" ")[4];
                        User receiver = getUserByName(userName);
                        if(receiver != null) {
                            System.out.println("Enviando mensaje a ALL(para el usuario "+receiver.getUsername()+")");
                            users.get(receiver).sendMessage(message);
                        }
                    } else {
                        System.out.println("Enviando mensaje a ALL ["+message.getText()+"]");
                        List<User> userList = getAllUsers();
                        User sender = message.getSender();
                        System.out.println("El mensaje viene de "+sender.getUsername());
                        System.out.println("UserList: ");
                        userList.forEach(user -> System.out.println(user.getUsername()));
                        userList.forEach(receiver -> {
                            if(!receiver.getUsername().equals("SERVER") && !receiver.equals(sender)) {
                                System.out.println("Lo recibe:"+receiver.getUsername());
                                users.get(receiver).sendMessage(message);
                            }
                        });
                    }
                } else {
                    GroupChat groupChat = (GroupChat) chat;
                    List<User> userList = groupChat.getUsers();
                    User sender = message.getSender();
                    userList.forEach(receiver -> {
                        if(!receiver.equals(sender)) {
                            users.get(receiver).sendMessage(message);
                        }
                    });
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void removeDisconnectedUsers(GroupChat chat) {
        for(User user : chat.getUsers()) {
//            if()
        }
    }

    private List<User> getAllUsers() {
        return new ArrayList<>(users.keySet());
    }

    private User getUserByName(String userName) {
        for(User user : users.keySet()) {
            if(user.getUsername().equals(userName)) {
                return user;
            }
        }
        return null;
    }

    private void setUsers(Map<User, Sender> users) {
        if(users == null) {
            throw new IllegalArgumentException("users map cannot be null");
        }
        this.users = users;
    }

    private void setQueue(BlockingQueue<Message> queue) {
        if(queue == null) {
            throw new IllegalArgumentException("Messages queue cannot be null");
        }
        this.queue = queue;
    }
}
