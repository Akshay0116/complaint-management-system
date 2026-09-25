package com.complaintsystem.app;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Built-in zero-dependency static HTTP Web Server using Java SE's com.sun.net.httpserver.
 * Serves the modern ResolveDesk Web UI at http://localhost:8080/
 */
public class WebServer {
    private static final int PORT = 8080;
    private static final Path WEB_ROOT = Paths.get("web").toAbsolutePath().normalize();

    public static void main(String[] args) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.createContext("/", new StaticFileHandler(WEB_ROOT));
            server.setExecutor(null); // default executor
            server.start();

            System.out.println("==============================================================");
            System.out.println("  ResolveDesk Complaint Management Portal - Web Server Active");
            System.out.println("==============================================================");
            System.out.println("  Local URL:    http://localhost:" + PORT + "/");
            System.out.println("  Web Directory: " + WEB_ROOT);
            System.out.println("  Student Login: Demo Account USR-101 / demo123");
            System.out.println("  Admin Login:   Demo Staff ADM-201 or Senior ADM-S01");
            System.out.println("--------------------------------------------------------------");
            System.out.println("  Press Ctrl+C in this terminal window to stop the server.");
            System.out.println("==============================================================");
        } catch (IOException e) {
            System.err.println("Failed to start web server on port " + PORT + ": " + e.getMessage());
            System.err.println("You can also open 'web/index.html' directly in any modern web browser!");
        }
    }

    private static class StaticFileHandler implements HttpHandler {
        private final Path root;

        public StaticFileHandler(Path root) {
            this.root = root;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String pathStr = exchange.getRequestURI().getPath();
            if (pathStr == null || pathStr.isEmpty() || pathStr.equals("/")) {
                pathStr = "/index.html";
            }

            // Prevent path traversal
            Path resolvedPath = root.resolve(pathStr.substring(1)).normalize();
            if (!resolvedPath.startsWith(root)) {
                sendResponse(exchange, 403, "403 Forbidden", "text/plain");
                return;
            }

            File file = resolvedPath.toFile();
            if (!file.exists() || file.isDirectory()) {
                sendResponse(exchange, 404, "404 Not Found", "text/plain");
                return;
            }

            String contentType = determineContentType(file.getName());
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");
            exchange.sendResponseHeaders(200, file.length());

            try (OutputStream os = exchange.getResponseBody();
                 FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int count;
                while ((count = fis.read(buffer)) > 0) {
                    os.write(buffer, 0, count);
                }
            }
        }

        private String determineContentType(String fileName) {
            String lower = fileName.toLowerCase();
            if (lower.endsWith(".html") || lower.endsWith(".htm")) return "text/html; charset=UTF-8";
            if (lower.endsWith(".css")) return "text/css; charset=UTF-8";
            if (lower.endsWith(".js")) return "application/javascript; charset=UTF-8";
            if (lower.endsWith(".json")) return "application/json; charset=UTF-8";
            if (lower.endsWith(".svg")) return "image/svg+xml";
            if (lower.endsWith(".png")) return "image/png";
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
            if (lower.endsWith(".ico")) return "image/x-icon";
            return "application/octet-stream";
        }

        private void sendResponse(HttpExchange exchange, int statusCode, String responseText, String contentType) throws IOException {
            byte[] bytes = responseText.getBytes();
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(statusCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }
}
