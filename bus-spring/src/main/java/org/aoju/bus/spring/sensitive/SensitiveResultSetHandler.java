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

import org.aoju.bus.core.codec.Base64;
import org.aoju.bus.core.consts.Charset;
import org.aoju.bus.core.consts.ModeType;
import org.aoju.bus.crypto.CryptoUtils;
import org.aoju.bus.sensitive.Builder;
import org.aoju.bus.sensitive.Provider;
import org.aoju.bus.sensitive.annotation.Privacy;
import org.aoju.bus.sensitive.annotation.Sensitive;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;


/**
 * 数据解密脱敏
 *
 * @author Kimi Liu
 * @version 3.0.9
 * @since JDK 1.8
 */
@Intercepts({@Signature(type = ResultSetHandler.class, method = "handleResultSets", args = {java.sql.Statement.class})})
public class SensitiveResultSetHandler implements Interceptor {

    private static final String MAPPED_STATEMENT = "mappedStatement";

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        final List<Object> results = (List<Object>) invocation.proceed();

        if (results.isEmpty()) {
            return results;
        }

        final ResultSetHandler statementHandler = Provider.realTarget(invocation.getTarget());
        final MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        final MappedStatement mappedStatement = (MappedStatement) metaObject.getValue(MAPPED_STATEMENT);
        final ResultMap resultMap = mappedStatement.getResultMaps().isEmpty() ? null : mappedStatement.getResultMaps().get(0);

        Object result0 = results.get(0);
        Sensitive enableSensitive = result0.getClass().getAnnotation(Sensitive.class);
        if (enableSensitive == null) {
            return results;
        }

        final Map<String, Privacy> sensitiveFieldMap = getSensitiveByResultMap(resultMap);
        final Map<String, org.aoju.bus.sensitive.annotation.Field> sensitiveBindedMap = getSensitiveBindedByResultMap(resultMap);

        if (sensitiveBindedMap.isEmpty() && sensitiveFieldMap.isEmpty()) {
            return results;
        }

        for (Object obj : results) {
            final MetaObject objMetaObject = mappedStatement.getConfiguration().newMetaObject(obj);
            for (Map.Entry<String, Privacy> entry : sensitiveFieldMap.entrySet()) {
                String property = entry.getKey();
                String value = (String) objMetaObject.getValue(property);
                if (value != null) {
                    String decryptValue = new String(CryptoUtils.decrypt(ModeType.SHA1withRSA, null, Base64.decode(value)), Charset.DEFAULT_UTF_8);
                    objMetaObject.setValue(property, decryptValue);
                }
            }
            for (Map.Entry<String, org.aoju.bus.sensitive.annotation.Field> entry : sensitiveBindedMap.entrySet()) {
                String property = entry.getKey();
                org.aoju.bus.sensitive.annotation.Field sensitiveBind = entry.getValue();
                String bindPropety = sensitiveBind.field();
                Builder.Type sensitiveType = sensitiveBind.type();
                try {
                    String value = (String) objMetaObject.getValue(bindPropety);
                    String resultValue = Builder.on(sensitiveType);
                    objMetaObject.setValue(property, resultValue);
                } catch (Exception e) {
                    //ignore it;
                }
            }
        }
        return results;
    }

    private Map<String, org.aoju.bus.sensitive.annotation.Field> getSensitiveBindedByResultMap(ResultMap resultMap) {
        if (resultMap == null) {
            return new HashMap<>(16);
        }
        Map<String, org.aoju.bus.sensitive.annotation.Field> sensitiveBindedMap = new HashMap<>(16);
        Class<?> clazz = resultMap.getType();
        for (Field field : clazz.getDeclaredFields()) {
            org.aoju.bus.sensitive.annotation.Field sensitiveBind = field.getAnnotation(org.aoju.bus.sensitive.annotation.Field.class);
            if (sensitiveBind != null) {
                sensitiveBindedMap.put(field.getName(), sensitiveBind);
            }
        }
        return sensitiveBindedMap;
    }

    private Map<String, Privacy> getSensitiveByResultMap(ResultMap resultMap) {
        if (resultMap == null) {
            return new HashMap<>(16);
        }
        return getSensitiveByType(resultMap.getType());
    }

    private Map<String, Privacy> getSensitiveByType(Class<?> clazz) {
        Map<String, Privacy> sensitiveFieldMap = new HashMap<>(16);

        for (Field field : clazz.getDeclaredFields()) {
            Privacy sensitiveField = field.getAnnotation(Privacy.class);
            if (sensitiveField != null) {
                sensitiveFieldMap.put(field.getName(), sensitiveField);
            }
        }
        return sensitiveFieldMap;
    }

    @Override
    public Object plugin(Object o) {
        return Plugin.wrap(o, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }

}
