package com.github.czsurvey.project.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * @author YanYu
 */
@AutoConfiguration
@ComponentScan(basePackages = "com.github.czsurvey.project")
@EntityScan(basePackages = "com.github.czsurvey.project.entity")
@EnableJpaRepositories(basePackages = "com.github.czsurvey.project")
public class ProjectConfiguration {
}
