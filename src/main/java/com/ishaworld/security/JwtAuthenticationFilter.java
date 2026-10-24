package com.ishaworld.security;

import com.ishaworld.user.repository.AppUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AdminUserDetailsService userDetailsService;
    private final AppUserRepository appUserRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            AdminUserDetailsService userDetailsService,
            AppUserRepository appUserRepository
    ) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.appUserRepository = appUserRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");

        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            try {
                if (jwtService.isValid(token)) {
                    AbstractAuthenticationToken authentication = JwtService.USER_ROLE.equals(jwtService.extractRole(token))
                            ? userAuthentication(jwtService.extractSubject(token))
                            : adminAuthentication(jwtService.extractSubject(token));
                    if (authentication != null) {
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            } catch (RuntimeException ex) {
                // An unusable token is treated as anonymous; protected routes then answer 401.
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private AbstractAuthenticationToken adminAuthentication(String email) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    private AbstractAuthenticationToken userAuthentication(String userId) {
        return appUserRepository.findById(Long.valueOf(userId))
                .filter(user -> user.isActive())
                .map(user -> new UsernamePasswordAuthenticationToken(
                        new AuthenticatedUser(user.getId(), user.getEmail()),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + JwtService.USER_ROLE))
                ))
                .orElse(null);
    }
}
