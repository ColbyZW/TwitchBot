package com.project.bot.service;

import com.project.bot.config.property.TwitchProperties;
import com.project.bot.model.TwitchTokenResponse;
import com.project.bot.socket.TwitchSocketClient;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Set;

@Slf4j
public class Twitch {

    private final TwitchProperties twitchProperties;
    private final TwitchSocketClient client;
    private final MessageHandler messageHandler;
    private final OauthHandler oauthHandler;
    private final Set<String> channelConnections = new HashSet<>();

    public Twitch(TwitchSocketClient client,
                  OauthHandler oauthHandler,
                  MessageHandler messageHandler,
                  TwitchProperties twitchProperties) {
        this.client = client;
        this.oauthHandler = oauthHandler;
        this.messageHandler = messageHandler;
        this.twitchProperties = twitchProperties;
    }

    /**
     * Waits until we receive an access token for the bot
     * Once we receive the bot token we begin connecting to all the queued channels
     * which we have received oauth authorization for
     */
    @PostConstruct
    public void start() {
        oauthHandler.getBotToken()
                .doOnNext(botToken -> {
                    channelConnections.add(botToken.getChannel());
                    client.openConnection();
                    handleSocket(botToken);
                    oauthHandler.getTokenFlux()
                            .doOnNext(token -> {
                                if (!channelConnections.contains(token.getChannel())) {
                                    log.info("Bot is joining channel: {}", token.getChannel());
                                    channelConnections.add(token.getChannel());
                                    client.send("JOIN #" + token.getChannel());
                                }
                            }).subscribe();
                })
                .subscribe();
    }

    /**
     * Opens the initial connection to Twitch IRC
     * Also assigns the message handler
     * @param token a {@link TwitchTokenResponse} containing the bots token
     */
    private void handleSocket(TwitchTokenResponse token) {
        log.info("Creating initial IRC connection for Twitch Bot");
        Mono.fromRunnable(() -> {
                    client.send("CAP REQ :twitch.tv/tags twitch.tv/commands");
                    client.send("PASS oauth:" + token.getToken());
                    client.send("NICK chibirabbitbot");
                })
                .thenMany(client.receive())
                .map(msg -> messageHandler.handleMessage(msg, client))
                .filter(msg -> !msg.isFromTwitch())
                .doOnNext(msg -> log.info("{}", msg))
                .doOnNext(msg -> client.send("PRIVMSG " + msg.getRoomName() + " :TESTING RESPONSE"))
                .subscribe();
    }

    @PreDestroy
    private void stop() {
        client.disconnect();
    }

}
