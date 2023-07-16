package com.kapusniak.tomasz.config.security;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Profile({"test", "jdbc"})
@Configuration
@EnableAutoConfiguration(exclude = OAuth2ClientAutoConfiguration.class)
public class SecurityDisabledConfig {
    @Bean
    @ConditionalOnProperty(prefix = "com.kapusniak.tomasz.app", name = "security.config", havingValue = "off")
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeHttpRequests(auth ->
                        auth
                                .anyRequest().permitAll());

        return http.build();
    }
}
