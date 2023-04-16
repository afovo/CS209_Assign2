package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MessageType;
import cn.edu.sustech.cs209.chatting.common.User;
import cn.edu.sustech.cs209.chatting.common.UserStatus;
import cn.edu.sustech.cs209.chatting.server.Exceptions.DuplicatedUserNameException;


import java.net.*;
import java.io.*;
import java.util.*;

public class Main {
    public static HashMap<String,User> userList = new HashMap<>();
    private static HashSet<ObjectOutputStream> writers = new HashSet<>();

    public static void main(String[] args) throws IOException {
        System.out.println("-----Starting server-----");
        ServerSocket server = new ServerSocket(25250);
        MessageType a = MessageType.Logout;
        System.out.println(a);
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
        private InputStream is;
        private OutputStream os;
        private ObjectOutputStream output;

        public ServerController(Socket socket) throws IOException {
            this.socket = socket;
        }

        public void run() {
            System.out.println("[Server]Attempting to connect a user...");
            try {
                is = socket.getInputStream();
                input = new ObjectInputStream(is);
                os = socket.getOutputStream();
                output = new ObjectOutputStream(os);

//                Message firstMessage = (Message) input.readObject();
//                checkDuplicateUsername(firstMessage);
//                writers.add(output);
//                sendNotification(firstMessage);
//                addToList();

                while (socket.isConnected()) {
                    Message inputMsg = (Message) input.readObject();
                    if (inputMsg != null) {
                        switch (inputMsg.getType()) {
                            case Register:
                                addToList(inputMsg);
                                writers.add(output);
                                break;
                            case Login:
                                break;
                            case Chat:
                                break;
                            case Logout:
                                break;
                        }
                    }
                }
            } catch (SocketException socketException) {
                System.err.println("Socket Exception for user " + user.getName());
            } catch (DuplicatedUserNameException exception) {
                System.err.println(exception);
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
//        private synchronized void checkDuplicateUsername(Message firstMessage) throws DuplicatedUserNameException {
//            logger.info(firstMessage.getName() + " is trying to connect");
//            if (!names.containsKey(firstMessage.getName())) {
//                this.name = firstMessage.getName();
//                user = new User();
//                user.setName(firstMessage.getName());
//                user.setStatus(Status.ONLINE);
//                user.setPicture(firstMessage.getPicture());
//
//                users.add(user);
//                names.put(name, user);
//
//                logger.info(name + " has been added to the list");
//            } else {
//                logger.error(firstMessage.getName() + " is already connected");
//                throw new DuplicateUsernameException(firstMessage.getName() + " is already connected");
//            }
//        }
//
//        private Message sendNotification(Message firstMessage) throws IOException {
//            Message msg = new Message();
//            msg.setMsg("has joined the chat.");
//            msg.setType(MessageType.NOTIFICATION);
//            msg.setName(firstMessage.getName());
//            msg.setPicture(firstMessage.getPicture());
//            write(msg);
//            return msg;
//        }
//
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

        /*
         * For displaying that a user has joined the server
         */
        private void addToList(Message inputMsg) throws IOException, DuplicatedUserNameException {
            String name = inputMsg.getSentBy();
            if (!userList.containsKey(name)) {
                user = new User(name);
                user.setStatus(UserStatus.ONLINE);
                userList.put(name, user);
                System.out.println("[Server]"+ name + " has been added to the list");
                Message msg = new Message(System.currentTimeMillis(),"server",name,
                        "[Server]"+ name + "has joined the chat", MessageType.Register);
                write(msg);
            } else {
                System.err.println(name + " is already connected");
                throw new DuplicatedUserNameException(name + " is already connected");
            }
        }

        /*
         * Creates and sends a Message type to the listeners.
         */
        private void write(Message msg) throws IOException {
            for (ObjectOutputStream writer : writers) {
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