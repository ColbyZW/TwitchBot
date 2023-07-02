package com.project.bot.sockets;

import com.project.bot.config.WebsocketConfig;
import com.project.bot.config.property.WebsocketProperties;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;

@Slf4j
public class TwitchSocket {
    private final WebsocketProperties websocketProperties;

    public TwitchSocket(WebsocketProperties websocketProperties) {
       this.websocketProperties = websocketProperties;
    }

    @PostConstruct
    public void openConnection() {
        WebSocketClient client = new ReactorNettyWebSocketClient();
        client.execute(
                URI.create(websocketProperties.getSocketUrl()),
                session -> session.send(
                                Mono.just(session.textMessage("TESTING")))
                        .thenMany(session.receive()
                                .map(WebSocketMessage::getPayloadAsText)
                                .log())
                        .then())
        .subscribe();
    }
}
