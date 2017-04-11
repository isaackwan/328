package sample;

import com.sun.javafx.util.Logging;
import javafx.application.Platform;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

public class DatabaseController {
    private Main main;
    @FXML
    private TableView<Song> songTable;
    @FXML
    private TableColumn<Song, String> nameColumn;
    @FXML
    private TableColumn<Song, String> filenameColumn;
    @FXML
    private TableColumn<Song, String> filesizeColumn;
    @FXML
    private TableColumn<Song, String> singerColumn;
    @FXML
    private TableColumn<Song, String> albumColumn;
    @FXML
    private TableColumn<Song, String> locationColumn;
    @FXML
    private TextField newSongFilename;
    @FXML
    private FilteredList<Song> filteredRepo = null;

    public void setMain(Main main) {
        this.main = main;
        //songTable.setItems(main.songRepo);
        songTable.setItems(filteredRepo = new FilteredList<Song>(main.songRepo, p -> true));
    }

    @FXML
    private void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<Song, String>("name"));
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Song, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Song, String> t) {
                        Song song = t.getTableView().getItems().get(t.getTablePosition().getRow());
                        String newValue = t.getNewValue().isEmpty() ? "N/A" : t.getNewValue();
                        song.setName(newValue);
                    }
                }
        );
        singerColumn.setCellValueFactory(new PropertyValueFactory<Song, String>("singer"));
        singerColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        singerColumn.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Song, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Song, String> t) {
                        Song song = t.getTableView().getItems().get(t.getTablePosition().getRow());
                        String newValue = t.getNewValue().isEmpty() ? "N/A" : t.getNewValue();
                        song.setSinger(newValue);
                    }
                }
        );
        albumColumn.setCellValueFactory(new PropertyValueFactory<Song, String>("album"));
        albumColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        albumColumn.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Song, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Song, String> t) {
                        Song song = t.getTableView().getItems().get(t.getTablePosition().getRow());
                        String newValue = t.getNewValue().isEmpty() ? "N/A" : t.getNewValue();
                        song.setAlbum(newValue);
                    }
                }
        );

        filenameColumn.setCellValueFactory(new PropertyValueFactory<Song, String>("filename"));
        filesizeColumn.setCellValueFactory(new PropertyValueFactory<Song, String>("filesize"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<Song, String>("location"));
        newSongFilename.textProperty().addListener(((observable, oldValue, newValue) -> {
            if (filteredRepo == null) {
                return;
            }
            filteredRepo.setPredicate(song -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                return song.matches(newValue.split(" "));
            });
        }));
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
        if (song == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Please select a song first.");
            alert.showAndWait();
            return;
        }
        main.switchToPlaybackView();
        main.player.play(song);
    }

    @FXML
    private void deleteSong(ActionEvent event) throws Exception {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Are you sure you want to remove the song?");
        alert.setContentText("Note: the current song will be stopped.");
        if (alert.showAndWait().get() != ButtonType.OK) {
            return;
        }
        main.player.stop();
        Song song = songTable.getSelectionModel().getSelectedItem();
        main.songRepo.remove(song);
    }

    @FXML
    private void addSong(ActionEvent event) throws Exception {
        try {
            LocalSong song = new LocalSong(newSongFilename.getText());
            newSongFilename.setText("");
            try {
                main.songRepo.getSong(song);
                Logger.getLogger("DatabaseController").fine("This is an existing song. Bypassed.");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("A same song with the same ID exists already.");
                alert.showAndWait();
            } catch (IndexOutOfBoundsException ex) {
                main.songRepo.add(song);
                Logger.getLogger("DatabaseController").fine("Added song to db");
            }
        } catch (FileNotFoundException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("The specified file is not found.");
            alert.showAndWait();
        }
    }

    @FXML
    private void persistDatabase(ActionEvent event) throws Exception {
        main.songRepo.persistDatabase();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText("Saved!");
        alert.showAndWait();
    }

    @FXML
    private void demo(ActionEvent event) throws Exception {
        Song song = songTable.getSelectionModel().getSelectedItem();
        if (song == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Please select a song first.");
            alert.showAndWait();
            return;
        }
        if (!(song instanceof RemoteSong)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Please select a *REMOTE* song.");
            alert.showAndWait();
            return;
        }
        TextInputDialog dialog = new TextInputDialog();
        dialog.setContentText("Please enter the desired file name:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(filename -> {
            AsyncPieceDownloader.demo((RemoteSong)song, filename).thenAccept((Void v) -> {
                Logger.getLogger("DatabaseController").info("Finished download for demo");
                Platform.runLater(new Runnable() { // another hack for JavaFX. Urgh.
                    public void run() {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setContentText("Download completed.");
                        alert.showAndWait();
                    }
                });
            });
        });
    }

    @FXML
    private void videoStreaming(ActionEvent event) throws Exception {
        Song song = songTable.getSelectionModel().getSelectedItem();
        if (song == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Please select a song first.");
            alert.showAndWait();
            return;
        }
        if (!(song instanceof RemoteSong)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Please select a *REMOTE* song.");
            alert.showAndWait();
            return;
        }
        VideoStreamer streamer = new VideoStreamer((RemoteSong) song);
        Logger.getLogger("DatabaseController").info("Started streaming service");
    }

    @FXML
    private void resetDb(ActionEvent event) {
        main.resetDb();
    }

}
