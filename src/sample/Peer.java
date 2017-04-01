package sample;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by isaac on 3/30/17.
 */
public class Peer {
    protected StringProperty uri;

    public Peer(String uri) {
        this.uri = new SimpleStringProperty(uri);
    }

    public CompletableFuture<Void> syncRemoteDatabase(SongRepo songRepo) {
        AsyncHttpClient asyncHttpClient = new DefaultAsyncHttpClient();
        CompletableFuture<BufferedReader> remote = asyncHttpClient
                                                .prepareGet(uri.get()+"/database.txt")
                                                .execute()
                                                .toCompletableFuture()
                                                .thenApply(res -> {return new InputStreamReader(res.getResponseBodyAsStream());})
                                                .thenApply(reader -> {return new BufferedReader(reader);});
        CompletableFuture<Void> mergeWithDb = remote.thenAccept(database -> {
            String line = null;
            String[] config;
            try {
                RemoteSong song;
                Song songInDb;
                while ((line = database.readLine()) != null) {
                    config = line.split(",,");
                    if (config.length != 5) {
                        continue;
                    }
                    song = new RemoteSong(uri.get()+"/"+config[0], config[0], config[2], config[3], config[4], Long.parseLong(config[1]));
                    try {
                        songInDb = songRepo.getSong(song);
                        if (songInDb instanceof RemoteSong) {
                            ((RemoteSong) songInDb).addUri(uri.get()+"/"+config[0]);
                            Logger.getGlobal().info("detected duplicate song: "+song);
                        } else {
                            Logger.getGlobal().info("skipping local: "+song);
                        }
                    } catch (IndexOutOfBoundsException ex) {
                        Logger.getGlobal().info("new song added: "+song);
                        songRepo.add(song);
                    }
                }
            } catch (Exception e) {
                Logger.getGlobal().log(Level.SEVERE, "An exception was thrown when parsing remote database", e);
            }
        }).exceptionally(ex -> {Logger.getGlobal().log(Level.SEVERE, "Exception during database synchronization", ex);return null;});
        return mergeWithDb;
    }

    public String getUri() {
        return uri.get();
    }

    public StringProperty uriProperty() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri.set(uri);
    }
}
