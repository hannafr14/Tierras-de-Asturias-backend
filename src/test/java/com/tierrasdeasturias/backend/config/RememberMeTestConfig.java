package com.tierrasdeasturias.backend.config;

import com.tierrasdeasturias.backend.services.JsonRememberMeServices;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.InMemoryTokenRepositoryImpl;

@TestConfiguration
public class RememberMeTestConfig {

    @Bean
    public JsonRememberMeServices rememberMeServices(
            UserDetailsService userDetailsService
    ) {
        var services = new JsonRememberMeServices(
                "remember-me-test-key",
                userDetailsService,
                new InMemoryTokenRepositoryImpl()
        );

        services.setTokenValiditySeconds(604800);
        services.setCookieCustomizer(cookie -> {
            cookie.setHttpOnly(true);
            cookie.setAttribute("SameSite", "Lax");
        });

        return services;
    }
}
