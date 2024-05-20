/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.shade.screw.process;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.BeanKit;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.EscapeKit;
import org.miaixz.bus.shade.screw.Config;
import org.miaixz.bus.shade.screw.engine.EngineFileType;
import org.miaixz.bus.shade.screw.metadata.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * AbstractBuilder
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractProcess implements Process {

    /**
     * 配置
     */
    protected Config config;
    /**
     * 表信息缓存
     */
    volatile Map<String, List<? extends Table>> tablesCaching = new ConcurrentHashMap<>();
    /**
     * 列信息缓存
     */
    volatile Map<String, List<Column>> columnsCaching = new ConcurrentHashMap<>();
    /**
     * 主键信息缓存
     */
    volatile Map<String, List<PrimaryKey>> primaryKeysCaching = new ConcurrentHashMap<>();

    protected AbstractProcess() {

    }

    /**
     * 构造方法
     *
     * @param config {@link Config}
     */
    protected AbstractProcess(Config config) {
        Assert.notNull(config, "Configuration can not be empty!");
        this.config = config;
    }

    /**
     * 过滤表
     * 存在指定生成和指定不生成，优先级为：如果指定生成，只会生成指定的表，未指定的不会生成，也不会处理忽略表
     *
     * @param tables {@link List} 处理前数据
     * @return {@link List} 处理过后的数据
     */
    protected List<TableSchema> filterTables(List<TableSchema> tables) {
        ProcessConfig produceConfig = config.getProduceConfig();
        if (!Objects.isNull(config) && !Objects.isNull(config.getProduceConfig())) {
            //指定生成的表名、前缀、后缀任意不为空，按照指定表生成，其余不生成，不会在处理忽略表
            if (CollKit.isNotEmpty(produceConfig.getDesignatedTableName())
                    //前缀
                    || CollKit.isNotEmpty(produceConfig.getDesignatedTablePrefix())
                    //后缀
                    || CollKit.isNotEmpty(produceConfig.getDesignatedTableSuffix())) {
                return handleDesignated(tables);
            }
            //处理忽略表
            else {
                return handleIgnore(tables);
            }
        }
        return tables;
    }

    /**
     * 处理指定表
     *
     * @param tables {@link List} 处理前数据
     * @return {@link List} 处理过后的数据
     */
    private List<TableSchema> handleDesignated(List<TableSchema> tables) {
        List<TableSchema> tableSchemas = new ArrayList<>();
        ProcessConfig produceConfig = this.config.getProduceConfig();
        if (!Objects.isNull(config) && !Objects.isNull(produceConfig)) {
            //指定表名
            if (CollKit.isNotEmpty(produceConfig.getDesignatedTableName())) {
                List<String> list = produceConfig.getDesignatedTableName();
                for (String name : list) {
                    tableSchemas.addAll(tables.stream().filter(j -> j.getTableName().equals(name))
                            .collect(Collectors.toList()));
                }
            }
            //指定表名前缀
            if (CollKit.isNotEmpty(produceConfig.getDesignatedTablePrefix())) {
                List<String> list = produceConfig.getDesignatedTablePrefix();
                for (String prefix : list) {
                    tableSchemas
                            .addAll(tables.stream().filter(j -> j.getTableName().startsWith(prefix))
                                    .collect(Collectors.toList()));
                }
            }
            //指定表名后缀
            if (CollKit.isNotEmpty(produceConfig.getDesignatedTableSuffix())) {
                List<String> list = produceConfig.getDesignatedTableSuffix();
                for (String suffix : list) {
                    tableSchemas
                            .addAll(tables.stream().filter(j -> j.getTableName().endsWith(suffix))
                                    .collect(Collectors.toList()));
                }
            }
            return tableSchemas;
        }
        return tableSchemas;
    }

    /**
     * 处理忽略
     *
     * @param tables {@link List} 处理前数据
     * @return {@link List} 处理过后的数据
     */
    private List<TableSchema> handleIgnore(List<TableSchema> tables) {
        ProcessConfig produceConfig = this.config.getProduceConfig();
        if (!Objects.isNull(this.config) && !Objects.isNull(produceConfig)) {
            //处理忽略表名
            if (CollKit.isNotEmpty(produceConfig.getIgnoreTableName())) {
                List<String> list = produceConfig.getIgnoreTableName();
                for (String name : list) {
                    tables = tables.stream().filter(j -> !j.getTableName().equals(name))
                            .collect(Collectors.toList());
                }
            }
            //忽略表名前缀
            if (CollKit.isNotEmpty(produceConfig.getIgnoreTablePrefix())) {
                List<String> list = produceConfig.getIgnoreTablePrefix();
                for (String prefix : list) {
                    tables = tables.stream().filter(j -> !j.getTableName().startsWith(prefix))
                            .collect(Collectors.toList());
                }
            }
            //忽略表名后缀
            if (CollKit.isNotEmpty(produceConfig.getIgnoreTableSuffix())) {
                List<String> list = produceConfig.getIgnoreTableSuffix();
                for (String suffix : list) {
                    tables = tables.stream().filter(j -> !j.getTableName().endsWith(suffix))
                            .collect(Collectors.toList());
                }
            }
            return tables;
        }
        return tables;
    }

    /**
     * 优化数据
     *
     * @param dataModel {@link DataSchema}
     */
    public void optimizeData(DataSchema dataModel) {
        // trim
        BeanKit.trimStrFields(dataModel);
        // tables
        List<TableSchema> tables = dataModel.getTables();
        // columns
        tables.forEach(i -> {
            // table escape xml
            BeanKit.trimStrFields(i);
            List<ColumnSchema> columns = i.getColumns();
            // columns escape xml
            columns.forEach(BeanKit::trimStrFields);
        });
        // if file type is word
        if (config.getEngineConfig().getFileType().equals(EngineFileType.WORD)) {
            // escape xml
            trimAllFields(dataModel);
            // tables
            tables.forEach(i -> {
                // table escape xml
                trimAllFields(i);
                List<ColumnSchema> columns = i.getColumns();
                // columns escape xml
                columns.forEach(AbstractProcess::trimAllFields);
            });
        }
        // if file type is markdown
        if (config.getEngineConfig().getFileType().equals(EngineFileType.MD)) {
            //escape xml
            replaceStrFields(dataModel);
            // columns
            tables.forEach(i -> {
                //table escape xml
                replaceStrFields(i);
                List<ColumnSchema> columns = i.getColumns();
                // columns escape xml
                columns.forEach(AbstractProcess::replaceStrFields);
            });
        }
    }


    /**
     * 转义bean中所有属性为字符串的
     *
     * @param bean {@link Object}
     */
    public static void trimAllFields(Object bean) {
        try {
            if (null != bean) {
                // 获取所有的字段包括public,private,protected,private
                Field[] fields = bean.getClass().getDeclaredFields();
                for (Field f : fields) {
                    if ("java.lang.String".equals(f.getType().getName())) {
                        // 获取字段名
                        String key = f.getName();
                        Object value = BeanKit.getFieldValue(bean, key);

                        if (null == value) {
                            continue;
                        }
                        BeanKit.setFieldValue(bean, key, EscapeKit.escapeXml(value.toString()));
                    }
                }
            }
        } catch (Exception e) {
            throw new InternalException(e);
        }
    }

    /**
     * bean 中所有属性为字符串的进行\n\t\s处理
     *
     * @param bean {@link Object}
     */
    public static void replaceStrFields(Object bean) {
        try {
            if (null != bean) {
                // 获取所有的字段包括public,private,protected,private
                Field[] fields = bean.getClass().getDeclaredFields();
                for (Field f : fields) {
                    if ("java.lang.String".equals(f.getType().getName())) {
                        // 获取字段名
                        String key = f.getName();
                        Object value = BeanKit.getFieldValue(bean, key);
                        if (null == value) {
                            continue;
                        }
                        BeanKit.setFieldValue(bean, key, replace(value.toString()));
                    }
                }
            }
        } catch (Exception e) {
            throw new InternalException(e);
        }
    }

    /**
     * 替换字符串中的空格、回车、换行符、制表符
     *
     * @param text 字符串信息
     * @return 替换后的字符串
     */
    public static String replace(CharSequence text) {
        String val = Normal.EMPTY;
        if (null != text) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            java.util.regex.Matcher m = p.matcher(text);
            val = m.replaceAll(Normal.EMPTY);
        }
        return val;
    }

}
