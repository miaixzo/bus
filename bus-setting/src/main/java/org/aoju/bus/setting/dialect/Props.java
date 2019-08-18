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
package org.aoju.bus.setting.dialect;

import org.aoju.bus.core.convert.Convert;
import org.aoju.bus.core.getter.BasicTypeGetter;
import org.aoju.bus.core.getter.OptBasicTypeGetter;
import org.aoju.bus.core.io.resource.ClassPathResource;
import org.aoju.bus.core.io.resource.FileResource;
import org.aoju.bus.core.io.resource.Resource;
import org.aoju.bus.core.io.resource.UriResource;
import org.aoju.bus.core.io.watch.SimpleWatcher;
import org.aoju.bus.core.io.watch.WatchMonitor;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.exception.CommonException;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.util.Date;
import java.util.Properties;

/**
 * Properties文件读取封装类
 *
 * @author Kimi Liu
 * @version 3.0.9
 * @since JDK 1.8
 */
public final class Props extends Properties implements BasicTypeGetter<String>, OptBasicTypeGetter<String> {

    private static final long serialVersionUID = 1935981579709590740L;

    /**
     * 属性文件的URL
     */
    private URL propertiesFileUrl;
    private WatchMonitor watchMonitor;
    /**
     * properties文件编码
     */
    private Charset charset = org.aoju.bus.core.consts.Charset.ISO_8859_1;

    /**
     * 构造
     */
    public Props() {
        super();
    }

    /**
     * 构造，使用相对于Class文件根目录的相对路径
     *
     * @param path
     */
    public Props(String path) {
        this(path, org.aoju.bus.core.consts.Charset.ISO_8859_1);
    }

    /**
     * 构造，使用相对于Class文件根目录的相对路径
     *
     * @param path        相对或绝对路径
     * @param charsetName 字符集
     */
    public Props(String path, String charsetName) {
        this(path, CharsetUtils.charset(charsetName));
    }

    /**
     * 构造，使用相对于Class文件根目录的相对路径
     *
     * @param path    相对或绝对路径
     * @param charset 字符集
     */
    public Props(String path, Charset charset) {
        Assert.notBlank(path, "Blank properties file path !");
        if (null != charset) {
            this.charset = charset;
        }
        this.load(ResourceUtils.getResourceObj(path));
    }

    /**
     * 构造
     *
     * @param propertiesFile 配置文件对象
     */
    public Props(File propertiesFile) {
        this(propertiesFile, StandardCharsets.ISO_8859_1);
    }

    /**
     * 构造
     *
     * @param propertiesFile 配置文件对象
     * @param charsetName    字符集
     */
    public Props(File propertiesFile, String charsetName) {
        this(propertiesFile, Charset.forName(charsetName));
    }

    /**
     * 构造
     *
     * @param propertiesFile 配置文件对象
     * @param charset        字符集
     */
    public Props(File propertiesFile, Charset charset) {
        Assert.notNull(propertiesFile, "Null properties file!");
        this.charset = charset;
        this.load(new FileResource(propertiesFile));
    }

    /**
     * 构造，相对于classes读取文件
     *
     * @param path  相对路径
     * @param clazz 基准类
     */
    public Props(String path, Class<?> clazz) {
        this(path, clazz, org.aoju.bus.core.consts.Charset.ISO_8859_1);
    }

    /**
     * 构造，相对于classes读取文件
     *
     * @param path        相对路径
     * @param clazz       基准类
     * @param charsetName 字符集
     */
    public Props(String path, Class<?> clazz, String charsetName) {
        this(path, clazz, CharsetUtils.charset(charsetName));
    }

    /**
     * 构造，相对于classes读取文件
     *
     * @param path    相对路径
     * @param clazz   基准类
     * @param charset 字符集
     */
    public Props(String path, Class<?> clazz, Charset charset) {
        Assert.notBlank(path, "Blank properties file path !");
        if (null != charset) {
            this.charset = charset;
        }
        this.load(new ClassPathResource(path, clazz));
    }

