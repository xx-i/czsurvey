package com.github.czsurvey.project.config;

import com.github.czsurvey.project.wxhandler.ScanHandler;
import lombok.RequiredArgsConstructor;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

import static me.chanjar.weixin.common.api.WxConsts.EventType.*;
import static me.chanjar.weixin.common.api.WxConsts.XmlMsgType.*;

/**
 * @author YanYu
 */
@AutoConfiguration
@RequiredArgsConstructor
public class WxMpConfiguration {

    private final ScanHandler scanHandler;

    @Bean
    public WxMpMessageRouter messageRouter(WxMpService wxMpService) {
        WxMpMessageRouter router = new WxMpMessageRouter(wxMpService);
        router.rule().msgType(EVENT).event(SCAN).handler(scanHandler).end();
        return router;
    }
}
