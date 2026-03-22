package com.student;

import com.student.server.StudentHttpServer;

public class Main {
    public static void main(String[] args) {
        int port = 8080;
        String portValue = System.getenv("PORT");
        if (portValue != null && !portValue.isBlank()) {
            try {
                port = Integer.parseInt(portValue);
            } catch (NumberFormatException ignored) {
                port = 8080;
            }
        }

        StudentHttpServer server = new StudentHttpServer(port);
        server.start();
    }
}
