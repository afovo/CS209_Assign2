package cn.edu.sustech.cs209.chatting.common;


import java.io.Serializable;

public class Message implements Serializable {
    public String chatName;//groupName or person(default)

    private Long timestamp;

    private String sentBy;

    private String sendTo;//group:a,b,c,d,...

    private String data;

    private MessageType type;

    public Boolean isGroup;
    public Message(Long timestamp, String sentBy, String sendTo, String data, MessageType type) {
        this.timestamp = timestamp;
        this.chatName = sendTo;
        this.sentBy = sentBy;
        this.sendTo = sendTo;
        this.data = data;
        this.type = type;
        this.isGroup = false;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getSentBy() {
        return sentBy;
    }

    public String getSendTo() {
        return sendTo;
    }

    public String getData() {
        return data;
    }

    public MessageType getType() {return type;}
}
