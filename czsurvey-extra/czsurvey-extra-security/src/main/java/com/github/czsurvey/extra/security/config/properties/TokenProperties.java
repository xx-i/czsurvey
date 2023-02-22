package com.github.czsurvey.extra.security.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * token配置属性
 * @author YanYu
 */
@Data
@ConfigurationProperties(prefix = "application.token")
public class TokenProperties {

    /** token的过期时间 */
    private Integer expirationInSeconds = 3600 * 12;

    /** token密钥 */
    private String secret;

    /** token请求头 */
    private String header = "Authorization";
}
