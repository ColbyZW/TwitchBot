package com.project.bot.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Data
@ConfigurationProperties(prefix="twitch")
public class WebsocketProperties {
    private String socketUrl;
}
