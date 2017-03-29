package sample;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;

import javafx.beans.property.SimpleStringProperty;
import org.asynchttpclient.*;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Created by isaac on 3/29/17.
 */
public class RemoteSong extends Song {
    private final int BYTES_PER_PIECE = 1024*300;
    private AsyncHttpClient httpClient = new DefaultAsyncHttpClient(new DefaultAsyncHttpClientConfig.Builder().setMaxConnections(100).build());
    private List<String> uri = new LinkedList<String>();
    private int totalPieces;
    private AtomicBoolean downloaded = new AtomicBoolean();
    private BlockingQueue<byte[]> payload = new LinkedBlockingQueue();

    public RemoteSong(String uri, String filename, String name, String singer, String album, int size) throws Exception {
        this.uri.add(uri);
        super.filename = filename;
        totalPieces = 1000;
        super.name = new SimpleStringProperty("test2");
    }

    @Override
    public byte[] read() throws IOException, InterruptedException {
        return payload.take();
    }

    @Override
    public AudioFormat getFormat() throws Exception {
        String firstUri = this.uri.get(0);
        Request req = new RequestBuilder()
                .setUrl(firstUri)
                .setHeader("Range", "bytes=0-43")
                .build();
        InputStream result = new DefaultAsyncHttpClient().executeRequest(req).get().getResponseBodyAsStream();
        if (result.read() != 'R') {
            throw new Exception("The target file is not a RIFF file.");
        }
        result.skip(11);
        if (result.read() != 'f') {
            throw new Exception("The implementation of this program assumes that fmt is placed before data.");
        }
        result.skip(3);
        int fmtChunkSize = readLittleEndian(result, 4);
        result.skip(2);
        int channels = readLittleEndian(result, 2);
        int sampleRate = readLittleEndian(result, 4);
        int byteRate = readLittleEndian(result, 4);
        int blockAlign = readLittleEndian(result, 2);
        int bitsPerSample = readLittleEndian(result, 2);
        if (result.read() != 'd') {
            throw new Exception("apparently we are not reading the 'data' chunk.");
        }
        result.skip(7);
        downloadInBackground();
        return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, bitsPerSample, channels, 4, byteRate/4, false);
    }

    @Override
    public void close() throws IOException {

    }

    private void downloadInBackground() {
        if (downloaded.compareAndSet(false, true) == true) {
            return;
        }
        new Thread()
        {
            public void run() {
                Request req;
                int targetServer = 0;
                int rangeLower, rangeUpper;
                for (int i = 0; i < totalPieces; i++) {
                    targetServer = (targetServer++) % uri.size();
                    rangeLower = 44+BYTES_PER_PIECE*i;
                    rangeUpper = rangeLower+BYTES_PER_PIECE-1;
                    req = new RequestBuilder()
                            .setUrl(uri.get(targetServer))
                            .setHeader("Range", "bytes="+rangeLower+"-"+rangeUpper)
                            .build();
                    httpClient.executeRequest(req).toCompletableFuture().thenAccept(resp -> {
                        byte[] buf = resp.getResponseBodyAsBytes();
                        payload.add(buf);
                    }).join(); // we're using sync method for now...
                }
            }
        }.start();
    }



    public void addUri(String uri) {
        synchronized (uri) {
            this.uri.add(uri);
        }
    }

    private static int readLittleEndian(InputStream stream, int bytes) throws IOException {
        int rtn = 0;
        for (int i = 0; i < bytes; i++) {
            rtn |= stream.read() << (8*i);
        }
        return rtn;
    }
}
