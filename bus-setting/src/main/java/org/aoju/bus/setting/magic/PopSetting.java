/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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

import org.aoju.bus.core.convert.Convert;
import org.aoju.bus.core.io.resource.ClassPathResource;
import org.aoju.bus.core.io.resource.FileResource;
import org.aoju.bus.core.io.resource.Resource;
import org.aoju.bus.core.io.resource.UriResource;
import org.aoju.bus.core.io.watchers.SimpleWatcher;
import org.aoju.bus.core.io.watchers.WatchMonitor;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.*;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.setting.Readers;
import org.aoju.bus.setting.metric.GroupMap;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.*;
import java.util.function.Consumer;

/**
 * 设置工具类  用于支持设置(配置)文件
 * 用于替换Properties类,提供功能更加强大的配置文件,同时对Properties文件向下兼容
 *
 * <pre>
 *  1、支持变量,默认变量命名为 ${变量名},变量只能识别读入行的变量,例如第6行的变量在第三行无法读取
 *  2、支持分组,分组为中括号括起来的内容,中括号以下的行都为此分组的内容,无分组相当于空字符分组,若某个key是name,加上分组后的键相当于group.name
 *  3、注释以#开头,但是空行和不带“=”的行也会被跳过,但是建议加#
 *  4、store方法不会保存注释内容,慎重使用
 * </pre>
 *
 * @author Kimi Liu
 * @version 6.2.3
 * @since JDK 1.8+
 */
public class PopSetting extends AbstractSetting implements Map<String, String> {

    /**
     * 附带分组的键值对存储
     */
    private final GroupMap groupMap = new GroupMap();

    /**
     * 本设置对象的字符集
     */
    protected java.nio.charset.Charset charset;
    /**
     * 是否使用变量
     */
    protected boolean isUseVariable;
    /**
     * 设定文件的URL
     */
    protected URL settingUrl;

    private Readers readers;
    private WatchMonitor watchMonitor;

    /**
     * 空构造
     */
    public PopSetting() {
    }

    /**
     * 构造
     *
     * @param path 相对路径或绝对路径
     */
    public PopSetting(String path) {
        this(path, false);
    }

    /**
     * 构造
     *
     * @param path          相对路径或绝对路径
     * @param isUseVariable 是否使用变量
     */
    public PopSetting(String path, boolean isUseVariable) {
        this(path, Charset.UTF_8, isUseVariable);
    }

    /**
     * 构造,使用相对于Class文件根目录的相对路径
     *
     * @param path          相对路径或绝对路径
     * @param charset       字符集
     * @param isUseVariable 是否使用变量
     */
    public PopSetting(String path, java.nio.charset.Charset charset, boolean isUseVariable) {
        Assert.notBlank(path, "Blank setting path !");
        this.init(FileKit.getResourceObj(path), charset, isUseVariable);
    }

    /**
     * 构造
     *
     * @param configFile    配置文件对象
     * @param charset       字符集
     * @param isUseVariable 是否使用变量
     */
    public PopSetting(File configFile, java.nio.charset.Charset charset, boolean isUseVariable) {
        Assert.notNull(configFile, "Null setting file define!");
        this.init(new FileResource(configFile), charset, isUseVariable);
    }

    /**
     * 构造,相对于classes读取文件
     *
     * @param path          相对ClassPath路径或绝对路径
     * @param clazz         基准类
     * @param charset       字符集
     * @param isUseVariable 是否使用变量
     */
    public PopSetting(String path, Class<?> clazz, java.nio.charset.Charset charset, boolean isUseVariable) {
        Assert.notBlank(path, "Blank setting path !");
        this.init(new ClassPathResource(path, clazz), charset, isUseVariable);
    }

    /**
     * 构造
     *
     * @param url           设定文件的URL
     * @param charset       字符集
     * @param isUseVariable 是否使用变量
     */
    public PopSetting(URL url, java.nio.charset.Charset charset, boolean isUseVariable) {
        Assert.notNull(url, "Null setting url define!");
        this.init(new UriResource(url), charset, isUseVariable);
    }

