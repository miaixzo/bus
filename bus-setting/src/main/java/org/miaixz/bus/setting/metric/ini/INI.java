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
package org.miaixz.bus.setting.metric.ini;

import java.util.*;
import java.util.function.Supplier;

public class INI {

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

    public INI() {
        elements = new ArrayList<>();
    }

    public INI(Supplier<List<IniElement>> listSupplier) {
        elements = listSupplier.get();
    }


    /**
     * 设置分区创建者功能
     *
     * @param iniSectionCreator {@link IniSectionCreator}
     * @return 当前类对象信息
     */
    public INI sectionCreator(IniSectionCreator iniSectionCreator) {
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
    public INI commentCreator(IniCommentCreator iniCommentCreator) {
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
    public INI propertyCreator(IniPropertyCreator iniPropertyCreator) {
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
    public INI skipLine(int length) {
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
    public INI plus(INI otherBuilder) {
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
    public INI plus(List<IniElement> elements) {
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
    public INI plusSection(String value) {
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
    public INI plusSection(String value, IniComment comment) {
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
    public INI plusSection(String value, String commentValue) {
        final int lineNumber = line++;
        final IniComment comment = iniCommentCreator.create(commentValue, lineNumber);
        final IniSection section = iniSectionCreator.create(value, lineNumber, comment);
        elements.add(section);
        this.lastSection = section;
        checkProps();
        return this;
    }

    /**
     * Plus a property
     *
     * @param key   key
     * @param value value
     * @return 当前类对象信息
     */
    public INI plusProperty(String key, String value) {
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
    public INI plusProperty(String key, String value, IniComment comment) {
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
    public INI plusProperty(String key, String value, String commentValue) {
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
    public INI plusProperties(Properties properties) {
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
    public INI plusProperties(Properties properties, IniComment comment) {
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
    public INI plusProperties(Properties properties, String commentValue) {
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

    public INI plusComment(String value) {
        final IniComment comment = iniCommentCreator.create(value, line++);
        elements.add(comment);
        return this;
    }

    public IniSetting build() {
        return new IniSetting(elements);
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

}
