package sample; /**
 * Created by isaac on 3/28/17.
 */

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.io.EOFException;
import java.io.File;
import java.util.concurrent.CompletableFuture;
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
    public final StringProperty lyrics = new SimpleStringProperty("");
    private final LyricsDisplay lyricsDisplay = new LyricsDisplay(lyrics);
    private Song songPlaying = null;
    private MediaPlayer mediaPlayer;

    /**
     * loads and immediately starts playing stuff
     */
    public void play(Song song) throws Exception {
        String songExtension = song.getExtension();
        Logger.getLogger("Player").info("Song Extension: "+songExtension);
        songPlaying = song;
        if (active.get() || (line != null && line.isRunning())) {
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
        final CompletableFuture<Void> loadLyrics = song.lyrics().thenAccept(stream -> lyricsDisplay.loadAndStop(stream));
        if (songExtension.equals(".wav") ) {
            synchronized (shouldStop) {
                shouldStop.set(false);
            }
            song.start();
            line = AudioSystem.getSourceDataLine(song.getFormat());
            line.open(song.getFormat());
            line.start();
            playbackThread = new Thread(new Thread() {
                @Override
                public void run() {
                    byte[] buf = null;
                    loadLyrics.join();
                    loadLyrics.thenAcceptAsync((Void v) -> lyricsDisplay.start()).exceptionally(ex -> {
                        Logger.getLogger("Player").log(Level.WARNING, "Failed to load lyrics", ex);
                        return null;
                    });
                    while (true) {
                        synchronized (shouldStop) {
                            while (shouldStop.get()) {
                                try {
                                    Logger.getLogger("Player").info("Pausing playback");
                                    shouldStop.wait();
                                    Logger.getLogger("Player").info("Resuming playback");
                                } catch (InterruptedException e) {
                                    Logger.getLogger("Player").warning("while waiting for write signal, the thread got interrupted");
                                }
                            }
                        }
                        try {
                            buf = song.read();
                            //Logger.getLogger("Player").info("Length of byte:"+ new String(buf));
                            line.write(buf, 0, buf.length);
                        } catch (IllegalArgumentException e) {
                            final String dataToBeWrittenInHex = new String(buf);
                            Logger.getLogger("Player").log(Level.WARNING, "Invalid data supplied. The data: " + dataToBeWrittenInHex, e);
                        } catch (EOFException e) {
                            Logger.getLogger("Player").info("EOF reached in Player");
                            break;
                        } catch (Exception e) {
                            Logger.getLogger("Player").log(Level.SEVERE, "Unknown exception thrown", e);
                        }
                    }
                    line.drain();
                    Logger.getLogger("Player").info("WAV player ended, cleaning up...");
                    line.close();
                    line = null;
                    active.set(false);
                    resetLabels();
                }
            });
            try {
                playbackThread.setDaemon(true);
            } catch (Exception ex) {
                Logger.getLogger("Player").log(Level.WARNING, "Failed to set playback thread as daemon.", ex);
            }
            playbackThread.start();
        } else if (songExtension.equals(".mp3") ) {
            String bip = song.getPath();
            Media hit;
            if (song instanceof LocalSong) {
                 hit = new Media(new File(bip).toURI().toString());
            } else if (song instanceof RemoteSong){
                //Logger.getLogger("Player").info("song.getUris:"+song.getUris().toString());
                AsyncPieceDownloader.downloadAsFile((RemoteSong)song, "_temp.mp3", 1024*700).join();
                hit = new Media(new File("_temp.mp3").toURI().toString());
            } else {
                throw new RuntimeException("The song is neither local or remote");
            }
            mediaPlayer = new MediaPlayer(hit);
            mediaPlayer.play();
            loadLyrics.join();
            loadLyrics.thenAcceptAsync((Void v) -> lyricsDisplay.start()).exceptionally(ex -> {
                Logger.getLogger("Player").log(Level.WARNING, "Failed to load lyrics", ex);
                return null;
            });
            final Runnable mp3EndCleanup = new Runnable() {
                @Override
                public void run() {
                    Logger.getLogger("Player").info("MP3 Player ended, cleaning up...");
                    if (song instanceof RemoteSong) {
                        Logger.getLogger("Player").info("This is a remote song. Deleting _temp.mp3");
                        (new File("_temp.mp3")).delete();
                    }
                    active.set(false);
                    resetLabels();
                }
            };
            mediaPlayer.setOnEndOfMedia(mp3EndCleanup);
            mediaPlayer.setOnStopped(mp3EndCleanup);
        } else {
            songPlaying = null;
            active.set(false);
            throw new IllegalArgumentException("This is neither mp3 or wav");
        }
    }

    public void pause() {
        synchronized (shouldStop) {
            shouldStop.set(true);
        }
        if (songPlaying.getExtension().equals(".wav")) {
            if (line != null) {
                line.stop();
            }
        } else if (songPlaying.getExtension().equals(".mp3")) {
            if (mediaPlayer != null) {
                mediaPlayer.pause();
            }
        }
        lyricsDisplay.pause();
    }

    public void unpause() {
        synchronized (shouldStop) {
            shouldStop.set(false);
            shouldStop.notifyAll();
        }
        if (songPlaying.getExtension().equals(".wav")) {
            if (line != null) {
                line.start();
            }
        } else if (songPlaying.getExtension().equals(".mp3")) {
            if (mediaPlayer != null) {
                mediaPlayer.play();
            }
        }
        lyricsDisplay.unpause();
    }

    public void stop() {
        if (songPlaying == null) {
            return;
        }
        if (songPlaying.getExtension().equals(".wav")) {
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

        if(songPlaying.getExtension().equals(".mp3")) {
            mediaPlayer.stop();
            songPlaying = null;
            active.set(false);
        }
        lyrics.set("");
    }

    /**
     * reset StringProperties
     */
    private void resetLabels() {
        Platform.runLater(new Runnable(){ // working around bug: http://stackoverflow.com/a/32489845/1348400
            @Override
            public void run() {
                name.unbind();
                name.set("");
                filename.unbind();
                filename.set("");
                singer.unbind();
                singer.set("");
                album.unbind();
                album.set("");
            }
        });
    }
}
