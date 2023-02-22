package com.github.czsurvey.project.wxhandler;

import com.github.czsurvey.extra.security.component.JwtTokenStore;
import com.github.czsurvey.extra.security.grant.WxQrcodeAuthenticationToken;
import com.github.czsurvey.project.request.WxQrcodeScene;
import lombok.RequiredArgsConstructor;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpMessageHandler;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.github.czsurvey.extra.security.grant.WxQrcodeAuthenticationToken.AuthenticationType.OPENID;
import static com.github.czsurvey.project.constant.AuthConstant.WX_LOGIN_TOKEN_KEY;

/**
 * @author YanYu
 */
@Component
@RequiredArgsConstructor
public class ScanHandler implements WxMpMessageHandler {

    private final AuthenticationManager authenticationManager;

    private final JwtTokenStore jwtTokenStore;

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage, Map<String, Object> context, WxMpService wxMpService, WxSessionManager sessionManager) throws WxErrorException {
        WxQrcodeScene wxQrcodeScene;
        try {
            wxQrcodeScene = WxQrcodeScene.resolve(wxMessage.getEventKey());
        } catch (Exception ignored) {
            return null;
        }

        return switch (wxQrcodeScene.getType()) {
            case LOGIN -> handlerLogin(wxQrcodeScene.getId(), wxMessage.getFromUser());
        };
    }

    public WxMpXmlOutMessage handlerLogin(String sceneId, String openId) {
        WxQrcodeAuthenticationToken token = new WxQrcodeAuthenticationToken(OPENID, openId);
        Authentication authenticate = authenticationManager.authenticate(token);
        String jwt = jwtTokenStore.createToken(authenticate);
        redisTemplate.opsForValue().set(WX_LOGIN_TOKEN_KEY + sceneId, jwt, 5 * 60, TimeUnit.SECONDS);
        return null;
    }
}
