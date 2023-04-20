package cn.edu.sustech.cs209.chatting.common;

public enum MessageType {
    Register, //When a user first Time login
    Login,
    UpdateUserList, //Every time a user login/out, the server send userList to all clients
    Chat,//  sendBy: a user   sendTo: the chatName
    Logout
}
