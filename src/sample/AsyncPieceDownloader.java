package sample;

import org.asynchttpclient.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by isaac on 3/30/17.
 */
public class AsyncPieceDownloader {
    private final int BYTES_PER_PIECE = 1024*300;
    private final AsyncHttpClient httpClient = new DefaultAsyncHttpClient(new DefaultAsyncHttpClientConfig.Builder().setMaxConnections(100).build());
    private final List<String> uris;
    private final Queue<byte[]> buffer;
    private final long size;
    private int batchId = 0;
    private long totalPieces;

    /**
     * creates a download job. the abstract downloader cannot be reused.
     * @param uris an List of uris to download from
     * @param buffer a reference to the buffer to hold the payload (supplied by caller)
     * @param size size of the file in bytes
     */
    public AsyncPieceDownloader(List<String> uris, Queue<byte[]> buffer, long size) {
        this.uris = uris;
        this.buffer = buffer;
        this.size = size;
        this.totalPieces = size/BYTES_PER_PIECE + ((size % BYTES_PER_PIECE == 0) ? 0 : 1); // Round up
    }

    /**
     * actually starts the download
     * @return a promise that resolves when the download is completed
     */
    public CompletableFuture<Void> download() {
        CompletableFuture<Void> promiseToDownloadEverything = CompletableFuture.completedFuture(null);
        for (int i = 0; i < totalPieces; i++) {
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
        for (int serverId = 0; serverId < uris.size() && batchId*uris.size()+serverId < totalPieces; serverId++) {
            promisesOfAllServers.add(downloadPieceFromServer(serverId));
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
        batchId++;
    }

    /**
     * download the data from a specified server, usually in a batch.
     * @param server the 0-indexed serverID
     * @return a promise holding the download data
     */
    private CompletableFuture<byte[]> downloadPieceFromServer(int server) {
        String uri = uris.get(server);
        long rangeLower = 44+BYTES_PER_PIECE*(batchId*uris.size() + server);
        long rangeUpper = rangeLower+BYTES_PER_PIECE-1;
        if (rangeLower > size) {
            throw new IllegalStateException("The byte ranges to be requested exceeds the file size.");
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

}
