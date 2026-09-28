package com.openclassrooms.etudiant.configuration.security;

import com.openclassrooms.etudiant.service.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Authenticates requests carrying a JWT in the {@code Authorization: Bearer} header.
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailService userDetailService;

    /**
     * Verifies the JWT and account status, then populates the security context.
     * Requests without a Bearer token continue through the chain without authentication by this filter.
     * A rejected token or an unauthorized account results in an HTTP 401 response.
     *
     * @param request the incoming HTTP request
     * @param response the HTTP response to populate if authentication is rejected
     * @param filterChain the filter chain to continue after authentication
     * @throws ServletException if processing the filter chain fails
     * @throws IOException if an input or output operation fails
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");

        if (authorization == null
                || !authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7).trim();

        try {
            String login = jwtService.extractLogin(token);

            if (login == null || login.isBlank()) {
                throw new IllegalArgumentException("Login absent du token");
            }

            UserDetails user = userDetailService.loadUserByUsername(login);

            // Vérifie notamment que le compte est actif et non verrouillé.
            new AccountStatusUserDetailsChecker().check(user);

            var authentication =
                    new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            user.getAuthorities()
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request)
            );

            var context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

        } catch (JwtException | IllegalArgumentException
                 | AuthenticationException exception) {

            SecurityContextHolder.clearContext();
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Token invalide, expiré ou compte non autorisé"
            );
            return;
        }

        filterChain.doFilter(request, response);
    }
}