package com.example.cluster.config;

import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.hazelcast.config.annotation.web.http.EnableHazelcastHttpSession;

@Configuration
@EnableHazelcastHttpSession(maxInactiveIntervalInSeconds = 1800) // 30 minutes
public class HazelcastConfig {

    @Value("${hazelcast.cluster.name:spring-boot-cluster}")
    private String clusterName;

    @Value("${hazelcast.network.port:5701}")
    private int networkPort;

    @Value("${hazelcast.network.port-auto-increment:true}")
    private boolean portAutoIncrement;

    @Value("${hazelcast.network.members:127.0.0.1:5701,127.0.0.1:5702}")
    private String[] members;

    @Bean
    public Config hazelcastConfig() {
        Config config = new Config();
        
        // Set cluster name
        config.setClusterName(clusterName);
        
        // Configure network settings
        NetworkConfig networkConfig = config.getNetworkConfig();
        networkConfig.setPort(networkPort);
        networkConfig.setPortAutoIncrement(portAutoIncrement);
        
        // Configure join settings
        JoinConfig joinConfig = networkConfig.getJoin();
        joinConfig.getMulticastConfig().setEnabled(false);
        
        // Configure TCP/IP cluster discovery
        TcpIpConfig tcpIpConfig = joinConfig.getTcpIpConfig();
        tcpIpConfig.setEnabled(true);
        for (String member : members) {
            tcpIpConfig.addMember(member);
        }
        
        // Configure session map
        MapConfig sessionMapConfig = new MapConfig();
        sessionMapConfig.setName("spring:session:sessions");
        sessionMapConfig.setTimeToLiveSeconds(1800); // 30 minutes
        sessionMapConfig.setMaxIdleSeconds(900); // 15 minutes
        
        // Configure backup settings for high availability
        sessionMapConfig.setBackupCount(1);
        sessionMapConfig.setAsyncBackupCount(0);
        
        // Configure eviction policy
        EvictionConfig evictionConfig = new EvictionConfig();
        evictionConfig.setEvictionPolicy(EvictionPolicy.LRU);
        evictionConfig.setMaxSizePolicy(MaxSizePolicy.PER_NODE);
        evictionConfig.setSize(10000);
        sessionMapConfig.setEvictionConfig(evictionConfig);
        
        config.addMapConfig(sessionMapConfig);
        
        // Configure management center (optional)
        ManagementCenterConfig managementCenterConfig = new ManagementCenterConfig();
        managementCenterConfig.setConsoleEnabled(true);
        config.setManagementCenterConfig(managementCenterConfig);
        
        // Configure logging
        config.setProperty("hazelcast.logging.type", "slf4j");
        config.setProperty("hazelcast.operation.call.timeout.millis", "30000");
        config.setProperty("hazelcast.operation.backup.timeout.millis", "5000");
        
        return config;
    }

    @Bean
    public HazelcastInstance hazelcastInstance() {
        return Hazelcast.newHazelcastInstance(hazelcastConfig());
    }
}

