package com.project.bot.socket;


import com.project.bot.config.property.WebsocketProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.net.URI;

@Slf4j
public class TwitchSocketClient {
    private final WebsocketProperties websocketProperties;
    private Sinks.Many<String> sendBuffer;
    private Sinks.Many<String> receiveBuffer;
    private WebSocketSession session;
    private Disposable subscription;
    private boolean isOpen;

    public TwitchSocketClient(WebsocketProperties websocketProperties) {
        this.websocketProperties = websocketProperties;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void send(String payload) {
        sendBuffer.tryEmitNext(payload);
    }

    public Flux<String> receive() {
        return receiveBuffer.asFlux();
    }

    /**
     * Instantiates the send and receive buffer for the WebsocketClient
     */
    public void openConnection() {
        sendBuffer = Sinks.many().unicast().onBackpressureBuffer();
        receiveBuffer = Sinks.many().unicast().onBackpressureBuffer();

        WebSocketClient client = new ReactorNettyWebSocketClient();

        subscription = client.execute(
                URI.create(websocketProperties.getSocketUrl()), this::handleSession)
                .then(Mono.fromRunnable(this::onClose))
                .subscribe();

        log.info("Opened websocket connection");
        isOpen = true;
    }

    private Mono<Void> handleSession(WebSocketSession session) {
       this.session = session;

       Mono<Void> input = session.receive()
               .map(WebSocketMessage::getPayloadAsText)
               .doOnNext(receiveBuffer::tryEmitNext)
               .then();

       Mono<Void> output = session.send(sendBuffer.asFlux().map(session::textMessage));

       return Mono.zip(input, output).then();
    }

    private void onClose() {
        session = null;
        isOpen = false;

        log.info("Closed session");
    }

    public void disconnect() {
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
            subscription = null;

            onClose();
        }
        log.info("Client closed");
    }
}
