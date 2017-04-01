package sample; /**
 * Created by isaac on 3/28/17.
 */

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.io.EOFException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Player {
    private SourceDataLine line;
    private Thread playbackThread;
    public final BooleanProperty shouldStop = new SimpleBooleanProperty(false);
    public final BooleanProperty active = new SimpleBooleanProperty(false);
    public final StringProperty name = new SimpleStringProperty("");
    public final StringProperty filename = new SimpleStringProperty("");
    public final StringProperty singer = new SimpleStringProperty("");
    public final StringProperty album = new SimpleStringProperty("");

    public void play(Song song) throws Exception {
        if (line != null && line.isRunning()) {
            Logger.getLogger("Player").log(Level.WARNING, "The player is already playing stuff.");
            return;
        } else if (line != null) {
            line.flush();
            line.close();
            line = null;
        }
        name.bind(song.nameProperty());
        filename.setValue(song.filename);
        singer.bind(song.singerProperty());
        album.bind(song.albumProperty());
        active.set(true);

        synchronized (shouldStop) {
            shouldStop.set(false);
        }
        line = AudioSystem.getSourceDataLine(song.getFormat());
        line.open(song.getFormat());
        line.start();
        playbackThread = new Thread(new Thread() {
            @Override
            public void run() {
                byte[] buf = null;
                while(true) {
                    synchronized (shouldStop) {
                        while(shouldStop.get()) {
                            try {
                                Logger.getLogger("Player").info("Pausing playback");
                                shouldStop.wait();
                                Logger.getLogger("Player").info("Resuming playback");
                            } catch (InterruptedException e) {Logger.getLogger("Player").warning("while waiting for write signal, the thread got interrupted");}
                        }
                    }
                    try {
                        buf = song.read();
                        line.write(buf, 0, buf.length);
                    } catch (EOFException e) {
                        Logger.getLogger("Player").info("EOF reached in Player");
                        break;
                    } catch (Exception e) {
                        Logger.getLogger("Player").log(Level.SEVERE, "Unknown exception thrown", e);
                    }
                }
                /*line.drain();
                Logger.getLogger("Player").info("Player ended, cleaning up...");
                line.close();
                line = null;
                active.set(false);*/
                resetLabels();
                // finished playing
                //line = null;
            }
        });
        playbackThread.start();
    }

    public void pause() {
        synchronized (shouldStop) {
            shouldStop.set(true);
        }
        if (line != null) {
            line.stop();
        }
    }

    public void unpause() {
        if (line != null) {
            line.start();
        }
        synchronized (shouldStop) {
            shouldStop.set(false);
            shouldStop.notifyAll();
        }
    }

    public void stop() {
        if (playbackThread != null) {
            playbackThread.stop();
        }
        if (line != null) {
            line.flush();
            line.close();
        }
        line = null;
        active.set(false);
    }

    private void resetLabels() {
        name.unbind();
        name.set("");
        filename.unbind();
        filename.set("");
        singer.unbind();
        singer.set("");
        album.unbind();
        album.set("");
    }
}
