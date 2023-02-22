package com.github.czsurvey.extra.security.grant;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * @author YanYu
 */
@Getter
public class WxQrcodeAuthenticationToken extends AbstractAuthenticationToken {

    private AuthenticationType type;

    private final Object principal;


    public WxQrcodeAuthenticationToken(UserDetails loginUser) {
        super(loginUser.getAuthorities());
        this.principal = loginUser;
        super.setAuthenticated(true);
    }

    public WxQrcodeAuthenticationToken(AuthenticationType type, String openId) {
        super(AuthorityUtils.NO_AUTHORITIES);
        this.principal = openId;
        this.type = type;
    }

    @Override
    public Object getCredentials() {
        return this.principal;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    public static enum AuthenticationType {

        /**
         * openId
         */
        OPENID,

        /**
         * 授权码
         */
        AUTHENTICATION_CODE
    }
}
