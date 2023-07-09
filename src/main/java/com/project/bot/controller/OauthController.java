package com.project.bot.controller;

import com.project.bot.config.property.TwitchProperties;
import com.project.bot.model.OauthResponse;
import com.project.bot.service.OauthHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
@RequestMapping("oauth")
@Slf4j
public class OauthController {
    private final OauthHandler oauthHandler;
    private final TwitchProperties twitchProperties;
    public OauthController(OauthHandler oauthHandler, TwitchProperties twitchProperties) {
       this.oauthHandler = oauthHandler;
       this.twitchProperties = twitchProperties;
    }

    @GetMapping("redirect")
    public void oauthCallback(@RequestParam String code,
                              @RequestParam String scope) {
        MultiValueMap<String, String> payload = new LinkedMultiValueMap<>();
        payload.add("client_id", twitchProperties.getClientId());
        payload.add("client_secret", twitchProperties.getClientSecret());
        payload.add("code", code);
        payload.add("grant_type", "authorization_code");
        payload.add("redirect_uri", twitchProperties.getRedirectUri());;

        WebClient client = WebClient.create();
        client.post()
                .uri(twitchProperties.getOauthUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromFormData(payload))
                .retrieve()
                .bodyToFlux(OauthResponse.class)
                .doOnNext(res -> {
                    oauthHandler.setCode(code);
                    oauthHandler.addTokenPair(res.getAccess_token(), res.getRefresh_token());
                })
                .subscribe();
   }

    @GetMapping("authorize")
    public Mono<Void> redirectToOauthFlow(ServerHttpResponse response) {
        String redirectUrl = twitchProperties.getUserAuth() + "?" + "response_type=code" +
                "&client_id=" + twitchProperties.getClientId() +
                "&redirect_uri=" + twitchProperties.getRedirectUri() +
                "&scope=" + twitchProperties.getScope();

        response.setStatusCode(HttpStatus.PERMANENT_REDIRECT);
        response.getHeaders().setLocation(URI.create(redirectUrl));
        return response.setComplete();
    }

}
