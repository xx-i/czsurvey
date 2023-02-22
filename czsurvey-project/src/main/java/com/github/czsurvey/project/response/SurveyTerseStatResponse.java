package com.github.czsurvey.project.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SurveyTerseStatResponse {

    /**
     * 今日新增数量
     */
    private Long todayCount;

    /**
     * 回答总数
     */
    private Long totalCount;

    /**
     * 平均答题时间
     */
    private Integer avgDuration;
}
