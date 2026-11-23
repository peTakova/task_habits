package task_habit.api.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import task_habit.api.config.JwtUtil;
import task_habit.api.model.User;
import task_habit.api.repository.UserRepository;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    public static final String AUTH_COOKIE = "authToken";

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public JwtRequestFilter(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String jwt = this.readTokenFromCookie(request);

        if (jwt != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Claims claims = this.jwtUtil.parseClaims(jwt);
                Long tokenUserId = JwtUtil.userIdOf(claims);
                // Token musí patriť presne tomuto účtu: sedieť musí ID aj email.
                // Inak by po zmazaní účtu / zmene emailu starý token fungoval pre nový účet s rovnakým emailom.
                this.userRepository.findById(tokenUserId == null ? -1L : tokenUserId)
                        .filter(user -> Objects.equals(user.getEmail(), claims.getSubject()))
                        .ifPresent(user -> this.authenticate(user, request));
            } catch (JwtException | IllegalArgumentException e) {
                // Neplatný alebo expirovaný token – request pokračuje ako neprihlásený.
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private void authenticate(User user, HttpServletRequest request) {
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(), user.getPassword(), List.of());
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String readTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (AUTH_COOKIE.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
