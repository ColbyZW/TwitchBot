package com.project.bot.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Data
@ConfigurationProperties(prefix="twitch")
public class TwitchProperties {
   private String oauthUri;
   private String userAuth;
   private String clientId;
   private String clientSecret;
   private String redirectUri;
   private String validateUri;
   private String scope;
   private String botChannel;
}
