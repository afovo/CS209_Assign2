package cn.edu.sustech.cs209.chatting.client;

import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Test extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        //
        // Test data
        //
        String[]chats = {"aaa","aaaaaaa"};

        //
        // Set up the model which is two lists of Players and a filter criteria
        //
        ReadOnlyObjectProperty<ObservableList<String>> chatsProperty =
                new SimpleObjectProperty<>(FXCollections.observableArrayList());

        ReadOnlyObjectProperty<FilteredList<String>> viewableChatsProperty =
                new SimpleObjectProperty<FilteredList<String>>(
                        new FilteredList<>(chatsProperty.get()
                        ));


        //
        // Build the UI
        //
        VBox vbox = new VBox();
        vbox.setPadding( new Insets(10));
        vbox.setSpacing(4);

        HBox hbox = new HBox();
        hbox.setSpacing( 2 );

//        ToggleGroup filterTG = new ToggleGroup();

        //
        // The toggleHandler action wills set the filter based on the TB selected
        //
        @SuppressWarnings("unchecked")
        EventHandler<ActionEvent> toggleHandler = (event) -> {
            ToggleButton tb = (ToggleButton)event.getSource();
            Predicate<String> filter = (Predicate<String>)tb.getUserData();
        };

        ToggleButton tbShowAll = new ToggleButton("Show All");
        tbShowAll.setSelected(true);
//        tbShowAll.setToggleGroup( filterTG );
        tbShowAll.setOnAction(toggleHandler);
        tbShowAll.setUserData( (Predicate<String>) (String p) -> true);

        //
        // Create a distinct list of teams from the Player objects, then create
        // ToggleButtons
        //
        List<ToggleButton> tbs = Arrays.stream( chats)
                .map( (name) -> {
                    ToggleButton tb = new ToggleButton( name );
                    tb.setOnAction( toggleHandler );
                    return tb;
                })
                .collect(Collectors.toList());

        hbox.getChildren().add( tbShowAll );
        hbox.getChildren().addAll( tbs );

        //
        // Create a ListView bound to the viewablePlayers property
        //
        ListView<String> lv = new ListView<>();
        lv.itemsProperty().bind( viewableChatsProperty );

        vbox.getChildren().addAll( hbox, lv );

        Scene scene = new Scene(vbox);

        primaryStage.setScene( scene );
        primaryStage.setOnShown((evt) -> {
            chatsProperty.get().addAll(chats);
        });

        primaryStage.show();

    }

    public static void main(String args[]) {
        launch(args);
    }

    static class Player {

        private final String team;
        private final String playerName;
        public Player(String team, String playerName) {
            this.team = team;
            this.playerName = playerName;
        }
        public String getTeam() {
            return team;
        }
        public String getPlayerName() {
            return playerName;
        }
        @Override
        public String toString() { return playerName + " (" + team + ")"; }
    }
}
