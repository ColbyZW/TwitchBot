package com.project.bot.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.bot.model.MessageType;
import com.project.bot.model.TwitchMessage;
import lombok.extern.slf4j.Slf4j;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.HashMap;

@Slf4j
public class MessageParser {

    private final ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public MessageParser() {
    }

    public TwitchMessage parseMessage(String message) {
        if (message.startsWith("PING")) {
            return new TwitchMessage(true, true);
        }
        if (message.contains("PRIVMSG")) {
            return parsePrivMsg(message);
        }
        return new TwitchMessage(true, false);
    }

    public TwitchMessage parsePrivMsg(String message) {
        StringBuilder key = new StringBuilder();
        StringBuilder value = new StringBuilder();
        HashMap<String, String> keyValues = new HashMap<>();
        boolean buildingKey = true;
        CharacterIterator iter = new StringCharacterIterator(message);

        while (iter.current() != CharacterIterator.DONE) {
            char cur = iter.current();
            if (cur == '=') {
                buildingKey = false;
                iter.next();
                continue;
            }
            if (cur == ';') {
                iter.next();
                buildingKey = true;
                keyValues.put(key.toString(), value.toString());
                key.setLength(0);
                value.setLength(0);
                continue;
            }

            if (buildingKey) {
                if (cur == '-') {
                    cur = Character.toUpperCase(iter.next());
                }
                key.append(cur);
            } else {
                value.append(cur);
            }

            iter.next();
        }
        // Add on the final key-value pair
        keyValues.put(key.toString(), value.toString());

        // Parses out the actual chat message
        // Sometimes it gets mingled within the userType
        String[] split = message.split("PRIVMSG", 2);
        if (split.length > 1) {
            String[] tokens = split[1].split(":", 2);

            keyValues.put("roomName", tokens[0].trim());
            keyValues.put("chatMessage", tokens[1].trim());
        }



        TwitchMessage msg = mapper.convertValue(keyValues, TwitchMessage.class);
        msg.setMessageType(MessageType.CHAT_MESSAGE);
        return msg;
    }
}
