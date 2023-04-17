package cn.edu.sustech.cs209.chatting.common;


import java.io.Serializable;

public class Message implements Serializable {
    private Long timestamp;

    private String sentBy;

    private String sendTo;

    private String data;

    private MessageType type;


    public Message(Long timestamp, String sentBy, String sendTo, String data, MessageType type) {
        this.timestamp = timestamp;
        this.sentBy = sentBy;
        this.sendTo = sendTo;
        this.data = data;
        this.type = type;
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
