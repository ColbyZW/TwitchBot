package com.project.bot.service;

import com.project.bot.model.TwitchMessage;
import com.project.bot.socket.TwitchSocketClient;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class MessageHandler {

    private final MessageParser messageParser;
    public MessageHandler(MessageParser messageParser) {
        this.messageParser = messageParser;
    }

    public TwitchMessage handleMessage(String message, TwitchSocketClient client) {
        TwitchMessage msg = messageParser.parseMessage(message);
        if (msg.isPing()) {
            log.info("Sending PING message");
            client.send("PONG :tmi.twitch.tv");
        }

        return msg;
    }
}
