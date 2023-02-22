package com.github.czsurvey.extra.security.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * @author YanYu
 */
public interface SystemUserDetailService extends UserDetailsService {

    /**
     * 通过微信的openId查找用户
     * @param openId openId
     * @return userDetails
     */
    UserDetails loadUserByWxOpenId(String openId);

    /**
     * 通过微信oauth的授权码查找用户
     * @param authorizationCode 授权码
     * @return userDetails
     */
    UserDetails loadUserDetailsByAuthorizationCode(String authorizationCode);
}
