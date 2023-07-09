package com.project.bot.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;

@Data
@NoArgsConstructor
public class TwitchMessage {
    @JsonDeserialize(using = NumericDeserializer.class)
    private boolean mod;

    @JsonDeserialize(using = NumericDeserializer.class)
    private boolean subscriber;

    private MessageType messageType;
    private boolean isFromTwitch;
    private boolean isPing;
    private String displayName;
    private String userId;
    private String roomId;
    private String id;
    private String chatMessage;
    private String roomName;

    public TwitchMessage(boolean isFromTwitch, boolean isPing) {
        this.isFromTwitch = isFromTwitch;
        this.isPing = isPing;
    }
}

class NumericDeserializer extends JsonDeserializer<Boolean> {
    @Override
    public Boolean deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        if ("1".equals(p.getText())) {
            return Boolean.TRUE;
        }
        if ("0".equals(p.getText())) {
            return Boolean.FALSE;
        }
        return null;
    }
}
