/*
 * ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 * ~                                                                               ~
 * ~ The MIT License (MIT)                                                         ~
 * ~                                                                               ~
 * ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 * ~                                                                               ~
 * ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 * ~ of this software and associated documentation files (the "Software"), to deal ~
 * ~ in the Software without restriction, including without limitation the rights  ~
 * ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 * ~ copies of the Software, and to permit persons to whom the Software is         ~
 * ~ furnished to do so, subject to the following conditions:                      ~
 * ~                                                                               ~
 * ~ The above copyright notice and this permission notice shall be included in    ~
 * ~ all copies or substantial portions of the Software.                           ~
 * ~                                                                               ~
 * ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 * ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 * ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 * ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 * ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 * ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 * ~ THE SOFTWARE.                                                                 ~
 * ~                                                                               ~
 * ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 */
package org.miaixz.bus.mapper;

import org.miaixz.bus.core.lang.loader.spi.NormalSpiLoader;
import org.miaixz.bus.mapper.builder.ColumnSchemaBuilder;
import org.miaixz.bus.mapper.builder.ColumnSchemaChain;
import org.miaixz.bus.mapper.builder.TableSchemaBuilder;
import org.miaixz.bus.mapper.builder.TableSchemaChain;

/**
 * 工厂实例持有类，管理表工厂和列工厂的处理链
 * <p>
 * 该类负责维护数据源切换的上下文，以及表结构和列结构的构建链。 通过 SPI 机制加载表和列的构建器，并提供了数据源切换的线程安全实现。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Holder<T> implements org.miaixz.bus.core.Holder<T> {

    /**
     * 表工厂处理链，通过 SPI 加载
     * <p>
     * 该链式处理器负责根据数据库表结构信息构建表模型。 通过 SPI 机制加载所有实现了 TableSchemaBuilder 接口的类，并按顺序组成处理链。
     * </p>
     */
    public static final TableSchemaBuilder.Chain TABLE_SCHEMA_CHAIN = new TableSchemaChain(
            NormalSpiLoader.loadList(false, TableSchemaBuilder.class));

    /**
     * 列工厂处理链，通过 SPI 加载
     * <p>
     * 该链式处理器负责根据数据库列结构信息构建列模型。 通过 SPI 机制加载所有实现了 ColumnSchemaBuilder 接口的类，并按顺序组成处理链。
     * </p>
     */
    public static final ColumnSchemaBuilder.Chain COLUMN_SCHEMA_CHAIN = new ColumnSchemaChain(
            NormalSpiLoader.loadList(false, ColumnSchemaBuilder.class));

    /**
     * 数据源键的线程局部变量
     * <p>
     * 使用 ThreadLocal 存储当前线程使用的数据源标识，确保多线程环境下数据源切换的线程安全性。 每个线程可以独立设置和获取自己使用的数据源，互不干扰。
     * </p>
     */
    private static final ThreadLocal<String> DATA_SOURCE_KEY = new ThreadLocal<>();

    /**
     * 默认数据源名称
     * <p>
     * 当没有显式指定数据源时，系统将使用此默认数据源。 该值在系统启动时通过配置文件或代码设置，并在整个应用生命周期内保持不变。
     * </p>
     */
    private static String DEFAULT_KEY;

    /**
     * 获取当前线程使用的数据源键 （包括默认值）
     * <p>
     * 如果当前线程没有设置数据源键，则返回 null。 通常在数据源路由时调用此方法获取当前应该使用的数据源。
     * </p>
     *
     * @return 当前线程使用的数据源键，如果未设置则返回 null
     */
    public static String getKey() {
        String dataSource = DATA_SOURCE_KEY.get();
        return dataSource != null ? dataSource : getDefault();
    }

    /**
     * 设置当前线程使用的数据源键
     * <p>
     * 在执行数据库操作前，调用此方法设置当前线程应该使用的数据源。 设置后的数据源键将影响当前线程后续所有的数据库操作，直到被清除或更改。
     * </p>
     *
     * @param key 数据源键，用于标识特定的数据源
     */
    public static void setKey(String key) {
        DATA_SOURCE_KEY.set(key);
    }

    /**
     * 清除当前线程的数据源键设置
     * <p>
     * 在数据库操作完成后，应该调用此方法清除数据源键设置，避免影响后续操作。 特别是在线程池环境中，不清除数据源键可能会导致线程复用时使用错误的数据源。
     * </p>
     */
    public static void remove() {
        DATA_SOURCE_KEY.remove();
    }

    /**
     * 设置默认数据源名称
     * <p>
     * 在系统初始化时调用此方法设置默认数据源。 默认数据源是当没有显式指定数据源时系统自动使用的数据源。
     * </p>
     *
     * @param name 默认数据源名称，不能为 null 或空字符串
     */
    public static void setDefault(String name) {
        DEFAULT_KEY = name;
    }

    /**
     * 获取默认数据源名称
     * <p>
     * 获取系统配置的默认数据源名称。 当没有显式指定数据源时，系统将使用此默认数据源。
     * </p>
     *
     * @return 默认数据源名称，如果未设置则可能返回 null
     */
    public static String getDefault() {
        return DEFAULT_KEY;
    }

}