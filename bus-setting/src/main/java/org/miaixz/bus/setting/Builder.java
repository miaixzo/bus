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
package org.miaixz.bus.setting;

import org.miaixz.bus.core.center.map.Dictionary;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.ResourceKit;
import org.miaixz.bus.setting.metric.ini.IniSetting;
import org.miaixz.bus.setting.metric.props.Props;
import org.miaixz.bus.setting.metric.setting.Setting;
import org.miaixz.bus.setting.metric.yaml.Yaml;
import org.yaml.snakeyaml.DumperOptions;

import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;
import java.util.Properties;

/**
 * 构建器创建{@link IniSetting}示例
 * 非线程安全
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Builder {

    /**
     * 从classpath或绝对路径加载YAML文件
     *
     * @param path YAML路径，相对路径相对classpath
     * @return 加载的内容，默认Map
     */
    public static Dictionary loadYaml(final String path) {
        return Yaml.load(path, Dictionary.class);
    }

    /**
     * 从classpath或绝对路径加载YAML文件
     *
     * @param <T>  Bean类型，默认map
     * @param path YAML路径，相对路径相对classpath
     * @param type 加载的Bean类型，即转换为的bean
     * @return 加载的内容，默认Map
     */
    public static <T> T loadYaml(final String path, final Class<T> type) {
        return Yaml.load(ResourceKit.getStream(path), type);
    }

    /**
     * 从流中加载YAML
     *
     * @param <T>  Bean类型，默认map
     * @param in   流
     * @param type 加载的Bean类型，即转换为的bean
     * @return 加载的内容，默认Map
     */
    public static <T> T loadYaml(final InputStream in, final Class<T> type) {
        return Yaml.load(IoKit.toBomReader(in), type);
    }

    /**
     * 加载YAML，加载完毕后关闭{@link Reader}
     *
     * @param reader {@link Reader}
     * @return 加载的Map
     */
    public static Dictionary loadYaml(final Reader reader) {
        return Yaml.load(reader, Dictionary.class);
    }

    /**
     * 加载YAML，加载完毕后关闭{@link Reader}
     *
     * @param <T>    Bean类型，默认map
     * @param reader {@link Reader}
     * @param type   加载的Bean类型，即转换为的bean
     * @return 加载的内容，默认Map
     */
    public static <T> T loadYaml(final Reader reader, final Class<T> type) {
        return Yaml.load(reader, type, true);
    }

    /**
     * 加载YAML
     *
     * @param <T>           Bean类型，默认map
     * @param reader        {@link Reader}
     * @param type          加载的Bean类型，即转换为的bean
     * @param isCloseReader 加载完毕后是否关闭{@link Reader}
     * @return 加载的内容，默认Map
     */
    public static <T> T loadYaml(final Reader reader, Class<T> type, final boolean isCloseReader) {
        return Yaml.load(reader, type, isCloseReader);
    }

    /**
     * 解析YAML
     *
     * @param content 数据内容
     */
    public static <T> T parseYaml(String content) {
        return Yaml.parse(content);
    }

    /**
     * 解析YAML
     *
     * @param prefix 前缀信息
     * @param map    数据内容
     */
    public static <T> T parseYaml(String prefix, Map<String, Object> map) {
        return Yaml.parse(prefix, map);
    }

    /**
     * 将Bean对象或者Map写出到{@link Writer}
     *
     * @param object 对象
     * @param writer {@link Writer}
     */
    public static void dumpYaml(final Object object, final Writer writer) {
        Yaml.dump(object, writer);
    }

    /**
     * 将Bean对象或者Map写出到{@link Writer}
     *
     * @param object        对象
     * @param writer        {@link Writer}
     * @param dumperOptions 输出风格
     */
    public static void dumpYaml(final Object object, final Writer writer, DumperOptions dumperOptions) {
        Yaml.dump(object, writer, dumperOptions);
    }

    /**
     * 将Bean对象或者Map写出到{@link Writer}
     *
     * @param properties 对象
     * @param value      输出风格
     * @return the string
     */
    public static String replaceYamlValue(final java.util.Properties properties, String value) {
        return Yaml.replaceRefValue(properties, value);
    }

    /**
     * 获取系统参数，例如用户在执行java命令时定义的 -Duse=bus
     *
     * @return 系统参数Props
     */
    public static Properties getProperties() {
        return Props.getProperties();
    }

    /**
     * 获取当前环境下的配置文件
     * name可以为不包括扩展名的文件名（默认.properties），也可以是文件名全称
     *
     * @param name 文件名，如果没有扩展名，默认为.properties
     * @return 当前环境下配置文件
     */
    public static Properties getProperties(final String name) {
        return Props.get(name);
    }

    /**
     * 获取给定路径找到的第一个配置文件
     * * name可以为不包括扩展名的文件名（默认.properties为结尾），也可以是文件名全称
     *
     * @param names 文件名，如果没有扩展名，默认为.properties
     * @return 当前环境下配置文件
     */
    public static Properties getPropertiesFound(final String... names) {
        return Props.getFirstFound(names);
    }

    /**
     * 获取当前环境下的配置文件
     * name可以为不包括扩展名的文件名（默认.setting为结尾），也可以是文件名全称
     *
     * @param name 文件名，如果没有扩展名，默认为.setting
     * @return 当前环境下配置文件
     */
    public static org.miaixz.bus.setting.Setting getSetting(final String name) {
        return Setting.get(name);
    }

    /**
     * 获取给定路径找到的第一个配置文件
     * * name可以为不包括扩展名的文件名（默认.setting为结尾），也可以是文件名全称
     *
     * @param names 文件名，如果没有扩展名，默认为.setting
     * @return 当前环境下配置文件
     */
    public static org.miaixz.bus.setting.Setting getSettingFirstFound(final String... names) {
        return Setting.getFirstFound(names);
    }

}
