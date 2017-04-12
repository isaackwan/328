package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.AnchorPane;
import sun.plugin.javascript.navig.Anchor;

import java.io.IOException;

public class PlaybackController {
    private Main main;
    @FXML
    private MenuBar menu;
    @FXML
    private ToolBar toolBar;
    @FXML
    private AnchorPane leftAnchorPane;
    @FXML
    private AnchorPane rightAnchorPane;
    @FXML
    private Label filename;
    @FXML
    private Label name;
    @FXML
    private Label singer;
    @FXML
    private Label album;
    @FXML
    private Label lyricsLabel;
    @FXML
    private Button pauseBtn;
    @FXML
    private Button unpauseBtn;
    @FXML
    private Button stopBtn;

    @FXML
    private void initialize() {
    }

    public void setMain(Main main) {
        this.main = main;
        filename.textProperty().bind(main.player.filename);
        name.textProperty().bind(main.player.name);
        singer.textProperty().bind(main.player.singer);
        album.textProperty().bind(main.player.album);
        lyricsLabel.textProperty().bind(main.player.lyrics);
        lyricsLabel.visibleProperty().bind(main.player.active);
        pauseBtn.visibleProperty().bind(main.player.shouldStop.not().and(main.player.active));
        unpauseBtn.visibleProperty().bind(main.player.shouldStop.and(main.player.active));
        stopBtn.visibleProperty().bind(main.player.active);
        setColor();
    }

    @FXML
    private void switchToPeersView(ActionEvent event) throws IOException {
        main.switchToPeersView();
    }

    @FXML
    private void switchToPlaybackView(ActionEvent event) throws IOException {
        main.switchToPlaybackView();
    }

    @FXML
    private void switchToDatabaseView(ActionEvent event) throws IOException {
        main.switchToDatabaseView();
    }

    @FXML
    private void pause(ActionEvent event) throws IOException {
        main.player.pause();
    }

    @FXML
    private void unpause(ActionEvent event) throws IOException {
        main.player.unpause();
    }

    @FXML
    private void stop(ActionEvent event) throws IOException {
        main.player.stop();
    }

    @FXML
    private void resetDb(ActionEvent event) throws IOException {
        stop(event);
        main.resetDb();
    }

    private void setColor() {
        String str1 = "";
        String str2 = "";
        switch (main.styleNum) {
            case 1:
                str1 = "#fa8072";
                str2 = "#ffa07a";
                break;
            case 2:
                str1 = "#f0e68c";
                str2 = "#ffd700";
                break;
            case 3:
                str1 = "#90ee90";
                str2 = "#32cd32";
                break;
            case 4:
                str1 = "#b0e0e6";
                str2 = "#87cefa";
                break;
            case 5:
                str1 = "#a9a9a9";
                str2 = "#d3d3d3";
                break;
        }
        menu.setStyle("-fx-background-color: " + str1 + ";");
        leftAnchorPane.setStyle("-fx-background-color: " + str2 + ";");
        rightAnchorPane.setStyle("-fx-background-color: " + str2 + ";");
        toolBar.setStyle("-fx-background-color: " + str1 + ";");
    }

    @FXML
    private void changeColor(){
        switch(main.styleNum){
            case 1: main.styleNum = 2;
                setColor();
                break;
            case 2: main.styleNum = 3;
                setColor();
                break;
            case 3: main.styleNum = 4;
                setColor();
                break;
            case 4: main.styleNum = 5;
                setColor();
                break;
            case 5: main.styleNum = 1;
                setColor();
                break;
        }
    }
}
