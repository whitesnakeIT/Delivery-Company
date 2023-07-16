package com.kapusniak.tomasz.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile({"default"})
@Configuration
@EnableCaching
public class CachingConfiguration {
}
