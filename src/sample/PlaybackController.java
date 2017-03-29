package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.io.IOException;

public class PlaybackController {
    private Main main;

    public void setMain(Main main) {
        this.main = main;
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

}
