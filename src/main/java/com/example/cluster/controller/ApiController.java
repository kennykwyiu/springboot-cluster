package com.example.cluster.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${spring.application.name:cluster-app}")
    private String applicationName;

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("port", serverPort);
        response.put("application", applicationName);
        return response;
    }

    @GetMapping("/info")
    public Map<String, Object> info(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("serverPort", serverPort);
        response.put("applicationName", applicationName);
        response.put("remoteAddr", request.getRemoteAddr());
        response.put("serverName", request.getServerName());
        response.put("serverPort", request.getServerPort());
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    @GetMapping("/session")
    public Map<String, Object> sessionInfo(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", session.getId());
        response.put("creationTime", session.getCreationTime());
        response.put("lastAccessedTime", session.getLastAccessedTime());
        response.put("maxInactiveInterval", session.getMaxInactiveInterval());
        response.put("serverPort", serverPort);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    @PostMapping("/session/attribute")
    public Map<String, Object> setSessionAttribute(
            @RequestParam String key, 
            @RequestParam String value, 
            HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        session.setAttribute(key, value);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Attribute set successfully");
        response.put("sessionId", session.getId());
        response.put("key", key);
        response.put("value", value);
        response.put("serverPort", serverPort);
        return response;
    }

    @GetMapping("/session/attribute/{key}")
    public Map<String, Object> getSessionAttribute(
            @PathVariable String key, 
            HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        
        Map<String, Object> response = new HashMap<>();
        if (session != null) {
            Object value = session.getAttribute(key);
            response.put("sessionId", session.getId());
            response.put("key", key);
            response.put("value", value);
        } else {
            response.put("message", "No session found");
        }
        response.put("serverPort", serverPort);
        return response;
    }

    @GetMapping("/test")
    public Map<String, Object> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "API is working without authentication");
        response.put("serverPort", serverPort);
        response.put("timestamp", LocalDateTime.now());
        response.put("cspEnabled", "Content Security Policy headers are active");
        return response;
    }
}

