package sample;

import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Created by isaac on 4/11/17.
 */
public class VideoStreamer extends NanoHTTPD {
    private PipedOutputStream stream = new PipedOutputStream();
    private PipedInputStream outstream = new PipedInputStream(stream);
    private RemoteSong song;

    public VideoStreamer(RemoteSong song) throws IOException {
        super(9011);
        this.song = song;
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        Logger.getLogger("VideoStreamer").info("Started web server for VideoStreamer at port 9011");
    }

    @Override
    public Response serve(IHTTPSession session) {
        return newChunkedResponse(Response.Status.OK, "video/mp4", outstream);
    }

    public void fetch() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                final BlockingQueue<byte[]> payload = new LinkedBlockingQueue();
                AtomicBoolean eof = new AtomicBoolean(false);
                AsyncPieceDownloader downloader = new AsyncPieceDownloader(song.getUris(), payload, song.getFilesize(), 1024*500, 0);
                downloader.download().thenAccept((Void v) -> {
                    eof.set(true);
                });
                byte[] c;
                try {
                    while (eof.get() == false) {
                        c = payload.take();
                        stream.write(c);
                    }
                    while ((c = payload.poll()) != null) {
                        stream.write(c);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    Logger.getLogger("VideoStreamer").warning("I/O Exception for VideoStream");
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    Logger.getLogger("VideoStreamer").warning("Interrupted Exception for VideoStream");
                }
                Logger.getLogger("VideoStreamer").info("End of stream for VideoStream source");
                try {
                    stream.close();
                } catch (IOException ex) {
                    Logger.getLogger("VideoStreamer").warning("Failed to close stream for VideoStreamer");
                }
            }
        };
        ForkJoinPool.commonPool().execute(task);
    }

    public void stop() {
        Logger.getLogger("VideoStreamer").info("Stopping VideoStreamer service");
        super.stop();
    }
}
