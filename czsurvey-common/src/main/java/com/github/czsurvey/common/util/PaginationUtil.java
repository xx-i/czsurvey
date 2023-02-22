package com.github.czsurvey.common.util;

import cn.hutool.extra.spring.SpringUtil;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.PathBuilderFactory;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.Querydsl;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponentsBuilder;

import javax.persistence.EntityManager;
import java.text.MessageFormat;
import java.util.function.Function;


/**
 * 分页工具类，将分页数据添加至请求头
 * jhipster <a href="https://github.com/jhipster/jhipster/blob/main/jhipster-framework">分页工具类</a>
 * @author YanYu
 */
public class PaginationUtil {

    private static final EntityManager EM = SpringUtil.getBean(EntityManager.class);

    private static final String HEADER_X_TOTAL_COUNT = "X-Total-Count";

    private static final String HEADER_LINK_FORMAT = "<{0}>; rel=\"{1}\"";

    public static <T>HttpHeaders generatePaginationHttpHeaders(UriComponentsBuilder uriBuilder, Page<T> page) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_X_TOTAL_COUNT, Long.toString(page.getTotalElements()));
        int pageNumber = page.getNumber();
        int pageSize = page.getSize();
        StringBuilder link = new StringBuilder();
        if (pageNumber < page.getTotalPages() - 1) {
            link.append(prepareLink(uriBuilder, pageNumber + 1, pageSize, "next"))
                .append(",");
        }
        if (pageNumber > 0) {
            link.append(prepareLink(uriBuilder, pageNumber - 1, pageSize, "prev"))
                .append(",");
        }
        link.append(prepareLink(uriBuilder, page.getTotalPages() - 1, pageSize, "last"))
            .append(",")
            .append(prepareLink(uriBuilder, 0, pageSize, "first"));
        headers.add(HttpHeaders.LINK, link.toString());
        return headers;
    }

    public static <T> Page<T> page(JPAQuery<T> query, Pageable pageable, Class<?> orderClass) {
        return page(query, pageable, orderClass, t -> t);
    }

    public static <T, R> Page<R> page(JPAQuery<T> query, Pageable pageable, Class<?> orderClass, Function<T, R> converter) {
        Querydsl querydsl = new Querydsl(EM, (new PathBuilderFactory()).create(orderClass));
        QueryResults<T> result = querydsl.applyPagination(pageable, query).fetchResults();
        return new PageImpl<>(result.getResults().stream().map(converter).toList(), pageable, result.getTotal());
    }

    private static String prepareLink(UriComponentsBuilder uriBuilder, int pageNumber, int pageSize, String relType) {
        return MessageFormat.format(HEADER_LINK_FORMAT, preparePageUri(uriBuilder, pageNumber, pageSize), relType);
    }

    private static String preparePageUri(UriComponentsBuilder uriBuilder, int pageNumber, int pageSize) {
        return uriBuilder.replaceQueryParam("page", Integer.toString(pageNumber))
            .replaceQueryParam("size", Integer.toString(pageSize))
            .toUriString()
            .replace(",", "%2C")
            .replace(";", "%#B");
    }
}
