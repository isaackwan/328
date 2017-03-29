package sample;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created by isaac on 3/29/17.
 */
public abstract class Song {
    protected StringProperty name;
    protected StringProperty singer;
    protected StringProperty album;
    protected String filename;

    @Override
    public int hashCode() {return filename.hashCode();}

    public boolean equals(Song song2) {
        return this.filename.equals(song2.filename);
    }
    public String getName() {
        return name.get();
    }
    public void setName(String name) {
        this.name.set(name);
    }
    public StringProperty nameProperty() {return name;}
    public String getSinger() {return singer.get();}
    public StringProperty singerProperty() {return singer;}
    public void setSinger(String singer) {this.singer.set(singer);}
    public String getAlbum() {return album.get();}
    public StringProperty albumProperty() {return album;}
    public void setAlbum(String album) {this.album.set(album);}
    public String toString() {
        return "Song: " + this.name.get();
    }
    abstract public byte[] read() throws IOException, InterruptedException;
    abstract public AudioFormat getFormat() throws Exception;
    abstract public void close() throws IOException;

}
