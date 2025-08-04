package com.example.cluster.config;

import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.hazelcast.config.annotation.web.http.EnableHazelcastHttpSession;

/**
 * Configuration for Hazelcast, used for distributed HTTP session management.
 * This class sets up the Hazelcast instance and defines how sessions are stored and managed
 * across the cluster nodes.
 */
@Configuration // Marks this class as a source of bean definitions.
@EnableHazelcastHttpSession(maxInactiveIntervalInSeconds = 1800) // Enables Hazelcast as the HTTP session store with a 30-minute inactive interval.
public class HazelcastConfig {

    // Injects the cluster name from application properties, defaulting to 'spring-boot-cluster'.
    @Value("${hazelcast.cluster.name:spring-boot-cluster}")
    private String clusterName;

    // Injects the network port for Hazelcast, defaulting to 5701.
    @Value("${hazelcast.network.port:5701}")
    private int networkPort;

    // Injects whether Hazelcast should auto-increment the port if the default is in use.
    @Value("${hazelcast.network.port-auto-increment:true}")
    private boolean portAutoIncrement;

    // Injects the list of known cluster members for TCP/IP discovery.
    @Value("${hazelcast.network.members:127.0.0.1:5701,127.0.0.1:5702}")
    private String[] members;

    /**
     * Defines the Hazelcast configuration bean.
     * This method sets up the cluster name, network settings, session map configuration, and other properties.
     * @return A configured Hazelcast Config object.
     */
    @Bean
    public Config hazelcastConfig() {
        Config config = new Config();
        
        // Set the name of the Hazelcast cluster. All members in the same cluster must have the same name.
        config.setClusterName(clusterName);
        
        // Configure network settings for Hazelcast members.
        NetworkConfig networkConfig = config.getNetworkConfig();
        networkConfig.setPort(networkPort); // Set the starting port for Hazelcast.
        networkConfig.setPortAutoIncrement(portAutoIncrement); // Allow port to increment if the set port is in use.
        
        // Configure how Hazelcast members discover each other.
        JoinConfig joinConfig = networkConfig.getJoin();
        // Disable multicast discovery, as it's often not suitable for cloud environments or specific network setups.
        joinConfig.getMulticastConfig().setEnabled(false);
        
        // Enable and configure TCP/IP discovery for explicit member listing.
        TcpIpConfig tcpIpConfig = joinConfig.getTcpIpConfig();
        tcpIpConfig.setEnabled(true);
        // Add all specified members to the TCP/IP discovery list.
        for (String member : members) {
            tcpIpConfig.addMember(member);
        }
        
        // Configure the map used for storing HTTP sessions.
        MapConfig sessionMapConfig = new MapConfig();
        sessionMapConfig.setName("spring:session:sessions"); // The name of the map where Spring Session stores sessions.
        sessionMapConfig.setTimeToLiveSeconds(1800); // Maximum time in seconds for an entry to stay in the map (30 minutes).
        sessionMapConfig.setMaxIdleSeconds(900); // Maximum time in seconds for an entry to be idle in the map (15 minutes).
        
        // Configure backup settings for high availability of sessions.
        sessionMapConfig.setBackupCount(1); // Number of synchronous backups. Ensures session data is replicated to 1 other node.
        sessionMapConfig.setAsyncBackupCount(0); // Number of asynchronous backups.
        
        // Configure the eviction policy for the session map.
        EvictionConfig evictionConfig = new EvictionConfig();
        evictionConfig.setEvictionPolicy(EvictionPolicy.LRU); // Least Recently Used eviction policy.
        evictionConfig.setMaxSizePolicy(MaxSizePolicy.PER_NODE); // Max size policy is per node.
        evictionConfig.setSize(10000); // Max number of entries per node before eviction starts.
        sessionMapConfig.setEvictionConfig(evictionConfig);
        
        config.addMapConfig(sessionMapConfig); // Add the session map configuration to the main Hazelcast configuration.
        
        // Configure Management Center (optional).
        // Management Center provides a web-based UI for monitoring and managing Hazelcast clusters.
        ManagementCenterConfig managementCenterConfig = new ManagementCenterConfig();
        managementCenterConfig.setConsoleEnabled(true); // Enable the Management Center console.
        config.setManagementCenterConfig(managementCenterConfig);
        
        // Configure Hazelcast logging to use SLF4J.
        config.setProperty("hazelcast.logging.type", "slf4j");
        // Set timeout for operations.
        config.setProperty("hazelcast.operation.call.timeout.millis", "30000");
        // Set timeout for backup operations.
        config.setProperty("hazelcast.operation.backup.timeout.millis", "5000");
        
        return config;
    }

    /**
     * Creates and returns a HazelcastInstance bean.
     * This is the main entry point for interacting with the Hazelcast cluster.
     * @return A HazelcastInstance object.
     */
    @Bean
    public HazelcastInstance hazelcastInstance() {
        // Creates a new Hazelcast instance with the defined configuration.
        return Hazelcast.newHazelcastInstance(hazelcastConfig());
    }
}


