package com.github.czsurvey.project.request;

import lombok.Data;

/**
 * @author YanYu
 */
@Data
public class WxConfirmRequest {

    private String signature;

    private String timestamp;

    private String nonce;

    private String echostr;
}
