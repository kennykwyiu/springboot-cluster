package com.example.cluster.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for handling API requests.
 * Provides endpoints for health checks, application information, session management, and general testing.
 */
@RestController // Marks this class as a REST controller, meaning it handles incoming web requests.
@RequestMapping("/api") // Base path for all endpoints in this controller.
public class ApiController {

    // Injects the server port from application properties, defaulting to 8080.
    @Value("${server.port:8080}")
    private String serverPort;

    // Injects the application name from application properties, defaulting to 'cluster-app'.
    @Value("${spring.application.name:cluster-app}")
    private String applicationName;

    /**
     * Health check endpoint.
     * Returns the application status, timestamp, port, and application name.
     * Accessible at /api/health.
     * @return A map containing health information.
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP"); // Indicates the application is running.
        response.put("timestamp", LocalDateTime.now()); // Current server time.
        response.put("port", serverPort); // The port on which this specific node is running.
        response.put("application", applicationName); // The name of the application.
        return response;
    }

    /**
     * Information endpoint.
     * Returns details about the server, including port, application name, and remote address.
     * Accessible at /api/info.
     * @param request The HttpServletRequest object to get request details.
     * @return A map containing server and application information.
     */
    @GetMapping("/info")
    public Map<String, Object> info(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("serverPort", serverPort); // The port of the current server.
        response.put("applicationName", applicationName); // The name of the application.
        response.put("remoteAddr", request.getRemoteAddr()); // The IP address of the client.
        response.put("serverName", request.getServerName()); // The hostname of the server.
        response.put("serverPort", request.getServerPort()); // The port the request was received on.
        response.put("timestamp", LocalDateTime.now()); // Current server time.
        return response;
    }

    /**
     * Session information endpoint.
     * Retrieves and returns details about the current HTTP session.
     * Accessible at /api/session.
     * @param request The HttpServletRequest object to get session details.
     * @return A map containing session information.
     */
    @GetMapping("/session")
    public Map<String, Object> sessionInfo(HttpServletRequest request) {
        HttpSession session = request.getSession(true); // Get the current session, create if it doesn't exist.
        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", session.getId()); // Unique ID of the session.
        response.put("creationTime", session.getCreationTime()); // Time when the session was created.
        response.put("lastAccessedTime", session.getLastAccessedTime()); // Last time the client sent a request associated with this session.
        response.put("maxInactiveInterval", session.getMaxInactiveInterval()); // Maximum time interval, in seconds, that the servlet container will keep this session open between client accesses.
        response.put("serverPort", serverPort); // The port of the current server handling the request.
        response.put("timestamp", LocalDateTime.now()); // Current server time.
        return response;
    }

    /**
     * Endpoint to set a session attribute.
     * Accessible via POST to /api/session/attribute with 'key' and 'value' parameters.
     * @param key The key for the session attribute.
     * @param value The value to set for the session attribute.
     * @param request The HttpServletRequest object.
     * @return A map confirming the attribute was set.
     */
    @PostMapping("/session/attribute")
    public Map<String, Object> setSessionAttribute(
            @RequestParam String key, // Request parameter for the attribute key.
            @RequestParam String value, // Request parameter for the attribute value.
            HttpServletRequest request) {
        HttpSession session = request.getSession(true); // Get the current session, create if it doesn't exist.
        session.setAttribute(key, value); // Set the attribute in the session.
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Attribute set successfully");
        response.put("sessionId", session.getId()); // Return the session ID.
        response.put("key", key); // Return the key that was set.
        response.put("value", value); // Return the value that was set.
        response.put("serverPort", serverPort); // The port of the current server.
        return response;
    }

    /**
     * Endpoint to get a session attribute.
     * Accessible via GET to /api/session/attribute/{key}.
     * @param key The key of the session attribute to retrieve.
     * @param request The HttpServletRequest object.
     * @return A map containing the session attribute value, or a message if no session is found.
     */
    @GetMapping("/session/attribute/{key}")
    public Map<String, Object> getSessionAttribute(
            @PathVariable String key, // Path variable for the attribute key.
            HttpServletRequest request) {
        HttpSession session = request.getSession(false); // Get the current session, do not create if it doesn't exist.
        
        Map<String, Object> response = new HashMap<>();
        if (session != null) {
            Object value = session.getAttribute(key); // Retrieve the attribute value.
            response.put("sessionId", session.getId()); // Return the session ID.
            response.put("key", key); // Return the key.
            response.put("value", value); // Return the retrieved value.
        } else {
            response.put("message", "No session found"); // Message if no active session exists.
        }
        response.put("serverPort", serverPort); // The port of the current server.
        return response;
    }

    /**
     * Simple test endpoint to confirm API is working without authentication.
     * Also indicates that CSP headers are active.
     * Accessible at /api/test.
     * @return A map confirming the API functionality and CSP status.
     */
    @GetMapping("/test")
    public Map<String, Object> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "API is working without authentication"); // Confirms authentication is disabled.
        response.put("serverPort", serverPort); // The port of the current server.
        response.put("timestamp", LocalDateTime.now()); // Current server time.
        response.put("cspEnabled", "Content Security Policy headers are active"); // Indicates CSP is applied.
        return response;
    }
}


