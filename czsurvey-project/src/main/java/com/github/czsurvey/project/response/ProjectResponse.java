package com.github.czsurvey.project.response;

import com.github.czsurvey.project.entity.Project;
import com.github.czsurvey.project.entity.enums.ProjectStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author YanYu
 */
@Data
@NoArgsConstructor
public class ProjectResponse {

    /**
     * 项目
     */
    private Project project;

    /**
     * 回收的数量
     */
    private Long quantityCollected;

    /**
     * 项目的状态
     */
    private ProjectStatus status;

    /**
     * 最近30天回收总数
     */
    private Long quantityCollectedLast30Days;

    /**
     * 题目数量
     */
    private Long questionCount;

    public ProjectResponse(Project project, ProjectStatus status) {
        this.project = project;
        this.status = status;
    }
}
