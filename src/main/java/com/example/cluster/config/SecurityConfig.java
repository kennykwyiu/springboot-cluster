package com.example.cluster.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

/**
 * Spring Security configuration for the application.
 * This class configures security settings, including disabling authentication
 * for all endpoints and adding various security headers like Content Security Policy (CSP).
 */
@Configuration // Marks this class as a source of bean definitions.
@EnableWebSecurity // Enables Spring Security's web security support and provides the Spring MVC integration.
public class SecurityConfig {

    /**
     * Configures the security filter chain that defines how HTTP requests are handled.
     * @param http The HttpSecurity object to configure.
     * @return A SecurityFilterChain instance.
     * @throws Exception if an error occurs during configuration.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable authentication for all endpoints.
            // This means any request to any URL will be permitted without requiring authentication.
            .authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll() // Allow all requests to be accessed without authentication.
            )
            // Disable CSRF (Cross-Site Request Forgery) protection.
            // This is done because authentication is disabled, and for stateless APIs, CSRF is often not required.
            .csrf(csrf -> csrf.disable())
            // Disable form login.
            // Since authentication is not required, there's no need for a login form.
            .formLogin(form -> form.disable())
            // Disable HTTP Basic authentication.
            // Similar to form login, this is disabled as authentication is not enforced.
            .httpBasic(basic -> basic.disable())
            // Configure various security headers to enhance application security.
            .headers(headers -> headers
                // Prevent the page from being displayed in a frame (iframe, frame, object).
                // This helps to mitigate clickjacking attacks.
                .frameOptions().deny()
                // Prevent browsers from MIME-sniffing a response away from the declared Content-Type.
                // This helps to prevent XSS attacks.
                .contentTypeOptions().and()
                // Configure HTTP Strict Transport Security (HSTS).
                // Forces communication over HTTPS for a specified duration, preventing man-in-the-middle attacks.
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000) // 1 year
                    .includeSubdomains(true) // Apply HSTS to all subdomains as well.
                )
                // Configure Referrer Policy.
                // Controls how much referrer information is included with requests.
                .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                .and()
                // Add custom header writers for additional security headers.
                .addHeaderWriter((request, response) -> {
                    // Content Security Policy (CSP) header.
                    // This header helps to prevent XSS attacks by specifying which content sources are allowed.
                    response.setHeader("Content-Security-Policy", 
                        "default-src 'self'; " + // Only allow resources from the same origin by default.
                        "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " + // Allow scripts from same origin, and inline/eval for development/specific needs.
                        "style-src 'self' 'unsafe-inline'; " + // Allow styles from same origin and inline styles.
                        "img-src 'self' data: https:; " + // Allow images from same origin, data URIs, and HTTPS sources.
                        "font-src 'self'; " + // Allow fonts from same origin.
                        "connect-src 'self'; " + // Allow connections (XHR, WebSockets) to same origin.
                        "media-src 'self'; " + // Allow media (audio, video) from same origin.
                        "object-src 'none'; " + // Disallow <object>, <embed>, or <applet> elements.
                        "child-src 'self'; " + // Allow frames and web workers from same origin.
                        "frame-ancestors 'none'; " + // Prevent embedding the page in iframes, frames, or objects.
                        "form-action 'self'; " + // Only allow form submissions to the same origin.
                        "base-uri 'self';" // Restrict the URLs that can be used in a document's <base> element.
                    );
                    
                    // X-Content-Type-Options header: Prevents MIME-sniffing.
                    response.setHeader("X-Content-Type-Options", "nosniff");
                    // X-Frame-Options header: Prevents clickjacking.
                    response.setHeader("X-Frame-Options", "DENY");
                    // X-XSS-Protection header: Enables the XSS filter in modern web browsers.
                    response.setHeader("X-XSS-Protection", "1; mode=block");
                    // Permissions-Policy header: Controls browser features and APIs.
                    response.setHeader("Permissions-Policy", 
                        "camera=(), microphone=(), geolocation=(), payment=()"); // Disable access to camera, microphone, geolocation, and payment APIs.
                })
            );

        // Build and return the configured SecurityFilterChain.
        return http.build();
    }
}


