package sample;

import javafx.application.Platform;
import javafx.beans.property.StringProperty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An abstract Lyrics Displayer. Usually only one is needed for one player, for an infinite amount of songs.
 */
public class LyricsDisplay {
    private BufferedReader text;
    private final StringProperty output;
    private final Timer timer = new Timer();
    private Instant startTime;
    private static final Pattern pattern = Pattern.compile("^\\[(\\d\\d):(\\d\\d).(\\d\\d)\\](.*)$");
    public LyricsDisplay(StringProperty output) {
        this.output = output;
        output.setValue("");
    }

    /**
     * load the lyrics stream, and resets the state
     * @param stream the lyrics file
     */
    public void loadAndStop(InputStream stream) {
        timer.purge();
        output.setValue("");
        if (stream == null) {
            Logger.getLogger("LyricsDisplay").info("During initalization, a null InputStream is given. Setting BufferReader as null instead.");
            text = null;
        } else {
            text = new BufferedReader(new InputStreamReader(stream));
        }
    }

    /**
     * starts displaying the lyrics
     */
    public void start() {
        if (text == null) {
            Logger.getLogger("LyricsDisplay").info("Returning from Lyrics Display's start() function, because lyrics BufferReader is null.");
            return;
        }
        startTime = Instant.now();
        scheduleNextTask();
    }

    /**
     * schedules the next task. Called in the first start() call and subsequent nextTask()s
     */
    private void scheduleNextTask() {
        if (startTime == null) {
            throw new IllegalStateException("start time is not defined");
        }
        try {
            String nextLine;
            Matcher results = null;
            while ((nextLine = text.readLine()) != null) { // reads till EOF
                results = pattern.matcher(nextLine);
                if (results.matches()) {
                    break;
                }
            }
            if (nextLine == null || results == null || (results != null && !results.matches())) {
                Logger.getLogger("LyricsDisplay").info("Cannot continue reading lyrics.");
                return;
            }
            long msFromOrigin = Integer.valueOf(results.group(1)) * 60 * 1000 + Integer.valueOf(results.group(2)) * 1000 + Integer.valueOf(results.group(3));
            final String lyrics = results.group(4);
            Date date = targetDate(startTime, msFromOrigin);
            timer.schedule(nextTask(lyrics), targetDate(startTime, msFromOrigin));
        } catch (IllegalStateException ex) {
            Logger.getLogger("LyricsDisplay").log(Level.WARNING, "RegExp Error while reading lyrics", ex);
        } catch (IOException ex) {
            Logger.getLogger("LyricsDisplay").log(Level.WARNING, "I/O Error while reading lyrics", ex);
        }
    }

    /**
     * @param lyrics Lyrics to be shown
     * @return TimerTask to be added to the scheduler (timer)
     */
    private TimerTask nextTask(String lyrics) {
        return new TimerTask(){
            public void run() {
                Platform.runLater(new Runnable(){ // working around bug: http://stackoverflow.com/a/32489845/1348400
                    @Override
                    public void run() {
                        output.setValue(lyrics);
                    }
                });
                scheduleNextTask();
            }
        };
    }

    /**
     * Calculates the Date when the lyrics is supposed to appear.
     * @param origin the original "base" time
     * @param offset how many ms after the lyrics is expected to show, calculated from {origin}
     * @return the Date computed by adding two "Instants" together
     */
    private final Date targetDate(Instant origin, long offset) {
        Instant target = origin.plusMillis(offset);
        return Date.from(target);
    }
}
