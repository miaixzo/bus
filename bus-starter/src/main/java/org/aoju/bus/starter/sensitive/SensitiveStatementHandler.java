/*
 * The MIT License
 *
 * Copyright (c) 2020 aoju.org All rights reserved.
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
package org.aoju.bus.starter.sensitive;

import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.mapper.handlers.AbstractSqlParserHandler;
import org.aoju.bus.sensitive.Builder;
import org.aoju.bus.sensitive.Provider;
import org.aoju.bus.sensitive.annotation.NShield;
import org.aoju.bus.sensitive.annotation.Privacy;
import org.aoju.bus.sensitive.annotation.Sensitive;
import org.aoju.bus.sensitive.annotation.Shield;
import org.aoju.bus.starter.SpringAware;
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
 * 数据脱敏加密
 *
 * @author Kimi Liu
 * @version 5.5.2
 * @since JDK 1.8+
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class SensitiveStatementHandler extends AbstractSqlParserHandler
        implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = realTarget(invocation.getTarget());
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue(DELEGATE_MAPPED_STATEMENT);
        SqlCommandType commandType = mappedStatement.getSqlCommandType();

        BoundSql boundSql = (BoundSql) metaObject.getValue("delegate.boundSql");
        Object params = boundSql.getParameterObject();
        if (params instanceof Map) {
            return invocation.proceed();
        }
        SensitiveProperties properties = SpringAware.getBean(SensitiveProperties.class);
        if (ObjectUtils.isNotEmpty(properties) && !properties.isDebug()) {
            Sensitive sensitive = params != null ? params.getClass().getAnnotation(Sensitive.class) : null;
            if (ObjectUtils.isNotEmpty(sensitive)) {
                handleParameters(sensitive, mappedStatement.getConfiguration(), boundSql, params, commandType);
            }
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

    private void handleParameters(Sensitive sensitive, Configuration configuration, BoundSql boundSql, Object param, SqlCommandType commandType) {
        Map<String, Object> newValues = new HashMap<>(16);
        MetaObject metaObject = configuration.newMetaObject(param);

        for (Field field : param.getClass().getDeclaredFields()) {
            Object value = metaObject.getValue(field.getName());

            if (value instanceof CharSequence) {
                if (isWriteCommand(commandType) && !Provider.alreadyBeSentisived(value)) {
                    // 数据脱敏
                    if (Builder.ALL.equals(sensitive.value()) || Builder.SENS.equals(sensitive.value())
                            && (Builder.ALL.equals(sensitive.stage()) || Builder.IN.equals(sensitive.stage()))) {
                        Logger.debug("Write data sensitive enabled ...");
                        value = handleSensitive(field, value);
                    }
                }
            }

            if (ObjectUtils.isNotEmpty(value)) {
                // 数据加密
                if (Builder.ALL.equals(sensitive.value()) || Builder.SAFE.equals(sensitive.value())
                        && (Builder.ALL.equals(sensitive.stage()) || Builder.IN.equals(sensitive.stage()))) {
                    Privacy privacy = field.getAnnotation(Privacy.class);
                    if (ObjectUtils.isNotEmpty(privacy) && StringUtils.isNotEmpty(privacy.value())) {
                        if (Builder.ALL.equals(privacy.value()) || Builder.IN.equals(privacy.value())) {
                            SensitiveProperties properties = SpringAware.getBean(SensitiveProperties.class);
                            if (ObjectUtils.isEmpty(properties)) {
                                throw new InstrumentException("Please check the request.crypto.encrypt");
                            }
                            Logger.debug("Write data encryption enabled ...");
                            value = org.aoju.bus.crypto.Builder.encrypt(properties.getEncrypt().getType(), properties.getEncrypt().getKey(), value.toString(), Charset.UTF_8);
                        }
                    }
                }
                newValues.put(field.getName(), value);
            }
        }
        for (Map.Entry<String, Object> entry : newValues.entrySet()) {
            boundSql.setAdditionalParameter(entry.getKey(), entry.getValue());
        }
    }

    private boolean isWriteCommand(SqlCommandType commandType) {
        return SqlCommandType.UPDATE.equals(commandType) || SqlCommandType.INSERT.equals(commandType);
    }

    private Object handleSensitive(Field field, Object value) {
        Shield sensitiveField = field.getAnnotation(Shield.class);
        if (ObjectUtils.isNotEmpty(sensitiveField)) {
            Builder.on(value);
        }
        NShield json = field.getAnnotation(NShield.class);
        if (ObjectUtils.isNotEmpty(json) && ObjectUtils.isNotEmpty(value)) {
            Map<String, Object> map = Provider.parseToObjectMap(value.toString());
            Shield[] keys = json.value();
            for (Shield f : keys) {
                String key = f.key();
                Object data = map.get(key);
                if (data != null) {
                    map.put(key, Builder.on(data));
                }
            }
            value = Provider.parseMaptoJSONString(map);
        }
        return value;
    }

}
