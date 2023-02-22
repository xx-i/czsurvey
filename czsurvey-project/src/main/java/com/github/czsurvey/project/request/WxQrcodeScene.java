package com.github.czsurvey.project.request;

import com.github.czsurvey.project.entity.enums.WxQrcodeSceneType;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author YanYu
 */
@Data
@AllArgsConstructor
public class WxQrcodeScene {

    public static final String SCENE_PREFIX = "wx_scene";

    private WxQrcodeSceneType type;

    private String id;

    public String toSceneStr() {
        return SCENE_PREFIX + ":" + type.name().toLowerCase() + ":" + id;
    }

    public static WxQrcodeScene resolve(String sceneStr) {
        String[] split = sceneStr.split(":");
        if (split.length != 3 || !SCENE_PREFIX.equals(split[0])) {
            throw new IllegalArgumentException("场景字符串解析失败");
        }
        return new WxQrcodeScene(WxQrcodeSceneType.ignoreCaseValueOf(split[1]), split[2]);
    }
}
