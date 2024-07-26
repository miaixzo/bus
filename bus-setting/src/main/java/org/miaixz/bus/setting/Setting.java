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
package org.miaixz.bus.setting;

import org.miaixz.bus.core.center.function.SupplierX;
import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.io.resource.Resource;
import org.miaixz.bus.core.io.watch.SimpleWatcher;
import org.miaixz.bus.core.io.watch.WatchMonitor;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.*;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.setting.magic.AbstractSetting;
import org.miaixz.bus.setting.magic.GroupedMap;
import org.miaixz.bus.setting.metric.props.Props;

import java.io.File;
import java.net.URL;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.*;
import java.util.function.Consumer;

/**
 * 设置工具类。 用于支持设置（配置）文件 用于替换Properties类，提供功能更加强大的配置文件，同时对Properties文件向下兼容
 *
 * <pre>
 *  1、支持变量，默认变量命名为 ${变量名}，变量只能识别读入行的变量，例如第6行的变量在第三行无法读取
 *  2、支持分组，分组为中括号括起来的内容，中括号以下的行都为此分组的内容，无分组相当于空字符分组，若某个key是name，加上分组后的键相当于group.name
 *  3、注释以#开头，但是空行和不带“=”的行也会被跳过，但是建议加#
 *  4、store方法不会保存注释内容，慎重使用
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Setting extends AbstractSetting implements Map<String, String> {

    /**
     * 默认字符集
     */
    public static final java.nio.charset.Charset DEFAULT_CHARSET = Charset.UTF_8;
    /**
     * 默认配置文件扩展名
     */
    public static final String EXT_NAME = "setting";
    private static final long serialVersionUID = -1L;
    /**
     * 附带分组的键值对存储
     */
    private final GroupedMap groupedMap = new GroupedMap();
    /**
     * 本设置对象的字符集
     */
    protected java.nio.charset.Charset charset;
    /**
     * 是否使用变量
     */
    protected boolean isUseVariable;
    /**
     * 设定文件的资源
     */
    protected Resource resource;
    /**
     * 当获取key对应值为{@code null}时是否打印debug日志提示用户，默认{@code false}
     */
    private boolean logIfNull;
    private Loader settingLoader;
    private WatchMonitor watchMonitor;

    /**
     * 空构造
     */
    public Setting() {
        this.charset = DEFAULT_CHARSET;
    }

    /**
     * 构造
     *
     * @param path 相对路径或绝对路径
     */
    public Setting(final String path) {
        this(path, false);
    }

    /**
     * 构造
     *
     * @param path          相对路径或绝对路径
     * @param isUseVariable 是否使用变量
     */
    public Setting(final String path, final boolean isUseVariable) {
        this(path, DEFAULT_CHARSET, isUseVariable);
    }

    /**
     * 构造，使用相对于Class文件根目录的相对路径
     *
     * @param path          相对路径或绝对路径
     * @param charset       字符集
     * @param isUseVariable 是否使用变量
     */
    public Setting(final String path, final java.nio.charset.Charset charset, final boolean isUseVariable) {
        Assert.notBlank(path, "Blank setting path !");
        this.init(ResourceKit.getResource(path), charset, isUseVariable);
    }

    /**
     * 构造
     *
     * @param configFile    配置文件对象
     * @param charset       字符集
     * @param isUseVariable 是否使用变量
     */
    public Setting(final File configFile, final java.nio.charset.Charset charset, final boolean isUseVariable) {
        Assert.notNull(configFile, "Null setting file define!");
        this.init(ResourceKit.getResource(configFile), charset, isUseVariable);
    }

    /**
     * 构造
     *
     * @param resource      Setting的Resource
     * @param charset       字符集
     * @param isUseVariable 是否使用变量
     */
    public Setting(final Resource resource, final java.nio.charset.Charset charset, final boolean isUseVariable) {
        this.init(resource, charset, isUseVariable);
    }

    /**
     * 构建一个空的Setting，用于手动加入参数
     *
     * @return Setting
     */
    public static Setting of() {
        return new Setting();
    }

    /**
     * 初始化设定文件
     *
     * @param resource      {@link Resource}
     * @param charset       字符集
     * @param isUseVariable 是否使用变量
     * @return 成功初始化与否
     */
    public boolean init(final Resource resource, final java.nio.charset.Charset charset, final boolean isUseVariable) {
        Assert.notNull(resource, "Setting resource must be not null!");
        this.resource = resource;
        this.charset = charset;
        this.isUseVariable = isUseVariable;

        return load();
    }

    /**
     * 重新加载配置文件
     *
     * @return 是否加载成功
     */
    synchronized public boolean load() {
        if (null == this.settingLoader) {
            settingLoader = new Loader(this.groupedMap, this.charset, this.isUseVariable);
        }
        return settingLoader.load(this.resource);
    }

    /**
     * 在配置文件变更时自动加载
     *
     * @param autoReload 是否自动加载
     */
    public void autoLoad(final boolean autoReload) {
        autoLoad(autoReload, null);
    }

    /**
     * 在配置文件变更时自动加载
     *
     * @param callback   加载完成回调
     * @param autoReload 是否自动加载
     */
    public void autoLoad(final boolean autoReload, final Consumer<Boolean> callback) {
        if (autoReload) {
            Assert.notNull(this.resource, "Setting resource must be not null !");
            // 先关闭之前的监听
            IoKit.closeQuietly(this.watchMonitor);
            this.watchMonitor = WatchKit.ofModify(resource.getUrl(), new SimpleWatcher() {

                private static final long serialVersionUID = -1L;

                @Override
                public void onModify(final WatchEvent<?> event, final WatchKey key) {
                    final boolean success = load();
                    // 如果有回调，加载完毕则执行回调
                    if (callback != null) {
                        callback.accept(success);
                    }
                }
            });
            this.watchMonitor.start();
            Logger.debug("Auto load for [{}] listenning...", this.resource.getUrl());
        } else {
            IoKit.closeQuietly(this.watchMonitor);
            this.watchMonitor = null;
        }
    }

    /**
     * 获得设定文件的URL
     *
     * @return 获得设定文件的路径
     */
    public URL getSettingUrl() {
        return (null == this.resource) ? null : this.resource.getUrl();
    }

    /**
     * 获得设定文件的路径
     *
     * @return 获得设定文件的路径
     */
    public String getSettingPath() {
        final URL settingUrl = getSettingUrl();
        return (null == settingUrl) ? null : settingUrl.getPath();
    }

    /**
     * 键值总数
     *
     * @return 键值总数
     */
    @Override
    public int size() {
        return this.groupedMap.size();
    }

    @Override
    public Object getObjByGroup(final CharSequence key, final CharSequence group, final Object defaultValue) {
        final String result = this.groupedMap.get(group, key);
        if (result == null && logIfNull) {
            Logger.debug("No data [{}] in group [{}] !", key, group);
        }
        return result;
    }

    /**
     * 获取并删除键值对，当指定键对应值非空时，返回并删除这个值，后边的键对应的值不再查找
     *
     * @param keys 键列表，常用于别名
     * @return 字符串值
     */
    public String getAndRemove(final String... keys) {
        String value = null;
        for (final String key : keys) {
            value = remove(key);
            if (null != value) {
                break;
            }
        }
        return value;
    }

    /**
     * 获得指定分组的所有键值对，此方法获取的是原始键值对，获取的键值对可以被修改
     *
     * @param group 分组
     * @return map
     */
    public Map<String, String> getMap(final String group) {
        final LinkedHashMap<String, String> map = this.groupedMap.get(group);
        return (null != map) ? map : new LinkedHashMap<>(0);
    }

    /**
     * 获取group分组下所有配置键值对，组成新的Setting
     *
     * @param group 分组
     * @return Setting
     */
    public Setting getSetting(final String group) {
        final Setting setting = new Setting();
        setting.putAll(this.getMap(group));
        return setting;
    }

    /**
     * 获取group分组下所有配置键值对，组成新的{@link java.util.Properties}
     *
     * @param group 分组
     * @return Properties对象
     */
    public java.util.Properties getProperties(final String group) {
        final java.util.Properties properties = new java.util.Properties();
        properties.putAll(getMap(group));
        return properties;
    }

    /**
     * 获取group分组下所有配置键值对，组成新的{@link Props}
     *
     * @param group 分组
     * @return Props对象
     */
    public Props getProps(final String group) {
        final Props props = new Props();
        props.putAll(getMap(group));
        return props;
    }

    /**
     * 持久化当前设置，会覆盖掉之前的设置 持久化不会保留之前的分组，注意如果配置文件在jar内部或者在exe中，此方法会报错。
     */
    public void store() {
        final URL resourceUrl = getSettingUrl();
        Assert.notNull(resourceUrl, "Setting path must be not null !");
        store(FileKit.file(resourceUrl));
    }

    /**
     * 持久化当前设置，会覆盖掉之前的设置 持久化不会保留之前的分组
     *
     * @param absolutePath 设置文件的绝对路径
     */
    public void store(final String absolutePath) {
        store(FileKit.touch(absolutePath));
    }

    /**
     * 持久化当前设置，会覆盖掉之前的设置 持久化不会保留之前的分组
     *
     * @param file 设置文件
     */
    public void store(final File file) {
        if (null == this.settingLoader) {
            settingLoader = new Loader(this.groupedMap, this.charset, this.isUseVariable);
        }
        settingLoader.store(file);
    }

    /**
     * 转换为{@link Props}对象，原分组变为前缀
     *
     * @return {@link Props}对象
     */
    public Props toProps() {
        final Props props = new Props();
        String group;
        for (final Entry<String, LinkedHashMap<String, String>> groupEntry : this.groupedMap.entrySet()) {
            group = groupEntry.getKey();
            for (final Entry<String, String> entry : groupEntry.getValue().entrySet()) {
                // 忽略null的键值对
                final String key = entry.getKey();
                final String value = entry.getValue();
                if (null != key && null != value) {
                    props.setProperty(StringKit.isEmpty(group) ? key : group + Symbol.C_DOT + key, value);
                }
            }
        }
        return props;
    }

    /**
     * 获取GroupedMap
     *
     * @return GroupedMap
     */
    public GroupedMap getGroupedMap() {
        return this.groupedMap;
    }

    /**
     * 获取所有分组
     *
     * @return 获得所有分组名
     */
    public List<String> getGroups() {
        return ListKit.of(this.groupedMap.keySet());
    }

    /**
     * 设置变量的正则 正则只能有一个group表示变量本身，剩余为字符 例如 \$\{(name)\}表示${name}变量名为name的一个变量表示
     *
     * @param regex 正则
     * @return this
     */
    public Setting setVarRegex(final String regex) {
        if (null == this.settingLoader) {
            throw new NullPointerException("Loader is null !");
        }
        this.settingLoader.setVarRegex(regex);
        return this;
    }

    /**
     * 自定义字符编码
     *
     * @param charset 字符编码
     * @return this
     */
    public Setting setCharset(final java.nio.charset.Charset charset) {
        this.charset = charset;
        return this;
    }

    /**
     * 设置当获取key对应值为{@code null}时是否打印debug日志提示用户
     *
     * @param logIfNull 当获取key对应值为{@code null}时是否打印debug日志提示用户
     * @return this
     */
    public Setting setLogIfNull(final boolean logIfNull) {
        this.logIfNull = logIfNull;
        return this;
    }

    /**
     * 某个分组对应的键值对是否为空
     *
     * @param group 分组
     * @return 是否为空
     */
    public boolean isEmpty(final String group) {
        return this.groupedMap.isEmpty(group);
    }

    /**
     * 指定分组中是否包含指定key
     *
     * @param group 分组
     * @param key   键
     * @return 是否包含key
     */
    public boolean containsKey(final String group, final String key) {
        return this.groupedMap.containsKey(group, key);
    }

    /**
     * 指定分组中是否包含指定值
     *
     * @param group 分组
     * @param value 值
     * @return 是否包含值
     */
    public boolean containsValue(final String group, final String value) {
        return this.groupedMap.containsValue(group, value);
    }

    /**
     * 将键值对加入到对应分组中
     *
     * @param key   键
     * @param group 分组
     * @param value 值
     * @return 此key之前存在的值，如果没有返回null
     */
    public String putByGroup(final String key, final String group, final String value) {
        return this.groupedMap.put(group, key, value);
    }

    /**
     * 从指定分组中删除指定值
     *
     * @param group 分组
     * @param key   键
     * @return 被删除的值，如果值不存在，返回null
     */
    public String remove(final String group, final Object key) {
        return this.groupedMap.remove(group, Convert.toString(key));
    }

    /**
     * 加入多个键值对到某个分组下
     *
     * @param group 分组
     * @param m     键值对
     * @return this
     */
    public Setting putAll(final String group, final Map<? extends String, ? extends String> m) {
        this.groupedMap.putAll(group, m);
        return this;
    }

    /**
     * 添加一个Stting到主配置中
     *
     * @param setting Setting配置
     * @return this
     */
    public Setting addSetting(final Setting setting) {
        for (final Entry<String, LinkedHashMap<String, String>> e : setting.getGroupedMap().entrySet()) {
            this.putAll(e.getKey(), e.getValue());
        }
        return this;
    }

    /**
     * 清除指定分组下的所有键值对
     *
     * @param group 分组
     * @return this
     */
    public Setting clear(final String group) {
        this.groupedMap.clear(group);
        return this;
    }

    /**
     * 指定分组所有键的Set
     *
     * @param group 分组
     * @return 键Set
     */
    public Set<String> keySet(final String group) {
        return this.groupedMap.keySet(group);
    }

    /**
     * 指定分组下所有值
     *
     * @param group 分组
     * @return 值
     */
    public Collection<String> values(final String group) {
        return this.groupedMap.values(group);
    }

    /**
     * 指定分组下所有键值对
     *
     * @param group 分组
     * @return 键值对
     */
    public Set<Entry<String, String>> entrySet(final String group) {
        return this.groupedMap.entrySet(group);
    }

    /**
     * 设置值
     *
     * @param key   键
     * @param value 值
     * @return this
     */
    public Setting set(final String key, final String value) {
        this.put(key, value);
        return this;
    }

    /**
     * 通过lambda批量设置值 实际使用时，可以使用getXXX的方法引用来完成键值对的赋值：
     * 
     * <pre>
     * User user = GenericBuilder.of(User::new).with(User::setUsername, "bus").build();
     * Setting.of().setFields(user::getNickname, user::getUsername);
     * </pre>
     *
     * @param fields lambda,不能为空
     * @return this
     */
    public Setting setFields(final SupplierX<String>... fields) {
        Arrays.stream(fields).forEach(f -> set(LambdaKit.getFieldName(f), f.get()));
        return this;
    }

    /**
     * 将键值对加入到对应分组中 此方法用于与getXXX统一参数顺序
     *
     * @param key   键
     * @param group 分组
     * @param value 值
     * @return 此key之前存在的值，如果没有返回null
     */
    public Setting setByGroup(final String key, final String group, final String value) {
        this.putByGroup(key, group, value);
        return this;
    }

    @Override
    public boolean isEmpty() {
        return this.groupedMap.isEmpty();
    }

    /**
     * 默认分组（空分组）中是否包含指定key对应的值
     *
     * @param key 键
     * @return 默认分组中是否包含指定key对应的值
     */
    @Override
    public boolean containsKey(final Object key) {
        return this.groupedMap.containsKey(DEFAULT_GROUP, Convert.toString(key));
    }

    /**
     * 默认分组（空分组）中是否包含指定值
     *
     * @param value 值
     * @return 默认分组中是否包含指定值
     */
    @Override
    public boolean containsValue(final Object value) {
        return this.groupedMap.containsValue(DEFAULT_GROUP, Convert.toString(value));
    }

    /**
     * 获取默认分组（空分组）中指定key对应的值
     *
     * @param key 键
     * @return 默认分组（空分组）中指定key对应的值
     */
    @Override
    public String get(final Object key) {
        return getString((String) key);
    }

    /**
     * 将指定键值对加入到默认分组（空分组）中
     *
     * @param key   键
     * @param value 值
     * @return 加入的值
     */
    @Override
    public String put(final String key, final String value) {
        return this.groupedMap.put(DEFAULT_GROUP, key, value);
    }

    /**
     * 移除默认分组（空分组）中指定值
     *
     * @param key 键
     * @return 移除的值
     */
    @Override
    public String remove(final Object key) {
        return remove(DEFAULT_GROUP, key);
    }

    /**
     * 将键值对Map加入默认分组（空分组）中
     *
     * @param m Map
     */
    @Override
    public void putAll(final Map<? extends String, ? extends String> m) {
        this.groupedMap.putAll(DEFAULT_GROUP, m);
    }

    /**
     * 清空默认分组（空分组）中的所有键值对
     */
    @Override
    public void clear() {
        this.groupedMap.clear(DEFAULT_GROUP);
    }

    /**
     * 获取默认分组（空分组）中的所有键列表
     *
     * @return 默认分组（空分组）中的所有键列表
     */
    @Override
    public Set<String> keySet() {
        return this.groupedMap.keySet(DEFAULT_GROUP);
    }

    /**
     * 获取默认分组（空分组）中的所有值列表
     *
     * @return 默认分组（空分组）中的所有值列表
     */
    @Override
    public Collection<String> values() {
        return this.groupedMap.values(DEFAULT_GROUP);
    }

    /**
     * 获取默认分组（空分组）中的所有键值对列表
     *
     * @return 默认分组（空分组）中的所有键值对列表
     */
    @Override
    public Set<Entry<String, String>> entrySet() {
        return this.groupedMap.entrySet(DEFAULT_GROUP);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((charset == null) ? 0 : charset.hashCode());
        result = prime * result + groupedMap.hashCode();
        result = prime * result + (isUseVariable ? 1231 : 1237);
        result = prime * result + ((this.resource == null) ? 0 : this.resource.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Setting other = (Setting) obj;
        if (charset == null) {
            if (other.charset != null) {
                return false;
            }
        } else if (!charset.equals(other.charset)) {
            return false;
        }
        if (!groupedMap.equals(other.groupedMap)) {
            return false;
        }
        if (isUseVariable != other.isUseVariable) {
            return false;
        }
        if (this.resource == null) {
            return other.resource == null;
        } else {
            return resource.equals(other.resource);
        }
    }

    @Override
    public String toString() {
        return groupedMap.toString();
    }

}
