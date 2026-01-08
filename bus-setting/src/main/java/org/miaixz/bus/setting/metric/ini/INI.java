/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

/**
 * A builder for creating an {@link IniSetting} object programmatically. It provides a fluent API to add sections,
 * properties, and comments.
 */
public class INI {

    /**
     * The list of INI elements (sections, properties, comments).
     */
    private final List<IniElement> elements;
    /**
     * A queue for properties added before their corresponding section is defined.
     */
    private final LinkedList<Supplier<IniProperty>> waitForSections = new LinkedList<>();
    /**
     * The most recently added section.
     */
    private IniSection lastSection;
    /**
     * The current line number, starting from 1.
     */
    private int line = 1;
    /**
     * The creator function for sections.
     */
    private IniSectionCreator iniSectionCreator = IniSectionCreator.DEFAULT;
    /**
     * The creator function for comments.
     */
    private IniCommentCreator iniCommentCreator = IniCommentCreator.DEFAULT;
    /**
     * The creator function for properties.
     */
    private IniPropertyCreator iniPropertyCreator = IniPropertyCreator.DEFAULT;

    /**
     * Constructs a new, empty INI builder.
     */
    public INI() {
        elements = new ArrayList<>();
    }

    /**
     * Constructs a new INI builder with a custom list supplier.
     *
     * @param listSupplier A supplier that provides the list to store elements in.
     */
    public INI(Supplier<List<IniElement>> listSupplier) {
        elements = listSupplier.get();
    }

    /**
     * Sets the creator function for sections.
     *
     * @param iniSectionCreator The {@link IniSectionCreator} to use.
     * @return This builder instance for chaining.
     */
    public INI sectionCreator(IniSectionCreator iniSectionCreator) {
        Objects.requireNonNull(iniSectionCreator);
        this.iniSectionCreator = iniSectionCreator;
        return this;
    }

    /**
     * Sets the creator function for comments.
     *
     * @param iniCommentCreator The {@link IniCommentCreator} to use.
     * @return This builder instance for chaining.
     */
    public INI commentCreator(IniCommentCreator iniCommentCreator) {
        Objects.requireNonNull(iniCommentCreator);
        this.iniCommentCreator = iniCommentCreator;
        return this;
    }

    /**
     * Sets the creator function for properties.
     *
     * @param iniPropertyCreator The {@link IniPropertyCreator} to use.
     * @return This builder instance for chaining.
     */
    public INI propertyCreator(IniPropertyCreator iniPropertyCreator) {
        Objects.requireNonNull(iniPropertyCreator);
        this.iniPropertyCreator = iniPropertyCreator;
        return this;
    }

    /**
     * Skips a specified number of lines by adding null elements, which represent empty lines.
     *
     * @param length The number of lines to skip.
     * @return This builder instance for chaining.
     */
    public INI skipLine(int length) {
        for (int i = 0; i < length; i++) {
            elements.add(null);
            line++;
        }
        return this;
    }

    /**
     * Appends all elements from another INI builder to this one.
     *
     * @param otherBuilder The other builder whose elements will be appended.
     * @return This builder instance for chaining.
     */
    public INI plus(INI otherBuilder) {
        this.elements.addAll(otherBuilder.elements);
        this.line += otherBuilder.line - 1;
        return this;
    }

    /**
     * Appends a list of {@link IniElement}s to this builder.
     *
     * @param elements The list of INI elements to append.
     * @return This builder instance for chaining.
     */
    public INI plus(List<IniElement> elements) {
        this.elements.addAll(elements);
        this.line += elements.size();
        return this;
    }

    /**
     * Adds a new section.
     *
     * @param value The name of the section.
     * @return This builder instance for chaining.
     */
    public INI plusSection(String value) {
        final IniSection section = iniSectionCreator.create(value, line++, null);
        elements.add(section);
        this.lastSection = section;
        checkProps();
        return this;
    }

    /**
     * Adds a new section with a comment.
     *
     * @param value   The name of the section.
     * @param comment The comment to associate with the section.
     * @return This builder instance for chaining.
     */
    public INI plusSection(String value, IniComment comment) {
        final IniSection section = iniSectionCreator.create(value, line++, comment);
        elements.add(section);
        this.lastSection = section;
        checkProps();
        return this;
    }

    /**
     * Adds a new section with a comment string.
     *
     * @param value        The name of the section.
     * @param commentValue The text of the comment.
     * @return This builder instance for chaining.
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
     * Adds a new property (key-value pair) to the current section.
     *
     * @param key   The property key.
     * @param value The property value.
     * @return This builder instance for chaining.
     */
    public INI plusProperty(String key, String value) {
        checkProps(() -> iniPropertyCreator.create(key, value, line++, null));
        return this;
    }

    /**
     * Adds a new property to the current section with a comment.
     *
     * @param key     The property key.
     * @param value   The property value.
     * @param comment The comment to associate with the property.
     * @return This builder instance for chaining.
     */
    public INI plusProperty(String key, String value, IniComment comment) {
        checkProps(() -> iniPropertyCreator.create(key, value, line++, comment));
        return this;
    }

    /**
     * Adds a new property to the current section with a comment string.
     *
     * @param key          The property key.
     * @param value        The property value.
     * @param commentValue The text of the comment.
     * @return This builder instance for chaining.
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
     * Adds all properties from a {@link Properties} object to the current section.
     *
     * @param properties The {@link Properties} object.
     * @return This builder instance for chaining.
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
     * Adds all properties from a {@link Properties} object to the current section, each with the same comment.
     *
     * @param properties The {@link Properties} object.
     * @param comment    The comment to associate with each property.
     * @return This builder instance for chaining.
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
     * Adds all properties from a {@link Properties} object to the current section, each with the same comment string.
     *
     * @param properties   The {@link Properties} object.
     * @param commentValue The text of the comment for each property.
     * @return This builder instance for chaining.
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

    /**
     * Adds a comment line.
     *
     * @param value The text of the comment.
     * @return This builder instance for chaining.
     */
    public INI plusComment(String value) {
        final IniComment comment = iniCommentCreator.create(value, line++);
        elements.add(comment);
        return this;
    }

    /**
     * Builds the final {@link IniSetting} object from the added elements.
     *
     * @return The constructed {@link IniSetting}.
     */
    public IniSetting build() {
        return new IniSetting(elements);
    }

    /**
     * Checks for and processes any queued properties that were waiting for a section to be defined.
     */
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

    /**
     * Adds a property. If no section has been defined yet, the property is queued. Otherwise, it is added to the last
     * defined section.
     *
     * @param propertySupplier A supplier for creating the {@link IniProperty}.
     */
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
