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

    private static class ServerController extends Thread {
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
                                notifyAll(newMessage(userList.keySet().toString(),MessageType.UpdateUserList));
                                break;
                            case Login:
                                break;
                            case Chat:
                                if (inputMsg.isGroup){//group chat
                                    String[]receivers = receiver.split(",");
                                    for (String r:receivers) {
                                        writers.get(r).writeObject(inputMsg);
                                    }
                                } else {
                                    if (sender.equals(receiver)) {

                                    } else {
                                        writers.get(receiver).writeObject(inputMsg);
                                    }
                                }
                                break;
                            case Logout:
                                break;
                        }
                    }
                }
            } catch (SocketException socketException) {
                System.err.println("Socket Exception for user " + user.getName());
            } catch (DuplicatedUserNameException exception) {
                try {
                    output.writeObject(newMessage("The user name already exists, please try again.",MessageType.Register));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } catch (Exception e){
                System.err.println("Exception in run() method for user: " + user.getName() + e);
            } finally {
//                closeConnections();
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

//        private Message removeFromList() throws IOException {
//            logger.debug("removeFromList() method Enter");
//            Message msg = new Message();
//            msg.setMsg("has left the chat.");
//            msg.setType(MessageType.DISCONNECTED);
//            msg.setName("SERVER");
//            msg.setUserlist(names);
//            write(msg);
//            logger.debug("removeFromList() method Exit");
//            return msg;
//        }

        private void addToList(Message inputMsg) throws IOException, DuplicatedUserNameException {
            String name = inputMsg.getSentBy();
            user = new User(name);
            if (!userList.containsKey(name)) {
                user.setStatus(UserStatus.ONLINE);
                userList.put(name, user);
                System.out.println("[Server] "+ name + " has been added to the list");
                Message msg = newMessage("[Server] You ("+name+") have joined the chat!", MessageType.Register);
                output.writeObject(msg);
                output.flush();
            } else {
                throw new DuplicatedUserNameException(name + " is already connected");
            }
        }

        private Message newMessage(String data, MessageType type){
            return new Message(System.currentTimeMillis(),"server",user.getName(),data,type);
        }

        private void notifyAll(Message msg) throws IOException {//notify All
            for (ObjectOutputStream writer : writers.values()) {
                writer.writeObject(msg);
                writer.reset();
            }
        }

        /*
         * Once a user has been disconnected, we close the open connections and remove the writers
         */
//        private synchronized void closeConnections()  {
//            logger.debug("closeConnections() method Enter");
//            logger.info("HashMap names:" + names.size() + " writers:" + writers.size() + " usersList size:" + users.size());
//            if (name != null) {
//                names.remove(name);
//                logger.info("User: " + name + " has been removed!");
//            }
//            if (user != null){
//                users.remove(user);
//                logger.info("User object: " + user + " has been removed!");
//            }
//            if (output != null){
//                writers.remove(output);
//                logger.info("Writer object: " + user + " has been removed!");
//            }
//            if (is != null){
//                try {
//                    is.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            if (os != null){
//                try {
//                    os.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            if (input != null){
//                try {
//                    input.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            try {
//                removeFromList();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            logger.info("HashMap names:" + names.size() + " writers:" + writers.size() + " usersList size:" + users.size());
//            logger.debug("closeConnections() method Exit");
//        }
    }
}