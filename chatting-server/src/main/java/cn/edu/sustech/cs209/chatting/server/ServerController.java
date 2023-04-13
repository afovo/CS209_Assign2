package cn.edu.sustech.cs209.chatting.server;

import java.util.ArrayList;

public class ServerController {
    public ArrayList<String> userList = new ArrayList();
    public static void main(String[] args) throws InterruptedException {
        ServerController serverController = new ServerController();
        System.out.println("Starting server");
        while (serverController.userList.size()!=0) {

        }
        if (serverController.userList.size()==0) {
            Thread.sleep(5000);
        }
    }
    public ServerController() {
    }
}
