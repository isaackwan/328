package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class PeersController {
    private Main main;

    @FXML
    private MenuBar menu;
    @FXML
    private TextField peerUri;
    @FXML
    private TableColumn<Peer, String> uriColumn;
    @FXML
    private TableView<Peer> peersTable;
    @FXML
    private AnchorPane anchorPane;

    @FXML
    private void addPeer(ActionEvent event) {
        main.peerRepo.add(new Peer(peerUri.getText()));
        peerUri.setText("");
    }

    public void setMain(Main main) {
        this.main = main;
        peersTable.setItems(main.peerRepo);
        setColor();
    }

    @FXML
    private void initialize() {
        uriColumn.setCellValueFactory(cellData -> cellData.getValue().uri);
        uriColumn.setCellFactory(TextFieldTableCell.forTableColumn());
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

    private void setColor() {
        String str1 = "";
        String str2 = "";
        switch (main.styleNum) {
            case 1:
                str1 = "#fa8072";
                str2 = "#ff0000";
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
                str2 = "#808080";
                break;
        }
        menu.setStyle("-fx-background-color: " + str1 + ";");
        peersTable.setStyle("-fx-background-color: " + str2 + ";");
        anchorPane.setStyle("-fx-background-color: " + str1 + ";");
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
