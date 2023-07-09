package com.project.bot.service;

import com.project.bot.config.property.TwitchProperties;
import com.project.bot.model.OauthResponse;
import com.project.bot.model.OauthValidateResponse;
import com.project.bot.model.TwitchTokenResponse;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.net.URI;
import java.time.Duration;
import java.util.HashMap;

@Slf4j
public class OauthHandler {
    private final TwitchProperties twitchProperties;
    private String oauthCode;
    private boolean startedHealthCheck = false;
    private HashMap<String ,String> accessTokenToRefreshToken;
    private HashMap<String, String> accessTokenToChannel;
    private final Sinks.Many<TwitchTokenResponse> tokenResponseSink = Sinks.many().unicast().onBackpressureBuffer();
    private final Sinks.One<TwitchTokenResponse> botToken = Sinks.one();

    public OauthHandler(TwitchProperties twitchProperties) {
        this.twitchProperties = twitchProperties;
    }

    public void setCode(String code) {
        oauthCode = code;
    }

    public void addTokenPair(String accessToken, String refreshToken) {
        if (accessTokenToRefreshToken == null) {
            accessTokenToRefreshToken = new HashMap<>();
        }
        accessTokenToRefreshToken.put(accessToken, refreshToken);

        // Start the periodic token health check in the background
        if (!startedHealthCheck) {
            startTokenHealthCheck();
        }
        checkTokenHealth(accessToken);
    }

    public void addChannelTokenPair(String channel, String accessToken) {
        if (accessTokenToChannel == null) {
            accessTokenToChannel = new HashMap<>();
        }
        accessTokenToChannel.put(accessToken, channel);
    }

    public Flux<TwitchTokenResponse> getTokenFlux() {
        return tokenResponseSink.asFlux();
    }

    public Mono<TwitchTokenResponse> getBotToken() {
        return botToken.asMono();
    }

    private void startTokenHealthCheck() {
        startedHealthCheck = true;
        Flux.interval(Duration.ofMinutes(30))
                .flatMap(tick -> Flux.fromIterable(accessTokenToRefreshToken.keySet()))
                .doOnNext(this::checkTokenHealth)
                .subscribe();
    }

    private void checkTokenHealth(String token) {
        WebClient client = WebClient.create();
        client.get()
                .uri(URI.create(twitchProperties.getValidateUri()))
                .header("Authorization", "OAuth " + token)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, res -> {
                    log.error("Token invalid, refreshing");
                    refreshCode(token);
                    return Mono.empty();
                })
                .bodyToFlux(OauthValidateResponse.class)
                .onErrorComplete()
                .doOnNext(res -> {
                    addChannelTokenPair(res.getLogin(), token);
                    TwitchTokenResponse tokenResponse = new TwitchTokenResponse();
                    tokenResponse.setChannel(res.getLogin());
                    tokenResponse.setToken(token);
                    tokenResponseSink.tryEmitNext(tokenResponse);

                    if (res.getLogin().equals(twitchProperties.getBotChannel())) {
                        botToken.tryEmitValue(tokenResponse);
                    }
                })
                .subscribe();
    }


    private void refreshCode(String token) {
        MultiValueMap<String, String> payload = new LinkedMultiValueMap<>();
        payload.add("client_id", twitchProperties.getClientId());
        payload.add("client_secret", twitchProperties.getClientSecret());
        payload.add("grant_type", "refresh_token");
        payload.add("refresh_token", accessTokenToRefreshToken.get(token));

        WebClient client = WebClient.create();
        client.post()
                .uri(twitchProperties.getOauthUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromFormData(payload))
                .retrieve()
                .bodyToFlux(OauthResponse.class)
                .doOnNext(res -> {
                    log.info("Got response back from refresh: {}", res);
                    String channel = accessTokenToChannel.get(token);
                    accessTokenToChannel.remove(token);
                    accessTokenToRefreshToken.remove(token);
                    addTokenPair(res.getAccess_token(), res.getRefresh_token());
                    addChannelTokenPair(channel, res.getAccess_token());
                })
                .subscribe();
    }
}
