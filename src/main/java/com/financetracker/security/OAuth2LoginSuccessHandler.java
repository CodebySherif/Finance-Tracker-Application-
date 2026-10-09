package com.financetracker.security;

import com.financetracker.model.User;
import com.financetracker.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/** After Google confirms identity: find/create our User, issue our own JWT, hand it to the SPA. */
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        String email = principal.getAttribute("email");
        User user = userService.findOrCreateGoogleUser(email);
        String token = jwtUtil.generateToken(user.getUsername());

        String url = UriComponentsBuilder.fromPath("/index.html")
                .queryParam("token", token)
                .queryParam("username", user.getUsername())
                .build().encode().toUriString();
        response.sendRedirect(url);
    }
}
