package com.project.bot.config;

import com.project.bot.config.property.TwitchProperties;
import com.project.bot.config.property.WebsocketProperties;
import com.project.bot.controller.OauthController;
import com.project.bot.service.MessageHandler;
import com.project.bot.service.MessageParser;
import com.project.bot.service.OauthHandler;
import com.project.bot.service.Twitch;
import com.project.bot.socket.TwitchSocketClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableConfigurationProperties({WebsocketProperties.class, TwitchProperties.class})
public class TwitchConfig {

    @Bean
    public Twitch twitchSocket(TwitchSocketClient client,
                               OauthHandler oauthHandler,
                               MessageHandler messageHandler,
                               TwitchProperties twitchProperties) {
        return new Twitch(client, oauthHandler, messageHandler, twitchProperties);
    }

    @Bean
    public TwitchSocketClient twitchSocketClient(WebsocketProperties websocketProperties) {
       return new TwitchSocketClient(websocketProperties);
    }

    @Bean
    public MessageHandler messageHandlingService(MessageParser messageParser) {
        return new MessageHandler(messageParser);
    }

    @Bean
    public MessageParser messageParser() {
        return new MessageParser();
    }

    @Bean
    public OauthHandler oauthHandler(TwitchProperties twitchProperties) {
        return new OauthHandler(twitchProperties);
    }

}
