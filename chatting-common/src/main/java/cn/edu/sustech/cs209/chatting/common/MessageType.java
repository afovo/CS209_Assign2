package cn.edu.sustech.cs209.chatting.common;

public enum MessageType {
    Register, //When a user first Time login
    Login,
    UpdateUserList, //Every time a user login/out, the server send userList to all clients
    Chat,// sender:: sentBy: username  sendTo: username
         // receiver:: If (groupName!=null && is not in chatList.keyset) add groupchat;
         // else if (sentBy not in chatList.keyset) add private chat;
         // else render the corresponding message content cell
    Logout
}
