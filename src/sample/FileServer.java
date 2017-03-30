/**
 * Created by isaac on 3/29/17.
 */

package sample;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.SimpleWebServer;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class FileServer implements AutoCloseable {
    private final SimpleWebServer server;
    public FileServer(int port) throws IOException {
        server = new SimpleWebServer(null, port, new File("database"), false);
        server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        Logger.getLogger("FileServer").info("Web server started successfully on port "+port);
    }
    public void close() {
        server.stop();
        Logger.getLogger("FileServer").info("Web server stopped.");
    }
}
