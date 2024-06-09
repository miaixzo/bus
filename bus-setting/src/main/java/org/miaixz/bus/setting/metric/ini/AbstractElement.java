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

/**
 * an abstract class for {@link IniElement}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractElement implements IniElement {

    /**
     * value of comment text.
     */
    private String value;
    /**
     * the line number.
     */
    private int lineNumber;
    /**
     * the originalValue
     */
    private String originalValue;
    /**
     * comment, nullable
     */
    private IniComment comment;

    AbstractElement(String value, String originalValue, int lineNumber) {
        this.value = value;
        this.lineNumber = lineNumber;
        this.originalValue = originalValue;
        this.comment = null;
    }

    /**
     * maybe have comment
     */
    AbstractElement(String value, String originalValue, int lineNumber, IniComment comment) {
        this.value = value;
        this.lineNumber = lineNumber;
        this.originalValue = originalValue;
        this.comment = comment;
    }

    /**
     * trim a value
     *
     * @param value char sequence value
     * @return trimmed value
     */
    protected static String trim(String value) {
        return value.trim();
    }

    /**
     * If the {@code value} changed, change the originalValue
     *
     * @param newValue when {@code value} changes, like {@link #setValue(String)} or {@link #setValue(java.util.function.Function)}
     * @return new originalValue
     */
    protected abstract String valueChanged(String newValue);

    /**
     * <p>this element's value.
     * maybe a {@code toString} value like {@code comment},
     * a property's value like {@code property} or a title value like {@code section} .
     *
     * @return some value
     */
    @Override
    public String value() {
        return this.value;
    }

    /**
     * change value.
     *
     * @param newValue a new value
     */
    protected void changeValue(String newValue) {
        value = newValue;
    }

    /**
     * change this element's value.
     * if you want to DIY how to set value, Recommended to cover {@link #changeValue(String)} instead of {@link #setValue(String)}
     *
     * @param newValue a new value
     * @return old value
     * @see #value()
     */
    @Override
    public String setValue(String newValue) {
        String old = value;
        changeValue(newValue);
        setOriginalValue(valueChanged(newValue));
        return old;
    }


    /**
     * Default is {@code originalValue.toString()}
     *
     * @return string value
     */
    @Override
    public String toString() {
        return toCompleteString();
    }

    /**
     * get the original string.
     *
     * @return original string value.
     */
    @Override
    public String getOriginalValue() {
        return originalValue;
    }


    protected void setOriginalValue(String newOriginalValue) {
        this.originalValue = newOriginalValue;
    }

    @Override
    public IniComment getComment() {
        return comment;
    }

    /**
     * clear comment (if exists).
     */
    @Override
    public void clearComment() {
        this.comment = null;
    }

    /**
     * like {@link #toString()}, without comment value(if exists).
     *
     * @return to string value without comment value.
     */
    @Override
    public String toNoCommentString() {
        return originalValue;
    }

    /**
     * Get complete information.
     * Take sec as an example：{@code section.toString() + all properties.toString() + comment.toString()}
     * In general, it is about the same as {@link #toString()}.
     *
     * @return the string
     */
    @Override
    public String toCompleteString() {
        return null == comment ? originalValue : originalValue + Symbol.SPACE + comment;
    }

    /**
     * the line number where you are.
     *
     * @return line number.
     */
    @Override
    public int line() {
        return lineNumber;
    }

    /**
     * Returns the length of this character sequence.  The length is the number
     * of 16-bit <code>char</code>s in the sequence.
     *
     * @return the number of <code>char</code>s in this sequence
     */
    @Override
    public int length() {
        return value().length();
    }

    /**
     * Returns the <code>char</code> value at the specified index.  An index ranges from zero
     * to length() - 1.  The first <code>char</code> value of the sequence is at
     * index zero, the next at index one, and so on, as for array
     * indexing.
     *
     * <p>If the <code>char</code> value specified by the index is a
     * <a href="{@docRoot}/java/lang/Character.html#unicode">surrogate</a>, the surrogate
     * value is returned.
     *
     * @param index the index of the <code>char</code> value to be returned
     * @return the specified <code>char</code> value
     * @throws IndexOutOfBoundsException if the index argument is negative or not less than
     *                                   length()
     */
    @Override
    public char charAt(int index) {
        return value().charAt(index);
    }

    /**
     * Returns a <code>CharSequence</code> that is a subsequence of this sequence.
     * The subsequence starts with the <code>char</code> value at the specified index and
     * ends with the <code>char</code> value at index end - 1.  The length
     * (in <code>char</code>s) of the
     * returned sequence is end - start, so if start == end
     * then an empty sequence is returned.
     *
     * @param start the start index, inclusive
     * @param end   the end index, exclusive
     * @return the specified subsequence
     * @throws IndexOutOfBoundsException if start or end are negative,
     *                                   if end is greater than length(),
     *                                   or if start is greater than end
     */
    @Override
    public CharSequence subSequence(int start, int end) {
        return value.subSequence(start, end);
    }

}
