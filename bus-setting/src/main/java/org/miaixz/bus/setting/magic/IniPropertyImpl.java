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
package org.miaixz.bus.setting.magic;

import org.miaixz.bus.core.lang.Symbol;

import java.util.function.Function;

/**
 * Ini file's parameters, like {@code property1=value1 }
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class IniPropertyImpl extends AbstractElement implements IniProperty {

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
    public IniPropertyImpl(IniSection section, String key, String value, String originalValue, int lineNumber) {
        super(value, originalValue, lineNumber);
        this.section = section;
        this.key = key;
    }

    /**
     * IniProperty constructor without section. maybe init later
     *
     * @see #IniPropertyImpl(IniSection, String, String, String, int)
     */
    /**
     * @param key           the property's key, not null
     * @param value         the property's value, null able
     * @param originalValue the property's original value
     * @param lineNumber    line number
     */
    public IniPropertyImpl(String key, String value, String originalValue, int lineNumber) {
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
    public IniPropertyImpl(IniSection section, String key, String value, int lineNumber) {
        super(value, key + Symbol.C_EQUAL + value, lineNumber);
        this.section = section;
        this.key = key;
    }

    public IniPropertyImpl(String key, String value, int lineNumber) {
        super(value, key + Symbol.C_EQUAL + value, lineNumber);
        this.key = key;
    }

    @Override
    public IniSection getSection() {
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
     * default ini property's comment is null.
     * there may be comments at the end of each element.
     * or null.
     * if this element is comment, return itself.
     * so, nullable, or see {@link #getCommentOptional}.
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
