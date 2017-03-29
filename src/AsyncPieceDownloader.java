package sample;

import org.asynchttpclient.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;

import static java.util.concurrent.CompletableFuture.allOf;

/**
 * Created by isaac on 3/30/17.
 */
public class AsyncPieceDownloader {
    private final int BYTES_PER_PIECE = 1024*300;
    private List<String> uri;
    private final int fileSize;
    private final int totalPieces;
    private final Queue<byte[]> buffer;
    private AsyncHttpClient httpClient = new DefaultAsyncHttpClient(new DefaultAsyncHttpClientConfig.Builder().setMaxConnections(100).build());
    public AsyncPieceDownloader(List<String> uri, int fileSize, Queue buffer) {
        this.uri = uri;
        this.fileSize = fileSize;
        this.totalPieces = fileSize/BYTES_PER_PIECE;
        this.buffer = buffer;
    }

    public CompletableFuture<Void> download(byte[] buffer) {
        CompletableFuture<Void> promise = CompletableFuture.supplyAsync((Void v) -> {});
        for (int i = 0; i < totalPieces; i+=uri.size()) {
            promise.thenAcceptAsync((Void v) -> {
                LinkedBlockingQueue<CompletableFuture<byte[]>> promisesForThisRound = new LinkedBlockingQueue<CompletableFuture<byte[]>>();
                for (int targetServer = 0; targetServer < uri.size(); targetServer++) {
                    int rangeLower = 44+BYTES_PER_PIECE*(i+targetServer);
                    int rangeUpper = rangeLower+BYTES_PER_PIECE-1;
                    promisesForThisRound.add(downloadOnePiece(uri.get(targetServer), rangeLower, rangeUpper));
                }
                CompletableFuture<Void> promiseForThisRound =
                        CompletableFuture.allOf((CompletableFuture<byte[]>[])promisesForThisRound.toArray())
                                .thenAccept((Void v) -> {
                                    Iterator<CompletableFuture<byte[]>> iterator = promisesForThisRound.iterator();
                                    while (iterator.hasNext()) {
                                        iterator.next().thenAcceptAsync(res -> {
                                            buffer.
                                        }).join();
                                    }
                                });
                promiseForThisRound.join();
            });
        }
    }

    private CompletableFuture<byte[]> downloadOnePiece(String uri, int rangeLower, int rangeUpper) {
        Request req = new RequestBuilder()
                .setUrl(uri)
                .setHeader("Range", "bytes="+rangeLower+"-"+rangeUpper)
                .build();
        CompletableFuture<byte[]> res = httpClient.executeRequest(req).toCompletableFuture().thenApply(resp -> {
            byte[] buf = resp.getResponseBodyAsBytes();
            return buf;
        });
        return res;
    }
}
