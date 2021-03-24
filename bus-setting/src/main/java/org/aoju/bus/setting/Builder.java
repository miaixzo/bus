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
package org.aoju.bus.setting;

import org.aoju.bus.core.toolkit.FileKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.setting.magic.Properties;
import org.aoju.bus.setting.magic.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 构建器创建{@link IniSetting}示例
 * 非线程安全
 *
 * @author Kimi Liu
 * @version 6.2.1
 * @since JDK 1.8+
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
     * 获取当前环境下的配置文件<br>
     * name可以为不包括扩展名的文件名（默认.properties），也可以是文件名全称
     *
     * @param name 文件名，如果没有扩展名，默认为.properties
     * @return 当前环境下配置文件
     */
    public static Properties getProperties(String name) {
        return CACHE_PROPS.computeIfAbsent(name, (filePath) -> {
            final String extName = FileKit.extName(filePath);
            if (StringKit.isEmpty(extName)) {
                filePath = filePath + ".properties";
            }
            return new Properties(filePath);
        });
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
