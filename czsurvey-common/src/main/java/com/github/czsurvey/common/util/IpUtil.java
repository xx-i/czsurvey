package com.github.czsurvey.common.util;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.HttpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.czsurvey.common.costant.CommonConstant;
import com.github.czsurvey.common.payload.AddrInfo;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class IpUtil {

    /**
     * 查询地理位置URL
     */
    public static final  String LOCATION_QUERY_URL = "http://whois.pconline.com.cn/ipJson.jsp";

    /**
     * 匹配地址信息正则
     */
    public static final String LOCATION_JSON_PATTERN = "IPCallBack\\((.*)\\)";

    /**
     * 获取省信息的键
     */
    public static final String PROVINCE_ATTRIBUTE = "pro";

    /**
     * 获取城市信息的键
     */
    public static final String CITY_ATTRIBUTE = "city";

    /**
     * 获取IP地址
     * @author yanyu
     */
    public static String getIp(HttpServletRequest request) {
        if (ObjectUtil.isEmpty(request)) {
            log.warn("request参数为空");
            return "-";
        }
        String ip = ServletUtil.getClientIP(request);
        return CommonConstant.LOCALHOST_IPV6.equals(ip) ? CommonConstant.LOCALHOST_IPV4 : ip;
    }

    public static AddrInfo getAddrInfo(String ip) {
        if (!Validator.isIpv4(ip)) {
            log.warn("{}不是一个IPV4地址", ip);
            return null;
        }
        // 判断是否是内网
        if (NetUtil.isInnerIP(ip)) {
            log.warn("{}是内网地址", ip);
            return null;
        }

        String province;
        String city;
        try {
            HashMap<String, Object> param = new HashMap<>();
            param.put("ip", ip);
            String responseResult = HttpUtil.get(LOCATION_QUERY_URL, param);

            Pattern pattern = Pattern.compile(LOCATION_JSON_PATTERN);
            Matcher matcher = pattern.matcher(responseResult);
            if (!matcher.find()) {
                log.error("获取地理位置接口正则匹配失败，接口地址 {}, 正则 {}", LOCATION_QUERY_URL, LOCATION_JSON_PATTERN);
                return null;
            }
            String jsonResult = matcher.group(1);

            ObjectMapper objectMapper = new ObjectMapper();
            Map<?, ?> map = objectMapper.readValue(jsonResult, Map.class);
            province = (String) map.get(PROVINCE_ATTRIBUTE);
            city = (String) map.get(CITY_ATTRIBUTE);
        } catch (Exception e) {
            log.error("获取地理位置失败，错误信息：{}", e.getMessage());
            return null;
        }
        return new AddrInfo(province, city);
    }

    public static AddrInfo getAddrInfo(HttpServletRequest request) {
        String ip = getIp(request);
        return getAddrInfo(ip);
    }
}
