package com.github.czsurvey.project.service;

import cn.hutool.core.util.StrUtil;
import com.github.czsurvey.extra.security.model.LoginUser;
import com.github.czsurvey.extra.security.service.SystemUserDetailService;
import com.github.czsurvey.project.entity.User;
import com.github.czsurvey.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;

/**
 * @author YanYu
 */
@Service
@RequiredArgsConstructor
public class UserService implements SystemUserDetailService {

    private final UserRepository userRepository;

    private final WxMpService wxMpService;

    @Override
    public UserDetails loadUserByUsername(String phoneOrEmail) throws UsernameNotFoundException {
        return userRepository.findByPhoneOrEmail(phoneOrEmail)
            .map(this::mapUserToLoginUser)
            .orElseThrow(() -> new UsernameNotFoundException("用户: " + phoneOrEmail + " 不存在"));
    }

    @Override
    public UserDetails loadUserByWxOpenId(String openId) {
        User user = userRepository.findTopByWxOpenid(openId).orElseGet(() -> createWxDefaultUser(openId));
        return mapUserToLoginUser(user);
    }

    @Override
    public UserDetails loadUserDetailsByAuthorizationCode(String authorizationCode) {
        WxOAuth2UserInfo userInfo;
        try {
            WxOAuth2AccessToken accessToken = wxMpService.getOAuth2Service().getAccessToken(authorizationCode);
            userInfo = wxMpService.getOAuth2Service().getUserInfo(accessToken, "zh_CN");
        } catch (Exception ignore) {
            throw new BadCredentialsException("无效的授权码");
        }
        User user = userRepository.findTopByWxOpenid(userInfo.getOpenid())
            .orElseGet(() -> createWxDefaultUser(userInfo));
        return mapUserToLoginUser(user);
    }

    public User createWxDefaultUser(String openId) {
        User user = new User();
        user.setNickname("微信用户" + (openId.length() > 8 ? openId.substring(0, 8) : openId));
        user.setWxOpenid(openId);
        user.setEnabled(true);
        userRepository.save(user);
        return user;
    }

    public User createWxDefaultUser(WxOAuth2UserInfo userInfo) {
        User user = new User();
        user.setNickname(userInfo.getNickname());
        user.setWxOpenid(userInfo.getOpenid());
        user.setAvatar(userInfo.getHeadImgUrl());
        user.setEnabled(true);
        userRepository.save(user);
        return user;
    }

    private LoginUser mapUserToLoginUser(User user) {
        String username = StrUtil.isBlank(user.getPhone()) ? user.getWxOpenid() : user.getPhone();
        return new LoginUser(user.getId(), username, user.getPassword(), user.getEnabled(), new HashSet<>(), new HashSet<>());
    }
}
