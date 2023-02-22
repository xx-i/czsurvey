package com.github.czsurvey.project.entity.enums;

/**
 * @author YanYu
 */
public enum WxQrcodeLoginStatus {

    /**
     * 等待扫码
     */
    WAITING,

    /**
     * 扫码完成
     */
    SCANNED,

    /**
     * 登录成功
     */
    SUCCESS,

    /**
     * 二维码过期
     */
    EXPIRED
}
