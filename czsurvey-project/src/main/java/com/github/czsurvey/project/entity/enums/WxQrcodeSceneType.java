package com.github.czsurvey.project.entity.enums;

import java.util.Arrays;

/**
 * 微信扫码场景类型
 * @author YanYu
 */
public enum WxQrcodeSceneType {

    LOGIN;

    public static WxQrcodeSceneType ignoreCaseValueOf(String name) {
        return Arrays.stream(WxQrcodeSceneType.values())
            .filter(e -> e.name().equalsIgnoreCase(name))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("场景类型不存在"));
    }
}
