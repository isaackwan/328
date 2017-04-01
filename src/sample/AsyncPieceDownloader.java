package sample;

import org.asynchttpclient.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
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
    private final BlockingQueue<byte[]> buffer;
    private final long size;
    private int batchId = 0;
    public AsyncPieceDownloader(List<String> uris, BlockingQueue<byte[]> buffer, long size) {
        this.uris = uris;
        this.buffer = buffer;
        this.size = size;
    }
    public CompletableFuture<Void> download() {
        final long totalPieces = size/BYTES_PER_PIECE + ((size % BYTES_PER_PIECE == 0) ? 0 : 1);
        CompletableFuture<Void> promiseToDownloadEverything = CompletableFuture.completedFuture(null);
        for (int i = 0; i < totalPieces; i++) {
            promiseToDownloadEverything = promiseToDownloadEverything.
                                            thenAcceptAsync(this::downloadNextBatch);
        }
        return promiseToDownloadEverything;
    }

    private void downloadNextBatch(Void v) {
        ArrayList<CompletableFuture<byte[]>> promisesOfAllServers = new ArrayList<CompletableFuture<byte[]>>();
        for (int serverId = 0; serverId < uris.size(); serverId++) {
            promisesOfAllServers.add(downloadPieceFromServer(serverId));
        }
        blockTillAllPromisesComplete(promisesOfAllServers);
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

    private CompletableFuture<byte[]> downloadPieceFromServer(int server) {
        String uri = uris.get(server);
        long rangeLower = 44+BYTES_PER_PIECE*(batchId++*uris.size() + server);
        long rangeUpper = rangeLower+BYTES_PER_PIECE-1;
        Request req = new RequestBuilder()
                .setUrl(uri)
                .setHeader("Range", "bytes="+rangeLower+"-"+rangeUpper)
                .build();
        final CompletableFuture<byte[]> promise = httpClient.executeRequest(req).toCompletableFuture().thenApply(resp -> resp.getResponseBodyAsBytes());
        return promise;
    }

    private void pushToBuffer(byte[] data) {
        buffer.add(data);
    }

    private static void blockTillAllPromisesComplete(List<CompletableFuture<byte[]>> list) {
        Iterator<CompletableFuture<byte[]>> iterator = list.iterator();
        while (iterator.hasNext()) {
            iterator.next().join();
        }
    }
}
