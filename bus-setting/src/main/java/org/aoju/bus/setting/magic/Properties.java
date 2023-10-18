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
package org.aoju.bus.setting.magic;

import org.aoju.bus.core.exception.InternalException;
import org.aoju.bus.core.getter.TypeGetter;
import org.aoju.bus.core.io.resource.Resource;
import org.aoju.bus.core.io.resource.UriResource;
import org.aoju.bus.core.io.watcher.SimpleWatcher;
import org.aoju.bus.core.io.watcher.WatchMonitor;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.lang.FileType;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.function.XFunction;
import org.aoju.bus.core.lang.function.XSupplier;
import org.aoju.bus.core.toolkit.*;
import org.aoju.bus.logger.Logger;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.Arrays;

/**
 * Properties文件读取封装类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class Properties extends java.util.Properties implements TypeGetter<CharSequence> {

    private static final long serialVersionUID = 1L;

    /**
     * 属性文件的Resource
     */
    private Resource resource;
    private WatchMonitor watchMonitor;
    /**
     * properties文件编码
     * issue#1701，此属性不能被序列化，故忽略序列化
     */
    private transient java.nio.charset.Charset charset = Charset.ISO_8859_1;

    /**
     * 构造，使用相对于Class文件根目录的相对路径
     *
     * @param path 配置文件路径，相对于ClassPath，或者使用绝对路径
     */
    public Properties(final String path) {
        this(path, null);
    }

    /**
     * 构造，使用相对于Class文件根目录的相对路径
     *
     * @param path    相对或绝对路径
     * @param charset 自定义编码
     */
    public Properties(final String path, final java.nio.charset.Charset charset) {
        Assert.notBlank(path, "Blank properties file path !");
        if (null != charset) {
            this.charset = charset;
        }
        this.load(FileKit.getUrl(path, Properties.class));
    }

    /**
     * 构造
     *
     * @param propertiesFile 配置文件对象
     */
    public Properties(final File propertiesFile) {
        this(propertiesFile, null);
    }

    /**
     * 构造
     */
    public Properties() {

    }

    /**
     * 构造
     *
     * @param propertiesFile 配置文件对象
     * @param charset        自定义编码
     */
    public Properties(final File propertiesFile, final java.nio.charset.Charset charset) {
        Assert.notNull(propertiesFile, "Null properties file!");
        if (null != charset) {
            this.charset = charset;
        }
        this.load(FileKit.getResource(propertiesFile));
    }

    /**
     * 构造，使用URL读取
     *
     * @param resource {@link Resource}
     * @param charset  自定义编码
     */
    public Properties(final Resource resource, final java.nio.charset.Charset charset) {
        Assert.notNull(resource, "Null properties URL !");
        if (null != charset) {
            this.charset = charset;
        }
        this.load(resource);
    }

    /**
     * 构造，使用URL读取
     *
     * @param properties 属性文件路径
     */
    public Properties(final java.util.Properties properties) {
        if (MapKit.isNotEmpty(properties)) {
            this.putAll(properties);
        }
    }

    /**
     * 构建一个空的Props，用于手动加入参数
     *
     * @return Properties
     */
    public static Properties of() {
        return new Properties();
    }

    /**
     * 获得Classpath下的Properties文件
     *
     * @param resource 资源（相对Classpath的路径）
     * @return Props
     */
    public static Properties of(final String resource) {
        return new Properties(resource);
    }

    /**
     * 获得Classpath下的Properties文件
     *
     * @param resource 资源（相对Classpath的路径）
     * @param charset  自定义编码
     * @return Properties
     */
    public static Properties of(final String resource, final java.nio.charset.Charset charset) {
        return new Properties(resource, charset);
    }

    /**
     * 加载配置文件内容到{@link java.util.Properties}中
     * 需要注意的是，如果资源文件的扩展名是.xml，会调用{@link java.util.Properties#loadFromXML(InputStream)} 读取。
     *
     * @param properties {@link java.util.Properties}文件
     * @param resource   资源
     * @param charset    编码，对XML无效
     */
    public static void load(final java.util.Properties properties, final Resource resource, final java.nio.charset.Charset charset) {
        final String filename = resource.getName();
        if (filename != null && filename.endsWith(FileType.TYPE_XML)) {
            // XML
            try (final InputStream in = resource.getStream()) {
                properties.loadFromXML(in);
            } catch (final IOException e) {
                throw new InternalException(e);
            }
        } else {
            // .properties
            try (final BufferedReader reader = resource.getReader(charset)) {
                properties.load(reader);
            } catch (final IOException e) {
                throw new InternalException(e);
            }
        }
    }

    /**
     * 初始化配置文件
     *
     * @param url {@link URL}
     */
    public void load(final URL url) {
        load(new UriResource(url));
    }

    /**
     * 初始化配置文件
     *
     * @param resource {@link Resource}
     */
    public void load(final Resource resource) {
        Assert.notNull(resource, "Props resource must be not null!");
        this.resource = resource;
        load(this, resource, this.charset);
    }

    /**
     * 重新加载配置文件
     */
    public void load() {
        this.load(this.resource);
    }

    /**
     * 在配置文件变更时自动加载
     *
     * @param autoReload 是否自动加载
     */
    public void autoLoad(final boolean autoReload) {
        if (autoReload) {
            Assert.notNull(this.resource, "Properties resource must be not null!");
            if (null != this.watchMonitor) {
                // 先关闭之前的监听
                this.watchMonitor.close();
            }
            this.watchMonitor = WatchKit.createModify(this.resource.getUrl(), new SimpleWatcher() {
                @Override
                public void onModify(final WatchEvent<?> event, final Path currentPath) {
                    load();
                }
            });
            this.watchMonitor.start();
        } else {
            IoKit.close(this.watchMonitor);
            this.watchMonitor = null;
        }
    }

    @Override
    public Object getObject(final CharSequence key, final Object defaultValue) {
        return ObjectKit.defaultIfNull(getProperty(StringKit.toString(key)), defaultValue);
    }

    /**
     * 根据lambda的方法引用，获取
     *
     * @param func 方法引用
     * @param <P>  参数类型
     * @param <T>  返回值类型
     * @return 获取表达式对应属性和返回的对象
     */
    public <P, T> T get(final XFunction<P, T> func) {
        final LambdaKit.Info lambdaInfo = LambdaKit.resolve(func);
        return get(lambdaInfo.getFieldName(), lambdaInfo.getReturnType());
    }

    /**
     * 获取并删除键值对，当指定键对应值非空时，返回并删除这个值，后边的键对应的值不再查找
     *
     * @param keys 键列表，常用于别名
     * @return 字符串值
     */
    public String getAndRemoveString(final String... keys) {
        Object value = null;
        for (final String key : keys) {
            value = remove(key);
            if (null != value) {
                break;
            }
        }
        return (String) value;
    }

    /**
     * 转换为标准的{@link java.util.Properties}对象
     *
     * @return {@link java.util.Properties}对象
     */
    public java.util.Properties toProperties() {
        final java.util.Properties properties = new java.util.Properties();
        properties.putAll(this);
        return properties;
    }

    /**
     * 将配置文件转换为Bean，支持嵌套Bean
     * 支持的表达式：
     *
     * <pre>
     * persion
     * persion.name
     * persons[3]
     * person.friends[5].name
     * ['person']['friends'][5]['name']
     * </pre>
     *
     * @param <T>       Bean类型
     * @param beanClass Bean类
     * @return Bean对象
     */
    public <T> T toBean(final Class<T> beanClass) {
        return toBean(beanClass, null);
    }

    /**
     * 将配置文件转换为Bean，支持嵌套Bean
     * 支持的表达式：
     *
     * <pre>
     * persion
     * persion.name
     * persons[3]
     * person.friends[5].name
     * ['person']['friends'][5]['name']
     * </pre>
     *
     * @param <T>       Bean类型
     * @param beanClass Bean类
     * @param prefix    公共前缀，不指定前缀传null，当指定前缀后非此前缀的属性被忽略
     * @return Bean对象
     */
    public <T> T toBean(final Class<T> beanClass, final String prefix) {
        final T bean = ReflectKit.newInstanceIfPossible(beanClass);
        return fillBean(bean, prefix);
    }

    /**
     * 将配置文件转换为Bean，支持嵌套Bean
     * 支持的表达式：
     *
     * <pre>
     * persion
     * persion.name
     * persons[3]
     * person.friends[5].name
     * ['person']['friends'][5]['name']
     * </pre>
     *
     * @param <T>    Bean类型
     * @param bean   Bean对象
     * @param prefix 公共前缀，不指定前缀传null，当指定前缀后非此前缀的属性被忽略
     * @return Bean对象
     */
    public <T> T fillBean(final T bean, String prefix) {
        prefix = StringKit.emptyIfNull(StringKit.addSuffixIfNot(prefix, Symbol.DOT));

        String key;
        for (final java.util.Map.Entry<Object, Object> entry : this.entrySet()) {
            key = (String) entry.getKey();
            if (false == StringKit.startWith(key, prefix)) {
                // 非指定开头的属性忽略掉
                continue;
            }
            try {
                BeanKit.setProperty(bean, StringKit.subSuf(key, prefix.length()), entry.getValue());
            } catch (final Exception e) {
                // 忽略注入失败的字段（这些字段可能用于其它配置）
                Logger.debug("Ignore property: [{}]", key);
            }
        }

        return bean;
    }

    /**
     * 设置值,无给定键创建之 设置后未持久化
     *
     * @param key   属性键
     * @param value 属性值
     */
    public void set(final String key, final Object value) {
        super.setProperty(key, value.toString());
    }

    /**
     * 通过lambda批量设置值
     * 实际使用时，可以使用getXXX的方法引用来完成键值对的赋值：
     * <pre>
     *     User user = GenericBuilder.of(User::new).with(User::setUsername, "bus").build();
     *     Setting.of().setFields(user::getNickname, user::getUsername);
     * </pre>
     *
     * @param fields lambda,不能为空
     * @return this
     */
    public Properties setFields(final XSupplier<?>... fields) {
        Arrays.stream(fields).forEach(f -> set(LambdaKit.getFieldName(f), f.get()));
        return this;
    }

    /**
     * 持久化当前设置，会覆盖掉之前的设置
     *
     * @param absolutePath 设置文件的绝对路径
     * @throws InternalException IO异常，可能为文件未找到
     */
    public void store(final String absolutePath) throws InternalException {
        Writer writer = null;
        try {
            writer = FileKit.getWriter(absolutePath, charset, false);
            super.store(writer, null);
        } catch (final IOException e) {
            throw new InternalException(e, "Store properties to [{}] error!", absolutePath);
        } finally {
            IoKit.close(writer);
        }
    }

    /**
     * 存储当前设置，会覆盖掉以前的设置
     *
     * @param path  相对路径
     * @param clazz 相对的类
     */
    public void store(final String path, final Class<?> clazz) {
        this.store(FileKit.getAbsolutePath(path, clazz));
    }

}
