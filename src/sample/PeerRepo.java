package sample;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Created by isaac on 3/29/17.
 */
public class PeerRepo extends SimpleListProperty<Peer> {
    private final String databasePrefix = "database";
    private final SongRepo songRepo;
    PeerRepo(SongRepo songRepo){
        super(FXCollections.observableArrayList());
        this.songRepo = songRepo;
    }
    public boolean add(Peer peer) {
        boolean rtn = super.add(peer);
        peer.syncRemoteDatabase(songRepo);
        return rtn;
    }
}