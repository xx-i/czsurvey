package com.github.czsurvey.extra.security.component;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.github.czsurvey.extra.security.config.properties.TokenProperties;
import com.github.czsurvey.extra.security.model.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;

import java.security.Key;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author YanYu
 */
@Slf4j
@RequiredArgsConstructor
public class JwtTokenStore {

    public static final String CLAIM_KEY_SUBJECT = "sub";

    public static final String CLAIM_KEY_ISSUED_AT = "iat";

    public static final String CLAIM_KEY_ID = "jti";

    public static final String LOGIN_TOKEN_KEY = "login_token:";

    private final TokenProperties tokenProperties;

    private final RedisTemplate<String, Object> redisTemplate;

    private final Key key;

    /**
     * 创建token
     */
    public String createToken(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        String token = null;

        if (principal instanceof LoginUser loginUser) {
            Map<String, Object> claims = new HashMap<>();
            claims.put(CLAIM_KEY_SUBJECT, loginUser.getUsername());
            claims.put(CLAIM_KEY_ISSUED_AT, DateUtil.date());
            String uuid = IdUtil.simpleUUID();
            claims.put(CLAIM_KEY_ID, uuid);

            token = Jwts.builder().setClaims(claims).signWith(key).compact();
            redisTemplate.opsForValue().set(
                generateLoginKey(loginUser.getUsername(), uuid),
                authentication,
                tokenProperties.getExpirationInSeconds(),
                TimeUnit.SECONDS
            );
        } else {
            throw new IllegalArgumentException("authentication.principal must be instance LoginUser");
        }
        return token;
    }

    /**
     * 从token中获取Authentication
     */
    public Optional<Authentication> getAuthenticationByToken(String token) {
        if (StrUtil.isBlank(token)) {
            return Optional.empty();
        }
        return getClaimsByToken(token)
            .map(claims -> (Authentication) redisTemplate.opsForValue().get(generateLoginKey(claims.getSubject(), claims.getId())));
    }

    /**
     * 删除token
     */
    public void removeToken(String token) {
        getClaimsByToken(token).ifPresent(claims -> redisTemplate.delete(generateLoginKey(claims.getSubject(), claims.getId())));

    }

    private String generateLoginKey(String username, String uuid) {
        return LOGIN_TOKEN_KEY + username + ":" + uuid;
    }

    private Optional<Claims> getClaimsByToken(String token) {
        Claims claims = null;
        try {
            claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        } catch (Exception ignored) {}
        return Optional.ofNullable(claims);
    }
}
