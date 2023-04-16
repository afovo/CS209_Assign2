package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;

/**
 * Created by Dominic on 01-May-16.
 */
public class User implements Serializable {
    public User(String name){
        this.name=name;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    String name;

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    String picture;
    UserStatus status;
}
