package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

public class Main extends Application {
    public static int styleNum = 1;
    public SongRepo songRepo = new SongRepo();
    public PeerRepo peerRepo = new PeerRepo(songRepo);
    private Stage primaryStage;
    public Player player = new Player();
    private FileServer fileServer = new FileServer(Optional.ofNullable(System.getenv("CSCI3280_PORT")).orElse("9001"));

    /**
     * constructor for the Main class which is created by the JavaFX runtime automatically.
     * @throws Exception
     */
    public Main() throws Exception {
        songRepo.seedFromFilesystem();
        StringProperty str = player.filename;

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
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent e) {
                Logger.getLogger("Main").info("Caught windows closing event, going to exit() the whole thing..");
                Platform.exit();
                System.exit(0);
            }
        });

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

    /**
     * resets both the song & peers databases. Does not reseed from database.
     */
    void resetDb() {
        songRepo.clear();
        peerRepo.clear();
        Logger.getLogger("Main").info("Finished resetting both databases.");
    }
}
