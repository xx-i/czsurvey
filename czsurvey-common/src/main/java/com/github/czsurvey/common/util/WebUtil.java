package com.github.czsurvey.common.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @author YanYu
 */
@Slf4j
@UtilityClass
public class WebUtil extends org.springframework.web.util.WebUtils {

    /**
     * 获取请求参数
     * @author yanyu
     */
    public String getParameter(String name) {
        return getRequest().getParameter(name);
    }

    /**
     * 获取 HttpServletRequest
     * @author yanyu
     */
    public HttpServletRequest getRequest() {
        return getRequestAttributes().getRequest();
    }

    /**
     * 获取 HttpResponse
     * @author yanyu
     */
    public HttpServletResponse getResponse() {
        return getRequestAttributes().getResponse();
    }

    /**
     * 获取 RequestAttributes
     * @author yanyu
     */
    public ServletRequestAttributes getRequestAttributes() {
        return (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    }

    /**
     * 获取 RequestURL
     * @author yanyu
     */
    public String getUrl() {
        HttpServletRequest request = getRequest();
        return request.getRequestURL().toString();
    }

    /**
     * 获取 RequestURI
     * @author yanyu
     */
    public String getUri() {
        HttpServletRequest request = getRequest();
        return request.getRequestURI();
    }
}