    /**
     * 构造，使用URL读取
     *
     * @param propertiesUrl 属性文件路径
     */
    public Props(URL propertiesUrl) {
        this(propertiesUrl, StandardCharsets.ISO_8859_1);
    }

    /**
     * 构造，使用URL读取
     *
     * @param propertiesUrl 属性文件路径
     * @param charsetName   字符集
     */
    public Props(URL propertiesUrl, String charsetName) {
        this(propertiesUrl, CharsetUtils.charset(charsetName));
    }

    /**
     * 构造，使用URL读取
     *
     * @param propertiesUrl 属性文件路径
     * @param charset       字符集
     */
    public Props(URL propertiesUrl, Charset charset) {
        Assert.notNull(propertiesUrl, "Null properties URL !");
        if (null != charset) {
            this.charset = charset;
        }
        this.load(new UriResource(propertiesUrl));
    }

    /**
     * 构造，使用URL读取
     *
     * @param properties 属性文件路径
     */
    public Props(Properties properties) {
        if (CollUtils.isNotEmpty(properties)) {
            this.putAll(properties);
        }
    }

    /**
     * 获得Classpath下的Properties文件
     *
     * @param resource 资源（相对Classpath的路径）
     * @return Properties
     */
    public static Properties getProp(String resource) {
        return new Props(resource);
    }

    /**
     * 获得Classpath下的Properties文件
     *
     * @param resource    资源（相对Classpath的路径）
     * @param charsetName 字符集
     * @return Properties
     */
    public static Properties getProp(String resource, String charsetName) {
        return new Props(resource, charsetName);
    }

    /**
     * 获得Classpath下的Properties文件
     *
     * @param resource 资源（相对Classpath的路径）
     * @param charset  字符集
     * @return Properties
     */
    public static Properties getProp(String resource, Charset charset) {
        return new Props(resource, charset);
    }

