package com.student.server;

import com.student.model.Student;
import com.student.service.StudentService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class StudentHttpServer {
    private final int port;
    private final StudentService studentService;

    public StudentHttpServer(int port) {
        this.port = port;
        this.studentService = new StudentService();
    }

    public void start() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/api/students", this::handleStudents);
            server.createContext("/", this::handleStaticFiles);
            server.setExecutor(Executors.newFixedThreadPool(8));
            server.start();
            System.out.println("Student Information System running on http://localhost:" + port);
        } catch (IOException e) {
            throw new RuntimeException("Could not start server", e);
        }
    }

    private void handleStudents(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if ("GET".equalsIgnoreCase(method) && "/api/students".equals(path)) {
            sendJson(exchange, 200, studentsToJson(studentService.getAllStudents()));
            return;
        }

        if ("POST".equalsIgnoreCase(method) && "/api/students".equals(path)) {
            Map<String, String> body = parseFormBody(exchange.getRequestBody());
            try {
                String enrollmentNo = parseEnrollmentNo(required(body, "enrollmentNo"));
                String name = required(body, "name");
                int age = parseInt(required(body, "age"), "age");
                String department = parseDepartment(required(body, "department"));
                double cgpa = parseDouble(required(body, "cgpa"), "cgpa");
                Student created = studentService.addStudent(enrollmentNo, name, age, department, cgpa);
                sendJson(exchange, 201, studentToJson(created));
            } catch (IllegalArgumentException ex) {
                sendJson(exchange, 400, "{\"error\":\"" + escapeJson(ex.getMessage()) + "\"}");
            }
            return;
        }

        if (("PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)) && path.startsWith("/api/students/")) {
            String idRaw = path.substring("/api/students/".length());
            int id;
            try {
                id = Integer.parseInt(idRaw);
            } catch (NumberFormatException ex) {
                sendJson(exchange, 400, "{\"error\":\"Invalid student id\"}");
                return;
            }

            if ("DELETE".equalsIgnoreCase(method)) {
                boolean deleted = studentService.deleteStudent(id);
                if (deleted) {
                    sendJson(exchange, 200, "{\"message\":\"Student deleted\"}");
                } else {
                    sendJson(exchange, 404, "{\"error\":\"Student not found\"}");
                }
                return;
            }

            Map<String, String> body = parseFormBody(exchange.getRequestBody());
            try {
                String enrollmentNo = parseEnrollmentNo(required(body, "enrollmentNo"));
                String name = required(body, "name");
                int age = parseInt(required(body, "age"), "age");
                String department = parseDepartment(required(body, "department"));
                double cgpa = parseDouble(required(body, "cgpa"), "cgpa");
                boolean updated = studentService.updateStudent(id, enrollmentNo, name, age, department, cgpa);
                if (updated) {
                    Student student = studentService.getById(id).orElseThrow();
                    sendJson(exchange, 200, studentToJson(student));
                } else {
                    sendJson(exchange, 404, "{\"error\":\"Student not found\"}");
                }
            } catch (IllegalArgumentException ex) {
                sendJson(exchange, 400, "{\"error\":\"" + escapeJson(ex.getMessage()) + "\"}");
            }
            return;
        }

        sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
    }

    private void handleStaticFiles(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if ("/".equals(path)) {
            path = "/index.html";
        }

        Path filePath = Path.of("web" + path).normalize();
        if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            sendText(exchange, 404, "Not Found", "text/plain");
            return;
        }

        String contentType = contentType(filePath.toString());
        byte[] content = Files.readAllBytes(filePath);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(200, content.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(content);
        }
    }

    private String contentType(String path) {
        if (path.endsWith(".html")) {
            return "text/html; charset=utf-8";
        }
        if (path.endsWith(".css")) {
            return "text/css; charset=utf-8";
        }
        if (path.endsWith(".js")) {
            return "application/javascript; charset=utf-8";
        }
        return "text/plain; charset=utf-8";
    }

    private void sendJson(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void sendText(HttpExchange exchange, int statusCode, String text, String contentType) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private Map<String, String> parseFormBody(InputStream requestBody) throws IOException {
        String body = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> map = new HashMap<>();
        if (body.isBlank()) {
            return map;
        }
        String[] pairs = body.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            String key = decodeUrl(keyValue[0]);
            String value = keyValue.length > 1 ? decodeUrl(keyValue[1]) : "";
            map.put(key, value);
        }
        return map;
    }

    private String decodeUrl(String raw) {
        return URLDecoder.decode(raw, StandardCharsets.UTF_8);
    }

    private String required(Map<String, String> map, String key) {
        String value = map.get(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(key + " is required");
        }
        return value.trim();
    }

    private int parseInt(String value, String fieldName) {
        try {
            int parsed = Integer.parseInt(value);
            if (parsed < 1 || parsed > 120) {
                throw new IllegalArgumentException(fieldName + " must be between 1 and 120");
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + " must be a valid integer");
        }
    }

    private String parseEnrollmentNo(String value) {
        if (!value.matches("^[0-9]+$")) {
            throw new IllegalArgumentException("enrollmentNo must be a valid integer");
        }
        return value;
    }

    private String parseDepartment(String value) {
        if (value.matches(".*\\d.*")) {
            throw new IllegalArgumentException("department cannot contain numbers");
        }
        return value;
    }

    private double parseDouble(String value, String fieldName) {
        try {
            double parsed = Double.parseDouble(value);
            if (parsed < 0.0 || parsed > 10.0) {
                throw new IllegalArgumentException(fieldName + " must be between 0.0 and 10.0");
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + " must be a valid number");
        }
    }

    private String studentsToJson(List<Student> students) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < students.size(); i++) {
            builder.append(studentToJson(students.get(i)));
            if (i < students.size() - 1) {
                builder.append(",");
            }
        }
        builder.append("]");
        return builder.toString();
    }

    private String studentToJson(Student student) {
        return "{" +
                "\"id\":" + student.getId() + "," +
                "\"enrollmentNo\":\"" + escapeJson(student.getEnrollmentNo()) + "\"," +
                "\"name\":\"" + escapeJson(student.getName()) + "\"," +
                "\"age\":" + student.getAge() + "," +
                "\"department\":\"" + escapeJson(student.getDepartment()) + "\"," +
                "\"cgpa\":" + student.getCgpa() +
                "}";
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
