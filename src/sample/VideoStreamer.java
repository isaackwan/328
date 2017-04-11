package sample;

import fi.iki.elonen.NanoHTTPD;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by isaac on 4/11/17.
 */
public class VideoStreamer extends NanoHTTPD {
    public VideoStreamer(RemoteSong song) throws IOException {
        super(9011);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        Logger.getLogger("VideoStreamer").info("Started web server for VideoStreamer");
    }

    @Override
    public Response serve(IHTTPSession session) {
        if (session.getMethod() == Method.HEAD) {
            return newChunkedResponse(Response.Status.OK, "video/mp4", null);
        }
        final Map<String,String> headers = session.getHeaders();
        throw new NotImplementedException();
    }
}
