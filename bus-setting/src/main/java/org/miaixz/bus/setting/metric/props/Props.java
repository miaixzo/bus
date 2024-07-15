/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.setting.metric.props;

import org.miaixz.bus.core.center.function.FunctionX;
import org.miaixz.bus.core.center.function.LambdaX;
import org.miaixz.bus.core.center.function.SupplierX;
import org.miaixz.bus.core.center.map.concurrent.SafeConcurrentHashMap;
import org.miaixz.bus.core.io.file.FileName;
import org.miaixz.bus.core.io.resource.Resource;
import org.miaixz.bus.core.io.watch.SimpleWatcher;
import org.miaixz.bus.core.io.watch.WatchMonitor;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.getter.TypeGetter;
import org.miaixz.bus.core.xyz.*;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.setting.Setting;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.Arrays;
import java.util.Map;

/**
 * Properties文件读取封装类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class Props extends java.util.Properties implements TypeGetter<CharSequence> {

    /**
     * 默认配置文件扩展名
     */
    public static final String EXT_NAME = "properties";

    private static final long serialVersionUID = -1L;
    /**
     * 配置文件缓存
     */
    private static final Map<String, Props> CACHE_PROPS = new SafeConcurrentHashMap<>();
    /**
     * 属性文件的Resource
     */
    private Resource resource;

    private WatchMonitor watchMonitor;
    /**
     * properties文件编码
     * 此属性不能被序列化，故忽略序列化
     */
    private transient java.nio.charset.Charset charset = Charset.ISO_8859_1;

    /**
     * 构造
     */
    public Props() {

    }

    /**
     * 构造，使用相对于Class文件根目录的相对路径
     *
     * @param path 配置文件路径，相对于ClassPath，或者使用绝对路径
     */
    public Props(final String path) {
        this(path, null);
    }

    /**
     * 构造，使用相对于Class文件根目录的相对路径
     *
     * @param path    相对或绝对路径
     * @param charset 自定义编码
     */
    public Props(final String path, final java.nio.charset.Charset charset) {
        Assert.notBlank(path, "Blank properties file path !");
        if (null != charset) {
            this.charset = charset;
        }
        this.load(ResourceKit.getResource(path));
    }

    /**
     * 构造
     *
     * @param propertiesFile 配置文件对象
     */
    public Props(final File propertiesFile) {
        this(propertiesFile, null);
    }

    /**
     * 构造
     *
     * @param propertiesFile 配置文件对象
     * @param charset        自定义编码
     */
    public Props(final File propertiesFile, final java.nio.charset.Charset charset) {
        Assert.notNull(propertiesFile, "Null properties file!");
        if (null != charset) {
            this.charset = charset;
        }
        this.load(ResourceKit.getResource(propertiesFile));
    }

    /**
     * 构造，使用URL读取
     *
     * @param resource {@link Resource}
     * @param charset  自定义编码
     */
    public Props(final Resource resource, final java.nio.charset.Charset charset) {
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
    public Props(final java.util.Properties properties) {
        if (MapKit.isNotEmpty(properties)) {
            this.putAll(properties);
        }
    }

    /**
     * 构建一个空的Props，用于手动加入参数
     *
     * @return Setting
     */
    public static Props of() {
        return new Props();
    }

    /**
     * 获得Classpath下的Properties文件
     *
     * @param resource 资源（相对Classpath的路径）
     * @return Properties
     */
    public static Props of(final String resource) {
        return new Props(resource);
    }

    /**
     * 获得Classpath下的Properties文件
     *
     * @param resource 资源（相对Classpath的路径）
     * @param charset  自定义编码
     * @return Properties
     */
    public static Props of(final String resource, final java.nio.charset.Charset charset) {
        return new Props(resource, charset);
    }

    /**
     * {@link Props}转为Props
     *
     * @param properties {@link Props}
     * @return Properties
     */
    public static Props of(final Props properties) {
        return new Props(properties);
    }

    /**
     * 获取当前环境下的配置文件
     * name可以为不包括扩展名的文件名（默认.properties），也可以是文件名全称
     *
     * @param name 文件名，如果没有扩展名，默认为.properties
     * @return 当前环境下配置文件
     */
    public static Props get(final String name) {
        return CACHE_PROPS.computeIfAbsent(name, (filePath) -> {
            final String extName = FileName.extName(filePath);
            if (StringKit.isEmpty(extName)) {
                filePath = filePath + "." + Setting.EXT_NAME;
            }
            return new Props(filePath);
        });
    }

    /**
     * 解析PROPS
     *
     * @param result  数据结果
     * @param content 数据内容
     */
    public static void parse(Map<String, Object> result, String content) {
        String[] lines = content.split("\n");
        for (String line : lines) {
            if (StringKit.isBlank(line)
                    || line.startsWith(Symbol.SHAPE)
                    || line.indexOf(Symbol.EQUAL) < 0) {
                continue;
            }
            // 考虑 value包含=的情况
            String key = line.substring(0, line.indexOf(Symbol.EQUAL)).trim();
            String value = line.substring(line.indexOf(Symbol.EQUAL) + 1).trim();
            if (StringKit.isNotBlank(value)) {
                result.put(key, value);
            }
        }
    }

    /**
     * 获取给定路径找到的第一个配置文件
     * * name可以为不包括扩展名的文件名（默认.properties为结尾），也可以是文件名全称
     *
     * @param names 文件名，如果没有扩展名，默认为.properties
     * @return 当前环境下配置文件
     */
    public static Props getFirstFound(final String... names) {
        for (final String name : names) {
            try {
                return get(name);
            } catch (final InternalException e) {
                //ignore
            }
        }
        return null;
    }

    /**
     * 获取系统参数，例如用户在执行java命令时定义的 -Duse=bus
     *
     * @return 系统参数Props
     */
    public static Props getProperties() {
        return new Props(System.getProperties());
    }

    /**
     * 初始化配置文件
     *
     * @param url {@link URL}
     */
    public void load(final URL url) {
        load(ResourceKit.getResource(url));
    }

    /**
     * 初始化配置文件
     *
     * @param resource {@link Resource}
     */
    public void load(final Resource resource) {
        Assert.notNull(resource, "Properties resource must be not null!");
        this.resource = resource;
        ResourceKit.loadTo(this, resource, this.charset);
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
            // 先关闭之前的监听
            IoKit.closeQuietly(this.watchMonitor);
            this.watchMonitor = WatchKit.ofModify(this.resource.getUrl(), new SimpleWatcher() {
                private static final long serialVersionUID = -1L;

                @Override
                public void onModify(final WatchEvent<?> event, final WatchKey key) {
                    load();
                }
            });
            this.watchMonitor.start();
        } else {
            IoKit.closeQuietly(this.watchMonitor);
            this.watchMonitor = null;
        }
    }

    @Override
    public Object getObject(final CharSequence key, final Object defaultValue) {
        Assert.notNull(key, "Key must be not null!");
        return ObjectKit.defaultIfNull(getProperty(key.toString()), defaultValue);
    }

    /**
     * 根据lambda的方法引用，获取
     *
     * @param func 方法引用
     * @param <P>  参数类型
     * @param <T>  返回值类型
     * @return 获取表达式对应属性和返回的对象
     */
    public <P, T> T get(final FunctionX<P, T> func) {
        final LambdaX lambdaX = LambdaKit.resolve(func);
        return get(lambdaX.getFieldName(), lambdaX.getReturnType());
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
     * 获取一个新的子属性，子属性键值对拥有公共前缀，以.分隔。
     * <pre>
     *     a.b
     *     a.c
     *     b.a
     * </pre>
     * 则调用getSubProps("a");得到
     * <pre>
     *     a.b
     *     a.c
     * </pre>
     *
     * @param prefix 前缀，可以不以.结尾
     * @return 子属性
     */
    public Props getSubProps(final String prefix) {
        final Props subProps = new Props();
        final String finalPrefix = StringKit.addSuffixIfNot(prefix, Symbol.DOT);
        final int prefixLength = finalPrefix.length();

        forEach((key, value) -> {
            final String keyStr = key.toString();
            if (StringKit.startWith(keyStr, finalPrefix)) {
                subProps.set(StringKit.subSuf(keyStr, prefixLength), value);
            }
        });

        return subProps;
    }

    /**
     * 转换为标准的{@link Props}对象
     *
     * @return {@link Props}对象
     */
    public Props toProperties() {
        final Props properties = new Props();
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
        return toBean(bean, prefix);
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
     * @param <T>  Bean类型
     * @param bean Bean对象
     * @return Bean对象
     */
    public <T> T toBean(final T bean) {
        return toBean(bean, null);
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
    public <T> T toBean(final T bean, String prefix) {
        prefix = StringKit.toStringOrEmpty(StringKit.addSuffixIfNot(prefix, Symbol.DOT));

        String key;
        for (final java.util.Map.Entry<Object, Object> entry : this.entrySet()) {
            key = (String) entry.getKey();
            if (!StringKit.startWith(key, prefix)) {
                // 非指定开头的属性忽略掉
                continue;
            }
            try {
                BeanKit.setProperty(bean, StringKit.subSuf(key, prefix.length()), entry.getValue());
            } catch (final Exception e) {
                // 忽略注入失败的字段（这些字段可能用于其它配置）
                Logger.debug("Ignore property: [{}],because of: {}", key, e);
            }
        }

        return bean;
    }

    /**
     * 设置值，无给定键创建之。设置后未持久化
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
    public Props setFields(final SupplierX<?>... fields) {
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
            IoKit.closeQuietly(writer);
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
