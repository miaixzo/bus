/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.starter.sensitive;

import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.toolkit.BooleanKit;
import org.aoju.bus.core.toolkit.ObjectKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.mapper.handler.AbstractSqlHandler;
import org.aoju.bus.sensitive.Builder;
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
 * @version 6.0.6
 * @since Java 17+
 */
@Intercepts({@Signature(type = ResultSetHandler.class, method = "handleResultSets", args = {java.sql.Statement.class})})
public class SensitiveResultSetHandler extends AbstractSqlHandler implements Interceptor {

    /**
     * 是否DEBUG模式
     */
    private boolean debug;
    /**
     * 解密类型
     */
    private String type;
    /**
     * 解密秘钥
     */
    private String key;


    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        final List<Object> results = (List<Object>) invocation.proceed();

        if (results.isEmpty()) {
            return results;
        }

        if (this.debug) {
            final ResultSetHandler statementHandler = realTarget(invocation.getTarget());
            final MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
            final MappedStatement mappedStatement = getMappedStatement(metaObject, MAPPEDSTATEMENT);
            final ResultMap resultMap = mappedStatement.getResultMaps().isEmpty() ? null : mappedStatement.getResultMaps().get(0);

            Sensitive sensitive = results.get(0).getClass().getAnnotation(Sensitive.class);
            if (ObjectKit.isEmpty(sensitive)) {
                return results;
            }

            final Map<String, Privacy> privacyMap = getSensitiveByResultMap(resultMap);
            for (Object object : results) {
                // 数据解密
                if (Builder.ALL.equals(sensitive.value()) || Builder.SAFE.equals(sensitive.value())
                        && (Builder.ALL.equals(sensitive.stage()) || Builder.OUT.equals(sensitive.stage()))) {
                    final MetaObject objMetaObject = mappedStatement.getConfiguration().newMetaObject(object);
                    for (Map.Entry<String, Privacy> entry : privacyMap.entrySet()) {
                        Privacy privacy = entry.getValue();
                        if (ObjectKit.isNotEmpty(privacy) && StringKit.isNotEmpty(privacy.value())) {
                            if (Builder.ALL.equals(privacy.value()) || Builder.OUT.equals(privacy.value())) {
                                String property = entry.getKey();
                                String value = (String) objMetaObject.getValue(property);
                                if (StringKit.isNotEmpty(value)) {
                                    Logger.debug("Query data decryption enabled ...");
                                    String decryptValue = org.aoju.bus.crypto.Builder.decrypt(this.type, this.key, value, Charset.UTF_8);
                                    objMetaObject.setValue(property, decryptValue);
                                }
                            }
                        }
                    }
                }
                // 数据脱敏
                if ((Builder.ALL.equals(sensitive.value()) || Builder.SENS.equals(sensitive.value()))
                        && (Builder.ALL.equals(sensitive.stage()) || Builder.OUT.equals(sensitive.stage()))) {
                    Logger.debug("Query data sensitive enabled ...");
                    Builder.on(object);
                }
            }
        }
        return results;
    }

    @Override
    public Object plugin(Object object) {
        if (object instanceof ResultSetHandler) {
            return Plugin.wrap(object, this);
        }
        return object;
    }

    @Override
    public void setProperties(Properties properties) {
        this.debug = BooleanKit.toBoolean(properties.getProperty("debug"));
        this.key = properties.getProperty("key");
        this.type = properties.getProperty("type");
    }

    private Map<String, Privacy> getSensitiveByResultMap(ResultMap resultMap) {
        if (null == resultMap) {
            return new HashMap<>(Normal._16);
        }
        return getSensitiveByType(resultMap.getType());
    }

    private Map<String, Privacy> getSensitiveByType(Class<?> clazz) {
        Map<String, Privacy> sensitiveFieldMap = new HashMap<>(Normal._16);
        for (Field field : clazz.getDeclaredFields()) {
            Privacy sensitiveField = field.getAnnotation(Privacy.class);
            if (null != sensitiveField) {
                sensitiveFieldMap.put(field.getName(), sensitiveField);
            }
        }
        return sensitiveFieldMap;
    }

}
