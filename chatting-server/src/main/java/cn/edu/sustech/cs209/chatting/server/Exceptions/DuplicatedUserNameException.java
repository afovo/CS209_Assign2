package cn.edu.sustech.cs209.chatting.server.Exceptions;

public class DuplicatedUserNameException extends Exception{
    public DuplicatedUserNameException(String message){
        super(message);
    }
}
