package sample;

import javafx.beans.property.StringProperty;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
    public String getFilename() {return filename;}
    abstract public long getFilesize() throws IOException;
    abstract public String getLocation();
    public String toString() {
        return "Song: " + this.name.get();
    }
    abstract public byte[] read() throws IOException, InterruptedException;
    abstract public String getPath();
    abstract public List<String> getUris();

    /**
     * @return a reference to a buffer for the next chunk
     * @throws Exception
     */
    abstract public AudioFormat getFormat() throws Exception;

    /**
     * close the file/Internet handle for downloading the file
     * @throws IOException
     */
    abstract public void close() throws IOException;

    /**
     * starts the pre-loading of the song
     */
    abstract public void start();
    abstract public CompletableFuture<InputStream> lyrics() throws IOException;

    /**
     * determines if, given an array of keywords, the file fits the given criteria
     * @param keywords
     * @return whether the file fits the given criteria
     */
    public boolean matches(String[] keywords) {
        for (String keyword : keywords) {
            final boolean foundInName = getName().contains(keyword);
            final boolean foundInSinger = getSinger().contains(keyword);
            if (!foundInSinger && !foundInName) {
                return false;
            }
        }
        return true;
    }

    public static String getExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int dotPosition = filename.lastIndexOf(".");
        String extension = "";
        if (dotPosition != -1) {
            extension = filename.substring(dotPosition);
        }
        return extension;
    }
}
