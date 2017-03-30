package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;

import java.io.IOException;

public class PeersController {
    private Main main;

    @FXML
    private TextField peerUri;
    @FXML
    private TableColumn<Peer, String> uriColumn;
    @FXML
    private TableView<Peer> peersTable;

    @FXML
    private void addPeer(ActionEvent event) {
        main.peerRepo.add(new Peer(peerUri.getText()));
        peerUri.setText("");
    }

    public void setMain(Main main) {
        this.main = main;
        peersTable.setItems(main.peerRepo);
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

}
