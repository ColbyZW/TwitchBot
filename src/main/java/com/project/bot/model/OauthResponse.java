package com.project.bot.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class OauthResponse {
    private String access_token;
    private int expires_in;
    private String refresh_token;
    private List<String> scope;
    private String token_type;
}
