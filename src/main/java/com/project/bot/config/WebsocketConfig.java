package com.project.bot.config;

import com.project.bot.config.property.WebsocketProperties;
import com.project.bot.sockets.TwitchSocket;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(WebsocketProperties.class)
public class WebsocketConfig {
    @Bean
    public TwitchSocket twitchSocket(WebsocketProperties websocketProperties) {
        return new TwitchSocket(websocketProperties);
    }

}
