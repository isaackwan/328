package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.io.IOException;

public class VideoController {
    private Main main;
    @FXML
    private Button stopBtn;

    public void setMain(Main main) {
        this.main = main;
        stopBtn.setDisable(false);
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
    private void resetDb(ActionEvent event) {
        main.resetDb();
    }

    @FXML
    private void stop(ActionEvent event) {
        if (main.videoStreamer != null) {
            main.videoStreamer.stop();
            stopBtn.setDisable(true);
        }
    }
}
