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

import org.miaixz.bus.core.lang.Symbol;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Ini file's Section
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class IniSectionService extends AbstractElement implements IniSection {

    /**
     * list of properties, or empty
     */
    private List<IniProperty> properties;

    public IniSectionService(String value, int lineNumber) {
        super(value, Symbol.C_BRACKET_LEFT + value + Symbol.C_BRACKET_RIGHT, lineNumber);
        properties = new ArrayList<>();
    }

    public IniSectionService(String value, String originalValue, int lineNumber) {
        super(value, originalValue, lineNumber);
        properties = new ArrayList<>();
    }

    public IniSectionService(String value, String originalValue, int lineNumber, Supplier<List<IniProperty>> listSupplier) {
        super(value, originalValue, lineNumber);
        properties = listSupplier.get();
    }

    public IniSectionService(String value, int lineNumber, IniComment comment) {
        super(value, Symbol.C_BRACKET_LEFT + value + Symbol.C_BRACKET_RIGHT, lineNumber, comment);
        properties = new ArrayList<>();
    }

    public IniSectionService(String value, String originalValue, int lineNumber, IniComment comment) {
        super(value, originalValue, lineNumber, comment);
        properties = new ArrayList<>();
    }

    public IniSectionService(String value, String originalValue, int lineNumber, IniComment comment, Supplier<List<IniProperty>> listSupplier) {
        super(value, originalValue, lineNumber, comment);
        properties = listSupplier.get();
    }

    /**
     * If the {@code value} changed, change the originalValue
     *
     * @param newValue when {@code value} changes, like {@link #setValue(String)} or {@link #setValue(Function)}
     * @return new originalValue
     */
    @Override
    protected String valueChanged(String newValue) {
        return "[" + newValue + "]";
    }

    /**
     * toString, with all iniProperties value.
     *
     * @return string with properties value.
     */
    @Override
    public String toPropertiesString() {
        StringJoiner joiner = new StringJoiner(System.getProperty("line.separator", Symbol.LF));
        joiner.add(toString());
        for (IniProperty p : this) {
            joiner.add(p);
        }
        return joiner.toString();
    }

    /**
     * get IniProperty list. will copy a new list.
     *
     * @return list.
     */
    @Override
    public List<IniProperty> getList() {
        return new ArrayList<>(properties);
    }

    /**
     * get IniProperty list. will copy a new list.
     *
     * @return list.
     */
    @Override
    public List<IniProperty> getList(Supplier<List<IniProperty>> listSupplier) {
        List<IniProperty> list = listSupplier.get();
        list.addAll(properties);
        return list;
    }

    /**
     * if you want to get the {@code IniProperty} list,
     * use {@link #getList()} or {@link #getList(Supplier)}.
     *
     * @return the real list.
     */
    @Override
    public List<IniProperty> getProxyList() {
        return properties;
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty();
    }

}
