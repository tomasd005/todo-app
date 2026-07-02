package com.todolist.api;

import com.todolist.application.service.TaskService;
import com.todolist.application.service.UserService;
import com.todolist.infrastructure.repository.JpaTaskRepository;
import com.todolist.infrastructure.repository.JpaUserRepository;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws IOException {
        UserService userService = new UserService(new JpaUserRepository());
        TaskService taskService = new TaskService(new JpaTaskRepository());
        SessionManager sessionManager = new SessionManager();

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new StaticResourceHandler("index.html"));
        server.createContext("/static/", new StaticResourceHandler(null));
        server.createContext("/api/register", new RegisterHandler(userService, sessionManager));
        server.createContext("/api/login", new LoginHandler(userService, sessionManager));
        server.createContext("/api/logout", new LogoutHandler(sessionManager));
        server.createContext("/api/tasks", new TaskHandler(taskService, sessionManager));

        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();

        System.out.println("Servidor iniciado em http://localhost:8080");
    }

    private static class StaticResourceHandler implements HttpHandler {
        private final String defaultResource;

        private StaticResourceHandler(String defaultResource) {
            this.defaultResource = defaultResource;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            URI uri = exchange.getRequestURI();
            String path = uri.getPath();
            String resourcePath;
            if ("/".equals(path) || "".equals(path)) {
                resourcePath = defaultResource;
            } else if (path.startsWith("/static/")) {
                resourcePath = path.substring("/static/".length());
            } else {
                resourcePath = defaultResource;
            }

            try (InputStream resourceStream = Main.class.getResourceAsStream("/web/" + resourcePath)) {
                if (resourceStream == null) {
                    sendNotFound(exchange);
                    return;
                }
                byte[] bytes = resourceStream.readAllBytes();
                exchange.getResponseHeaders().add("Content-Type", getContentType(resourcePath));
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            }
        }

        private String getContentType(String resourcePath) {
            if (resourcePath.endsWith(".js")) {
                return "application/javascript; charset=UTF-8";
            }
            if (resourcePath.endsWith(".css")) {
                return "text/css; charset=UTF-8";
            }
            if (resourcePath.endsWith(".html")) {
                return "text/html; charset=UTF-8";
            }
            return "application/octet-stream";
        }

        private void sendNotFound(HttpExchange exchange) throws IOException {
            byte[] message = "Recurso não encontrado".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(404, message.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(message);
            }
        }
    }

    private static class RegisterHandler implements HttpHandler {
        private final UserService userService;
        private final SessionManager sessionManager;

        RegisterHandler(UserService userService, SessionManager sessionManager) {
            this.userService = userService;
            this.sessionManager = sessionManager;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            Map<String, String> data = parseForm(exchange);
            String username = data.getOrDefault("username", "").trim();
            String password = data.getOrDefault("password", "").trim();

            try {
                var user = userService.register(username, password);
                String token = sessionManager.createSession(user.getId());
                exchange.getResponseHeaders().add("Set-Cookie", "SESSION=" + token + "; Path=/; HttpOnly");
                sendJson(exchange, 201, "{\"message\": \"Usuário registrado\"}");
            } catch (IllegalArgumentException exception) {
                sendJson(exchange, 400, "{\"error\": \"" + escapeJson(exception.getMessage()) + "\"}");
            }
        }
    }

    private static class LoginHandler implements HttpHandler {
        private final UserService userService;
        private final SessionManager sessionManager;

        LoginHandler(UserService userService, SessionManager sessionManager) {
            this.userService = userService;
            this.sessionManager = sessionManager;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            Map<String, String> data = parseForm(exchange);
            String username = data.getOrDefault("username", "").trim();
            String password = data.getOrDefault("password", "").trim();

            var user = userService.login(username, password);
            if (user.isPresent()) {
                String token = sessionManager.createSession(user.get().getId());
                exchange.getResponseHeaders().add("Set-Cookie", "SESSION=" + token + "; Path=/; HttpOnly");
                sendJson(exchange, 200, "{\"message\": \"Login realizado\"}");
            } else {
                sendJson(exchange, 401, "{\"error\": \"Usuário ou senha inválidos\"}");
            }
        }
    }

    private static class LogoutHandler implements HttpHandler {
        private final SessionManager sessionManager;

        LogoutHandler(SessionManager sessionManager) {
            this.sessionManager = sessionManager;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            String sessionId = sessionManager.extractSession(exchange);
            if (sessionId != null) {
                sessionManager.invalidateSession(sessionId);
            }
            sendJson(exchange, 200, "{\"message\": \"Logout efetuado\"}");
        }
    }

    private static class TaskHandler implements HttpHandler {
        private final TaskService taskService;
        private final SessionManager sessionManager;

        TaskHandler(TaskService taskService, SessionManager sessionManager) {
            this.taskService = taskService;
            this.sessionManager = sessionManager;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Optional<String> userId = sessionManager.currentUserId(exchange);
            if (userId.isEmpty()) {
                sendJson(exchange, 401, "{\"error\": \"Acesso não autorizado\"}");
                return;
            }

            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                handleList(exchange);
                return;
            }

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                handleCreate(exchange);
                return;
            }

            exchange.sendResponseHeaders(405, -1);
        }

        private void handleList(HttpExchange exchange) throws IOException {
            List<com.todolist.domain.model.Task> tasks = taskService.listTasks();
            String body = "[" + tasks.stream().map(Main::toJson).collect(Collectors.joining(",")) + "]";
            sendJson(exchange, 200, body);
        }

        private void handleCreate(HttpExchange exchange) throws IOException {
            Map<String, String> data = parseForm(exchange);
            String title = data.getOrDefault("title", "").trim();
            String description = data.getOrDefault("description", "").trim();
            String priority = data.getOrDefault("priority", "MEDIUM").trim();
            String dueDate = data.getOrDefault("dueDate", "").trim();

            try {
                com.todolist.domain.model.Task task = taskService.createTask(
                        title,
                        description,
                        com.todolist.domain.model.Priority.valueOf(priority.toUpperCase()),
                        ""
                );
                if (!dueDate.isEmpty()) {
                    task.setDueDate(LocalDate.parse(dueDate));
                    taskService.saveTask(task);
                }
                sendJson(exchange, 201, "{\"message\": \"Tarefa criada\"}");
            } catch (Exception ex) {
                sendJson(exchange, 400, "{\"error\": \"" + escapeJson(ex.getMessage()) + "\"}");
            }
        }
    }

    private static Map<String, String> parseForm(HttpExchange exchange) throws IOException {
        String content = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        if (content.isBlank()) {
            return Collections.emptyMap();
        }
        return java.util.Arrays.stream(content.split("&"))
                .map(pair -> pair.split("=", 2))
                .filter(parts -> parts.length == 2)
                .collect(Collectors.toMap(
                        parts -> urlDecode(parts[0]),
                        parts -> urlDecode(parts[1])
                ));
    }

    private static String urlDecode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static void sendJson(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String escapeJson(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String toJson(com.todolist.domain.model.Task task) {
        return "{" +
                "\"id\":\"" + escapeJson(task.getId()) + "\"," +
                "\"title\":\"" + escapeJson(task.getTitle()) + "\"," +
                "\"description\":\"" + escapeJson(task.getDescription()) + "\"," +
                "\"priority\":\"" + task.getPriority() + "\"," +
                "\"status\":\"" + task.getStatus() + "\"," +
                "\"dueDate\":\"" + (task.getDueDate() != null ? task.getDueDate() : "") + "\"" +
                "}";
    }

    private static class SessionManager {
        private final Map<String, String> sessions = new ConcurrentHashMap<>();

        public String createSession(String userId) {
            String token = UUID.randomUUID().toString();
            sessions.put(token, userId);
            return token;
        }

        public Optional<String> currentUserId(HttpExchange exchange) {
            String sessionId = extractSession(exchange);
            return Optional.ofNullable(sessionId).map(sessions::get);
        }

        public String extractSession(HttpExchange exchange) {
            List<String> cookies = exchange.getRequestHeaders().get("Cookie");
            if (cookies == null) {
                return null;
            }
            for (String cookieHeader : cookies) {
                String[] parts = cookieHeader.split(";\s*");
                for (String part : parts) {
                    if (part.startsWith("SESSION=")) {
                        return part.substring("SESSION=".length());
                    }
                }
            }
            return null;
        }

        public void invalidateSession(String sessionId) {
            sessions.remove(sessionId);
        }
    }
}
