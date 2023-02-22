package com.github.czsurvey.project.config;

import com.github.czsurvey.extra.security.model.LoginUser;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.EntityManager;
import java.util.Optional;

/**
 * jpa审计配置
 */
@AutoConfiguration
@EnableJpaAuditing
public class JpaConfiguration {

    @Bean
    public AuditorAware<Long> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
                return Optional.of(loginUser).map(LoginUser::getId);
            }
            return Optional.empty();
        };
    }

    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
        return new JPAQueryFactory(entityManager);
    }
}