    /**
     * 初始化配置文件
     *
     * @param urlResource {@link UriResource}
     */
    public void load(Resource urlResource) {
        this.propertiesFileUrl = urlResource.getUrl();
        if (null == this.propertiesFileUrl) {
            throw new CommonException("Can not find properties file: [{}]", urlResource);
        }
        try (final BufferedReader reader = urlResource.getReader(charset)) {
            super.load(reader);
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 重新加载配置文件
     */
    public void load() {
        this.load(new UriResource(this.propertiesFileUrl));
    }

    /**
     * 在配置文件变更时自动加载
     *
     * @param autoReload 是否自动加载
     */
    public void autoLoad(boolean autoReload) {
        if (autoReload) {
            if (null != this.watchMonitor) {
                this.watchMonitor.close();
            }
            try {
                watchMonitor = WatchMonitor.create(Paths.get(this.propertiesFileUrl.toURI()));
                watchMonitor.setWatcher(new SimpleWatcher() {
                    @Override
                    public void onModify(WatchEvent<?> event, Path currentPath) {
                        load();
                    }
                }).start();
            } catch (Exception e) {
                throw new CommonException("Setting auto load not support url: [{}]", this.propertiesFileUrl);
            }
        } else {
            IoUtils.close(this.watchMonitor);
            this.watchMonitor = null;
        }
    }

    @Override
    public Object getObj(String key, Object defaultValue) {
        return getStr(key, null == defaultValue ? null : defaultValue.toString());
    }

    @Override
    public Object getObj(String key) {
        return getObj(key, null);
    }

    @Override
    public String getStr(String key, String defaultValue) {
        return super.getProperty(key, defaultValue);
    }

    @Override
    public String getStr(String key) {
        return super.getProperty(key);
    }

    @Override
    public Integer getInt(String key, Integer defaultValue) {
        return Convert.toInt(getStr(key), defaultValue);
    }

    @Override
    public Integer getInt(String key) {
        return getInt(key, null);
    }

    @Override
    public Boolean getBool(String key, Boolean defaultValue) {
        return Convert.toBool(getStr(key), defaultValue);
    }

    @Override
    public Boolean getBool(String key) {
        return getBool(key, null);
    }

    @Override
    public Long getLong(String key, Long defaultValue) {
        return Convert.toLong(getStr(key), defaultValue);
    }

    @Override
    public Long getLong(String key) {
        return getLong(key, null);
    }

    @Override
    public Character getChar(String key, Character defaultValue) {
        final String value = getStr(key);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        return value.charAt(0);
    }

    @Override
    public Character getChar(String key) {
        return getChar(key, null);
    }

    @Override
    public Float getFloat(String key) {
        return getFloat(key, null);
    }

    @Override
    public Float getFloat(String key, Float defaultValue) {
        return Convert.toFloat(getStr(key), defaultValue);
    }

    @Override
    public Double getDouble(String key, Double defaultValue) throws NumberFormatException {
        return Convert.toDouble(getStr(key), defaultValue);
    }

    @Override
    public Double getDouble(String key) throws NumberFormatException {
        return getDouble(key, null);
    }

    @Override
    public Short getShort(String key, Short defaultValue) {
        return Convert.toShort(getStr(key), defaultValue);
    }

    @Override
    public Short getShort(String key) {
        return getShort(key, null);
    }

    @Override
    public Byte getByte(String key, Byte defaultValue) {
        return Convert.toByte(getStr(key), defaultValue);
    }

    @Override
    public Byte getByte(String key) {
        return getByte(key, null);
    }

    @Override
    public BigDecimal getBigDecimal(String key, BigDecimal defaultValue) {
        final String valueStr = getStr(key);
        if (StringUtils.isBlank(valueStr)) {
            return defaultValue;
        }

        try {
            return new BigDecimal(valueStr);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Override
    public BigDecimal getBigDecimal(String key) {
        return getBigDecimal(key, null);
    }

    @Override
    public BigInteger getBigInteger(String key, BigInteger defaultValue) {
        final String valueStr = getStr(key);
        if (StringUtils.isBlank(valueStr)) {
            return defaultValue;
        }

        try {
            return new BigInteger(valueStr);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Override
    public BigInteger getBigInteger(String key) {
        return getBigInteger(key, null);
    }

    @Override
    public <E extends Enum<E>> E getEnum(Class<E> clazz, String key, E defaultValue) {
        return Convert.toEnum(clazz, getStr(key), defaultValue);
    }

    @Override
    public <E extends Enum<E>> E getEnum(Class<E> clazz, String key) {
        return getEnum(clazz, key, null);
    }

    @Override
    public Date getDate(String key, Date defaultValue) {
        return Convert.toDate(getStr(key), defaultValue);
    }

    @Override
    public Date getDate(String key) {
        return getDate(key, null);
    }

    /**
     * 获取并删除键值对，当指定键对应值非空时，返回并删除这个值，后边的键对应的值不再查找
     *
     * @param keys 键列表，常用于别名
     * @return 字符串值
     * @since 4.1.21
     */
    public String getAndRemoveStr(String... keys) {
        Object value = null;
        for (String key : keys) {
            value = remove(key);
            if (null != value) {
                break;
            }
        }
        return (String) value;
    }

    /**
     * 设置值，无给定键创建之。设置后未持久化
     *
     * @param key   属性键
     * @param value 属性值
     */
    public void setProperty(String key, Object value) {
        super.setProperty(key, value.toString());
    }

    /**
     * 持久化当前设置，会覆盖掉之前的设置
     *
     * @param absolutePath 设置文件的绝对路径
     * @throws CommonException IO异常，可能为文件未找到
     */
    public void store(String absolutePath) throws CommonException {
        Writer writer = null;
        try {
            writer = FileUtils.getWriter(absolutePath, charset, false);
            super.store(writer, null);
        } catch (IOException e) {
            throw new CommonException("Store properties to [{}] error!", absolutePath);
        } finally {
            IoUtils.close(writer);
        }
    }

    /**
     * 存储当前设置，会覆盖掉以前的设置
     *
     * @param path  相对路径
     * @param clazz 相对的类
     */
    public void store(String path, Class<?> clazz) {
        this.store(FileUtils.getAbsolutePath(path, clazz));
    }

}
