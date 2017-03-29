package sample;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    public SongRepo songRepo = new SongRepo();
    public PeerRepo peerRepo = new PeerRepo(songRepo);
    private Stage primaryStage;
    public Player player = new Player();
    private FileServer fileServer = new FileServer();

    public Main() throws Exception {
        songRepo.seedFromFilesystem();
        /*synchronized (songRepo) {
            peerRepo.add(new Peer("http://localhost:2015"));
        }
        synchronized (songRepo) {
            peerRepo.add(new Peer("http://localhost:2016"));
        }*/
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("database.fxml"));
        Scene scene = new Scene(loader.load(), 800, 600);
        DatabaseController controller = loader.getController();
        controller.setMain(this);
        primaryStage.setTitle("Music Database");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.sizeToScene();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    void switchToPeersView() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("peers.fxml"));
        Scene scene = new Scene(loader.load(), 800, 600);
        PeersController controller = loader.getController();
        controller.setMain(this);
        primaryStage.setTitle("Peers Management");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.sizeToScene();
    }

    void switchToDatabaseView() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("database.fxml"));
        Scene scene = new Scene(loader.load(), 800, 600);
        DatabaseController controller = loader.getController();
        controller.setMain(this);
        primaryStage.setTitle("Music Database");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.sizeToScene();
    }

    void switchToPlaybackView() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("playback.fxml"));
        Scene scene = new Scene(loader.load(), 800, 600);
        PlaybackController controller = loader.getController();
        controller.setMain(this);
        primaryStage.setTitle("Playback");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.sizeToScene();
    }
}
