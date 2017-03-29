package sample; /**
 * Created by isaac on 3/28/17.
 */
import javax.sound.sampled.*;
import java.io.EOFException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Player {
    private static final Logger LOGGER = Logger.getLogger("Player");
    private static final int perChunkSize = 1024*1024;
    private SourceDataLine line;
    private boolean shouldStop = false;
    public void play(Song song) throws Exception {
        if (line != null && line.isRunning()) {
            LOGGER.log(Level.WARNING, "The player is already playing stuff.");
            return;
        }
        line = AudioSystem.getSourceDataLine(song.getFormat());
        line.open(song.getFormat());
        line.start();
        line.addLineListener(event -> {
            if (event.getType() == LineEvent.Type.STOP) {
                shouldStop = false;
            }
        });
        Thread t = new Thread(new Thread() {
            @Override
            public void run() {
                while(!shouldStop) {
                    byte[] buf = null;
                    try {
                        buf = song.read();
                    } catch (EOFException e) {
                        LOGGER.fine("EOF");
                        break;
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Unknown exception thrown", e);
                    }
                    line.write(buf, 0, buf.length);
                }
                // finished playing
                line = null;
            }
        });
        t.start();
    }

    public void pause() {
        line.stop();
    }

    public void unpause() {
        line.start();
    }

    public void stop() {
        shouldStop = true;
        line.stop();
        line = null;
        shouldStop = false;
    }
}
