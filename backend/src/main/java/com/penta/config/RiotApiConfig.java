package com.penta.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class RiotApiConfig {
    
    @Value("${riot.api.key}")
    private String riotApiKey;
    
    @Value("${riot.api.base-url}")
    private String riotApiBaseUrl;
    
    @Value("${riot.api.timeout}")
    private int timeout;
    
    @Bean
    public WebClient riotWebClient() {
        return WebClient.builder()
                .baseUrl(riotApiBaseUrl)
                .defaultHeader("X-Riot-Token", riotApiKey)
                .build();
    }
    
    public String getRiotApiKey() {
        return riotApiKey;
    }
    
    public String getRiotApiBaseUrl() {
        return riotApiBaseUrl;
    }
    
    public int getTimeout() {
        return timeout;
    }
}
