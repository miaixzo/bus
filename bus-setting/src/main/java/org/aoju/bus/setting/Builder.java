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
package org.aoju.bus.setting;

import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.map.Dictionary;
import org.aoju.bus.core.toolkit.FileKit;
import org.aoju.bus.core.toolkit.IoKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.setting.magic.Properties;
import org.aoju.bus.setting.magic.*;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 构建器创建{@link IniSetting}示例
 * 非线程安全
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Builder {

    /**
     * 配置文件缓存
     */
    private static final Map<String, Properties> CACHE_PROPS = new ConcurrentHashMap<>();
    /**
     * 配置文件缓存
     */
    private static final Map<String, PopSetting> CACHE_SETTING = new ConcurrentHashMap<>();

    /**
     * 元素
     */
    private List<IniElement> elements;
    /**
     * 等待第一部分
     */
    private LinkedList<Supplier<IniProperty>> waitForSections = new LinkedList<>();
    /**
     * 最后一节
     */
    private IniSection lastSection;
    /**
     * 行号从1开始
     */
    private int line = 1;
    /**
     * section creator
     *
     * @see #sectionCreator(IniSectionCreator)
     */
    private IniSectionCreator iniSectionCreator = IniSectionCreator.DEFAULT;
    /**
     * comment creator
     *
     * @see #commentCreator(IniCommentCreator)
     */
    private IniCommentCreator iniCommentCreator = IniCommentCreator.DEFAULT;
    /**
     * property creator
     *
     * @see #propertyCreator(IniPropertyCreator)
     */
    private IniPropertyCreator iniPropertyCreator = IniPropertyCreator.DEFAULT;

    public Builder() {
        elements = new ArrayList<>();
    }

    public Builder(Supplier<List<IniElement>> listSupplier) {
        elements = listSupplier.get();
    }

    /**
     * 获取当前环境下的配置文件
     * name可以为不包括扩展名的文件名（默认.properties），也可以是文件名全称
     *
     * @param name 文件名，如果没有扩展名，默认为.properties
     * @return 当前环境下配置文件
     */
    public static Properties getProperties(String name) {
        return CACHE_PROPS.computeIfAbsent(name, (filePath) -> {
            final String suffix = FileKit.getSuffix(filePath);
            if (StringKit.isEmpty(suffix)) {
                filePath = filePath + ".properties";
            }
            return new Properties(filePath);
        });
    }

    /**
     * 从classpath或绝对路径加载YAML文件
     *
     * @param path YAML路径，相对路径相对classpath
     * @return 加载的内容，默认Map
     */
    public static Dictionary load(String path) {
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
    public static <T> T load(String path, Class<T> type) {
        return load(FileKit.getStream(path), type);
    }

    /**
     * 从流中加载YAML
     *
     * @param <T>  Bean类型，默认map
     * @param in   流
     * @param type 加载的Bean类型，即转换为的bean
     * @return 加载的内容，默认Map
     */
    public static <T> T load(InputStream in, Class<T> type) {
        return load(IoKit.getReader(in), type);
    }

    /**
     * 加载YAML，加载完毕后关闭{@link Reader}
     *
     * @param reader {@link Reader}
     * @return 加载的Map
     */
    public static Dictionary load(Reader reader) {
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
    public static <T> T load(Reader reader, Class<T> type) {
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
    public static <T> T load(Reader reader, Class<T> type, boolean isCloseReader) {
        Assert.notNull(reader, "Reader must be not null !");
        if (null == type) {
            type = (Class<T>) Object.class;
        }

        final Yaml yaml = new Yaml();
        try {
            return yaml.loadAs(reader, type);
        } finally {
            if (isCloseReader) {
                IoKit.close(reader);
            }
        }
    }

    /**
     * 解析PROPS
     *
     * @param result  数据结果
     * @param content 数据内容
     */
    public static void parsePropsMap(Map<String, Object> result, String content) {
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
     * 解析YAML
     *
     * @param result  数据结果
     * @param content 数据内容
     */
    public static void parseYamlMap(Map<String, Object> result, String content) {
        Yaml yaml = new Yaml();
        try {
            Map map = yaml.load(content);
            parseYamlMap(null, result, map);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析YAML
     *
     * @param prefix  前缀信息
     * @param result  数据结果
     * @param content 数据内容
     */
    public static void parseYamlMap(String prefix, Map<String, Object> result, Map<String, Object> content) {
        Object value;
        String currentKey;
        for (Object key : content.keySet()) {
            currentKey = prefix == null ? key.toString() : prefix + Symbol.DOT + key.toString();
            value = content.get(key);
            if (value instanceof Map) {
                parseYamlMap(currentKey, result, (Map) value);
            } else {
                result.put(currentKey, value);
            }
        }
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

    /**
     * 将Bean对象或者Map写出到{@link Writer}
     *
     * @param object 对象
     * @param writer {@link Writer}
     */
    public static void dump(Object object, Writer writer) {
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
    public static void dump(Object object, Writer writer, DumperOptions dumperOptions) {
        if (null == dumperOptions) {
            dumperOptions = new DumperOptions();
        }
        final Yaml yaml = new Yaml(dumperOptions);
        yaml.dump(object, writer);
    }

    /**
     * 设置分区创建者功能
     *
     * @param iniSectionCreator {@link IniSectionCreator}
     * @return 当前类对象信息
     */
    public Builder sectionCreator(IniSectionCreator iniSectionCreator) {
        Objects.requireNonNull(iniSectionCreator);
        this.iniSectionCreator = iniSectionCreator;
        return this;
    }

    /**
     * 设置评论创建者功能
     *
     * @param iniCommentCreator {@link IniCommentCreator}
     * @return 当前类对象信息
     */
    public Builder commentCreator(IniCommentCreator iniCommentCreator) {
        Objects.requireNonNull(iniCommentCreator);
        this.iniCommentCreator = iniCommentCreator;
        return this;
    }

    /**
     * 设置属性创建器功能
     *
     * @param iniPropertyCreator {@link IniPropertyCreator}
     * @return 当前类对象信息
     */
    public Builder propertyCreator(IniPropertyCreator iniPropertyCreator) {
        Objects.requireNonNull(iniPropertyCreator);
        this.iniPropertyCreator = iniPropertyCreator;
        return this;
    }

    /**
     * 跳过线，向行添加空值
     *
     * @param length 跳过线
     * @return 当前类对象信息
     */
    public Builder skipLine(int length) {
        for (int i = 0; i < length; i++) {
            elements.add(null);
            line++;
        }
        return this;
    }

    /**
     * Plus other builder
     *
     * @param otherBuilder other builder
     * @return 当前类对象信息
     */
    public Builder plus(Builder otherBuilder) {
        this.elements.addAll(otherBuilder.elements);
        this.line += otherBuilder.line - 1;
        return this;
    }

    /**
     * Plus iniElement list
     *
     * @param elements IniElement list
     * @return 当前类对象信息
     */
    public Builder plus(List<IniElement> elements) {
        this.elements.addAll(elements);
        this.line += elements.size();
        return this;
    }

    /**
     * Plus a section
     *
     * @param value section value
     * @return 当前类对象信息
     */
    public Builder plusSection(String value) {
        final IniSection section = iniSectionCreator.create(value, line++, null);
        elements.add(section);
        this.lastSection = section;
        checkProps();
        return this;
    }

    /**
     * Plus a section with comment
     *
     * @param value   section value
     * @param comment comment
     * @return 当前类对象信息
     */
    public Builder plusSection(String value, IniComment comment) {
        final IniSection section = iniSectionCreator.create(value, line++, comment);
        elements.add(section);
        this.lastSection = section;
        checkProps();
        return this;
    }

    /**
     * Plus a section with comment
     *
     * @param value        section value
     * @param commentValue comment value
     * @return 当前类对象信息
     */
    public Builder plusSection(String value, String commentValue) {
        final int lineNumber = line++;
        final IniComment comment = iniCommentCreator.create(commentValue, lineNumber);
        final IniSection section = iniSectionCreator.create(value, lineNumber, comment);
        elements.add(section);
        this.lastSection = section;
        checkProps();
        return this;
    }

    private void checkProps() {
        if (null != this.lastSection && !waitForSections.isEmpty()) {
            while (!waitForSections.isEmpty()) {
                final IniProperty property = waitForSections.removeLast().get();
                property.setSection(this.lastSection);
                this.lastSection.add(property);
                elements.add(property);
            }
        }
    }

    private void checkProps(Supplier<IniProperty> propertySupplier) {
        if (null == this.lastSection) {
            this.waitForSections.addFirst(propertySupplier);
        } else {
            checkProps();
            final IniProperty property = propertySupplier.get();
            property.setSection(this.lastSection);
            this.lastSection.add(property);
            elements.add(property);
        }
    }

    /**
     * Plus a property
     *
     * @param key   key
     * @param value value
     * @return 当前类对象信息
     */
    public Builder plusProperty(String key, String value) {
        checkProps(() -> iniPropertyCreator.create(key, value, line++, null));
        return this;
    }

    /**
     * Plus a property
     *
     * @param key     key
     * @param value   value
     * @param comment 描述信息
     * @return 当前类对象信息
     */
    public Builder plusProperty(String key, String value, IniComment comment) {
        checkProps(() -> iniPropertyCreator.create(key, value, line++, comment));
        return this;
    }

    /**
     * Plus a property
     *
     * @param key          key
     * @param value        value
     * @param commentValue 描述信息
     * @return 当前类对象信息
     */
    public Builder plusProperty(String key, String value, String commentValue) {
        checkProps(() -> {
            final int lineNumber = line++;
            IniComment comment = iniCommentCreator.create(commentValue, lineNumber);
            return iniPropertyCreator.create(key, value, lineNumber, comment);
        });
        return this;
    }

    /**
     * Plus properties
     *
     * @param properties properties
     * @return 当前类对象信息
     */
    public Builder plusProperties(java.util.Properties properties) {
        final Set<String> names = properties.stringPropertyNames();
        for (String key : names) {
            String value = properties.getProperty(key);
            checkProps(() -> iniPropertyCreator.create(key, value, line++, null));
        }
        return this;
    }

    /**
     * Plus properties
     *
     * @param properties properties
     * @param comment    描述信息
     * @return 当前类对象信息
     */
    public Builder plusProperties(java.util.Properties properties, IniComment comment) {
        final Set<String> names = properties.stringPropertyNames();
        for (String key : names) {
            String value = properties.getProperty(key);
            checkProps(() -> iniPropertyCreator.create(key, value, line++, comment));
        }
        return this;
    }

    /**
     * Plus properties
     *
     * @param properties   properties
     * @param commentValue 描述信息
     * @return 当前类对象信息
     */
    public Builder plusProperties(java.util.Properties properties, String commentValue) {
        final Set<String> names = properties.stringPropertyNames();
        for (String key : names) {
            String value = properties.getProperty(key);
            checkProps(() -> {
                final int lineNumber = line++;
                IniComment comment = iniCommentCreator.create(commentValue, lineNumber);
                return iniPropertyCreator.create(key, value, lineNumber, comment);
            });
        }
        return this;
    }

    public Builder plusComment(String value) {
        final IniComment comment = iniCommentCreator.create(value, line++);
        elements.add(comment);
        return this;
    }

    public IniSetting build() {
        return new IniSetting(elements);
    }

    /**
     * section create function
     */
    @FunctionalInterface
    public interface IniSectionCreator {
        IniSectionCreator DEFAULT = IniSectionImpl::new;

        /**
         * create a section by value
         *
         * @param value   value
         * @param line    line number
         * @param comment comment, nullable
         * @return {@link IniSection}
         */
        IniSection create(String value, int line, IniComment comment);
    }

    /**
     * section create function
     */
    @FunctionalInterface
    public interface IniCommentCreator {
        IniCommentCreator DEFAULT = IniCommentImpl::byValue;

        /**
         * create a Comment by value
         *
         * @param value value
         * @param line  line number
         * @return {@link IniComment}
         */
        IniComment create(String value, int line);
    }

    /**
     * property create function
     */
    @FunctionalInterface
    public interface IniPropertyCreator {
        /**
         * this default function will ignore comment.
         */
        IniPropertyCreator DEFAULT = (k, v, l, c) -> new IniPropertyImpl(k, v, l);

        /**
         * create a property by value
         *
         * @param key     key
         * @param value   value
         * @param line    line
         * @param comment comment, nullable
         * @return {@link IniProperty}
         */
        IniProperty create(String key, String value, int line, IniComment comment);
    }

}
