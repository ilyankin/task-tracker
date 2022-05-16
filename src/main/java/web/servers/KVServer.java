package web.servers;

import com.sun.net.httpserver.HttpServer;
import web.servers.handlers.KVServer.LoadHandler;
import web.servers.handlers.KVServer.RegisterHandler;
import web.servers.handlers.KVServer.SaveHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KVServer {
    public static Charset defaultCharset = StandardCharsets.UTF_8;
    public final int port;
    private final String apiKey;
    private HttpServer KVServer;
    private final Map<String, String> data = new HashMap<>();

    public KVServer(int port, String hostname, boolean isDebugMode) {
        this.port = port;
        apiKey = isDebugMode ? "DEBUG" : generateApiKey();
        try {
            KVServer = HttpServer.create(new InetSocketAddress(hostname, port), 0);
        } catch (IOException e) {
            System.out.println("Oops, something went wrong when creating a KVServer on the port: " + port
                    + " and with hostname: " + hostname);
            e.printStackTrace();
        }
        loadContext();
    }

    public KVServer(int port, String hostname) {
        this(port, hostname, false);
    }

    public void start() {
        System.out.println("The KVServer is running on the port " + port);
        System.out.println("Api key: " + apiKey);
        KVServer.start();
    }

    public void stop(int delay) {
        KVServer.stop(delay);
        System.out.println("The KVServer's been stopped on the port " + port);
    }

    public static void setDefaultCharset(Charset charset) {
        defaultCharset = charset;
    }

    private String generateApiKey() {
        return UUID.randomUUID().toString();
    }

    private void loadContext() {
        KVServer.createContext("/register", new RegisterHandler(apiKey));
        KVServer.createContext("/save", new SaveHandler(apiKey, data));
        KVServer.createContext("/load", new LoadHandler(apiKey, data));
    }
}
