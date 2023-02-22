package com.github.czsurvey.project.request;

import com.github.czsurvey.project.entity.enums.ProjectStatus;
import lombok.Data;

/**
 * @author YanYu
 */
@Data
public class ProjectRequest {

    /**
     * 项目名
     */
    private String name;


    /**
     * 项目状态
     */
    private ProjectStatus status;

    /**
     * 文件夹ID
     */
    private Long folderId;

    private Boolean trash;
}
