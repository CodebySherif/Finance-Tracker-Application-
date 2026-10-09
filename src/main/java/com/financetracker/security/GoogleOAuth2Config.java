package com.financetracker.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

/**
 * Deliberately uses our own "app.oauth2.google.*" property namespace rather
 * than Spring Boot's "spring.security.oauth2.client.registration.*" one.
 * Spring Boot's own OAuth2 auto-configuration throws a hard startup error if
 * that namespace is present but blank, which would break the app for anyone
 * who hasn't set up Google credentials yet. This namespace + the
 * @ConditionalOnProperty guard below means: no client-id configured -> this
 * bean simply doesn't exist -> SecurityConfig skips oauth2Login() entirely ->
 * the app runs exactly as it did before Google was ever added.
 */
@Configuration
public class GoogleOAuth2Config {

    @Value("${app.oauth2.google.client-id:}")
    private String clientId;

    @Value("${app.oauth2.google.client-secret:}")
    private String clientSecret;

    @Bean
    @ConditionalOnProperty(prefix = "app.oauth2.google", name = "client-id")
    public ClientRegistrationRepository clientRegistrationRepository() {
        ClientRegistration google = CommonOAuth2Provider.GOOGLE.getBuilder("google")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .scope("openid", "email", "profile")
                .build();
        return new InMemoryClientRegistrationRepository(google);
    }
}
