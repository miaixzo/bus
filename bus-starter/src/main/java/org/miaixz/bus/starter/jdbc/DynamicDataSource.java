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
package org.miaixz.bus.starter.jdbc;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.logger.Logger;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 多数据源支持
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

    /**
     * 所有数据源的key集合
     */
    private static final Set<Object> keySet = new LinkedHashSet<>();
    private static final byte[] lock = Normal.EMPTY_BYTE_ARRAY;
    /**
     * 单例句柄
     */
    private static volatile DynamicDataSource INSTANCE;

    /**
     * 单例方法
     *
     * @return the DynamicDataSource
     */
    public static synchronized DynamicDataSource getInstance() {
        if (null == INSTANCE) {
            synchronized (lock) {
                if (null == INSTANCE) {
                    INSTANCE = new DynamicDataSource();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * AbstractRoutingDataSource 抽象类实现方法， 即获取当前线程数据源的key
     *
     * @return 当前数据源key
     */
    @Override
    protected Object determineCurrentLookupKey() {
        String key = DataSourceHolder.getKey();
        if (!keySet.contains(key)) {
            Logger.info(String.format("==> DataSource: Unable to locate datasource by key '%s'. Default will be used.",
                    key));
        }
        Logger.debug("==> DataSource: [{}]", key);
        return key;
    }

    /**
     * 在获取key的集合,目的只是为了添加一些告警日志
     */
    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        try {
            Field sourceMapField = AbstractRoutingDataSource.class.getDeclaredField("resolvedDataSources");
            sourceMapField.setAccessible(true);
            Map<Object, javax.sql.DataSource> sourceMap = (Map<Object, javax.sql.DataSource>) sourceMapField.get(this);
            keySet.addAll(sourceMap.keySet());
            sourceMapField.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public void setTargetDataSources(Map<Object, Object> map) {
        super.setTargetDataSources(map);
        keySet.add(map.keySet());
        this.afterPropertiesSet();
    }

    /**
     * 获取所有数据源
     *
     * @return 数据源映射
     */
    public Map<Object, Object> getAllDataSources() {
        try {
            Field targetDataSourcesField = AbstractRoutingDataSource.class.getDeclaredField("targetDataSources");
            targetDataSourcesField.setAccessible(true);

            Map<Object, Object> targetDataSources = (Map<Object, Object>) targetDataSourcesField.get(this);
            return targetDataSources;
        } catch (Exception e) {
            Logger.error("==> DataSource: [{}]", "Failed to get all datasources");
            return new HashMap<>();
        }
    }

    /**
     * 动态增加数据源
     *
     * @param key        数据源key
     * @param dataSource 数据源信息
     */
    public synchronized static void addDataSource(String key, javax.sql.DataSource dataSource) {
        if (null != dataSource && dataSource instanceof AbstractRoutingDataSource) {
            try {
                Field sourceMapField = AbstractRoutingDataSource.class.getDeclaredField("resolvedDataSources");
                sourceMapField.setAccessible(true);
                Map<Object, javax.sql.DataSource> sourceMap = (Map<Object, javax.sql.DataSource>) sourceMapField
                        .get(getInstance().getDefaultDataSource());
                sourceMap.put(key, dataSource);
                keySet.add(key);
                sourceMapField.setAccessible(false);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                Logger.error("==> DataSource: [{}]", "Failed to add  datasource");
            }
        }
    }

    /**
     * 判断指定DataSrouce当前是否存在
     *
     * @param key 数据源key
     * @return the true/false
     */
    public boolean containsKey(String key) {
        return keySet.contains(key);
    }

    /**
     * 移除数据源
     * 
     * @param key 数据源名称
     */
    public void remove(String key) {
        Map<Object, Object> targetDataSources = getAllDataSources();
        targetDataSources.remove(key);
        keySet.remove(key);
        super.setTargetDataSources(targetDataSources);
        super.afterPropertiesSet();
    }

    /**
     * 获取默认数据源
     *
     * @return the dataSource
     */
    public javax.sql.DataSource getDefaultDataSource() {
        return super.determineTargetDataSource();
    }

}
