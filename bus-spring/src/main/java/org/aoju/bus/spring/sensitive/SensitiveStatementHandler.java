/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.spring.sensitive;

import org.aoju.bus.core.consts.Charset;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.crypto.CryptoUtils;
import org.aoju.bus.sensitive.Builder;
import org.aoju.bus.sensitive.Provider;
import org.aoju.bus.sensitive.annotation.JSON;
import org.aoju.bus.sensitive.annotation.Privacy;
import org.aoju.bus.sensitive.annotation.Sensitive;
import org.aoju.bus.spring.SpringContextAware;
import org.aoju.bus.spring.crypto.CryptoProperties;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 数据加密脱敏
 *
 * @author Kimi Liu
 * @version 3.2.8
 * @since JDK 1.8
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class SensitiveStatementHandler implements Interceptor {

    private static final String MAPPEDSTATEMENT = "delegate.mappedStatement";
    private static final String BOUND_SQL = "delegate.boundSql";

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = Provider.realTarget(invocation.getTarget());
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue(MAPPEDSTATEMENT);
        SqlCommandType commandType = mappedStatement.getSqlCommandType();

        BoundSql boundSql = (BoundSql) metaObject.getValue(BOUND_SQL);
        Object params = boundSql.getParameterObject();
        if (params instanceof Map) {
            return invocation.proceed();
        }
        Sensitive enableSensitive = params != null ? params.getClass().getAnnotation(Sensitive.class) : null;
        if (ObjectUtils.isNotEmpty(enableSensitive)) {
            handleParameters(mappedStatement.getConfiguration(), boundSql, params, commandType);
        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object o) {
        return Plugin.wrap(o, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }

    private void handleParameters(Configuration configuration, BoundSql boundSql, Object param, SqlCommandType commandType) {
        Map<String, Object> newValues = new HashMap<>(16);
        MetaObject metaObject = configuration.newMetaObject(param);

        for (Field field : param.getClass().getDeclaredFields()) {
            Object value = metaObject.getValue(field.getName());
            Object newValue = value;
            if (value instanceof CharSequence) {
                newValue = handlePrivacy(field, newValue);
                if (isWriteCommand(commandType) && !Provider.alreadyBeSentisived(newValue)) {
                    newValue = handleSensitiveField(field, newValue);
                    newValue = handleSensitiveJSONField(field, newValue);
                }
            }
            if (value != null && newValue != null && !value.equals(newValue)) {
                newValues.put(field.getName(), newValue);
            }
        }
        for (Map.Entry<String, Object> entry : newValues.entrySet()) {
            boundSql.setAdditionalParameter(entry.getKey(), entry.getValue());
        }
    }

    private boolean isWriteCommand(SqlCommandType commandType) {
        return SqlCommandType.UPDATE.equals(commandType) || SqlCommandType.INSERT.equals(commandType);
    }

    private Object handlePrivacy(Field field, Object value) {
        Privacy privacy = field.getAnnotation(Privacy.class);
        Object newValue = value;
        if (ObjectUtils.isNotEmpty(privacy) && value != null) {
            CryptoProperties properties = SpringContextAware.getBean(CryptoProperties.class);
            if(ObjectUtils.isEmpty(properties)){
                throw new InstrumentException("please check the request.crypto.encrypt");
            }
            newValue = CryptoUtils.encrypt(properties.getEncrypt().getType(), properties.getEncrypt().getKey(), value.toString(), Charset.UTF_8);
        }
        return newValue;
    }

    private Object handleSensitiveField(Field field, Object value) {
        org.aoju.bus.sensitive.annotation.Field sensitiveField = field.getAnnotation(org.aoju.bus.sensitive.annotation.Field.class);
        Object newValue = value;
        if (sensitiveField != null && value != null) {
            newValue = Builder.on(value);
        }
        return newValue;
    }

    private Object handleSensitiveJSONField(Field field, Object value) {
        JSON sensitiveJSONField = field.getAnnotation(JSON.class);
        Object newValue = value;
        if (sensitiveJSONField != null && value != null) {
            newValue = processJsonField(newValue, sensitiveJSONField);
        }
        return newValue;
    }

    /**
     * 在json中进行脱敏
     *
     * @param newValue new
     * @param json     脱敏的字段
     * @return json
     */
    private Object processJsonField(Object newValue, JSON json) {
        try {
            Map<String, Object> map = Provider.parseToObjectMap(newValue.toString());
            org.aoju.bus.sensitive.annotation.Field[] keys = json.value();
            for (org.aoju.bus.sensitive.annotation.Field field : keys) {
                String key = field.key();
                Object oldData = map.get(key);
                if (oldData != null) {
                    String newData = Builder.on(oldData);
                    map.put(key, newData);
                }
            }
            return Provider.parseMaptoJSONString(map);
        } catch (Throwable e) {
            return newValue;
        }
    }

}
