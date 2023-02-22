package com.github.czsurvey.project.entity.enums;

/**
 * @author YanYu
 */
public enum RoleName {

    /**
     * 普通用户
     */
    ROLE_USER,

    /**
     * 不会被持久化到数据库，通过微信登录的用户会被临时添加上这个角色
     */
    ROLE_WX_USER,

    /**
     * 管理员
     */
    ROLE_ADMIN,
}
