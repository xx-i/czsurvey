package com.github.czsurvey.project.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author YanYu
 */
@Data
public class LoginRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
