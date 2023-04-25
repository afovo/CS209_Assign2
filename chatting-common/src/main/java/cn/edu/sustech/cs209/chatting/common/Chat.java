package cn.edu.sustech.cs209.chatting.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Chat {
    private String chatName;
    private Set<User> users;
    private String clientViewUsers; //a,b,c,...   or simply one name
    private ArrayList<Message> messages;
    public boolean isGroup;

    public Chat(String name, String clientViewUsers) {
        this.chatName = name;
        this.clientViewUsers = clientViewUsers;
        this.isGroup = true;
        this.messages = new ArrayList<>();
    }

    public Chat(String name) {
        this.chatName = name;
        this.clientViewUsers = name;
        this.isGroup = false;
        this.messages = new ArrayList<>();
    }

    public String getClientViewUsers() {
        return clientViewUsers;
    }

    public String getName() {
        return chatName;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }
}

