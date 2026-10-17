package com.tierrasdeasturias.backend.controllers;

import com.tierrasdeasturias.backend.dtos.AuthResponseDto;
import com.tierrasdeasturias.backend.dtos.LoginRequestDto;
import com.tierrasdeasturias.backend.dtos.RegisterRequestDto;
import com.tierrasdeasturias.backend.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import com.tierrasdeasturias.backend.exceptions.InvalidCredentialsException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.core.Authentication;

import java.util.List;
import com.tierrasdeasturias.backend.services.JsonRememberMeServices;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final JsonRememberMeServices rememberMeServices;
    private final HttpSessionSecurityContextRepository securityContextRepository =
        new HttpSessionSecurityContextRepository();

    private final SessionAuthenticationStrategy sessionAuthenticationStrategy;

    public AuthController(
            AuthService authService,
            AuthenticationManager authenticationManager,
            CsrfTokenRepository csrfTokenRepository,
            JsonRememberMeServices rememberMeServices
    ) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.rememberMeServices = rememberMeServices;
        this.sessionAuthenticationStrategy =
                new CompositeSessionAuthenticationStrategy(List.of(
                        new ChangeSessionIdAuthenticationStrategy(),
                        new CsrfAuthenticationStrategy(csrfTokenRepository)
                ));
    }

    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken csrfToken) {
        return csrfToken;
    }

    @GetMapping("/me")
    public AuthResponseDto me(Authentication authentication) {
        return authService.getUserByEmail(authentication.getName());
    }

    @PostMapping("/register")
    public AuthResponseDto register(@Valid @RequestBody RegisterRequestDto request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponseDto login(
            @Valid @RequestBody LoginRequestDto loginRequest,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        try {
            var authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            AuthResponseDto user = authService.getUserByEmail(authentication.getName());

            sessionAuthenticationStrategy.onAuthentication(
                    authentication, request, response
            );

            var context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, request, response);
            request.setAttribute("rememberMe", loginRequest.isRememberMe());

            if (loginRequest.isRememberMe()) {
                rememberMeServices.loginSuccess(request, response, authentication);
            } else {
                rememberMeServices.loginFail(request, response);
            }

            return user;
        } catch (AuthenticationException exception) {
            rememberMeServices.loginFail(request, response);
            throw new InvalidCredentialsException("Invalid email or password");
        }
    }
}