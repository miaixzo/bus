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
package org.miaixz.bus.core.builder;

/**
 * 使用{@link ToStringBuilder}创建<code>toString</code>
 *
 * <p>
 * 这个类是作为单例对象使用的。没有必要每次都实例化一个新样式。 只需实例化一次，
 * 根据需要自定义值，并将结果存储在公共静态final变量中，供程序的其余部分访问.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StandardToStringStyle extends ToStringStyle {

    private static final long serialVersionUID = 1L;

    public StandardToStringStyle() {
        super();
    }

    @Override
    public boolean isUseClassName() {
        return super.isUseClassName();
    }

    @Override
    public void setUseClassName(final boolean useClassName) {
        super.setUseClassName(useClassName);
    }

    @Override
    public boolean isUseShortClassName() {
        return super.isUseShortClassName();
    }

    @Override
    public void setUseShortClassName(final boolean useShortClassName) {
        super.setUseShortClassName(useShortClassName);
    }

    @Override
    public boolean isUseIdentityHashCode() {
        return super.isUseIdentityHashCode();
    }

    @Override
    public void setUseIdentityHashCode(final boolean useIdentityHashCode) {
        super.setUseIdentityHashCode(useIdentityHashCode);
    }

    @Override
    public boolean isUseFieldNames() {
        return super.isUseFieldNames();
    }

    @Override
    public void setUseFieldNames(final boolean useFieldNames) {
        super.setUseFieldNames(useFieldNames);
    }

    @Override
    public boolean isDefaultFullDetail() {
        return super.isDefaultFullDetail();
    }

    @Override
    public void setDefaultFullDetail(final boolean defaultFullDetail) {
        super.setDefaultFullDetail(defaultFullDetail);
    }


    @Override
    public boolean isArrayContentDetail() {
        return super.isArrayContentDetail();
    }

    @Override
    public void setArrayContentDetail(final boolean arrayContentDetail) {
        super.setArrayContentDetail(arrayContentDetail);
    }

    @Override
    public String getArrayStart() {
        return super.getArrayStart();
    }

    @Override
    public void setArrayStart(final String arrayStart) {
        super.setArrayStart(arrayStart);
    }

    @Override
    public String getArrayEnd() {
        return super.getArrayEnd();
    }

    @Override
    public void setArrayEnd(final String arrayEnd) {
        super.setArrayEnd(arrayEnd);
    }

    @Override
    public String getArraySeparator() {
        return super.getArraySeparator();
    }

    @Override
    public void setArraySeparator(final String arraySeparator) {
        super.setArraySeparator(arraySeparator);
    }

    @Override
    public String getContentStart() {
        return super.getContentStart();
    }

    @Override
    public void setContentStart(final String contentStart) {
        super.setContentStart(contentStart);
    }

    @Override
    public String getContentEnd() {
        return super.getContentEnd();
    }

    @Override
    public void setContentEnd(final String contentEnd) {
        super.setContentEnd(contentEnd);
    }

    @Override
    public String getFieldNameValueSeparator() {
        return super.getFieldNameValueSeparator();
    }

    @Override
    public void setFieldNameValueSeparator(final String fieldNameValueSeparator) {
        super.setFieldNameValueSeparator(fieldNameValueSeparator);
    }

    @Override
    public String getFieldSeparator() {
        return super.getFieldSeparator();
    }

    @Override
    public void setFieldSeparator(final String fieldSeparator) {
        super.setFieldSeparator(fieldSeparator);
    }

    @Override
    public boolean isFieldSeparatorAtStart() {
        return super.isFieldSeparatorAtStart();
    }

    @Override
    public void setFieldSeparatorAtStart(final boolean fieldSeparatorAtStart) {
        super.setFieldSeparatorAtStart(fieldSeparatorAtStart);
    }

    @Override
    public boolean isFieldSeparatorAtEnd() {
        return super.isFieldSeparatorAtEnd();
    }

    @Override
    public void setFieldSeparatorAtEnd(final boolean fieldSeparatorAtEnd) {
        super.setFieldSeparatorAtEnd(fieldSeparatorAtEnd);
    }

    @Override
    public String getNullText() {
        return super.getNullText();
    }

    @Override
    public void setNullText(final String nullText) {
        super.setNullText(nullText);
    }

    @Override
    public String getSizeStartText() {
        return super.getSizeStartText();
    }

    @Override
    public void setSizeStartText(final String sizeStartText) {
        super.setSizeStartText(sizeStartText);
    }

    @Override
    public String getSizeEndText() {
        return super.getSizeEndText();
    }

    @Override
    public void setSizeEndText(final String sizeEndText) {
        super.setSizeEndText(sizeEndText);
    }

    @Override
    public String getSummaryObjectStartText() {
        return super.getSummaryObjectStartText();
    }

    @Override
    public void setSummaryObjectStartText(final String summaryObjectStartText) {
        super.setSummaryObjectStartText(summaryObjectStartText);
    }

    @Override
    public String getSummaryObjectEndText() {
        return super.getSummaryObjectEndText();
    }

    @Override
    public void setSummaryObjectEndText(final String summaryObjectEndText) {
        super.setSummaryObjectEndText(summaryObjectEndText);
    }

}
