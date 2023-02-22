package com.github.czsurvey.project.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author YanYu
 */
@Data
public class RolePrimaryKey implements Serializable {

    private Long userId;

    private String role;
}
