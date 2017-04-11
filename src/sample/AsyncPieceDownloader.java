package sample;

import org.asynchttpclient.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by isaac on 3/30/17.
 */
public class AsyncPieceDownloader {
    private final int piece_size;
    private final AsyncHttpClient httpClient = new DefaultAsyncHttpClient(new DefaultAsyncHttpClientConfig.Builder().setMaxConnections(100).build());
    private final List<String> uris;
    private final Queue<byte[]> buffer;
    private final int offset;
    private final long size;
    private AtomicInteger batch = new AtomicInteger(0);

    /**
     * creates a download job. the abstract downloader cannot be reused.
     * @param uris an List of uris to download from
     * @param buffer a reference to the buffer to hold the payload (supplied by caller)
     * @param size size of the file in bytes
     * @param piece_size the number of bytes per piece
     * @param offset the number of bytes to skip. This is usually 44 for WAV (PCM) files.
     */
    public AsyncPieceDownloader(List<String> uris, Queue<byte[]> buffer, long size, int piece_size, int offset) {
        this.piece_size = piece_size;
        this.uris = uris;
        this.buffer = buffer;
        this.size = size;
        this.offset = offset;
    }

    /**
     * creates a download job, exclusively for downloading MP3s. the abstract downloader cannot be reused.
     * @param uris an List of uris to download from
     * @param buffer a reference to the buffer to hold the payload (supplied by caller)
     * @param size size of the file in bytes
     */
    public AsyncPieceDownloader(List<String> uris, Queue<byte[]> buffer, long size) {
        this(uris, buffer, size, 1024*600, 44);
    }

    /**
     * actually starts the download
     * @return a promise that resolves when the download is completed
     */
    public CompletableFuture<Void> download() {
        CompletableFuture<Void> promiseToDownloadEverything = CompletableFuture.completedFuture(null);
        for (int i = 0; i*piece_size*uris.size()+offset < size; i++) {
            promiseToDownloadEverything = promiseToDownloadEverything.
                                            thenAcceptAsync(this::downloadNextBatch);
        }
        return promiseToDownloadEverything;
    }

    /**
     * download the next batch (i.e. from different servers) of data
     */
    private void downloadNextBatch(Void v) {
        ArrayList<CompletableFuture<byte[]>> promisesOfAllServers = new ArrayList<CompletableFuture<byte[]>>();
        final int batchId = this.batch.getAndIncrement();
        for (int serverId = 0; serverId < uris.size() && (batchId*uris.size()+serverId)*piece_size+offset < size; serverId++) {
            promisesOfAllServers.add(downloadPieceFromServer(serverId, batchId));
        }
        Iterator<CompletableFuture<byte[]>> iterator = promisesOfAllServers.iterator();
        while (iterator.hasNext()) {
            iterator.next()
                    .thenAcceptAsync(this::pushToBuffer)
                    .exceptionally(ex -> {
                        Logger.getLogger("AsyncPieceDownloader").log(Level.WARNING, "Failed to download piece from server", ex);
                        return null;
                    })
                    .join();
        }
    }

    /**
     * download the data from a specified server, usually in a batch.
     * @param serverId the 0-indexed serverID
     * @return a promise holding the download data
     */
    private CompletableFuture<byte[]> downloadPieceFromServer(int serverId, int batchId) {
        String uri = uris.get(serverId);
        long rangeLower = offset + piece_size*(batchId*uris.size() + serverId);
        long rangeUpper = rangeLower + piece_size -1;
        if (rangeUpper >= size) {
            rangeUpper = size-1;
        }
        if (rangeLower >= size) {
            Logger.getLogger("AsyncPieceDownloader").warning("The byte ranges to be requested exceeds the file size.");
            return CompletableFuture.completedFuture(new byte[0]);
        }
        Request req = new RequestBuilder()
                .setUrl(uri)
                .setHeader("Range", "bytes="+rangeLower+"-"+rangeUpper)
                .build();
        final CompletableFuture<byte[]> promise = httpClient
                                                    .executeRequest(req)
                                                    .toCompletableFuture()
                                                    .thenApply(resp -> {
                                                        if (resp.getStatusCode() >= 300) {
                                                            throw new RuntimeException("the HTTP response indicates an error for request: "+req);
                                                        }
                                                        return resp;
                                                    })
                                                    .thenApply(resp -> resp.getResponseBodyAsBytes());
        return promise;
    }

    private void pushToBuffer(byte[] data) {
        buffer.add(data);
    }

    public static CompletableFuture<Void> downloadAsFile(RemoteSong song, String filename, int piece_size) {
        LinkedBlockingQueue<byte[]> queue = new LinkedBlockingQueue();
        AsyncPieceDownloader downloader = new AsyncPieceDownloader(song.getUris(), queue, song.getFilesize(), piece_size, 0);
        return downloader.download().thenApply((Void v) -> {
            try (FileOutputStream stream = new FileOutputStream(filename)) {
                byte[] buf;
                while ((buf = queue.poll()) != null) {
                    stream.write(buf);
                }
                Logger.getLogger("AsyncPieceDownloader").info("Finished download");
            } catch (IOException ex) {
                Logger.getLogger("AsyncPieceDownloader").log(Level.SEVERE, "Failed to download: IO Exception");
            }
            return null;
        });
    }

    public static CompletableFuture<Void> demo(RemoteSong song, String filename) {
        return downloadAsFile(song, filename, 1024*50);
    }
}
