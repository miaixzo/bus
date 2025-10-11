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
 * Provides automatic identification and configuration of database pagination dialects. This class manages a mapping of
 * dialect aliases to their implementations and handles the dynamic selection of the appropriate dialect based on the
 * database connection.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PageAutoDialect {

    /**
     * Stores a mapping of dialect aliases to their implementation classes.
     */
    private static Map<String, Class<? extends Dialect>> dialectAliasMap = new LinkedHashMap<>();
    /**
     * Stores a mapping of auto-dialect aliases to their implementation classes.
     */
    private static Map<String, Class<? extends AutoDialect>> autoDialectMap = new LinkedHashMap<>();
    /**
     * Flag indicating whether automatic dialect recognition is enabled. Default is true.
     */
    private boolean autoDialect = true;
    /**
     * Caches dialect implementations, with the key being the JDBC URL or dialect class name.
     */
    private Map<Object, AbstractPaging> urlDialectMap = new ConcurrentHashMap<>();
    /**
     * Thread-local storage for dialect instances, ensuring each thread has its own dialect.
     */
    private ThreadLocal<AbstractPaging> dialectThreadLocal = new ThreadLocal<>();
    /**
     * Configuration properties for the pagination plugin.
     */
    private Properties properties;
    /**
     * Delegate for automatic dialect recognition.
     */
    private AutoDialect autoDialectDelegate;
    /**
     * Reentrant lock for thread-safe operations, especially during dialect initialization.
     */
    private ReentrantLock lock = new ReentrantLock();
    /**
     * The default dialect instance, used when auto-dialect is disabled or a specific dialect is configured.
     */
    private AbstractPaging delegate;

    static {
        // Register database dialect aliases
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

        // Register auto-dialect aliases
        registerAutoDialectAlias("old", Early.class);
        registerAutoDialectAlias("hikari", Hikari.class);
        registerAutoDialectAlias("druid", Druid.class);
        registerAutoDialectAlias("default", Defalut.class);
    }

    /**
     * Registers a database dialect alias with its corresponding implementation class.
     *
     * @param alias        the alias for the dialect (e.g., "mysql", "oracle")
     * @param dialectClass the {@link Dialect} implementation class
     */
    public static void registerDialectAlias(String alias, Class<? extends Dialect> dialectClass) {
        dialectAliasMap.put(alias, dialectClass);
    }

    /**
     * Registers an auto-dialect alias with its corresponding implementation class.
     *
     * @param alias            the alias for the auto-dialect
     * @param autoDialectClass the {@link AutoDialect} implementation class
     */
    public static void registerAutoDialectAlias(String alias, Class<? extends AutoDialect> autoDialectClass) {
        autoDialectMap.put(alias, autoDialectClass);
    }

    /**
     * Extracts the dialect name from a JDBC URL.
     *
     * @param jdbcUrl the JDBC URL string
     * @return the dialect name if recognized, otherwise null
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
     * Resolves a dialect class from its name or alias.
     *
     * @param className the dialect class name or alias
     * @return the resolved dialect implementation class
     * @throws Exception if the class does not exist or cannot be loaded
     */
    public static Class resloveDialectClass(String className) throws Exception {
        if (dialectAliasMap.containsKey(className.toLowerCase())) {
            return dialectAliasMap.get(className.toLowerCase());
        } else {
            return Class.forName(className);
        }
    }

    /**
     * Instantiates a dialect object from its class name or alias.
     *
     * @param dialectClass the dialect class name or alias
     * @param properties   the properties to set on the dialect instance
     * @return an instance of {@link AbstractPaging}
     * @throws PageException if instantiation fails or the class is not a valid dialect implementation
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
     * Retrieves the current dialect delegate. If a thread-local dialect is set, it returns that; otherwise, it returns
     * the default delegate.
     *
     * @return the current {@link AbstractPaging} instance
     */
    public AbstractPaging getDelegate() {
        if (delegate != null) {
            return delegate;
        }
        return dialectThreadLocal.get();
    }

    /**
     * Clears the thread-local dialect delegate.
     */
    public void clearDelegate() {
        dialectThreadLocal.remove();
    }

    /**
     * Retrieves the thread-local dialect instance.
     *
     * @return the thread-local {@link AbstractPaging} instance
     */
    public AbstractPaging getDialectThreadLocal() {
        return dialectThreadLocal.get();
    }

    /**
     * Sets the thread-local dialect instance.
     *
     * @param delegate the {@link AbstractPaging} instance to set for the current thread
     */
    public void setDialectThreadLocal(AbstractPaging delegate) {
        this.dialectThreadLocal.set(delegate);
    }

    /**
     * Initializes the dialect delegate, supporting runtime specification of the dialect. If a specific dialect class is
     * provided, it will be used. Otherwise, it attempts to auto-detect the dialect.
     *
     * @param ms           the MyBatis MappedStatement
     * @param dialectClass the dialect implementation class name or alias (e.g., "mysql", "oracle"), can be null for
     *                     auto-detection
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
     * Automatically retrieves the pagination dialect implementation based on the MappedStatement and DataSource.
     *
     * @param ms the MyBatis MappedStatement
     * @return an instance of {@link AbstractPaging} representing the detected dialect
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
                    urlDialectMap.put(
                            dialectKey,
                            autoDialectDelegate.extractDialect(dialectKey, ms, dataSource, properties));
                }
            } finally {
                lock.unlock();
            }
        }
        return urlDialectMap.get(dialectKey);
    }

    /**
     * Initializes the custom auto-dialect implementation based on properties.
     *
     * @param properties the configuration properties
     * @throws IllegalArgumentException if the configured auto-dialect class does not exist
     * @throws RuntimeException         if the auto-dialect class does not provide a parameterless constructor
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
     * Initializes custom dialect alias configurations from properties. The {@code dialectAlias} property should be a
     * semicolon-separated list of key-value pairs (e.g., "alias1=xx.dialectClass;alias2=dialectClass2").
     *
     * @param properties the configuration properties
     * @throws IllegalArgumentException if the dialectAlias parameter is misconfigured or a configured dialect
     *                                  implementation class does not exist
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
     * Sets the pagination configuration properties for this {@code PageAutoDialect} instance. This method initializes
     * auto-dialect settings, registers custom dialect aliases, and sets the default dialect.
     *
     * @param properties the properties to set
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
