package sample;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class DatabaseController {
    private Main main;
    @FXML
    private TableView<Song> songTable;
    @FXML
    private TableColumn<Song, String> nameColumn;

    public void setMain(Main main) {
        this.main = main;
        songTable.setItems(main.songRepo);
    }

    @FXML
    private void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<Song, String>("name"));
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
    private void playSong(ActionEvent event) throws Exception {
        Song song = songTable.getSelectionModel().getSelectedItem();
        main.switchToPlaybackView();
        main.player.play(song);
    }

    @FXML
    private void persistDatabase(ActionEvent event) throws Exception {
        main.songRepo.persistDatabase();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Persisting Database");
        alert.setHeaderText(null);
        alert.setContentText("Saved!");
        alert.showAndWait();
    }

}
