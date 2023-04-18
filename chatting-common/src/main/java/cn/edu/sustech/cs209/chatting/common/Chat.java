package cn.edu.sustech.cs209.chatting.common;

import java.util.ArrayList;

public class Chat {
    private String name;
    private User[] users;
    private String[] userViewUsers;
    private ArrayList<Message> messages;

    public Chat(String name, User[] users) {
        this.name = name;
        this.users = users;
        this.messages = new ArrayList<>();
    }

    public Chat(String name, String[] users) {
        this.name = name;
        this.userViewUsers = users;
        this.messages = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }
}

