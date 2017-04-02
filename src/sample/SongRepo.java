package sample;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

import java.io.*;
import java.util.Iterator;
import java.util.logging.Logger;

public class SongRepo extends SimpleListProperty<Song> {
    private final String databasePrefix = "database";
    SongRepo(){
        super(FXCollections.observableArrayList());
    }

    /**
     * Load the database.json from filesystem into the Song repository
     * @throws Exception
     */
    public void seedFromFilesystem() throws Exception {
        try (BufferedReader database = new BufferedReader(new FileReader(databasePrefix+"/database.txt"))) {
            String line = null;
            String[] config;
            while ((line = database.readLine()) != null) {
                config = line.split(",,");
                if (config.length != 5) {
                    continue;
                }
                add(new LocalSong(databasePrefix+"/"+config[0], config[0], config[2], config[3], config[4]));
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger("SongRepo").info("Database seed file not found. Skipping.");
        }

    }

    public Song getSong(Song song1) throws IndexOutOfBoundsException {
        Iterator<Song> songs = iterator();
        Song song2;
        while(songs.hasNext()) {
            song2 = songs.next();
            if (song1.equals(song2)) {
                return song2;
            }
        }
        throw new IndexOutOfBoundsException();
    }

    /**
     * persist the in-memory database by storing the contents in a local file
     * @throws IOException
     */
    public void persistDatabase() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter("database/database.txt", false));
        Iterator<Song> iterator = iterator();
        while (iterator.hasNext()) {
            Song song = iterator.next();
            if (!(song instanceof LocalSong)) {
                continue;
            }
            writer.append(((LocalSong) song).serialize());
            writer.newLine();
        }
        writer.close();
        Logger.getGlobal().finer("completed writing to database file");
    }
}