    /**
     * 初始化设定文件
     *
     * @param resource      {@link Resource}
     * @param charset       字符集
     * @param isUseVariable 是否使用变量
     * @return 成功初始化与否
     */
    public boolean init(Resource resource, java.nio.charset.Charset charset, boolean isUseVariable) {
        if (null == resource) {
            throw new NullPointerException("Null setting url define!");
        }
        this.settingUrl = resource.getUrl();
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
        if (null == this.readers) {
            readers = new Readers(this.groupMap, this.charset, this.isUseVariable);
        }
        return readers.load(new UriResource(this.settingUrl));
    }

    /**
     * 在配置文件变更时自动加载
     *
     * @param autoReload 是否自动加载
     */
    public void autoLoad(boolean autoReload) {
        autoLoad(autoReload, null);
    }

    /**
     * 在配置文件变更时自动加载
     *
     * @param callback   加载完成回调
     * @param autoReload 是否自动加载
     */
    public void autoLoad(boolean autoReload, Consumer<Boolean> callback) {
        if (autoReload) {
            Assert.notNull(this.settingUrl, "Setting URL is null !");
            if (null != this.watchMonitor) {
                // 先关闭之前的监听
                this.watchMonitor.close();
            }
            this.watchMonitor = WatchKit.createModify(this.settingUrl, new SimpleWatcher() {
                @Override
                public void onModify(WatchEvent<?> event, Path currentPath) {
                    boolean success = load();
                    // 如果有回调，加载完毕则执行回调
                    if (null != callback) {
                        callback.accept(success);
                    }
                }
            });
            this.watchMonitor.start();
            Logger.debug("Auto load for [{}] listenning...", this.settingUrl);
        } else {
            IoKit.close(this.watchMonitor);
            this.watchMonitor = null;
        }
    }

    /**
     * 获得设定文件的URL
     *
     * @return 获得设定文件的路径
     */
    public URL getSettingUrl() {
        return this.settingUrl;
    }

    /**
     * @return 获得设定文件的路径
     */
    public String getSettingPath() {
        return (null == this.settingUrl) ? null : this.settingUrl.getPath();
    }

    /**
     * 键值总数
     *
     * @return 键值总数
     */
    public int size() {
        return this.groupMap.size();
    }

    @Override
    public String getByGroup(String key, String group) {
        return this.groupMap.get(group, key);
    }

    /**
     * 获取并删除键值对,当指定键对应值非空时,返回并删除这个值,后边的键对应的值不再查找
     *
     * @param keys 键列表,常用于别名
     * @return 值
     */
    public Object getAndRemove(String... keys) {
        Object value = null;
        for (String key : keys) {
            value = remove(key);
            if (null != value) {
                break;
            }
        }
        return value;
    }

    /**
     * 获取并删除键值对,当指定键对应值非空时,返回并删除这个值,后边的键对应的值不再查找
     *
     * @param keys 键列表,常用于别名
     * @return 字符串值
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
     * 获得指定分组的所有键值对,此方法获取的是原始键值对,获取的键值对可以被修改
     *
     * @param group 分组
     * @return map
     */
    public Map<String, String> getMap(String group) {
        return this.groupMap.get(group);
    }

    /**
     * 获得group对应的子Setting
     *
     * @param group 分组
     * @return {@link PopSetting}
     */
    public PopSetting getSetting(String group) {
        final PopSetting popSetting = new PopSetting();
        popSetting.putAll(this.getMap(group));
        return popSetting;
    }

    /**
     * 获得group对应的子Properties
     *
     * @param group 分组
     * @return Properties对象
     */
    public java.util.Properties getProperties(String group) {
        final java.util.Properties properties = new java.util.Properties();
        properties.putAll(getMap(group));
        return properties;
    }

    /**
     * 获得group对应的子Props
     *
     * @param group 分组
     * @return Props对象
     */
    public Properties getProps(String group) {
        final Properties properties = new Properties();
        properties.putAll(getMap(group));
        return properties;
    }

    /**
     * 持久化当前设置,会覆盖掉之前的设置
     * 持久化不会保留之前的分组，注意如果配置文件在jar内部或者在exe中，此方法会报错。
     */
    public void store() {
        Assert.notNull(this.settingUrl, "Setting path must be not null !");
        store(FileKit.file(this.settingUrl));
    }

