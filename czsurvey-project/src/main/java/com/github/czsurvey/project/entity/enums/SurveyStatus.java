package com.github.czsurvey.project.entity.enums;

public enum SurveyStatus {

    /**
     * 问卷状态正常
     */
    NORMAL,

    /**
     * 未开启
     */
    NOT_OPEN,

    /**
     * 回答频率超过限制
     */
    ALREADY_ANSWERED,

    /**
     * 回答频率超过限制，但是可以修改之前的回答
     */
    ALREADY_ANSWERED_BUT_CAD_MODIFY,

    /**
     * 问卷未开始
     */
    NOT_STATED,

    /**
     * 问卷已经结束
     */
    FINISHED,

    /**
     * 超过回收数量
     */
    EXCEED_LIMIT,

    /**
     * 超过回收数量限制，但是可以修改之前的回答
     */
    EXCEED_LIMIT_BUT_CAN_MODIFY
    ;

}
