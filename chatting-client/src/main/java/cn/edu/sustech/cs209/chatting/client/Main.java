package cn.edu.sustech.cs209.chatting.client;

import com.sun.glass.ui.Menu;
import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;

public class Main extends Application {
    public static void main(String[] args){
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"));
        stage.setScene(new Scene(fxmlLoader.load()));
        stage.setTitle("Chatting Client");
        stage.setOnCloseRequest(new EventHandler() {
            @Override
            public void handle(Event event) {
                System.exit(0);
            }
        });

        stage.show();
    }
}