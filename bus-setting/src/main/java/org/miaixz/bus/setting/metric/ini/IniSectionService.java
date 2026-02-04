/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.setting.metric.ini;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.function.Supplier;

import org.miaixz.bus.core.lang.Keys;
import org.miaixz.bus.core.lang.Symbol;

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

    public IniSectionService(String value, String originalValue, int lineNumber,
            Supplier<List<IniProperty>> listSupplier) {
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

    public IniSectionService(String value, String originalValue, int lineNumber, IniComment comment,
            Supplier<List<IniProperty>> listSupplier) {
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
        StringJoiner joiner = new StringJoiner(System.getProperty(Keys.LINE_SEPARATOR, Symbol.LF));
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
     * if you want to get the {@code IniProperty} list, use {@link #getList()} or {@link #getList(Supplier)}.
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
