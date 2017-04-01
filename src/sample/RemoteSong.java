package sample;

import javafx.beans.property.SimpleStringProperty;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;

import javax.sound.sampled.AudioFormat;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Created by isaac on 3/29/17.
 */
public class RemoteSong extends Song {
    private List<String> uris = new LinkedList<String>();
    private int totalPieces;
    private BlockingQueue<byte[]> payload = new LinkedBlockingQueue();
    private long filesize;
    private boolean payloadEof = false;

    public RemoteSong(String uri, String filename, String name, String singer, String album, long filesize) throws Exception {
        super.filename = filename;
        super.name = new SimpleStringProperty(name);
        super.singer = new SimpleStringProperty(singer);
        super.album = new SimpleStringProperty(album);
        this.filesize = filesize;
        this.uris.add(uri);
        totalPieces = 1000;
    }

    @Override
    public byte[] read() throws IOException, InterruptedException {
        if (!payloadEof) {
            return payload.take();
        } else {
            try {
                return payload.remove();
            } catch (NoSuchElementException ex) {
                throw new EOFException();
            }
        }
    }

    @Override
    public AudioFormat getFormat() throws Exception {
        String firstUri = this.uris.get(0);
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
        final int fmtChunkSize = readLittleEndian(result, 4);
        result.skip(2);
        int channels = readLittleEndian(result, 2);
        final int sampleRate = readLittleEndian(result, 4);
        final int byteRate = readLittleEndian(result, 4);
        final int blockAlign = readLittleEndian(result, 2);
        final int bitsPerSample = readLittleEndian(result, 2);
        final int frameSize = bitsPerSample / 8 * 2;
        if (result.read() != 'd') {
            throw new Exception("apparently we are not reading the 'data' chunk.");
        }
        result.skip(7);
        return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, bitsPerSample, channels, frameSize, byteRate/frameSize, false);
    }

    @Override
    public void close() throws IOException {

    }

    public void start() {
        payload.clear();
        payloadEof = false;
        AsyncPieceDownloader downloader = new AsyncPieceDownloader(uris, payload, filesize);
        downloader.download().thenAccept((Void v) -> {
            payloadEof = true;
        }).exceptionally(ex -> {
            Logger.getLogger("RemoteSong").log(Level.SEVERE, "Failed to download in 2nd larger promise", ex);
            return null;
        });
        /*new Thread()
        {
            public void run() {
                Request req;
                int targetServer = 0;
                int rangeLower, rangeUpper;
                for (int i = 0; i < totalPieces; i++) {
                    targetServer = (targetServer++) % uris.size();
                    rangeLower = 44+BYTES_PER_PIECE*i;
                    rangeUpper = rangeLower+BYTES_PER_PIECE-1;
                    req = new RequestBuilder()
                            .setUrl(uris.get(targetServer))
                            .setHeader("Range", "bytes="+rangeLower+"-"+rangeUpper)
                            .build();
                    final CompletableFuture<Void> promise = httpClient.executeRequest(req).toCompletableFuture().thenAccept(resp -> {
                        byte[] buf = resp.getResponseBodyAsBytes();
                        payload.add(buf);
                    }).exceptionally(ex -> {
                        Logger.getLogger("RemoteSong").log(Level.WARNING, "failed to download remote song", ex);
                        return null;
                    });
                    promise.join(); // we're using sync method for now...
                }
                payloadEof = true;
            }
        }.start();*/
    }



    public void addUri(String uri) {
        synchronized (uri) {
            this.uris.add(uri);
        }
    }

    private static int readLittleEndian(InputStream stream, int bytes) throws IOException {
        int rtn = 0;
        for (int i = 0; i < bytes; i++) {
            rtn |= stream.read() << (8*i);
        }
        return rtn;
    }

    @Override
    public long getFilesize() {
        return filesize;
    }

    public String getLocation() {
        StringBuilder sb = new StringBuilder();
        for (Iterator<String> iterator = uris.iterator(); iterator.hasNext();) {
            sb.append(iterator.next());
            sb.append(", ");
        }
        return sb.toString();
    }

    public CompletableFuture<InputStream> lyrics() {
        return new DefaultAsyncHttpClient()
                .prepareGet(uris.get(0)+".lrc")
                .execute()
                .toCompletableFuture()
                .thenApply(res -> res.getResponseBodyAsStream());
    }
}
