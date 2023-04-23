package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Chat;
import cn.edu.sustech.cs209.chatting.common.Emoji;
import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MessageType;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;


public class Controller implements Initializable {
    static HashMap<String, Chat> allChats;
    static String currentChatName;
    @FXML
    public Label currentChatMembers;
    @FXML
    public ListView<String> chatList;
    @FXML
    ListView<Message> chatContentList;

    @FXML
    public TextArea inputArea;
    @FXML
    public Button emoji;
    @FXML
    public Button fileBtn;
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
    public void initialize(URL url, ResourceBundle resourceBundle) { //Thread of JavaFX Application
        try {
            Socket socket = new Socket("localhost", 25250);
            clientController = new ClientController(socket, currentUsername, currentOnlineCnt,
                    chatList, chatContentList, currentChatMembers);
            new Thread(clientController).start();
            loginFrameInitialize();
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
            currentChatMembers.setText(c.getClientViewUsers());
        } else { //new private chat
            allChats.put(name, new Chat(name));
            chatList.getItems().add(name);
            ClientController.sendMessage(newMessage(name, "", MessageType.Chat));
        }
    }
    @FXML
    public void createGroupChat() throws IOException {
        Stage stage = new Stage();
        stage.setTitle("Select users");
        stage.setWidth(280);
        stage.setHeight(400);
        ScrollPane scrollPane = new ScrollPane();// 使用一个滚动板面
        VBox box = new VBox(); // 滚动板面里放行垂直布局， VBox里放多个复选框
        Button button = new Button("OK");
        button.setOnAction(e -> {
            stage.close();
        });

        HashSet<String> select = new HashSet<>();
        for (String t : userList) {
            CheckBox cb = new CheckBox(t);
            if (t.equals(username)) { //the user itself
                select.add(t);
                continue;
            }
            cb.selectedProperty().addListener(new ChangeListener<Boolean>() {
                public void changed(ObservableValue<? extends Boolean> ov,
                                    Boolean old_val, Boolean new_val) {
                    if (cb.isSelected()) {
                        select.add(t);
                    } else {
                        select.remove(t);
                    }
                }
            });
            box.getChildren().add(cb);
            VBox.setMargin(cb,new Insets(10,10,0,10));// 设置间距
        }
        box.getChildren().add(button);
        VBox.setMargin(button,new Insets(10,10,0,10));
        scrollPane.setContent(box);
        Scene scene = new Scene(scrollPane);
        stage.setScene(scene);
        stage.showAndWait();

        if (select.size() > 1) {
            StringBuilder chatName = new StringBuilder();
            List<String>res = select.stream().sorted().limit(3).collect(Collectors.toList());
            for (int i = 0;i < res.size();i++) {
                chatName.append(res.get(i));
                if (i < res.size()-1) {
                    chatName.append(", ");
                }
            }
            if (select.size() > 3) {
                chatName.append("...");
            }
            chatName.append(" (").append(select.size()).append(")");
            String finalChatName = chatName.toString();
            currentChatName = finalChatName;
            String s = select.toString();
            groupChatHandler(finalChatName, s.substring(1, s.length() - 1));
        }
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
    public void groupChatHandler(String finalChatName, String members) throws IOException {
        Chat c = allChats.get(finalChatName);
        if (c != null) {
            chatContentList.getItems().clear();
            chatContentList.getItems().setAll(c.getMessages());
            currentChatMembers.setText(c.getClientViewUsers());
        } else { //new group chat
            Chat groupChat = new Chat(finalChatName, members);
            allChats.put(finalChatName, groupChat);
            chatList.getItems().add(finalChatName);
            Message message = newMessage(members, "", MessageType.Chat);
            message.chatName = finalChatName;
            message.isGroup = true;
            ClientController.sendMessage(message);
        }
    }

    public void setSendFileHandler() {
        fileBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                Stage stage = new Stage();
                FileChooser fileChooser = new FileChooser();
                //设置文件上传类型
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("excel files (*.xlsx)", "*.xlsx");
                fileChooser.getExtensionFilters().add(extFilter);
                File file = fileChooser.showOpenDialog(stage);
                inputArea.setText("📂| " + file.getPath());

                //          业务逻辑代码

//                FileChooser fileChooserSave = new FileChooser();
//                //保存文件类型
//                FileChooser.ExtensionFilter extFilter1 = new FileChooser.ExtensionFilter("excel files (*.xlsx)", "*.xlsx");
//                fileChooserSave.getExtensionFilters().add(extFilter1);
//                File fileSave = fileChooserSave.showSaveDialog(primaryStage);
//                textArea.appendText("\n" + "你保存的文件路径为：" + fileSave.getPath());
            }
        });
    }
    @FXML
    public void showEmojiList() {
        AtomicReference<String> emoji = new AtomicReference<>();
        Stage stage = new Stage();
        ComboBox<String> emojiSel = new ComboBox<>();
        for (String s : Emoji.emoji) {
            emojiSel.getItems().add(s);
        }
        Button okBtn = new Button("OK");
        okBtn.setOnAction(e -> {
            emoji.set(emojiSel.getSelectionModel().getSelectedItem());
            stage.close();
        });
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 20, 20, 20));
        box.getChildren().addAll(emojiSel, okBtn);
        stage.setScene(new Scene(box));
        stage.showAndWait();
        if (emoji.get() != null) {
            inputArea.setText(inputArea.getText()+emoji.get());
        }
    }
    @FXML
    public void doSendChat() throws IOException {
        if (currentChatName == null) {
            generateAlert("Please select or create a chat!");
            return;
        }
        String data = inputArea.getText();
        if (!data.equals("")) {
            Chat c = allChats.get(currentChatName);
            Message message = newMessage(c.getClientViewUsers(), data, MessageType.Chat);
            if (c.isGroup) {
                message.chatName = currentChatName;
                message.isGroup = true;
            }
            c.getMessages().add(message);
            chatContentList.getItems().clear();
            chatContentList.getItems().setAll(c.getMessages());
            currentChatMembers.setText(c.getClientViewUsers());
            inputArea.clear();
            ClientController.sendMessage(message);
        } else {
            generateAlert("Please input something!");
        }
    }
    private static Message newMessage(String sendTo, String data, MessageType type) {
        return new Message(System.currentTimeMillis(), username, sendTo, data, type);
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
            Chat c = allChats.get(currentChatName);
            chatContentList.getItems().clear();
            chatContentList.getItems().setAll(c.getMessages());
            currentChatMembers.setText(c.getClientViewUsers());
            System.out.println(newValue);
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
        private Label currentChatMembers;

        public ClientController(Socket socket, Label currentUserName, Label currentOnlineCnt, ListView<String>chatList,
                                ListView<Message>chatContentList, Label currentChatMembers) {
            this.socket = socket;
            this.currentUsername = currentUserName;
            this.currentOnlineCnt = currentOnlineCnt;
            this.chatList = chatList;
            this.chatContentList = chatContentList;
            this.currentChatMembers = currentChatMembers;
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
                                String[]list = names.substring(1, names.length() - 1).split(", ");
                                if (userList != null && list.length < userList.length) { //A user left
                                    HashSet<String>old = new HashSet<>(Arrays.asList(userList));
                                    HashSet<String>current = new HashSet<>(Arrays.asList(list));
                                    old.removeAll(current);
                                    String removed = old.toString().substring(1, old.toString().length()-1);
                                    Platform.runLater(() -> {
                                        generateAlert(removed + " left the platform👋");
                                    });
                                }
                                userList = list;
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
                                        c = new Chat(chatName, receiver);
                                        c.isGroup = true;
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
                                    if (currentChatName != null && currentChatName.equals(chatName)) {
                                        Chat finalC = c;
                                        Platform.runLater(() -> {
                                            chatContentList.getItems().clear();
                                            chatContentList.getItems().setAll(finalC.getMessages());
                                            currentChatMembers.setText(finalC.getClientViewUsers());
                                        });
                                    }
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

}
