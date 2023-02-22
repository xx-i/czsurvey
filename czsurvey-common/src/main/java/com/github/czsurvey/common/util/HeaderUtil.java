package com.github.czsurvey.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static com.github.czsurvey.common.costant.CommonConstant.APPLICATION_NAME;


/**
 * HTTP 请求头工具类
 * jhipster <a href="https://github.com/jhipster/jhipster/blob/main/jhipster-framework">HeaderUtil</a>
 * @author YanYu
 */
@Slf4j
public class HeaderUtil {

    public static HttpHeaders createAlert(String message, String param) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-" + APPLICATION_NAME + "-alert", message);
        headers.add("X-" + APPLICATION_NAME + "-params", URLEncoder.encode(param, StandardCharsets.UTF_8));
        return headers;
    }

    /**
     * 实体创建成功的请求头
     */
    public static HttpHeaders createEntityCreationAlert(boolean enableTranslation, String entityName, String param) {
        String message = enableTranslation ? APPLICATION_NAME + "." + entityName + ".created"
            : "A new " + entityName + " is created with identifier " + param;
        return createAlert(message, param);
    }

    /**
     * 实体更新成功的请求头
     */
    public static HttpHeaders createEntityUpdateAlert(boolean enableTranslation, String entityName, String param) {
        String message = enableTranslation ? APPLICATION_NAME + "." + entityName + ".updated"
            : "A " + entityName + " is updated with identifier " + param;
        return createAlert(message, param);
    }

    /**
     * 实体删除成功的请求头
     */
    public static HttpHeaders createEntityDeletionAlert(boolean enableTranslation, String entityName, String param) {
        String message = enableTranslation ? APPLICATION_NAME + "." + entityName + ".deleted"
            : "A " + entityName + " is deleted with identifier " + param;
        return createAlert(message, param);
    }

    /**
     * 失败的请求头
     */
    public static HttpHeaders createFailureAlert(boolean enableTranslation, String entityName, String errorKey, String defaultMessage) {
        log.error("Entity processing failed, {}", defaultMessage);

        String message = enableTranslation ? "error." + errorKey : defaultMessage;

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-" + APPLICATION_NAME + "-error", message);
        headers.add("X-" + APPLICATION_NAME + "-params", entityName);
        return headers;
    }
}