    /**
     * 持久化当前设置,会覆盖掉之前的设置
     * 持久化不会保留之前的分组
     *
     * @param absolutePath 设置文件的绝对路径
     */
    public void store(String absolutePath) {
        store(FileKit.touch(absolutePath));
    }

    /**
     * 持久化当前设置，会覆盖掉之前的设置
     * 持久化不会保留之前的分组
     *
     * @param file 设置文件
     */
    public void store(File file) {
        if (null == this.readers) {
            readers = new Readers(this.groupMap, this.charset, this.isUseVariable);
        }
        readers.store(file);
    }

    /**
     * 转换为Properties对象,原分组变为前缀
     *
     * @return Properties对象
     */
    public java.util.Properties toProperties() {
        final java.util.Properties properties = new java.util.Properties();
        String group;
        for (Entry<String, LinkedHashMap<String, String>> groupEntry : this.groupMap.entrySet()) {
            group = groupEntry.getKey();
            for (Entry<String, String> entry : groupEntry.getValue().entrySet()) {
                properties.setProperty(StringKit.isEmpty(group) ? entry.getKey() : group + Symbol.DOT + entry.getKey(), entry.getValue());
            }
        }
        return properties;
    }

    /**
     * 获取GroupedMap
     *
     * @return GroupedMap
     */
    public GroupMap getGroupMap() {
        return this.groupMap;
    }

    /**
     * 获取所有分组
     *
     * @return 获得所有分组名
     */
    public List<String> getGroups() {
        return CollKit.newArrayList(this.groupMap.keySet());
    }

    /**
     * 设置变量的正则
     * 正则只能有一个group表示变量本身,剩余为字符 例如 \$\{(name)\}表示${name}变量名为name的一个变量表示
     *
     * @param regex 正则
     */
    public void setVarRegex(String regex) {
        if (null == this.readers) {
            throw new NullPointerException("SettingLoader is null !");
        }
        this.readers.setVarRegex(regex);
    }

    /**
     * 某个分组对应的键值对是否为空
     *
     * @param group 分组
     * @return 是否为空
     */
    public boolean isEmpty(String group) {
        return this.groupMap.isEmpty(group);
    }

    /**
     * 指定分组中是否包含指定key
     *
     * @param group 分组
     * @param key   键
     * @return 是否包含key
     */
    public boolean containsKey(String group, String key) {
        return this.groupMap.containsKey(group, key);
    }

    /**
     * 指定分组中是否包含指定值
     *
     * @param group 分组
     * @param value 值
     * @return 是否包含值
     */
    public boolean containsValue(String group, String value) {
        return this.groupMap.containsValue(group, value);
    }

    /**
     * 获取分组对应的值,如果分组不存在或者值不存在则返回null
     *
     * @param group 分组
     * @param key   键
     * @return 值, 如果分组不存在或者值不存在则返回null
     */
    public String get(String group, String key) {
        return this.groupMap.get(group, key);
    }

    /**
     * 将键值对加入到对应分组中
     *
     * @param group 分组
     * @param key   键
     * @param value 值
     * @return 此key之前存在的值, 如果没有返回null
     */
    public String put(String group, String key, String value) {
        return this.groupMap.put(group, key, value);
    }

    /**
     * 从指定分组中删除指定值
     *
     * @param group 分组
     * @param key   键
     * @return 被删除的值, 如果值不存在, 返回null
     */
    public String remove(String group, Object key) {
        return this.groupMap.remove(group, Convert.toString(key));
    }

    /**
     * 加入多个键值对到某个分组下
     *
     * @param group 分组
     * @param m     键值对
     * @return this
     */
    public PopSetting putAll(String group, Map<? extends String, ? extends String> m) {
        this.groupMap.putAll(group, m);
        return this;
    }

    /**
     * 清除指定分组下的所有键值对
     *
     * @param group 分组
     * @return this
     */
    public PopSetting clear(String group) {
        this.groupMap.clear(group);
        return this;
    }

    /**
     * 指定分组所有键的Set
     *
     * @param group 分组
     * @return 键Set
     */
    public Set<String> keySet(String group) {
        return this.groupMap.keySet(group);
    }

