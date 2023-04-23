package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.*;
import cn.edu.sustech.cs209.chatting.server.Exceptions.DuplicatedUserNameException;


import java.net.*;
import java.io.*;
import java.util.*;

public class Main {
    public static HashMap<String,User> userList = new HashMap<>();
    public static ArrayList<Chat> chatList = new ArrayList<>();
    private static HashMap<String, ObjectOutputStream> writers = new HashMap<>();

    public static void main(String[] args) throws IOException {
        System.out.println("-----Starting server-----");
        ServerSocket server = new ServerSocket(25250);
        System.out.println("-----[Server] Waiting connection-----");

        while(true){
            Socket sock = server.accept();
            ServerController serverController = new ServerController(sock);
            serverController.start();
        }
    }

    private static class ServerController extends Thread { // one client thread
        private Socket socket;
        private User user;
        private ObjectInputStream input;
        private ObjectOutputStream output;

        public ServerController(Socket socket) throws IOException {
            this.socket = socket;
        }

        @Override
        public void run() {
            System.out.println("[Server]Attempting to connect a user...");
            try {
                input = new ObjectInputStream(socket.getInputStream());
                output = new ObjectOutputStream(socket.getOutputStream());

                while (socket.isConnected()) {
                    Message inputMsg = (Message) input.readObject();
                    if (inputMsg != null) {
                        String sender = inputMsg.getSentBy();
                        String receiver = inputMsg.getSendTo();
                        String chatName = inputMsg.chatName;
                        switch (inputMsg.getType()) {
                            case Register:
                                addToList(inputMsg);
                                writers.put(sender,output);
                                updateAllUserLists();
                                break;
                            case Login:
                                break;
                            case Chat:
                                if (inputMsg.isGroup){ //group chat
                                    String[]receivers = receiver.split(", ");
                                    for (String r:receivers) {
                                        if (!r.equals(sender) && writers.get(r)!=null) {
                                            writers.get(r).writeObject(inputMsg);
                                        }
                                    }
                                } else { //private chat
                                    if (sender.equals(receiver)) {
                                        //self chatting, do nothing
                                    } else {
                                        if (writers.get(receiver) != null) { // if still online
                                            writers.get(receiver).writeObject(inputMsg);
                                        }
                                    }
                                }
                                break;
                            case Logout:
                                break;
                        }
                    }
                }
            } catch (SocketException socketException) {
//                System.err.println("Socket Exception for user " + user.getName());
                closeConnection();
            } catch (DuplicatedUserNameException exception) {
                try {
                    output.writeObject(newMessage("The user name already exists, please try again.",
                            user.getName(), MessageType.Register));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } catch (Exception e){
                System.err.println("Exception in run() method for user: " + user.getName() + e);
            }
        }

        private void updateAllUserLists() throws IOException {//update userList, notify all
            ObjectOutputStream writer;
            for (String receiver : writers.keySet()) {
                writer = writers.get(receiver);
                writer.writeObject(newMessage(userList.keySet().toString(), receiver, MessageType.UpdateUserList));
                writer.reset();
            }
        }

//        private Message changeStatus(Message inputmsg) throws IOException {
//            logger.debug(inputmsg.getName() + " has changed status to  " + inputmsg.getStatus());
//            Message msg = new Message();
//            msg.setName(user.getName());
//            msg.setType(MessageType.STATUS);
//            msg.setMsg("");
//            User userObj = names.get(name);
//            userObj.setStatus(inputmsg.getStatus());
//            write(msg);
//            return msg;
//        }
//
        private void addToList(Message inputMsg) throws IOException, DuplicatedUserNameException {
            String name = inputMsg.getSentBy();
            user = new User(name);
            if (!userList.containsKey(name)) {
                user.setStatus(UserStatus.ONLINE);
                userList.put(name, user);
                System.out.println("[Server] "+ name + " has been added to the list");
                Message msg = newMessage("[Server] You ("+name+") have joined the chat!",
                        name, MessageType.Register);
                output.writeObject(msg);
                output.flush();
            } else {
                throw new DuplicatedUserNameException(name + " is already connected");
            }
        }
        /*
         * Once a user has been disconnected, we close the open connections and remove the writers
         */
        private void removeFromList() throws IOException{
            if (userList != null) {
                userList.remove(user.getName());
                System.out.println("User: " + user.getName() + " has been removed!");
                updateAllUserLists();
            }
        }
        private synchronized void closeConnection(){
            if (output != null){
                writers.remove(user.getName());
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (input != null){
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                removeFromList();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        private Message newMessage(String data, String sendTo, MessageType type){
            return new Message(System.currentTimeMillis(),"server",sendTo,data,type);
        }
    }
}