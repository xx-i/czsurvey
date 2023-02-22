package com.github.czsurvey.extra.security.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 配置白名单
 * @author YanYu
 */
@Data
@ConfigurationProperties(prefix = "application.security.ignore-urls")
public class IgnoreUrlsProperties {

    private List<String> urls = new ArrayList<>();

}
