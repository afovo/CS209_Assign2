package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Chat;
import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MessageType;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;



public class Controller implements Initializable {
    static HashMap<String, Chat> allChats;
    static String currentChatName;
    @FXML
    public ListView<String> chatList;
    @FXML
    ListView<Message> chatContentList;

    @FXML
    public TextArea inputArea;
    @FXML
    public Button send;
    @FXML
    public Label currentUsername;
    @FXML
    public Label currentOnlineCnt;

    static String username;
    static String[]userList;

    ClientController clientController; //Thread for message sending/receiving

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) { //JavaFX Application Thread
        try {
            Socket socket = new Socket("localhost", 25250);
            clientController = new ClientController(socket, currentUsername, currentOnlineCnt, chatList, chatContentList);
            new Thread(clientController).start();
            loginFrameInitialize();
            //ToDo: local history
            ListSelectListener chatListener = new ListSelectListener();
            chatList.getSelectionModel().selectedItemProperty().addListener(chatListener);
            chatContentList.setCellFactory(new MessageCellFactory());
            allChats = new HashMap<>();
        } catch (IOException e) {
            Platform.runLater(() -> {
                generateAlert("Cannot connect to the Server :(");
            });
            throw new RuntimeException(e);
        }
    }

    private void loginFrameInitialize() {
        // Create the custom dialog.
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Login Dialog");
        dialog.setHeaderText("CS209 Chatting Platform");

        // Set the button types.
        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        // Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField inputName = new TextField();
        inputName.setPromptText("Username");

        grid.add(new Label("Username:"), 0, 0);
        grid.add(inputName, 1, 0);

        // Enable/Disable login button depending on whether a username was entered.
        Button loginButton = (Button) dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);

        // Do some validation (using the Java 8 lambda syntax).
        inputName.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });
        dialog.getDialogPane().setContent(grid);

        // Request focus on the username field by default.
        Platform.runLater(inputName::requestFocus);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get().getButtonData().equals(ButtonBar.ButtonData.OK_DONE)) { // 单击了确定按钮OK_DONE
            if (inputName.getText() != null) {
                username = inputName.getText();
                try {
                    ClientController.sendMessage(newMessage("", "", MessageType.Register));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } else { // 单击了取消按钮CANCEL_CLOSE
            Platform.exit();
        }
    }

    private static void generateAlert(String info) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information Dialog");
        alert.setHeaderText(null);
        alert.setContentText(info);
        alert.showAndWait();
    }
    @FXML
    public void createPrivateChat() throws IOException {
        AtomicReference<String> user = new AtomicReference<>();

        Stage stage = new Stage();
        ComboBox<String> userSel = new ComboBox<>();
        for (String s : userList) {
            userSel.getItems().add(s);
        }
        Button okBtn = new Button("OK");
        okBtn.setOnAction(e -> {
            user.set(userSel.getSelectionModel().getSelectedItem());
            stage.close();
        });
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 20, 20, 20));
        box.getChildren().addAll(userSel, okBtn);
        stage.setScene(new Scene(box));
        stage.showAndWait();

        String name = user.get();
        currentChatName = name;
        if (name != null) {
            privateChatHandler(name);
        }
    }
    public void privateChatHandler(String name) throws IOException {
        Chat c = allChats.get(name);
        if (c != null) {
            chatContentList.getItems().clear();
            chatContentList.getItems().setAll(c.getMessages());
        } else { //new private chat
            allChats.put(name, new Chat(name));
            chatList.getItems().add(name);
            ClientController.sendMessage(newMessage(name, "", MessageType.Chat));
        }
    }

    @FXML
    public void createGroupChat() {
        /*
         * A new dialog should contain a multi-select list, showing all user's name.
         * You can select several users that will be joined in the group chat, including yourself.
         * <p>
         * The naming rule for group chats is similar to WeChat:
         * If there are > 3 users: display the first three usernames, sorted in lexicographic order, then use ellipsis with the number of users, for example:
         * UserA, UserB, UserC... (10)
         * If there are <= 3 users: do not display the ellipsis, for example:
         * UserA, UserB (2)
         */
    }
    private static Message newMessage(String sendTo, String data, MessageType type) {
        return new Message(System.currentTimeMillis(), username, sendTo, data, type);
    }
    @FXML
    public void doSendChat() throws IOException {
        String data = inputArea.getText();
        if (!data.equals("")) {
            Chat c = allChats.get(currentChatName);
            Message message = newMessage(c.getClientViewUsers(), data, MessageType.Chat);
            c.getMessages().add(message);
            chatContentList.getItems().clear();
            chatContentList.getItems().setAll(c.getMessages());
            inputArea.clear();
            ClientController.sendMessage(message);
        } else {
            generateAlert("Please input something!");
        }
    }

    private static class ClientController implements Runnable {
        Socket socket;
        private static ObjectOutputStream output;
        private static ObjectInputStream input;
        private Label currentUsername;
        private Label currentOnlineCnt;
        private ListView<String> chatList;
        private ListView<Message> chatContentList;

        public ClientController(Socket socket, Label currentUserName, Label currentOnlineCnt, ListView<String>chatList,
                                ListView<Message>chatContentList) {
            this.socket = socket;
            this.currentUsername = currentUserName;
            this.currentOnlineCnt = currentOnlineCnt;
            this.chatList = chatList;
            this.chatContentList = chatContentList;
        }

        public static void sendMessage(Message msg) throws IOException {
            output.writeObject(msg);
            output.flush();
        }

        @Override
        public void run() {
            try {
                output = new ObjectOutputStream(socket.getOutputStream());
                input = new ObjectInputStream(socket.getInputStream());
                while (socket.isConnected()) {
                    Message message;
                    message = (Message) input.readObject();
                    if (message != null) {
                        System.out.println("Message received:" + message.getData() + " MessageType:" + message.getType());
                        String sender = message.getSentBy();
                        String receiver = message.getSendTo();
                        String chatName = message.chatName;
                        switch (message.getType()) {
                            case Register:
                                Platform.runLater(() -> {
                                    generateAlert(message.getData());
                                    if (message.getData().equals("The user name already exists, please try again.")) {
                                        Platform.exit();
                                        System.exit(-1);
                                    } else {
                                        currentUsername.setText(username);
                                    }
                                });
                                break;
                            case Login:
                                break;
                            case UpdateUserList:
                                String names = message.getData();
                                userList = names.substring(1, names.length() - 1).split(", ");
                                Platform.runLater(() -> {
                                    currentOnlineCnt.setText("Online: " + userList.length);
                                });
                                break;
                            case Chat:
                                if (!message.isGroup) {
                                    chatName = sender;
                                }
                                Chat c = allChats.get(chatName);
                                String finalChatName = chatName;
                                if (c == null) { //new chat
                                    if (message.isGroup){ //group
                                        c = new Chat(chatName,receiver);
                                    } else { //private
                                        c = new Chat(chatName);
                                    }
                                    allChats.put(chatName, c);
                                    Platform.runLater(() -> {
                                        generateAlert("[New Chat] " + finalChatName);
                                        chatList.getItems().add(finalChatName);
                                    });
                                } else { //existing chat
                                    c.getMessages().add(message);
                                    Platform.runLater(() -> {
                                        generateAlert("[New Message] " + finalChatName);
                                    });
                                }

                                if (currentChatName == null) { // if no current chat, update the chatContent
                                    currentChatName = chatName;
                                    Chat finalC = c;
                                    Platform.runLater(() -> {
                                        chatContentList.getItems().clear();
                                        chatContentList.getItems().setAll(finalC.getMessages());
                                    });
                                }
                                break;
                            case Logout:
                                break;
                            default:
                                break;
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                Platform.runLater(() -> {
                    generateAlert("The server has been shut down :(");
                    Platform.exit();
                });
            }
        }
    }

    private class MessageCellFactory implements Callback<ListView<Message>, ListCell<Message>> {
        @Override
        public ListCell<Message> call(ListView<Message> param) {
            return new ListCell<Message>() {

                @Override
                public void updateItem(Message msg, boolean empty) {
                    super.updateItem(msg, empty);
                    if (empty || Objects.isNull(msg)) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }

                    HBox wrapper = new HBox();
                    Label nameLabel = new Label(msg.getSentBy());
                    Label msgLabel = new Label(msg.getData());

                    nameLabel.setPrefSize(50, 20);
                    nameLabel.setWrapText(true);
                    nameLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

                    if (username.equals(msg.getSentBy())) {
                        wrapper.setAlignment(Pos.TOP_RIGHT);
                        wrapper.getChildren().addAll(msgLabel, nameLabel);
                        msgLabel.setPadding(new Insets(0, 20, 0, 0));
                    } else {
                        wrapper.setAlignment(Pos.TOP_LEFT);
                        wrapper.getChildren().addAll(nameLabel, msgLabel);
                        msgLabel.setPadding(new Insets(0, 0, 0, 20));
                    }

                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setGraphic(wrapper);
                }
            };
        }
    }
    private class ListSelectListener implements ChangeListener<Object> {
        @Override
        public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
            currentChatName = (String) newValue;
            chatContentList.getItems().clear();
            chatContentList.getItems().setAll(allChats.get(currentChatName).getMessages());
            System.out.println(newValue);
        }
    }
}
