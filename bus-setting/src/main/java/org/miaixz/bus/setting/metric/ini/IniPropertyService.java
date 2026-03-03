/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.setting.metric.ini;

import java.util.function.Function;

import org.miaixz.bus.core.lang.Symbol;

/**
 * Ini file's parameters, like {@code property1=value1 }
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class IniPropertyService extends AbstractElement implements IniProperty {

    /**
     * from section
     */
    private IniSection section;

    private String key;

    /**
     * IniProperty constructor
     *
     * @param section       property section
     * @param key           the property's key, not null
     * @param value         the property's value, null able
     * @param originalValue the original value of this property line
     * @param lineNumber    line number
     */
    public IniPropertyService(IniSection section, String key, String value, String originalValue, int lineNumber) {
        super(value, originalValue, lineNumber);
        this.section = section;
        this.key = key;
    }

    /**
     * IniProperty constructor without section. maybe init later
     *
     * @see #IniPropertyService(IniSection, String, String, String, int)
     */
    /**
     * @param key           the property's key, not null
     * @param value         the property's value, null able
     * @param originalValue the property's original value
     * @param lineNumber    line number
     */
    public IniPropertyService(String key, String value, String originalValue, int lineNumber) {
        super(value, originalValue, lineNumber);
        this.key = key;
    }

    /**
     * IniProperty constructor
     *
     * @param section    property section
     * @param key        the property's key, not null
     * @param value      the property's value, null able
     * @param lineNumber line number
     */
    public IniPropertyService(IniSection section, String key, String value, int lineNumber) {
        super(value, key + Symbol.C_EQUAL + value, lineNumber);
        this.section = section;
        this.key = key;
    }

    public IniPropertyService(String key, String value, int lineNumber) {
        super(value, key + Symbol.C_EQUAL + value, lineNumber);
        this.key = key;
    }

    @Override
    public IniSection getSection() {
        /**
         * Gets the section to which this property belongs.
         *
         * @return the section containing this property, or null if this property is not in a section
         */
        return this.section;
    }

    @Override
    public void setSection(IniSection section) {
        this.section = section;
    }

    @Override
    public String key() {
        return this.key;
    }

    @Override
    public void changeKey(String newKey) {
        this.key = newKey;
    }

    @Override
    public String setKey(String newKey) {
        String old = key;
        changeKey(newKey);
        setOriginalValue(keyChanged(newKey));
        return old;
    }

    /**
     * when key changed, get the new originalValue.
     *
     * @param newKey new key.
     * @return original value.
     */
    protected String keyChanged(String newKey) {
        return key + Symbol.C_EQUAL + newKey;
    }

    /**
     * when value changed, update originalValue.
     *
     * @param newValue when {@code value} changes, like {@link #setValue(String)} or {@link #setValue(Function)}
     * @return new originalValue
     */
    @Override
    protected String valueChanged(String newValue) {
        return key + Symbol.C_EQUAL + newValue;
    }

    /**
     * default ini property's comment is null. there may be comments at the end of each element. or null. if this
     * element is comment, return itself. so, nullable, or see {@link #getCommentOptional}.
     *
     * @return comment end of the element or null. if element, return itself.
     * @see #getCommentOptional()
     */
    @Override
    public IniComment getComment() {
        return null;
    }

    /**
     * @see #key()
     */
    @Override
    public String getKey() {
        return key();
    }

}
