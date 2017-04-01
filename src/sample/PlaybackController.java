package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.IOException;

public class PlaybackController {
    private Main main;
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
        pauseBtn.visibleProperty().bind(main.player.shouldStop.not().and(main.player.active));
        unpauseBtn.visibleProperty().bind(main.player.shouldStop.and(main.player.active));
        stopBtn.visibleProperty().bind(main.player.active);
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
}