    /**
     * 指定分组下所有值
     *
     * @param group 分组
     * @return 值
     */
    public Collection<String> values(String group) {
        return this.groupMap.values(group);
    }

    /**
     * 指定分组下所有键值对
     *
     * @param group 分组
     * @return 键值对
     */
    public Set<Entry<String, String>> entrySet(String group) {
        return this.groupMap.entrySet(group);
    }

    /**
     * 设置值
     *
     * @param key   键
     * @param value 值
     * @return this
     */
    public PopSetting set(String key, String value) {
        this.groupMap.put(Normal.EMPTY, key, value);
        return this;
    }

    @Override
    public boolean isEmpty() {
        return this.groupMap.isEmpty();
    }

    /**
     * 默认分组(空分组)中是否包含指定key对应的值
     *
     * @param key 键
     * @return 默认分组中是否包含指定key对应的值
     */
    @Override
    public boolean containsKey(Object key) {
        return this.groupMap.containsKey(Normal.EMPTY, Convert.toString(key));
    }

    /**
     * 默认分组(空分组)中是否包含指定值
     *
     * @param value 值
     * @return 默认分组中是否包含指定值
     */
    @Override
    public boolean containsValue(Object value) {
        return this.groupMap.containsValue(Normal.EMPTY, Convert.toString(value));
    }

    /**
     * 获取默认分组(空分组)中指定key对应的值
     *
     * @param key 键
     * @return 默认分组(空分组)中指定key对应的值
     */
    @Override
    public String get(Object key) {
        return this.groupMap.get(Normal.EMPTY, Convert.toString(key));
    }

    /**
     * 将指定键值对加入到默认分组(空分组)中
     *
     * @param key   键
     * @param value 值
     * @return 加入的值
     */
    @Override
    public String put(String key, String value) {
        return this.groupMap.put(Normal.EMPTY, key, value);
    }

    /**
     * 移除默认分组(空分组)中指定值
     *
     * @param key 键
     * @return 移除的值
     */
    @Override
    public String remove(Object key) {
        return this.groupMap.remove(Normal.EMPTY, Convert.toString(key));
    }

    /**
     * 将键值对Map加入默认分组(空分组)中
     *
     * @param m Map
     */
    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        this.groupMap.putAll(Normal.EMPTY, m);
    }

    /**
     * 清空默认分组(空分组)中的所有键值对
     */
    @Override
    public void clear() {
        this.groupMap.clear(Normal.EMPTY);
    }

    /**
     * 获取默认分组(空分组)中的所有键列表
     *
     * @return 默认分组(空分组)中的所有键列表
     */
    @Override
    public Set<String> keySet() {
        return this.groupMap.keySet(Normal.EMPTY);
    }

    /**
     * 获取默认分组(空分组)中的所有值列表
     *
     * @return 默认分组(空分组)中的所有值列表
     */
    @Override
    public Collection<String> values() {
        return this.groupMap.values(Normal.EMPTY);
    }

    /**
     * 获取默认分组(空分组)中的所有键值对列表
     *
     * @return 默认分组(空分组)中的所有键值对列表
     */
    @Override
    public Set<Entry<String, String>> entrySet() {
        return this.groupMap.entrySet(Normal.EMPTY);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((null == charset) ? 0 : charset.hashCode());
        result = prime * result + groupMap.hashCode();
        result = prime * result + (isUseVariable ? 1231 : 1237);
        result = prime * result + ((null == settingUrl) ? 0 : settingUrl.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (null == obj) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PopSetting other = (PopSetting) obj;
        if (null == charset) {
            if (null != other.charset) {
                return false;
            }
        } else if (false == charset.equals(other.charset)) {
            return false;
        }
        if (false == groupMap.equals(other.groupMap)) {
            return false;
        }
        if (isUseVariable != other.isUseVariable) {
            return false;
        }
        if (null == settingUrl) {
            return null == other.settingUrl;
        } else {
            return settingUrl.equals(other.settingUrl);
        }
    }

    @Override
    public String toString() {
        return groupMap.toString();
    }

}
