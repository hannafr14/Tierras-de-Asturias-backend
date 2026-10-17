package com.tierrasdeasturias.backend.config;

import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import com.tierrasdeasturias.backend.services.JsonRememberMeServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
public class RememberMeConfig {

    @Bean
    public PersistentTokenRepository persistentTokenRepository(
            DataSource dataSource
    ) {
        var repository = new JdbcTokenRepositoryImpl();
        repository.setDataSource(dataSource);
        return repository;
    }

    @Bean
    public JsonRememberMeServices rememberMeServices(
            @Value("${app.security.remember-me.key}") String key,
            @Value("${app.security.remember-me.validity-seconds}") int validitySeconds,
            UserDetailsService userDetailsService,
            PersistentTokenRepository tokenRepository
    ) {
        var services = new JsonRememberMeServices(
                key, userDetailsService, tokenRepository
        );

        services.setTokenValiditySeconds(validitySeconds);
        services.setCookieCustomizer(cookie -> {
            cookie.setHttpOnly(true);
            cookie.setAttribute("SameSite", "Lax");
        });

        return services;
    }
}
