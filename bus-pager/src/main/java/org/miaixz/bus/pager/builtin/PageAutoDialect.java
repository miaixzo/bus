/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org mybatis.io and other contributors.         ~
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
package org.miaixz.bus.pager.builtin;

import org.apache.ibatis.mapping.MappedStatement;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.PageException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.pager.AutoDialect;
import org.miaixz.bus.pager.Builder;
import org.miaixz.bus.pager.Dialect;
import org.miaixz.bus.pager.dialect.AbstractPaging;
import org.miaixz.bus.pager.dialect.auto.Defalut;
import org.miaixz.bus.pager.dialect.auto.Druid;
import org.miaixz.bus.pager.dialect.auto.Early;
import org.miaixz.bus.pager.dialect.auto.Hikari;
import org.miaixz.bus.pager.dialect.base.*;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 基础方言信息
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PageAutoDialect {

    private static Map<String, Class<? extends Dialect>> dialectAliasMap = new LinkedHashMap<>();
    private static Map<String, Class<? extends AutoDialect>> autoDialectMap = new LinkedHashMap<>();

    static {
        // 注册别名
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
        // 解决 informix-sqli
        registerDialectAlias("informix-sqli", Informix.class);

        registerDialectAlias("sqlserver", SqlServer.class);
        registerDialectAlias("sqlserver2012", SqlServer2012.class);

        registerDialectAlias("derby", SqlServer2012.class);
        // 达梦数据库
        registerDialectAlias("dm", Oracle.class);
        // 阿里云PPAS数据库
        registerDialectAlias("edb", Oracle.class);
        // 神通数据库
        registerDialectAlias("oscar", Oscar.class);
        registerDialectAlias("clickhouse", MySql.class);
        // 瀚高数据库
        registerDialectAlias("highgo", Hsqldb.class);
        // 虚谷数据库
        registerDialectAlias("xugu", Hsqldb.class);
        registerDialectAlias("impala", Hsqldb.class);
        registerDialectAlias("firebirdsql", Firebird.class);
        // 人大金仓数据库
        registerDialectAlias("kingbase", PostgreSql.class);
        // 人大金仓新版本kingbase8
        registerDialectAlias("kingbase8", PostgreSql.class);
        // 行云数据库
        registerDialectAlias("xcloud", CirroData.class);

        // openGauss数据库
        registerDialectAlias("opengauss", PostgreSql.class);

        // 注册 AutoDialect
        // 想要实现和以前版本相同的效果时，可以配置 autoDialectClass=old
        registerAutoDialectAlias("old", Early.class);
        registerAutoDialectAlias("hikari", Hikari.class);
        registerAutoDialectAlias("druid", Druid.class);
        // 不配置时，默认使用 Defalut
        registerAutoDialectAlias("default", Defalut.class);
    }

    /**
     * 自动获取dialect,如果没有setProperties或setSqlUtilConfig，也可以正常进行
     */
    private boolean autoDialect = true;
    /**
     * 缓存 dialect 实现，key 有两种，分别为 jdbcurl 和 dialectClassName
     */
    private Map<Object, AbstractPaging> urlDialectMap = new ConcurrentHashMap<Object, AbstractPaging>();
    private ThreadLocal<AbstractPaging> dialectThreadLocal = new ThreadLocal<AbstractPaging>();
    /**
     * 属性配置
     */
    private Properties properties;
    private AutoDialect autoDialectDelegate;
    private ReentrantLock lock = new ReentrantLock();
    private AbstractPaging delegate;

    public static void registerDialectAlias(String alias, Class<? extends Dialect> dialectClass) {
        dialectAliasMap.put(alias, dialectClass);
    }

    public static void registerAutoDialectAlias(String alias, Class<? extends AutoDialect> autoDialectClass) {
        autoDialectMap.put(alias, autoDialectClass);
    }

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
     * 反射类
     *
     * @param className
     * @return
     * @throws Exception
     */
    public static Class resloveDialectClass(String className) throws Exception {
        if (dialectAliasMap.containsKey(className.toLowerCase())) {
            return dialectAliasMap.get(className.toLowerCase());
        } else {
            return Class.forName(className);
        }
    }

    /**
     * 初始化 basic
     *
     * @param dialectClass
     * @param properties
     */
    public static AbstractPaging instanceDialect(String dialectClass, Properties properties) {
        AbstractPaging dialect;
        if (StringKit.isEmpty(dialectClass)) {
            throw new PageException("When you use the PageContext pagination plugin, you must set the basic property");
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

    // 获取当前的代理对象
    public AbstractPaging getDelegate() {
        if (delegate != null) {
            return delegate;
        }
        return dialectThreadLocal.get();
    }

    // 移除代理对象
    public void clearDelegate() {
        dialectThreadLocal.remove();
    }

    public AbstractPaging getDialectThreadLocal() {
        return dialectThreadLocal.get();
    }

    public void setDialectThreadLocal(AbstractPaging delegate) {
        this.dialectThreadLocal.set(delegate);
    }

    /**
     * 多数据动态获取时，每次需要初始化，还可以运行时指定具体的实现
     *
     * @param ms
     * @param dialectClass 分页实现，必须是 {@link AbstractPaging} 实现类，可以使用当前类中注册的别名，例如 "mysql", "oracle"
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
     * 自动获取分页方言实现
     *
     * @param ms
     * @return
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
     * 初始化自定义 AutoDialect
     *
     * @param properties
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
     * 初始化方言别名
     *
     * @param properties
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
                        // 允许配置如 dm=oracle, 直接引用oracle实现
                        if (dialectAliasMap.containsKey(kv[1])) {
                            registerDialectAlias(kv[0], dialectAliasMap.get(kv[1]));
                        } else {
                            Class<? extends Dialect> diallectClass = (Class<? extends Dialect>) Class.forName(kv[1]);
                            // 允许覆盖已有的实现
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

    public void setProperties(Properties properties) {

        this.properties = properties;
        // 初始化自定义AutoDialect
        initAutoDialectClass(properties);
        // 使用 sqlserver2012 作为默认分页方式，这种情况在动态数据源时方便使用
        String useSqlserver2012 = properties.getProperty("useSqlserver2012");
        if (StringKit.isNotEmpty(useSqlserver2012) && Boolean.parseBoolean(useSqlserver2012)) {
            registerDialectAlias("sqlserver", SqlServer2012.class);
            registerDialectAlias("sqlserver2008", SqlServer.class);
        }
        initDialectAlias(properties);
        // 指定的 Pager 数据库方言，和 不同
        String dialect = properties.getProperty("pagerDialect");
        // 运行时获取数据源
        String runtimeDialect = properties.getProperty("autoRuntimeDialect");
        // 1.动态多数据源
        if (StringKit.isNotEmpty(runtimeDialect) && "TRUE".equalsIgnoreCase(runtimeDialect)) {
            this.autoDialect = false;
        }
        // 2.动态获取方言
        else if (StringKit.isEmpty(dialect)) {
            autoDialect = true;
        }
        // 3.指定方言
        else {
            autoDialect = false;
            this.delegate = instanceDialect(dialect, properties);
        }
    }
}
