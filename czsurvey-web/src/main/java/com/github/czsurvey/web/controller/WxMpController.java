package com.github.czsurvey.web.controller;

import com.github.czsurvey.project.request.WxConfirmRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * @author YanYu
 */
@Slf4j
@RestController
@RequestMapping("/api/wx/mp")
@RequiredArgsConstructor
public class WxMpController {

    private final WxMpService wxMpService;

    private final WxMpMessageRouter wxMpMessageRouter;

    @GetMapping(value = "/message", produces = MediaType.TEXT_PLAIN_VALUE)
    public String confirm(WxConfirmRequest request) {
        if (request == null || !wxMpService.checkSignature(request.getTimestamp(), request.getNonce(), request.getSignature())) {
            log.error("微信消息确认失败，message: {}", request);
            return "false";
        }
        log.info("微信消息确认成功");
        return request.getEchostr();
    }

    @PostMapping(value = "/message", produces = "text/plain;charset=UTF-8")
    public String message(
        WxConfirmRequest confirmRequest,
        @RequestBody String message,
        @RequestParam(name = "encrypt_type", required = false) String encType,
        @RequestParam(name = "msg_signature", required = false) String msgSignature
    ) {
        if (confirmRequest == null || !wxMpService.checkSignature(confirmRequest.getTimestamp(), confirmRequest.getNonce(), confirmRequest.getSignature())) {
            return null;
        }
        boolean isSignature = encType != null;
        if (isSignature && !"aes".equals(msgSignature)) {
            return null;
        }

        WxMpXmlMessage receiveMsg = isSignature ? WxMpXmlMessage.fromEncryptedXml(
            message,
            wxMpService.getWxMpConfigStorage(),
            confirmRequest.getTimestamp(),
            confirmRequest.getNonce(),
            msgSignature
        ) : WxMpXmlMessage.fromXml(message);

        WxMpXmlOutMessage sendMsg = wxMpMessageRouter.route(receiveMsg);
        if (sendMsg == null) {
            return "";
        }
        return isSignature ? sendMsg.toEncryptedXml(wxMpService.getWxMpConfigStorage()) : sendMsg.toXml();
    }
}
