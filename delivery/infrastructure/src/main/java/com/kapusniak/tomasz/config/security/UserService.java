package com.kapusniak.tomasz.config.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    public String getUserEmail() {
        Object principal = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        if (principal instanceof Jwt jwt) {
            return (String) jwt.getClaims().get("email");
        }

        throw new IllegalStateException("User not authenticated");
    }
}
