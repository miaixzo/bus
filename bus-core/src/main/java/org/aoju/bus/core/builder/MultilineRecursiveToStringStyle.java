/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.builder;


import org.aoju.bus.core.utils.ClassUtils;

/**
 * <p>Works with {@link ToStringBuilder} to create a "deep" <code>toString</code>.
 * But instead a single line like the {@link RecursiveToStringStyle} this creates a multiline String
 * similar to the {@link ToStringStyle#MULTI_LINE_STYLE}.</p>
 *
 * <p>To use this class write code as follows:</p>
 *
 * <pre>
 * public class Job {
 *   String title;
 *   ...
 * }
 *
 * public class Person {
 *   String name;
 *   int age;
 *   boolean smoker;
 *   Job job;
 *
 *   ...
 *
 *   public String toString() {
 *     return new ReflectionToStringBuilder(this, new MultilineRecursiveToStringStyle()).toString();
 *   }
 * }
 * </pre>
 *
 * <p>
 * This will produce a toString of the format:
 * <code>Person@7f54[
 * &nbsp; name=Stephen,
 * &nbsp; age=29,
 * &nbsp; smoker=false,
 * &nbsp; job=Job@43cd2[
 * &nbsp; &nbsp; title=Manager
 * &nbsp;  ]
 * ]
 * </code>
 * </p>
 *
 * @author Kimi Liu
 * @version 3.0.9
 * @since JDK 1.8
 */
public class MultilineRecursiveToStringStyle extends RecursiveToStringStyle {

    /**
     * Required for serialization support.
     *
     * @see java.io.Serializable
     */
    private static final long serialVersionUID = 1L;

    /**
     * Indenting of inner lines.
     */
    private static final int INDENT = 2;

    /**
     * Current indenting.
     */
    private int spaces = 2;

    /**
     * Constructor.
     */
    public MultilineRecursiveToStringStyle() {
        super();
        resetIndent();
    }

    /**
     * Resets the fields responsible for the line breaks and indenting.
     * Must be invoked after changing the {@link #spaces} value.
     */
    private void resetIndent() {
        setArrayStart("{" + System.lineSeparator() + spacer(spaces));
        setArraySeparator("," + System.lineSeparator() + spacer(spaces));
        setArrayEnd(System.lineSeparator() + spacer(spaces - INDENT) + "}");

        setContentStart("[" + System.lineSeparator() + spacer(spaces));
        setFieldSeparator("," + System.lineSeparator() + spacer(spaces));
        setContentEnd(System.lineSeparator() + spacer(spaces - INDENT) + "]");
    }

    /**
     * Creates a StringBuilder responsible for the indenting.
     *
     * @param spaces how far to indent
     * @return a StringBuilder with {spaces} leading space characters.
     */
    private StringBuilder spacer(final int spaces) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < spaces; i++) {
            sb.append(" ");
        }
        return sb;
    }

    @Override
    public void appendDetail(final StringBuffer buffer, final String fieldName, final Object value) {
        if (!ClassUtils.isPrimitiveWrapper(value.getClass()) && !String.class.equals(value.getClass())
                && accept(value.getClass())) {
            spaces += INDENT;
            resetIndent();
            buffer.append(ReflectionToStringBuilder.toString(value, this));
            spaces -= INDENT;
            resetIndent();
        } else {
            super.appendDetail(buffer, fieldName, value);
        }
    }

    @Override
    protected void appendDetail(final StringBuffer buffer, final String fieldName, final Object[] array) {
        spaces += INDENT;
        resetIndent();
        super.appendDetail(buffer, fieldName, array);
        spaces -= INDENT;
        resetIndent();
    }

    @Override
    protected void reflectionAppendArrayDetail(final StringBuffer buffer, final String fieldName, final Object array) {
        spaces += INDENT;
        resetIndent();
        super.reflectionAppendArrayDetail(buffer, fieldName, array);
        spaces -= INDENT;
        resetIndent();
    }

    @Override
    protected void appendDetail(final StringBuffer buffer, final String fieldName, final long[] array) {
        spaces += INDENT;
        resetIndent();
        super.appendDetail(buffer, fieldName, array);
        spaces -= INDENT;
        resetIndent();
    }

    @Override
    protected void appendDetail(final StringBuffer buffer, final String fieldName, final int[] array) {
        spaces += INDENT;
        resetIndent();
        super.appendDetail(buffer, fieldName, array);
        spaces -= INDENT;
        resetIndent();
    }

    @Override
    protected void appendDetail(final StringBuffer buffer, final String fieldName, final short[] array) {
        spaces += INDENT;
        resetIndent();
        super.appendDetail(buffer, fieldName, array);
        spaces -= INDENT;
        resetIndent();
    }

    @Override
    protected void appendDetail(final StringBuffer buffer, final String fieldName, final byte[] array) {
        spaces += INDENT;
        resetIndent();
        super.appendDetail(buffer, fieldName, array);
        spaces -= INDENT;
        resetIndent();
    }

    @Override
    protected void appendDetail(final StringBuffer buffer, final String fieldName, final char[] array) {
        spaces += INDENT;
        resetIndent();
        super.appendDetail(buffer, fieldName, array);
        spaces -= INDENT;
        resetIndent();
    }

    @Override
    protected void appendDetail(final StringBuffer buffer, final String fieldName, final double[] array) {
        spaces += INDENT;
        resetIndent();
        super.appendDetail(buffer, fieldName, array);
        spaces -= INDENT;
        resetIndent();
    }

    @Override
    protected void appendDetail(final StringBuffer buffer, final String fieldName, final float[] array) {
        spaces += INDENT;
        resetIndent();
        super.appendDetail(buffer, fieldName, array);
        spaces -= INDENT;
        resetIndent();
    }

    @Override
    protected void appendDetail(final StringBuffer buffer, final String fieldName, final boolean[] array) {
        spaces += INDENT;
        resetIndent();
        super.appendDetail(buffer, fieldName, array);
        spaces -= INDENT;
        resetIndent();
    }

}
