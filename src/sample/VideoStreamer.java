package sample;

import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Created by isaac on 4/11/17.
 */
public class VideoStreamer extends NanoHTTPD {
    private PipedOutputStream stream = new PipedOutputStream();
    private PipedInputStream outstream = new PipedInputStream(stream);

    public VideoStreamer(RemoteSong song) throws IOException {
        super(9011);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        Logger.getLogger("VideoStreamer").info("Started web server for VideoStreamer at port 9011");
    }

    @Override
    public Response serve(IHTTPSession session) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                try {
                    stream.write(99);
                    stream.write(70);
                } catch (IOException ex) {
                    Logger.getLogger("VideoStreamer").warning("Failed to copy to video streamer.");
                }
            }
        }, 0, 3, TimeUnit.SECONDS);
        return newChunkedResponse(Response.Status.OK, "video/mp4", outstream);
    }
}
