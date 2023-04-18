package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;

public class User implements Serializable {
    String name;
    String picture;
    UserStatus status;
    public User(String name){
        this.name=name;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

}
