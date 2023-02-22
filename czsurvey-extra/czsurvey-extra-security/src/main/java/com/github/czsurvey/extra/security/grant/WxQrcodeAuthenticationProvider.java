package com.github.czsurvey.extra.security.grant;

import com.github.czsurvey.extra.security.service.SystemUserDetailService;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;

import static com.github.czsurvey.extra.security.grant.WxQrcodeAuthenticationToken.AuthenticationType.OPENID;

/**
 * @author YanYu
 */
@Slf4j
@RequiredArgsConstructor
public class WxQrcodeAuthenticationProvider implements AuthenticationProvider {

    private final SystemUserDetailService userDetailService;

    @Setter
    private UserDetailsChecker preAuthenticationChecks = new AccountStatusUserDetailsChecker();

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (authentication.getCredentials() == null) {
            log.debug("Failed to authenticate since no credentials provided");
            throw new BadCredentialsException("Bad credentials");
        }
        WxQrcodeAuthenticationToken requestToken = (WxQrcodeAuthenticationToken) authentication;
        if (requestToken.getType() == null) {
            log.debug("Failed to authenticate since no credentials provided");
            throw new BadCredentialsException("Bad credentials");
        }
        UserDetails userDetails;
        if (requestToken.getType().equals(OPENID)) {
            userDetails = userDetailService.loadUserByWxOpenId(requestToken.getPrincipal().toString());
        } else {
            userDetails = userDetailService.loadUserDetailsByAuthorizationCode(requestToken.getPrincipal().toString());
        }

        preAuthenticationChecks.check(userDetails);

        WxQrcodeAuthenticationToken token = new WxQrcodeAuthenticationToken(userDetails);
        token.setDetails(authentication.getDetails());
        return token;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.isAssignableFrom(WxQrcodeAuthenticationToken.class);
    }
}
