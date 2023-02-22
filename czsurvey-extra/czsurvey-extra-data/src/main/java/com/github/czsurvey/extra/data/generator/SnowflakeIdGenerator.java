package com.github.czsurvey.extra.data.generator;


import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;

/**
 * @author YanYu
 */
public class SnowflakeIdGenerator implements IdentifierGenerator {

    private static final Snowflake snowflake = IdUtil.getSnowflake();

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        return snowflake.nextId();
    }
}
