package com.example.cluster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.session.hazelcast.config.annotation.web.http.EnableHazelcastHttpSession;

/**
 * Main entry point for the Spring Boot Cluster Application.
 * This class configures and launches the Spring Boot application.
 */
@SpringBootApplication // Marks this class as a Spring Boot application.
@EnableHazelcastHttpSession // Enables Hazelcast for HTTP session management, allowing sessions to be shared across cluster nodes.
public class ClusterApplication {

    public static void main(String[] args) {
        // Starts the Spring Boot application.
        SpringApplication.run(ClusterApplication.class, args);
    }
}


