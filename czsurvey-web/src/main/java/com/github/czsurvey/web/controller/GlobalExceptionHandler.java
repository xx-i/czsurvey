package com.github.czsurvey.web.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.zalando.problem.spring.web.advice.ProblemHandling;
import org.zalando.problem.spring.web.advice.security.SecurityAdviceTrait;

/**
 * @author YanYu
 */
@ControllerAdvice
public class GlobalExceptionHandler implements ProblemHandling, SecurityAdviceTrait { }
