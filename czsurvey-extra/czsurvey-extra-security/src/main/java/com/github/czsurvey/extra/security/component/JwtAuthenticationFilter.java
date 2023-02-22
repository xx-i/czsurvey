package com.github.czsurvey.extra.security.component;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * jwt认证过滤器
 * @author YanYu
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION_TOKEN_HEADER = "Authorization";

    public static final String BEARER_TOKEN_PREFIX = "Bearer ";

    private final JwtTokenStore jwtTokenStore;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        jwtTokenStore.getAuthenticationByToken(obtainToken(request))
            .ifPresent(authentication -> SecurityContextHolder.getContext().setAuthentication(authentication));
        filterChain.doFilter(request, response);
    }

    private String obtainToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_TOKEN_HEADER);
        if (StrUtil.isNotBlank(bearerToken) && bearerToken.startsWith(BEARER_TOKEN_PREFIX)) {
            return bearerToken.substring(BEARER_TOKEN_PREFIX.length());
        }
        return null;
    }
}
