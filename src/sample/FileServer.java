/**
 * Created by isaac on 3/29/17.
 */

package sample;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import fi.iki.elonen.SimpleWebServer;
import fi.iki.elonen.NanoHTTPD;

public class FileServer {
    SimpleWebServer server = new SimpleWebServer(null, 9001, new File("database"), false);
    public FileServer() throws IOException {
        server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, true);
    }
}
