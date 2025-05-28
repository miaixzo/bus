/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.starter.sensitive;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.mapper.handler.MapperHandler;
import org.miaixz.bus.pager.handler.SqlParserHandler;
import org.miaixz.bus.sensitive.Builder;
import org.miaixz.bus.sensitive.Provider;
import org.miaixz.bus.sensitive.magic.annotation.NShield;
import org.miaixz.bus.sensitive.magic.annotation.Privacy;
import org.miaixz.bus.sensitive.magic.annotation.Sensitive;
import org.miaixz.bus.sensitive.magic.annotation.Shield;

/**
 * 数据脱敏加密
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SensitiveStatementHandler<T> extends SqlParserHandler implements MapperHandler<T> {

    /**
     * 加密类型
     */
    private String type;
    /**
     * 加密秘钥
     */
    private String key;

    /**
     * 执行分页查询，处理 COUNT 和分页逻辑。
     *
     * @param executor        MyBatis 执行器
     * @param mappedStatement MappedStatement 对象
     * @param parameter       查询参数
     * @param rowBounds       分页参数
     * @param resultHandler   结果处理器
     * @param boundSql        绑定的 SQL
     * @param result          分页查询结果
     */
    @Override
    public void query(Object result, Executor executor, MappedStatement mappedStatement, Object parameter,
            RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        SqlCommandType commandType = mappedStatement.getSqlCommandType();
        Object params = boundSql.getParameterObject();
        if (params instanceof Map) {
            return;
        }

        Sensitive sensitive = null != params ? params.getClass().getAnnotation(Sensitive.class) : null;
        if (ObjectKit.isNotEmpty(sensitive)) {
            handleParameters(sensitive, mappedStatement.getConfiguration(), boundSql, params, commandType);
        }
    }

    @Override
    public boolean setProperties(Properties properties) {
        this.key = properties.getProperty("key");
        this.type = properties.getProperty("type");
        return true;
    }

    private void handleParameters(Sensitive sensitive, Configuration configuration, BoundSql boundSql, Object param,
            SqlCommandType commandType) {
        Map<String, Object> newValues = new HashMap<>(Normal._16);
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

            if (ObjectKit.isNotEmpty(value)) {
                // 数据加密
                if (Builder.ALL.equals(sensitive.value()) || Builder.SAFE.equals(sensitive.value())
                        && (Builder.ALL.equals(sensitive.stage()) || Builder.IN.equals(sensitive.stage()))) {
                    Privacy privacy = field.getAnnotation(Privacy.class);
                    if (ObjectKit.isNotEmpty(privacy) && StringKit.isNotEmpty(privacy.value())) {
                        if (Builder.ALL.equals(privacy.value()) || Builder.IN.equals(privacy.value())) {
                            Logger.debug("Write data encryption enabled ...");
                            value = org.miaixz.bus.crypto.Builder.encrypt(this.type, this.key, value.toString(),
                                    Charset.UTF_8);
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
        if (ObjectKit.isNotEmpty(sensitiveField)) {
            Builder.on(value);
        }
        NShield json = field.getAnnotation(NShield.class);
        if (ObjectKit.isNotEmpty(json) && ObjectKit.isNotEmpty(value)) {
            Map<String, Object> map = JsonKit.toMap(value.toString());
            Shield[] keys = json.value();
            for (Shield f : keys) {
                String key = f.key();
                Object data = map.get(key);
                if (null != data) {
                    map.put(key, Builder.on(data));
                }
            }
            value = JsonKit.toJsonString(map);
        }
        return value;
    }

}
