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
package org.miaixz.bus.setting.metric.yaml;

import org.miaixz.bus.core.center.map.Dictionary;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.ResourceKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.yaml.snakeyaml.DumperOptions;

import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * 基于Snakeyaml的的YAML读写工具
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Yaml {

    /**
     * 从classpath或绝对路径加载YAML文件
     *
     * @param path YAML路径，相对路径相对classpath
     * @return 加载的内容，默认Map
     */
    public static Dictionary load(final String path) {
        return load(path, Dictionary.class);
    }

    /**
     * 从classpath或绝对路径加载YAML文件
     *
     * @param <T>  Bean类型，默认map
     * @param path YAML路径，相对路径相对classpath
     * @param type 加载的Bean类型，即转换为的bean
     * @return 加载的内容，默认Map
     */
    public static <T> T load(final String path, final Class<T> type) {
        return load(ResourceKit.getStream(path), type);
    }

    /**
     * 从流中加载YAML
     *
     * @param <T>  Bean类型，默认map
     * @param in   流
     * @param type 加载的Bean类型，即转换为的bean
     * @return 加载的内容，默认Map
     */
    public static <T> T load(final InputStream in, final Class<T> type) {
        return load(IoKit.toBomReader(in), type);
    }

    /**
     * 加载YAML，加载完毕后关闭{@link Reader}
     *
     * @param reader {@link Reader}
     * @return 加载的Map
     */
    public static Dictionary load(final Reader reader) {
        return load(reader, Dictionary.class);
    }

    /**
     * 加载YAML，加载完毕后关闭{@link Reader}
     *
     * @param <T>    Bean类型，默认map
     * @param reader {@link Reader}
     * @param type   加载的Bean类型，即转换为的bean
     * @return 加载的内容，默认Map
     */
    public static <T> T load(final Reader reader, final Class<T> type) {
        return load(reader, type, true);
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
    public static <T> T load(final Reader reader, Class<T> type, final boolean isCloseReader) {
        Assert.notNull(reader, "Reader must be not null !");
        if (null == type) {
            type = (Class<T>) Object.class;
        }

        final org.yaml.snakeyaml.Yaml yaml = new org.yaml.snakeyaml.Yaml();
        try {
            return yaml.loadAs(reader, type);
        } finally {
            if (isCloseReader) {
                IoKit.closeQuietly(reader);
            }
        }
    }

    /**
     * 解析YAML
     *
     * @param content 数据内容
     */
    public static <T> T parse(String content) {
        return parse(null, new org.yaml.snakeyaml.Yaml().load(content));
    }

    /**
     * 解析YAML
     *
     * @param prefix 前缀信息
     * @param map    数据内容
     */
    public static <T> T parse(String prefix, Map<String, Object> map) {
        Object value;
        String currentKey;
        Map result = new HashMap<>();
        for (Object key : map.keySet()) {
            currentKey = prefix == null ? key.toString() : prefix + Symbol.DOT + key.toString();
            value = map.get(key);
            if (value instanceof Map) {
                parse(currentKey, (Map) value);
            } else {
                result.put(currentKey, value);
            }
        }
        return (T) result;
    }

    /**
     * 将Bean对象或者Map写出到{@link Writer}
     *
     * @param object 对象
     * @param writer {@link Writer}
     */
    public static void dump(final Object object, final Writer writer) {
        final DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        dump(object, writer, options);
    }

    /**
     * 将Bean对象或者Map写出到{@link Writer}
     *
     * @param object        对象
     * @param writer        {@link Writer}
     * @param dumperOptions 输出风格
     */
    public static void dump(final Object object, final Writer writer, DumperOptions dumperOptions) {
        if (null == dumperOptions) {
            dumperOptions = new DumperOptions();
        }
        final org.yaml.snakeyaml.Yaml yaml = new org.yaml.snakeyaml.Yaml(dumperOptions);
        yaml.dump(object, writer);
    }

    /**
     * 替换本地变量占位符
     *
     * @param properties 属性信息
     * @param value      值信息
     * @return 替换后的信息
     */
    public static String replaceRefValue(java.util.Properties properties, String value) {
        if (!value.contains(Symbol.DOLLAR + Symbol.BRACE_LEFT)) {
            return value;
        } else {
            String[] segments = value.split("\\$\\{");
            StringBuilder finalValue = new StringBuilder();

            for (int i = 0; i < segments.length; ++i) {
                String seg = StringKit.trimToNull(segments[i]);
                if (!StringKit.isBlank(seg)) {
                    if (seg.contains(Symbol.BRACE_RIGHT)) {
                        String refKey = seg.substring(0, seg.indexOf(Symbol.BRACE_RIGHT)).trim();
                        String withBraceString = null;
                        if (seg.contains(Symbol.BRACE_LEFT)) {
                            withBraceString = seg.substring(seg.indexOf(Symbol.BRACE_RIGHT) + 1);
                        }

                        String defaultValue = null;
                        int defaultValSpliterIndex = refKey.indexOf(Symbol.COLON);
                        if (defaultValSpliterIndex > 0) {
                            defaultValue = refKey.substring(defaultValSpliterIndex + 1);
                            refKey = refKey.substring(0, defaultValSpliterIndex);
                        }

                        String refValue = System.getProperty(refKey);
                        if (StringKit.isBlank(refValue)) {
                            refValue = System.getenv(refKey);
                        }

                        if (StringKit.isBlank(refValue)) {
                            refValue = properties.getProperty(refKey);
                        }

                        if (StringKit.isBlank(refValue)) {
                            refValue = defaultValue;
                        }

                        if (StringKit.isBlank(refValue)) {
                            finalValue.append(Symbol.DOLLAR + Symbol.BRACE_LEFT + refKey + Symbol.BRACE_RIGHT);
                        } else {
                            finalValue.append(refValue);
                        }

                        if (withBraceString != null) {
                            finalValue.append(withBraceString);
                        } else {
                            String[] segments2 = seg.split("\\}");
                            if (segments2.length == 2) {
                                finalValue.append(segments2[1]);
                            }
                        }
                    } else {
                        finalValue.append(seg);
                    }
                }
            }
            return finalValue.toString();
        }
    }

}
