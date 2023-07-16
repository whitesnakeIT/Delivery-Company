package com.kapusniak.tomasz.config.security;

import com.nimbusds.jose.shaded.gson.internal.LinkedTreeMap;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.kapusniak.tomasz.config.security.Roles.ADMIN;
import static com.kapusniak.tomasz.config.security.Roles.USER;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Profile({"default", "docker", "test"})
class SecurityConfig {

    private final KeycloakLogoutHandler keycloakLogoutHandler;

    private final KeycloakRoleConverter keycloakRoleConverter;

    @Bean
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
    }

    private GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return (authorities) -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>(authorities);

            authorities.forEach(authority -> {
                if (authority instanceof OidcUserAuthority oidcUserAuthority) {
                    OidcIdToken idToken = oidcUserAuthority.getIdToken();
                    LinkedTreeMap<String, Object> realmAccess = idToken.getClaim("realm_access");
                    List<String> roles = (List<String>) realmAccess.get("roles");

                    Set<SimpleGrantedAuthority> authoritiesFromToken = roles.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toSet());

                    mappedAuthorities.addAll(authoritiesFromToken);

                }
            });

            return mappedAuthorities;
        };
    }

    @ConditionalOnProperty(prefix = "com.kapusniak.tomasz.app", name = "security.config", havingValue = "keycloak", matchIfMissing = true)
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors().and()
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers(
                                        HttpMethod.GET,
                                        "/api/v1/customers/*",
                                        "/api/v1/tracking/*",
                                        "/api/v1/deliveries/*",
                                        "/api/v1/orders/*",
                                        "/api/v1/couriers/*"
                                )
                                .hasAnyAuthority(ADMIN.toString(), USER.toString())
                                .requestMatchers(
                                        "/swagger-ui/**",
                                        "/api/v1/**"
                                )
                                .hasAuthority(ADMIN.toString())

                                .anyRequest().hasAuthority(ADMIN.toString())
                );

        http.oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userAuthoritiesMapper(userAuthoritiesMapper())))
                .logout()
                .addLogoutHandler(keycloakLogoutHandler)
                .logoutSuccessUrl("/");

//        http.sessionManagement((sessionManagement) -> sessionManagement
//                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.oauth2ResourceServer()
                .jwt()
                .jwtAuthenticationConverter(keycloakRoleConverter);

        return http.build();
    }

}