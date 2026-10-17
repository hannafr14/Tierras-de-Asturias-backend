package com.tierrasdeasturias.backend.services;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

public class JsonRememberMeServices
        extends PersistentTokenBasedRememberMeServices {

    public JsonRememberMeServices(
            String key,
            UserDetailsService userDetailsService,
            PersistentTokenRepository tokenRepository
    ) {
        super(key, userDetailsService, tokenRepository);
    }

    @Override
    protected boolean rememberMeRequested(
            HttpServletRequest request,
            String parameter
    ) {
        return Boolean.TRUE.equals(request.getAttribute("rememberMe"));
    }
}
