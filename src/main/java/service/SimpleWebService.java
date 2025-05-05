package service;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;

public class SimpleWebService {
    private HttpServer server;

    public SimpleWebService(int port, File htmlFile) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new HtmlFileHandler(htmlFile));
        server.setExecutor(null);
        server.start();
    }

    static class HtmlFileHandler implements HttpHandler {
        private final File htmlFile;

        public HtmlFileHandler(File htmlFile) {
            this.htmlFile = htmlFile;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            byte[] response = Files.readAllBytes(htmlFile.toPath());
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        }
    }
}
