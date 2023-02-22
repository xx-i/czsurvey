package com.github.czsurvey.extra.security.config;

import com.github.czsurvey.extra.security.component.JwtAuthenticationFilter;
import com.github.czsurvey.extra.security.component.JwtTokenStore;
import com.github.czsurvey.extra.security.config.properties.IgnoreUrlsProperties;
import com.github.czsurvey.extra.security.config.properties.TokenProperties;
import com.github.czsurvey.extra.security.grant.WxQrcodeAuthenticationProvider;
import com.github.czsurvey.extra.security.service.SystemUserDetailService;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.zalando.problem.spring.web.advice.security.SecurityProblemSupport;

import java.security.Key;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

/**
 * @author YanYu
 */
@AutoConfiguration
@EnableConfigurationProperties({TokenProperties.class, IgnoreUrlsProperties.class})
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Import(SecurityProblemSupport.class)
@RequiredArgsConstructor
@ConditionalOnBean(SystemUserDetailService.class)
public class SecurityConfiguration {

    private final TokenProperties tokenProperties;

    private final IgnoreUrlsProperties ignoreUrlsProperties;

    private final SecurityProblemSupport securityProblemSupport;

    @Getter
    private List<GlobalAuthenticationConfigurerAdapter> globalAuthConfigurers = Collections.emptyList();

    @Bean
    public JwtTokenStore jwtTokenStore(TokenProperties tokenProperties, @Qualifier("jdkSerializeRedisTemplate") RedisTemplate<String, Object> redisTemplate, Key key) {
        return new JwtTokenStore(tokenProperties, redisTemplate, key);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenStore jwtTokenStore) {
        return new JwtAuthenticationFilter(jwtTokenStore);
    }

    @Bean
    public WxQrcodeAuthenticationProvider wxQrcodeAuthenticationProvider(SystemUserDetailService userDetailService) {
        return new WxQrcodeAuthenticationProvider(userDetailService);
    }

    @Bean
    public AuthenticationManager authenticationManager(ObjectPostProcessor<Object> objectPostProcessor, SystemUserDetailService userDetailService) throws Exception {
        AuthenticationManagerBuilder builder = new AuthenticationManagerBuilder(objectPostProcessor);
        for (GlobalAuthenticationConfigurerAdapter config : this.globalAuthConfigurers) {
            builder.apply(config);
        }
        return builder
            .userDetailsService(userDetailService)
            .passwordEncoder(passwordEncoder())
            .and()
            .authenticationProvider(new WxQrcodeAuthenticationProvider(userDetailService))
            .build();
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry = http.authorizeHttpRequests();
        ignoreUrlsProperties
            .getUrls()
            .forEach(url -> {
                String[] urlSplit = url.split(":");
                if (urlSplit.length == 1) {
                    registry.antMatchers(url).permitAll();
                } else {
                    registry.antMatchers(getHttpMethod(urlSplit[0]), urlSplit[1]).permitAll();
                }
            });
        registry
            .antMatchers(HttpMethod.OPTIONS).permitAll()
            .anyRequest().authenticated()
            .and()
            .csrf()
            .disable()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .exceptionHandling()
            .authenticationEntryPoint(securityProblemSupport)
            .accessDeniedHandler(securityProblemSupport)
            .and()
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .cors();
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public Key key() {
        return Keys.hmacShaKeyFor(Base64.getDecoder().decode(tokenProperties.getSecret()));
    }

    @Autowired(required = false)
    public void setGlobalAuthenticationConfigurers(List<GlobalAuthenticationConfigurerAdapter> configurers) {
        configurers.sort(AnnotationAwareOrderComparator.INSTANCE);
        this.globalAuthConfigurers = configurers;
    }

    private HttpMethod getHttpMethod(String method) {
        String methodStr = method.toUpperCase();
        return switch (methodStr) {
            case "GET" -> HttpMethod.GET;
            case "POST" -> HttpMethod.POST;
            case "PUT" -> HttpMethod.PUT;
            case "DELETE" -> HttpMethod.DELETE;
            case "HEAD" -> HttpMethod.HEAD;
            case "PATCH" -> HttpMethod.PATCH;
            case "OPTIONS" -> HttpMethod.OPTIONS;
            case "TRACE" -> HttpMethod.TRACE;
            default -> throw new IllegalArgumentException("找不到HttpMethod: " + method);
        };
    }

    public static void main(String[] args) {
        System.out.println(new BCryptPasswordEncoder().encode("123456"));
    }
}
