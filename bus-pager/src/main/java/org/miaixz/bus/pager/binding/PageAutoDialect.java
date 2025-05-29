/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org mapper.io and other contributors.         ~
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
package org.miaixz.bus.pager.binding;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import javax.sql.DataSource;

import org.apache.ibatis.mapping.MappedStatement;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.PageException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.pager.dialect.AutoDialect;
import org.miaixz.bus.pager.Builder;
import org.miaixz.bus.pager.Dialect;
import org.miaixz.bus.pager.dialect.AbstractPaging;
import org.miaixz.bus.pager.dialect.auto.Defalut;
import org.miaixz.bus.pager.dialect.auto.Druid;
import org.miaixz.bus.pager.dialect.auto.Early;
import org.miaixz.bus.pager.dialect.auto.Hikari;
import org.miaixz.bus.pager.dialect.base.*;

/**
 * 提供数据库分页方言的自动识别和配置功能。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PageAutoDialect {

    /**
     * 存储方言别名与实现类的映射
     */
    private static Map<String, Class<? extends Dialect>> dialectAliasMap = new LinkedHashMap<>();
    /**
     * 存储自动方言别名与实现类的映射
     */
    private static Map<String, Class<? extends AutoDialect>> autoDialectMap = new LinkedHashMap<>();
    /**
     * 是否启用自动方言识别
     */
    private boolean autoDialect = true;
    /**
     * 缓存方言实现，键为JDBC URL或方言类名
     */
    private Map<Object, AbstractPaging> urlDialectMap = new ConcurrentHashMap<>();
    /**
     * 线程本地存储的方言实例
     */
    private ThreadLocal<AbstractPaging> dialectThreadLocal = new ThreadLocal<>();
    /**
     * 配置属性
     */
    private Properties properties;
    /**
     * 自动方言识别委托
     */
    private AutoDialect autoDialectDelegate;
    /**
     * 线程安全锁
     */
    private ReentrantLock lock = new ReentrantLock();
    /**
     * 默认方言实例
     */
    private AbstractPaging delegate;

    static {
        // 注册数据库方言别名
        registerDialectAlias("hsqldb", Hsqldb.class);
        registerDialectAlias("h2", Hsqldb.class);
        registerDialectAlias("phoenix", Hsqldb.class);
        registerDialectAlias("postgresql", PostgreSql.class);
        registerDialectAlias("mysql", MySql.class);
        registerDialectAlias("mariadb", MySql.class);
        registerDialectAlias("sqlite", MySql.class);
        registerDialectAlias("herddb", HerdDB.class);
        registerDialectAlias("oracle", Oracle.class);
        registerDialectAlias("oracle9i", Oracle9i.class);
        registerDialectAlias("db2", Db2.class);
        registerDialectAlias("as400", AS400.class);
        registerDialectAlias("informix", Informix.class);
        registerDialectAlias("informix-sqli", Informix.class);
        registerDialectAlias("sqlserver", SqlServer.class);
        registerDialectAlias("sqlserver2012", SqlServer2012.class);
        registerDialectAlias("derby", SqlServer2012.class);
        registerDialectAlias("dm", Oracle.class);
        registerDialectAlias("edb", Oracle.class);
        registerDialectAlias("oscar", Oscar.class);
        registerDialectAlias("clickhouse", MySql.class);
        registerDialectAlias("highgo", Hsqldb.class);
        registerDialectAlias("xugu", Xugudb.class);
        registerDialectAlias("impala", Hsqldb.class);
        registerDialectAlias("firebirdsql", Firebird.class);
        registerDialectAlias("kingbase", PostgreSql.class);
        registerDialectAlias("kingbase8", PostgreSql.class);
        registerDialectAlias("xcloud", CirroData.class);
        registerDialectAlias("opengauss", PostgreSql.class);
        registerDialectAlias("sundb", Oracle.class);

        // 注册自动方言别名
        registerAutoDialectAlias("old", Early.class);
        registerAutoDialectAlias("hikari", Hikari.class);
        registerAutoDialectAlias("druid", Druid.class);
        registerAutoDialectAlias("default", Defalut.class);
    }

    /**
     * 注册方言别名。
     *
     * @param alias        方言别名
     * @param dialectClass 方言实现类
     */
    public static void registerDialectAlias(String alias, Class<? extends Dialect> dialectClass) {
        dialectAliasMap.put(alias, dialectClass);
    }

    /**
     * 注册自动方言别名。
     *
     * @param alias            自动方言别名
     * @param autoDialectClass 自动方言实现类
     */
    public static void registerAutoDialectAlias(String alias, Class<? extends AutoDialect> autoDialectClass) {
        autoDialectMap.put(alias, autoDialectClass);
    }

    /**
     * 从JDBC URL提取方言名称。
     *
     * @param jdbcUrl JDBC URL
     * @return 方言名称，若无法识别则返回null
     */
    public static String fromJdbcUrl(String jdbcUrl) {
        final String url = jdbcUrl.toLowerCase();
        for (String dialect : dialectAliasMap.keySet()) {
            if (url.contains(Symbol.COLON + dialect.toLowerCase() + Symbol.COLON)) {
                return dialect;
            }
        }
        return null;
    }

    /**
     * 解析方言类。
     *
     * @param className 方言类名或别名
     * @return 方言实现类
     * @throws Exception 若类不存在或无法加载
     */
    public static Class resloveDialectClass(String className) throws Exception {
        if (dialectAliasMap.containsKey(className.toLowerCase())) {
            return dialectAliasMap.get(className.toLowerCase());
        } else {
            return Class.forName(className);
        }
    }

    /**
     * 实例化方言对象。
     *
     * @param dialectClass 方言类名或别名
     * @param properties   配置属性
     * @return 方言实例
     * @throws PageException 若实例化失败
     */
    public static AbstractPaging instanceDialect(String dialectClass, Properties properties) {
        AbstractPaging dialect;
        if (StringKit.isEmpty(dialectClass)) {
            throw new PageException("When you use the PageContext pagination handler, you must set the basic property");
        }
        try {
            Class sqlDialectClass = resloveDialectClass(dialectClass);
            if (AbstractPaging.class.isAssignableFrom(sqlDialectClass)) {
                dialect = (AbstractPaging) sqlDialectClass.newInstance();
            } else {
                throw new PageException(
                        "When using PageContext, the dialect must be an implementation class that implements the "
                                + AbstractPaging.class.getCanonicalName() + " interface!");
            }
        } catch (Exception e) {
            throw new PageException("error initializing basic dialectclass[" + dialectClass + "]" + e.getMessage(), e);
        }
        dialect.setProperties(properties);
        return dialect;
    }

    /**
     * 获取当前方言代理对象。
     *
     * @return 方言实例
     */
    public AbstractPaging getDelegate() {
        if (delegate != null) {
            return delegate;
        }
        return dialectThreadLocal.get();
    }

    /**
     * 清除线程本地方言代理。
     */
    public void clearDelegate() {
        dialectThreadLocal.remove();
    }

    /**
     * 获取线程本地方言实例。
     *
     * @return 方言实例
     */
    public AbstractPaging getDialectThreadLocal() {
        return dialectThreadLocal.get();
    }

    /**
     * 设置线程本地方言实例。
     *
     * @param delegate 方言实例
     */
    public void setDialectThreadLocal(AbstractPaging delegate) {
        this.dialectThreadLocal.set(delegate);
    }

    /**
     * 初始化方言代理，支持运行时指定方言。
     *
     * @param ms           MyBatis映射语句
     * @param dialectClass 方言实现类或别名，如"mysql"、"oracle"
     */
    public void initDelegateDialect(MappedStatement ms, String dialectClass) {
        if (StringKit.isNotEmpty(dialectClass)) {
            AbstractPaging dialect = urlDialectMap.get(dialectClass);
            if (dialect == null) {
                lock.lock();
                try {
                    if ((dialect = urlDialectMap.get(dialectClass)) == null) {
                        dialect = instanceDialect(dialectClass, properties);
                        urlDialectMap.put(dialectClass, dialect);
                    }
                } finally {
                    lock.unlock();
                }
            }
            dialectThreadLocal.set(dialect);
        } else if (delegate == null) {
            if (autoDialect) {
                this.delegate = autoGetDialect(ms);
            } else {
                dialectThreadLocal.set(autoGetDialect(ms));
            }
        }
    }

    /**
     * 自动获取分页方言实现。
     *
     * @param ms MyBatis映射语句
     * @return 方言实例
     */
    public AbstractPaging autoGetDialect(MappedStatement ms) {
        DataSource dataSource = ms.getConfiguration().getEnvironment().getDataSource();
        Object dialectKey = autoDialectDelegate.extractDialectKey(ms, dataSource, properties);
        if (dialectKey == null) {
            return autoDialectDelegate.extractDialect(dialectKey, ms, dataSource, properties);
        } else if (!urlDialectMap.containsKey(dialectKey)) {
            lock.lock();
            try {
                if (!urlDialectMap.containsKey(dialectKey)) {
                    urlDialectMap.put(dialectKey,
                            autoDialectDelegate.extractDialect(dialectKey, ms, dataSource, properties));
                }
            } finally {
                lock.unlock();
            }
        }
        return urlDialectMap.get(dialectKey);
    }

    /**
     * 初始化自定义自动方言实现。
     *
     * @param properties 配置属性
     */
    private void initAutoDialectClass(Properties properties) {
        String autoDialectClassStr = properties.getProperty("autoDialectClass");
        if (StringKit.isNotEmpty(autoDialectClassStr)) {
            try {
                Class<? extends AutoDialect> autoDialectClass;
                if (autoDialectMap.containsKey(autoDialectClassStr)) {
                    autoDialectClass = autoDialectMap.get(autoDialectClassStr);
                } else {
                    autoDialectClass = (Class<AutoDialect>) Class.forName(autoDialectClassStr);
                }
                this.autoDialectDelegate = Builder.newInstance(autoDialectClass, properties);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Make sure that the AutoDialect implementation class ("
                        + autoDialectClassStr + ") for the autoDialectClass configuration exists!", e);
            } catch (Exception e) {
                throw new RuntimeException(autoDialectClassStr + "Class must provide a constructor without parameters",
                        e);
            }
        } else {
            this.autoDialectDelegate = new Defalut();
        }
    }

    /**
     * 初始化方言别名配置。
     *
     * @param properties 配置属性
     */
    private void initDialectAlias(Properties properties) {
        String dialectAlias = properties.getProperty("dialectAlias");
        if (StringKit.isNotEmpty(dialectAlias)) {
            String[] alias = dialectAlias.split(Symbol.SEMICOLON);
            for (int i = 0; i < alias.length; i++) {
                String[] kv = alias[i].split(Symbol.EQUAL);
                if (kv.length != 2) {
                    throw new IllegalArgumentException("dialectAlias parameter misconfigured,"
                            + "Please follow alias1=xx.dialectClass; alias2=dialectClass2!");
                }
                for (int j = 0; j < kv.length; j++) {
                    try {
                        if (dialectAliasMap.containsKey(kv[1])) {
                            registerDialectAlias(kv[0], dialectAliasMap.get(kv[1]));
                        } else {
                            Class<? extends Dialect> diallectClass = (Class<? extends Dialect>) Class.forName(kv[1]);
                            registerDialectAlias(kv[0], diallectClass);
                        }
                    } catch (ClassNotFoundException e) {
                        throw new IllegalArgumentException(
                                "Make sure the Dialect implementation class configured by dialectAlias exists!", e);
                    }
                }
            }
        }
    }

    /**
     * 设置分页配置属性。
     *
     * @param properties 配置属性
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
        initAutoDialectClass(properties);
        String useSqlserver2012 = properties.getProperty("useSqlserver2012");
        if (StringKit.isNotEmpty(useSqlserver2012) && Boolean.parseBoolean(useSqlserver2012)) {
            registerDialectAlias("sqlserver", SqlServer2012.class);
            registerDialectAlias("sqlserver2008", SqlServer.class);
        }
        initDialectAlias(properties);
        String dialect = properties.getProperty("pagerDialect");
        String runtimeDialect = properties.getProperty("autoRuntimeDialect");
        if (StringKit.isNotEmpty(runtimeDialect) && "TRUE".equalsIgnoreCase(runtimeDialect)) {
            this.autoDialect = false;
        } else if (StringKit.isEmpty(dialect)) {
            autoDialect = true;
        } else {
            autoDialect = false;
            this.delegate = instanceDialect(dialect, properties);
        }
    }

}