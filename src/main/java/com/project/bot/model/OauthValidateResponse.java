package com.project.bot.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class OauthValidateResponse {
    private String client_id;
    private String login;
    private List<String> scopes;
    private String user_id;
    private int expires_in;
}
