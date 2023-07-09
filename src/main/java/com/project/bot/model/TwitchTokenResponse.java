package com.project.bot.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TwitchTokenResponse {
    private String token;
    private String channel;
}
