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
 * Created by isaac on 4/1/17.
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
    public void start() {
        if (text == null) {
            Logger.getLogger("LyricsDisplay").info("Returning from Lyrics Display's start() function, because lyrics BufferReader is null.");
            return;
        }
        startTime = Instant.now();
        scheduleNextTask();
    }
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
    private final Date targetDate(Instant origin, long offset) {
        Instant target = origin.plusMillis(offset);
        return Date.from(target);
    }
}
