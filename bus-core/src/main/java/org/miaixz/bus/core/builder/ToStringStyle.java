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

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.toolkit.ClassKit;
import org.miaixz.bus.core.toolkit.EscapeKit;
import org.miaixz.bus.core.toolkit.ObjectKit;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * <p>Controls <code>String</code> formatting for {@link ToStringBuilder}.
 * The main public interface is always via <code>ToStringBuilder</code>.</p>
 * <p>
 * 控制<code>String</code>格式{@link ToStringBuilder}.
 * 主公共接口总是通过<code>ToStringBuilder</code>
 *
 * <p>
 * 这些类将被用作单例(Singletons)没有必要每次都实例化新样式
 * 程序通常会在这个类上使用一个预定义的常量。或者，可以使用
 * {@link StandardToStringStyle}类来设置各个设置。因此，
 * 大多数样式不需要子类化就可以实现
 * </p>
 *
 * <pre>
 * public class MyStyle extends ToStringStyle {
 *   protected void appendDetail(StringBuffer buffer, String fieldName, Object value) {
 *     if (value instanceof Date) {
 *       value = new SimpleDateFormat("yyyy-MM-dd").format(value);
 *     }
 *     buffer.append(value);
 *   }
 * }
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class ToStringStyle implements Serializable {

    /**
     * 默认的toString样式
     *
     * <pre>
     * Person@182f0db[name=John Doe,age=33,smoker=false]
     * </pre>
     */
    public static final ToStringStyle DEFAULT_STYLE = new DefaultToStringStyle();
    /**
     * 多行toString样式
     *
     * <pre>
     * Person@182f0db[
     *   name=John Doe
     *   age=33
     *   smoker=false
     * ]
     * </pre>
     */
    public static final ToStringStyle MULTI_LINE_STYLE = new MultiLineToStringStyle();
    /**
     * 无字段名toString样式
     *
     * <pre>
     * Person@182f0db[John Doe,33,false]
     * </pre>
     */
    public static final ToStringStyle NO_FIELD_NAMES_STYLE = new NoFieldNameToStringStyle();
    /**
     * 短前缀toString样式
     *
     * <pre>
     * Person[name=John Doe,age=33,smoker=false]
     * </pre>
     */
    public static final ToStringStyle SHORT_PREFIX_STYLE = new ShortPrefixToStringStyle();
    /**
     * 简单的toString样式
     *
     * <pre>
     * John Doe,33,false
     * </pre>
     */
    public static final ToStringStyle SIMPLE_STYLE = new SimpleToStringStyle();
    /**
     * 没有类名的toString样式
     *
     * <pre>
     * [name=John Doe,age=33,smoker=false]
     * </pre>
     */
    public static final ToStringStyle NO_CLASS_NAME_STYLE = new NoClassNameToStringStyle();
    /**
     * JSON toString样式
     *
     * <pre>
     * {"name": "John Doe", "age": 33, "smoker": true}
     * </pre>
     */
    public static final ToStringStyle JSON_STYLE = new JsonToStringStyle();
    private static final long serialVersionUID = 1L;
    /**
     * reflectionToString方法用于检测循环对象引用和避免无限循环的对象注册表.
     */
    private static final ThreadLocal<WeakHashMap<Object, Object>> REGISTRY =
            new ThreadLocal<>();
    /**
     * 是否使用字段名，默认是true.
     */
    private boolean useFieldNames = true;
    /**
     * 是否使用类名，默认为true
     */
    private boolean useClassName = true;
    /**
     * 是否使用简短的类名，默认是false
     */
    private boolean useShortClassName = false;
    /**
     * 是否使用标识哈希码，默认为true.
     */
    private boolean useIdentityHashCode = true;
    /**
     * 内容开始 <code>'['</code>.
     */
    private String contentStart = Symbol.BRACKET_LEFT;
    /**
     * 内容结束 <code>']'</code>.
     */
    private String contentEnd = Symbol.BRACKET_RIGHT;
    /**
     * 字段名值分隔符 <code>'='</code>.
     */
    private String fieldNameValueSeparator = Symbol.EQUAL;
    /**
     * 是否应在任何其他字段之前添加字段分隔符.
     */
    private boolean fieldSeparatorAtStart = false;
    /**
     * 是否应在任何其他字段之后添加字段分隔符.
     */
    private boolean fieldSeparatorAtEnd = false;
    /**
     * 字段分隔符 <code>,</code>.
     */
    private String fieldSeparator = Symbol.COMMA;
    /**
     * 数字开始 <code>{</code>.
     */
    private String arrayStart = Symbol.BRACE_LEFT;
    /**
     * 数字分隔符 <code>,</code>.
     */
    private String arraySeparator = Symbol.COMMA;
    /**
     * 数组内容的详细信息t.
     */
    private boolean arrayContentDetail = true;
    /**
     * 数组结束 <code>}</code>.
     */
    private String arrayEnd = Symbol.BRACE_RIGHT;
    /**
     * 当fullDetail为null时使用的值，默认值true
     */
    private boolean defaultFullDetail = true;
    /**
     * The <code>null</code> text <code>'&lt;null&gt;'</code>.
     */
    private String nullText = "<null>";
    /**
     * 摘要大小文本开始 <code>'&lt;size'</code>.
     */
    private String sizeStartText = "<size=";
    /**
     * 摘要大小文本结束 <code>'&gt;'</code>.
     */
    private String sizeEndText = Symbol.GT;
    /**
     * 摘要大小文本开始 <code>'&lt;'</code>.
     */
    private String summaryObjectStartText = Symbol.LT;
    /**
     * 摘要大小文本结束 <code>'&gt;'</code>.
     */
    private String summaryObjectEndText = Symbol.GT;

    protected ToStringStyle() {
        super();
    }

    static Map<Object, Object> getRegistry() {
        return REGISTRY.get();
    }

    static boolean isRegistered(final Object value) {
        final Map<Object, Object> m = getRegistry();
        return null != m && m.containsKey(value);
    }

    static void register(final Object value) {
        if (null != value) {
            final Map<Object, Object> m = getRegistry();
            if (null == m) {
                REGISTRY.set(new WeakHashMap<>());
            }
            getRegistry().put(value, null);
        }
    }

    static void unregister(final Object value) {
        if (null != value) {
            final Map<Object, Object> m = getRegistry();
            if (null != m) {
                m.remove(value);
                if (m.isEmpty()) {
                    REGISTRY.remove();
                }
            }
        }
    }

    /**
     * <p>Append to the <code>toString</code> the superclass toString.</p>
     * <p>NOTE: It assumes that the toString has been created from the same ToStringStyle. </p>
     *
     * <p>A <code>null</code> <code>superToString</code> is ignored.</p>
     *
     * @param buffer        the <code>StringBuffer</code> to populate
     * @param superToString the <code>super.toString()</code>
     */
    public void appendSuper(final StringBuffer buffer, final String superToString) {
        appendToString(buffer, superToString);
    }

    /**
     * <p>Append to the <code>toString</code> another toString.</p>
     * <p>NOTE: It assumes that the toString has been created from the same ToStringStyle. </p>
     *
     * <p>A <code>null</code> <code>toString</code> is ignored.</p>
     *
     * @param buffer   the <code>StringBuffer</code> to populate
     * @param toString the additional <code>toString</code>
     */
    public void appendToString(final StringBuffer buffer, final String toString) {
        if (null != toString) {
            final int pos1 = toString.indexOf(contentStart) + contentStart.length();
            final int pos2 = toString.lastIndexOf(contentEnd);
            if (pos1 != pos2 && pos1 >= 0 && pos2 >= 0) {
                if (fieldSeparatorAtStart) {
                    removeLastFieldSeparator(buffer);
                }
                buffer.append(toString, pos1, pos2);
                appendFieldSeparator(buffer);
            }
        }
    }

    /**
     * <p>Append to the <code>toString</code> the start of data indicator.</p>
     *
     * @param buffer the <code>StringBuffer</code> to populate
     * @param object the <code>Object</code> to build a <code>toString</code> for
     */
    public void appendStart(final StringBuffer buffer, final Object object) {
        if (null != object) {
            appendClassName(buffer, object);
            appendIdentityHashCode(buffer, object);
            appendContentStart(buffer);
            if (fieldSeparatorAtStart) {
                appendFieldSeparator(buffer);
            }
        }
    }

    /**
     * <p>Append to the <code>toString</code> the end of data indicator.</p>
     *
     * @param buffer the <code>StringBuffer</code> to populate
     * @param object the <code>Object</code> to build a
     *               <code>toString</code> for.
     */
    public void appendEnd(final StringBuffer buffer, final Object object) {
        if (!this.fieldSeparatorAtEnd) {
            removeLastFieldSeparator(buffer);
        }
        appendContentEnd(buffer);
        unregister(object);
    }

    /**
     * <p>Remove the last field separator from the buffer.</p>
     *
     * @param buffer the <code>StringBuffer</code> to populate
     */
    protected void removeLastFieldSeparator(final StringBuffer buffer) {
        final int len = buffer.length();
        final int sepLen = fieldSeparator.length();
        if (len > 0 && sepLen > 0 && len >= sepLen) {
            boolean match = true;
            for (int i = 0; i < sepLen; i++) {
                if (buffer.charAt(len - 1 - i) != fieldSeparator.charAt(sepLen - 1 - i)) {
                    match = false;
                    break;
                }
            }
            if (match) {
                buffer.setLength(len - sepLen);
            }
        }
    }


    /**
     * <p>Append to the <code>toString</code> an <code>Object</code>
     * value, printing the full <code>toString</code> of the
     * <code>Object</code> passed in.</p>
     *
     * @param buffer     the <code>StringBuffer</code> to populate
     * @param fieldName  the field name
     * @param value      the value to add to the <code>toString</code>
     * @param fullDetail <code>true</code> for detail, <code>false</code>
     *                   for summary info, <code>null</code> for style decides
     */
    public void append(final StringBuffer buffer, final String fieldName, final Object value, final Boolean fullDetail) {
        appendFieldStart(buffer, fieldName);

        if (null == value) {
            appendNullText(buffer, fieldName);

        } else {
            appendInternal(buffer, fieldName, value, isFullDetail(fullDetail));
        }

        appendFieldEnd(buffer, fieldName);
    }

    /**
     * <p>Append to the <code>toString</code> an <code>Object</code>,
     * correctly interpreting its type.</p>
     *
     * <p>This method performs the main lookup by Class type to correctly
     * route arrays, <code>Collections</code>, <code>Maps</code> and
     * <code>Objects</code> to the appropriate method.</p>
     *
     * <p>Either detail or summary views can be specified.</p>
     *
     * <p>If a cycle is detected, an object will be appended with the
     * <code>Object.toString()</code> format.</p>
     *
     * @param buffer    the <code>StringBuffer</code> to populate
     * @param fieldName the field name, typically not used as already appended
     * @param value     the value to add to the <code>toString</code>,
     *                  not <code>null</code>
     * @param detail    output detail or not
     */
    protected void appendInternal(final StringBuffer buffer, final String fieldName, final Object value, final boolean detail) {
        if (isRegistered(value)
                && !(value instanceof Number || value instanceof Boolean || value instanceof Character)) {
            appendCyclicObject(buffer, fieldName, value);
            return;
        }

        register(value);

        try {
            if (value instanceof Collection<?>) {
                if (detail) {
                    appendDetail(buffer, fieldName, (Collection<?>) value);
                } else {
                    appendSummarySize(buffer, fieldName, ((Collection<?>) value).size());
                }

            } else if (value instanceof Map<?, ?>) {
                if (detail) {
                    appendDetail(buffer, fieldName, (Map<?, ?>) value);
                } else {
                    appendSummarySize(buffer, fieldName, ((Map<?, ?>) value).size());
                }

            } else if (value instanceof long[]) {
                if (detail) {
                    appendDetail(buffer, fieldName, (long[]) value);
                } else {
                    appendSummary(buffer, fieldName, (long[]) value);
                }

            } else if (value instanceof int[]) {
                if (detail) {
                    appendDetail(buffer, fieldName, (int[]) value);
                } else {
                    appendSummary(buffer, fieldName, (int[]) value);
                }

            } else if (value instanceof short[]) {
                if (detail) {
                    appendDetail(buffer, fieldName, (short[]) value);
                } else {
                    appendSummary(buffer, fieldName, (short[]) value);
                }

            } else if (value instanceof byte[]) {
                if (detail) {
                    appendDetail(buffer, fieldName, (byte[]) value);
                } else {
                    appendSummary(buffer, fieldName, (byte[]) value);
                }

            } else if (value instanceof char[]) {
                if (detail) {
                    appendDetail(buffer, fieldName, (char[]) value);
                } else {
                    appendSummary(buffer, fieldName, (char[]) value);
                }

            } else if (value instanceof double[]) {
                if (detail) {
                    appendDetail(buffer, fieldName, (double[]) value);
                } else {
                    appendSummary(buffer, fieldName, (double[]) value);
                }

            } else if (value instanceof float[]) {
                if (detail) {
                    appendDetail(buffer, fieldName, (float[]) value);
                } else {
                    appendSummary(buffer, fieldName, (float[]) value);
                }

            } else if (value instanceof boolean[]) {
                if (detail) {
                    appendDetail(buffer, fieldName, (boolean[]) value);
                } else {
                    appendSummary(buffer, fieldName, (boolean[]) value);
                }

            } else if (value.getClass().isArray()) {
                if (detail) {
                    appendDetail(buffer, fieldName, (Object[]) value);
                } else {
                    appendSummary(buffer, fieldName, (Object[]) value);
                }

            } else {
                if (detail) {
                    appendDetail(buffer, fieldName, value);
                } else {
                    appendSummary(buffer, fieldName, value);
                }
            }
        } finally {
            unregister(value);
        }
    }

    /**
     * <p>Append to the <code>toString</code> an <code>Object</code>
     * value that has been detected to participate in a cycle. This
     * implementation will print the standard string value of the value.</p>
     *
     * @param buffer    the <code>StringBuffer</code> to populate
     * @param fieldName the field name, typically not used as already appended
     * @param value     the value to add to the <code>toString</code>,
     *                  not <code>null</code>
     */
    protected void appendCyclicObject(final StringBuffer buffer, final String fieldName, final Object value) {
        ObjectKit.identityToString(buffer, value);
    }

    /**
     * <p>Append to the <code>toString</code> an <code>Object</code>
     * value, printing the full detail of the <code>Object</code>.</p>
     *
     * @param buffer    the <code>StringBuffer</code> to populate
     * @param fieldName the field name, typically not used as already appended
     * @param value     the value to add to the <code>toString</code>,
     *                  not <code>null</code>
     */
    protected void appendDetail(final StringBuffer buffer, final String fieldName, final Object value) {
        buffer.append(value);
    }

    /**
     * <p>Append to the <code>toString</code> a <code>Collection</code>.</p>
     *
     * @param buffer    the <code>StringBuffer</code> to populate
     * @param fieldName the field name, typically not used as already appended
     * @param coll      the <code>Collection</code> to add to the
     *                  <code>toString</code>, not <code>null</code>
     */
    protected void appendDetail(final StringBuffer buffer, final String fieldName, final Collection<?> coll) {
        buffer.append(coll);
    }

    /**
     * <p>Append to the <code>toString</code> a <code>Map</code>.</p>
     *
     * @param buffer    the <code>StringBuffer</code> to populate
     * @param fieldName the field name, typically not used as already appended
     * @param map       the <code>Map</code> to add to the <code>toString</code>,
     *                  not <code>null</code>
     */
    protected void appendDetail(final StringBuffer buffer, final String fieldName, final Map<?, ?> map) {
        buffer.append(map);
    }

    /**
     * <p>Append to the <code>toString</code> an <code>Object</code>
     * value, printing a summary of the <code>Object</code>.</P>
     *
     * @param buffer    the <code>StringBuffer</code> to populate
     * @param fieldName the field name, typically not used as already appended
     * @param value     the value to add to the <code>toString</code>,
     *                  not <code>null</code>
     */
    protected void appendSummary(final StringBuffer buffer, final String fieldName, final Object value) {
        buffer.append(summaryObjectStartText);
        buffer.append(getShortClassName(value.getClass()));
        buffer.append(summaryObjectEndText);
    }


    /**
     * <p>Append to the <code>toString</code> a <code>long</code>
     * value.</p>
     *
     * @param buffer    the <code>StringBuffer</code> to populate
     * @param fieldName the field name
     * @param value     the value to add to the <code>toString</code>
     */
    public void append(final StringBuffer buffer, final String fieldName, final long value) {
        appendFieldStart(buffer, fieldName);
        appendDetail(buffer, fieldName, value);
        appendFieldEnd(buffer, fieldName);
    }

    /**
     * <p>Append to the <code>toString</code> a <code>long</code>
     * value.</p>
     *
     * @param buffer    the <code>StringBuffer</code> to populate
     * @param fieldName the field name, typically not used as already appended
     * @param value     the value to add to the <code>toString</code>
     */
    protected void appendDetail(final StringBuffer buffer, final String fieldName, final long value) {
        buffer.append(value);
    }


    /**
     * <p>Append to the <code>toString</code> an <code>int</code>
     * value.</p>
     *
     * @param buffer    the <code>StringBuffer</code> to populate
     * @param fieldName the field name
     * @param value     the value to add to the <code>toString</code>
     */
    public void append(final StringBuffer buffer, final String fieldName, final int value) {
        appendFieldStart(buffer, fieldName);
        appendDetail(buffer, fieldName, value);
        appendFieldEnd(buffer, fieldName);
    }

    /**
     * <p>Append to the <code>toString</code> an <code>int</code>
     * value.</p>
     *
     * @param buffer    the <code>StringBuffer</code> to populate
     * @param fieldName the field name, typically not used as already appended
     * @param value     the value to add to the <code>toString</code>
     */
    protected void appendDetail(final StringBuffer buffer, final String fieldName, final int value) {
        buffer.append(value);
    }


    /**
     * <p>Append to the <code>toString</code> a <code>short</code>
     * value.</p>
     *
     * @param buffer    the <code>StringBuffer</code> to populate
     * @param fieldName the field name
     * @param value     the value to add to the <code>toString</code>
     */
    public void append(final StringBuffer buffer, final String fieldName, final short value) {
        appendFieldStart(buffer, fieldName);
        appendDetail(buffer, fieldName, value);
        appendFieldEnd(buffer, fieldName);
    }

    /**
     * <p>Append to the <code>toString</code> a <code>short</code>
     * value.</p>
     *
     * @param buffer    the <code>StringBuffer</code> to populate
     * @param fieldName the field name, typically not used as already appended
     * @param value     the value to add to the <code>toString</code>
     */
    protected void appendDetail(final StringBuffer buffer, final String fieldName, final short value) {
        buffer.append(value);
    }


    /**
     * <p>Append to the <code>toString</code> a <code>byte</code>
     * value.</p>
     *
     * @param buffer    the <code>StringBuffer</code> to populate
     * @param fieldName the field name
     * @param value     the value to add to the <code>toString</code>
     */
    public void append(final StringBuffer buffer, final String fieldName, final byte value) {
        appendFieldStart(buffer, fieldName);
        appendDetail(buffer, fieldName, value);
        appendFieldEnd(buffer, fieldName);
    }

    /**
     * <p>Append to the <code>toString</code> a <code>byte</code>
     * value.</p>
     *
     * @param buffer    the <code>StringBuffer</code> to populate
     * @param fieldName the field name, typically not used as already appended
     * @param value     the value to add to the <code>toString</code>
     */
    protected void appendDetail(final StringBuffer buffer, final String fieldName, final byte value) {
        buffer.append(value);
    }


    /**
     * <p>Append to the <code>toString</code> a <code>char</code>
     * value.</p>
     *
     * @param buffer    the <code>StringBuffer</code> to populate
     * @param fieldName the field name
     * @param value     the value to add to the <code>toString</code>
     */
    public void append(final StringBuffer buffer, final String fieldName, final char value) {
        appendFieldStart(buffer, fieldName);
        appendDetail(buffer, fieldName, value);
        appendFieldEnd(buffer, fieldName);
    }

    /**
     * <p>Append to the <code>toString</code> a <code>char</code>
     * value.</p>
     *
     * @param buffer    the <code>StringBuffer</code> to populate
     * @param fieldName the field name, typically not used as already appended
     * @param value     the value to add to the <code>toString</code>
     */
    protected void appendDetail(final StringBuffer buffer, final String fieldName, final char value) {
        buffer.append(value);
    }


    /**
     * <p>Append to the <code>toString</code> a <code>double</code>
     * value.</p>
     *
     * @param buffer    the <code>StringBuffer</code> to populate
     * @param fieldName the field name
     * @param value     the value to add to the <code>toString</code>
     */
    public void append(final StringBuffer buffer, final String fieldName, final double value) {
        appendFieldStart(buffer, fieldName);
        appendDetail(buffer, fieldName, value);
        appendFieldEnd(buffer, fieldName);
    }

    /**
     * <p>Append to the <code>toString</code> a <code>double</code>
     * value.</p>
     *
     * @param buffer    the <code>StringBuffer</code> to populate
     * @param fieldName the field name, typically not used as already appended
     * @param value     the value to add to the <code>toString</code>
     */
    protected void appendDetail(final StringBuffer buffer, final String fieldName, final double value) {
        buffer.append(value);
    }


    /**
     * <p>Append to the <code>toString</code> a <code>float</code>
     * value.</p>
     *
     * @param buffer    the <code>StringBuffer</code> to populate
     * @param fieldName the field name
     * @param value     the value to add to the <code>toString</code>
     */
    public void append(final StringBuffer buffer, final String fieldName, final float value) {
        appendFieldStart(buffer, fieldName);
        appendDetail(buffer, fieldName, value);
        appendFieldEnd(buffer, fieldName);
    }

    /**
     * <p>Append to the <code>toString</code> a <code>float</code>
     * value.</p>
     *
     * @param buffer    the <code>StringBuffer</code> to populate
     * @param fieldName the field name, typically not used as already appended
     * @param value     the value to add to the <code>toString</code>
     */
    protected void appendDetail(final StringBuffer buffer, final String fieldName, final float value) {
        buffer.append(value);
    }


    /**
     * <p>Append to the <code>toString</code> a <code>boolean</code>
     * value.</p>
     *
     * @param buffer    the <code>StringBuffer</code> to populate
     * @param fieldName the field name
     * @param value     the value to add to the <code>toString</code>
     */
    public void append(final StringBuffer buffer, final String fieldName, final boolean value) {
        appendFieldStart(buffer, fieldName);
        appendDetail(buffer, fieldName, value);
        appendFieldEnd(buffer, fieldName);
    }

    /**
     * <p>Append to the <code>toString</code> a <code>boolean</code>
     * value.</p>
     *
     * @param buffer    the <code>StringBuffer</code> to populate
     * @param fieldName the field name, typically not used as already appended
     * @param value     the value to add to the <code>toString</code>
     */
    protected void appendDetail(final StringBuffer buffer, final String fieldName, final boolean value) {
        buffer.append(value);
    }

    /**
     * <p>Append to the <code>toString</code> an <code>Object</code>
     * array.</p>
     *
     * @param buffer     the <code>StringBuffer</code> to populate
     * @param fieldName  the field name
     * @param array      the array to add to the toString
     * @param fullDetail <code>true</code> for detail, <code>false</code>
     *                   for summary info, <code>null</code> for style decides
     */
    public void append(final StringBuffer buffer, final String fieldName, final Object[] array, final Boolean fullDetail) {
        appendFieldStart(buffer, fieldName);

        if (null == array) {
            appendNullText(buffer, fieldName);

        } else if (isFullDetail(fullDetail)) {
            appendDetail(buffer, fieldName, array);

        } else {
            appendSummary(buffer, fieldName, array);
        }

        appendFieldEnd(buffer, fieldName);
    }


    /**
     * <p>Append to the <code>toString</code> the detail of an
     * <code>Object</code> array.</p>
     *
     * @param buffer    the <code>StringBuffer</code> to populate
     * @param fieldName the field name, typically not used as already appended
     * @param array     the array to add to the <code>toString</code>,
     *                  not <code>null</code>
     */
    protected void appendDetail(final StringBuffer buffer, final String fieldName, final Object[] array) {
        buffer.append(arrayStart);
        for (int i = 0; i < array.length; i++) {
            final Object item = array[i];
            if (i > 0) {
                buffer.append(arraySeparator);
            }
            if (null == item) {
                appendNullText(buffer, fieldName);

            } else {
                appendInternal(buffer, fieldName, item, arrayContentDetail);
            }
        }
        buffer.append(arrayEnd);
    }

    /**
     * <p>Append to the <code>toString</code> the detail of an array type.</p>
     *
     * @param buffer    the <code>StringBuffer</code> to populate
     * @param fieldName the field name, typically not used as already appended
     * @param array     the array to add to the <code>toString</code>,
     *                  not <code>null</code>
     */
    protected void reflectionAppendArrayDetail(final StringBuffer buffer, final String fieldName, final Object array) {
        buffer.append(arrayStart);
        final int length = Array.getLength(array);
        for (int i = 0; i < length; i++) {
            final Object item = Array.get(array, i);
            if (i > 0) {
                buffer.append(arraySeparator);
            }
            if (null == item) {
                appendNullText(buffer, fieldName);

            } else {
                appendInternal(buffer, fieldName, item, arrayContentDetail);
            }
        }
        buffer.append(arrayEnd);
    }

    /**
     * <p>Append to the <code>toString</code> a summary of an
     * <code>Object</code> array.</p>
     *
     * @param buffer    the <code>StringBuffer</code> to populate
     * @param fieldName the field name, typically not used as already appended
     * @param array     the array to add to the <code>toString</code>,
     *                  not <code>null</code>
     */
    protected void appendSummary(final StringBuffer buffer, final String fieldName, final Object[] array) {
        appendSummarySize(buffer, fieldName, array.length);
    }


    /**
     * <p>Append to the <code>toString</code> a <code>long</code>
     * array.</p>
     *
     * @param buffer     the <code>StringBuffer</code> to populate
     * @param fieldName  the field name
     * @param array      the array to add to the <code>toString</code>
     * @param fullDetail <code>true</code> for detail, <code>false</code>
     *                   for summary info, <code>null</code> for style decides
     */
    public void append(final StringBuffer buffer, final String fieldName, final long[] array, final Boolean fullDetail) {
        appendFieldStart(buffer, fieldName);

        if (null == array) {
            appendNullText(buffer, fieldName);

        } else if (isFullDetail(fullDetail)) {
            appendDetail(buffer, fieldName, array);

        } else {
            appendSummary(buffer, fieldName, array);
        }

        appendFieldEnd(buffer, fieldName);
    }

    /**
     * <p>Append to the <code>toString</code> the detail of a
     * <code>long</code> array.</p>
     *
     * @param buffer    the <code>StringBuffer</code> to populate
     * @param fieldName the field name, typically not used as already appended
     * @param array     the array to add to the <code>toString</code>,
     *                  not <code>null</code>
     */
    protected void appendDetail(final StringBuffer buffer, final String fieldName, final long[] array) {
        buffer.append(arrayStart);
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                buffer.append(arraySeparator);
            }
            appendDetail(buffer, fieldName, array[i]);
        }
        buffer.append(arrayEnd);
    }

    /**
     * <p>Append to the <code>toString</code> a summary of a
     * <code>long</code> array.</p>
     *
     * @param buffer    the <code>StringBuffer</code> to populate
     * @param fieldName the field name, typically not used as already appended
     * @param array     the array to add to the <code>toString</code>,
     *                  not <code>null</code>
     */
    protected void appendSummary(final StringBuffer buffer, final String fieldName, final long[] array) {
        appendSummarySize(buffer, fieldName, array.length);
    }


    /**
     * <p>Append to the <code>toString</code> an <code>int</code>
     * array.</p>
     *
     * @param buffer     the <code>StringBuffer</code> to populate
     * @param fieldName  the field name
     * @param array      the array to add to the <code>toString</code>
     * @param fullDetail <code>true</code> for detail, <code>false</code>
     *                   for summary info, <code>null</code> for style decides
     */
    public void append(final StringBuffer buffer, final String fieldName, final int[] array, final Boolean fullDetail) {
        appendFieldStart(buffer, fieldName);

        if (null == array) {
            appendNullText(buffer, fieldName);

        } else if (isFullDetail(fullDetail)) {
            appendDetail(buffer, fieldName, array);

        } else {
            appendSummary(buffer, fieldName, array);
        }

        appendFieldEnd(buffer, fieldName);
    }

    /**
     * <p>Append to the <code>toString</code> the detail of an
     * <code>int</code> array.</p>
     *
     * @param buffer    the <code>StringBuffer</code> to populate
     * @param fieldName the field name, typically not used as already appended
     * @param array     the array to add to the <code>toString</code>,
     *                  not <code>null</code>
     */
    protected void appendDetail(final StringBuffer buffer, final String fieldName, final int[] array) {
        buffer.append(arrayStart);
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                buffer.append(arraySeparator);
            }
            appendDetail(buffer, fieldName, array[i]);
        }
        buffer.append(arrayEnd);
    }

    /**
     * <p>Append to the <code>toString</code> a summary of an
     * <code>int</code> array.</p>
     *
     * @param buffer    the <code>StringBuffer</code> to populate
     * @param fieldName the field name, typically not used as already appended
     * @param array     the array to add to the <code>toString</code>,
     *                  not <code>null</code>
     */
    protected void appendSummary(final StringBuffer buffer, final String fieldName, final int[] array) {
        appendSummarySize(buffer, fieldName, array.length);
    }


    /**
     * <p>Append to the <code>toString</code> a <code>short</code>
     * array.</p>
     *
     * @param buffer     the <code>StringBuffer</code> to populate
     * @param fieldName  the field name
     * @param array      the array to add to the <code>toString</code>
     * @param fullDetail <code>true</code> for detail, <code>false</code>
     *                   for summary info, <code>null</code> for style decides
     */
    public void append(final StringBuffer buffer, final String fieldName, final short[] array, final Boolean fullDetail) {
        appendFieldStart(buffer, fieldName);

        if (null == array) {
            appendNullText(buffer, fieldName);

        } else if (isFullDetail(fullDetail)) {
            appendDetail(buffer, fieldName, array);

        } else {
            appendSummary(buffer, fieldName, array);
        }

        appendFieldEnd(buffer, fieldName);
    }

    /**
     * <p>Append to the <code>toString</code> the detail of a
     * <code>short</code> array.</p>
     *
     * @param buffer    the <code>StringBuffer</code> to populate
     * @param fieldName the field name, typically not used as already appended
     * @param array     the array to add to the <code>toString</code>,
     *                  not <code>null</code>
     */
    protected void appendDetail(final StringBuffer buffer, final String fieldName, final short[] array) {
        buffer.append(arrayStart);
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                buffer.append(arraySeparator);
            }
            appendDetail(buffer, fieldName, array[i]);
        }
        buffer.append(arrayEnd);
    }

    /**
     * <p>Append to the <code>toString</code> a summary of a
     * <code>short</code> array.</p>
     *
     * @param buffer    the <code>StringBuffer</code> to populate
     * @param fieldName the field name, typically not used as already appended
     * @param array     the array to add to the <code>toString</code>,
     *                  not <code>null</code>
     */
    protected void appendSummary(final StringBuffer buffer, final String fieldName, final short[] array) {
        appendSummarySize(buffer, fieldName, array.length);
    }


    /**
     * <p>Append to the <code>toString</code> a <code>byte</code>
     * array.</p>
     *
     * @param buffer     the <code>StringBuffer</code> to populate
     * @param fieldName  the field name
     * @param array      the array to add to the <code>toString</code>
     * @param fullDetail <code>true</code> for detail, <code>false</code>
     *                   for summary info, <code>null</code> for style decides
     */
    public void append(final StringBuffer buffer, final String fieldName, final byte[] array, final Boolean fullDetail) {
        appendFieldStart(buffer, fieldName);

        if (null == array) {
            appendNullText(buffer, fieldName);

        } else if (isFullDetail(fullDetail)) {
            appendDetail(buffer, fieldName, array);

        } else {
            appendSummary(buffer, fieldName, array);
        }

        appendFieldEnd(buffer, fieldName);
    }

    /**
     * <p>Append to the <code>toString</code> the detail of a
     * <code>byte</code> array.</p>
     *
     * @param buffer    the <code>StringBuffer</code> to populate
     * @param fieldName the field name, typically not used as already appended
     * @param array     the array to add to the <code>toString</code>,
     *                  not <code>null</code>
     */
    protected void appendDetail(final StringBuffer buffer, final String fieldName, final byte[] array) {
        buffer.append(arrayStart);
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                buffer.append(arraySeparator);
            }
            appendDetail(buffer, fieldName, array[i]);
        }
        buffer.append(arrayEnd);
    }

    /**
     * <p>Append to the <code>toString</code> a summary of a
     * <code>byte</code> array.</p>
     *
     * @param buffer    the <code>StringBuffer</code> to populate
     * @param fieldName the field name, typically not used as already appended
     * @param array     the array to add to the <code>toString</code>,
     *                  not <code>null</code>
     */
    protected void appendSummary(final StringBuffer buffer, final String fieldName, final byte[] array) {
        appendSummarySize(buffer, fieldName, array.length);
    }


    /**
     * <p>Append to the <code>toString</code> a <code>char</code>
     * array.</p>
     *
     * @param buffer     the <code>StringBuffer</code> to populate
     * @param fieldName  the field name
     * @param array      the array to add to the <code>toString</code>
     * @param fullDetail <code>true</code> for detail, <code>false</code>
     *                   for summary info, <code>null</code> for style decides
     */
    public void append(final StringBuffer buffer, final String fieldName, final char[] array, final Boolean fullDetail) {
        appendFieldStart(buffer, fieldName);

        if (null == array) {
            appendNullText(buffer, fieldName);

        } else if (isFullDetail(fullDetail)) {
            appendDetail(buffer, fieldName, array);

        } else {
            appendSummary(buffer, fieldName, array);
        }

        appendFieldEnd(buffer, fieldName);
    }

    /**
     * <p>Append to the <code>toString</code> the detail of a
     * <code>char</code> array.</p>
     *
     * @param buffer    the <code>StringBuffer</code> to populate
     * @param fieldName the field name, typically not used as already appended
     * @param array     the array to add to the <code>toString</code>,
     *                  not <code>null</code>
     */
    protected void appendDetail(final StringBuffer buffer, final String fieldName, final char[] array) {
        buffer.append(arrayStart);
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                buffer.append(arraySeparator);
            }
            appendDetail(buffer, fieldName, array[i]);
        }
        buffer.append(arrayEnd);
    }

    /**
     * <p>Append to the <code>toString</code> a summary of a
     * <code>char</code> array.</p>
     *
     * @param buffer    the <code>StringBuffer</code> to populate
     * @param fieldName the field name, typically not used as already appended
     * @param array     the array to add to the <code>toString</code>,
     *                  not <code>null</code>
     */
    protected void appendSummary(final StringBuffer buffer, final String fieldName, final char[] array) {
        appendSummarySize(buffer, fieldName, array.length);
    }

    /**
     * <p>Append to the <code>toString</code> a <code>double</code>
     * array.</p>
     *
     * @param buffer     the <code>StringBuffer</code> to populate
     * @param fieldName  the field name
     * @param array      the array to add to the toString
     * @param fullDetail <code>true</code> for detail, <code>false</code>
     *                   for summary info, <code>null</code> for style decides
     */
    public void append(final StringBuffer buffer, final String fieldName, final double[] array, final Boolean fullDetail) {
        appendFieldStart(buffer, fieldName);

        if (null == array) {
            appendNullText(buffer, fieldName);

        } else if (isFullDetail(fullDetail)) {
            appendDetail(buffer, fieldName, array);

        } else {
            appendSummary(buffer, fieldName, array);
        }

        appendFieldEnd(buffer, fieldName);
    }

    /**
     * <p>Append to the <code>toString</code> the detail of a
     * <code>double</code> array.</p>
     *
     * @param buffer    the <code>StringBuffer</code> to populate
     * @param fieldName the field name, typically not used as already appended
     * @param array     the array to add to the <code>toString</code>,
     *                  not <code>null</code>
     */
    protected void appendDetail(final StringBuffer buffer, final String fieldName, final double[] array) {
        buffer.append(arrayStart);
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                buffer.append(arraySeparator);
            }
            appendDetail(buffer, fieldName, array[i]);
        }
        buffer.append(arrayEnd);
    }

    /**
     * <p>Append to the <code>toString</code> a summary of a
     * <code>double</code> array.</p>
     *
     * @param buffer    the <code>StringBuffer</code> to populate
     * @param fieldName the field name, typically not used as already appended
     * @param array     the array to add to the <code>toString</code>,
     *                  not <code>null</code>
     */
    protected void appendSummary(final StringBuffer buffer, final String fieldName, final double[] array) {
        appendSummarySize(buffer, fieldName, array.length);
    }

    /**
     * <p>Append to the <code>toString</code> a <code>float</code>
     * array.</p>
     *
     * @param buffer     the <code>StringBuffer</code> to populate
     * @param fieldName  the field name
     * @param array      the array to add to the toString
     * @param fullDetail <code>true</code> for detail, <code>false</code>
     *                   for summary info, <code>null</code> for style decides
     */
    public void append(final StringBuffer buffer, final String fieldName, final float[] array, final Boolean fullDetail) {
        appendFieldStart(buffer, fieldName);

        if (null == array) {
            appendNullText(buffer, fieldName);

        } else if (isFullDetail(fullDetail)) {
            appendDetail(buffer, fieldName, array);

        } else {
            appendSummary(buffer, fieldName, array);
        }

        appendFieldEnd(buffer, fieldName);
    }

    /**
     * <p>Append to the <code>toString</code> the detail of a
     * <code>float</code> array.</p>
     *
     * @param buffer    the <code>StringBuffer</code> to populate
     * @param fieldName the field name, typically not used as already appended
     * @param array     the array to add to the <code>toString</code>,
     *                  not <code>null</code>
     */
    protected void appendDetail(final StringBuffer buffer, final String fieldName, final float[] array) {
        buffer.append(arrayStart);
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                buffer.append(arraySeparator);
            }
            appendDetail(buffer, fieldName, array[i]);
        }
        buffer.append(arrayEnd);
    }

    /**
     * <p>Append to the <code>toString</code> a summary of a
     * <code>float</code> array.</p>
     *
     * @param buffer    the <code>StringBuffer</code> to populate
     * @param fieldName the field name, typically not used as already appended
     * @param array     the array to add to the <code>toString</code>,
     *                  not <code>null</code>
     */
    protected void appendSummary(final StringBuffer buffer, final String fieldName, final float[] array) {
        appendSummarySize(buffer, fieldName, array.length);
    }


    /**
     * <p>Append to the <code>toString</code> a <code>boolean</code>
     * array.</p>
     *
     * @param buffer     the <code>StringBuffer</code> to populate
     * @param fieldName  the field name
     * @param array      the array to add to the toString
     * @param fullDetail <code>true</code> for detail, <code>false</code>
     *                   for summary info, <code>null</code> for style decides
     */
    public void append(final StringBuffer buffer, final String fieldName, final boolean[] array, final Boolean fullDetail) {
        appendFieldStart(buffer, fieldName);

        if (null == array) {
            appendNullText(buffer, fieldName);

        } else if (isFullDetail(fullDetail)) {
            appendDetail(buffer, fieldName, array);

        } else {
            appendSummary(buffer, fieldName, array);
        }

        appendFieldEnd(buffer, fieldName);
    }

    /**
     * <p>Append to the <code>toString</code> the detail of a
     * <code>boolean</code> array.</p>
     *
     * @param buffer    the <code>StringBuffer</code> to populate
     * @param fieldName the field name, typically not used as already appended
     * @param array     the array to add to the <code>toString</code>,
     *                  not <code>null</code>
     */
    protected void appendDetail(final StringBuffer buffer, final String fieldName, final boolean[] array) {
        buffer.append(arrayStart);
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                buffer.append(arraySeparator);
            }
            appendDetail(buffer, fieldName, array[i]);
        }
        buffer.append(arrayEnd);
    }

    /**
     * <p>Append to the <code>toString</code> a summary of a
     * <code>boolean</code> array.</p>
     *
     * @param buffer    the <code>StringBuffer</code> to populate
     * @param fieldName the field name, typically not used as already appended
     * @param array     the array to add to the <code>toString</code>,
     *                  not <code>null</code>
     */
    protected void appendSummary(final StringBuffer buffer, final String fieldName, final boolean[] array) {
        appendSummarySize(buffer, fieldName, array.length);
    }


    /**
     * <p>Append to the <code>toString</code> the class name.</p>
     *
     * @param buffer the <code>StringBuffer</code> to populate
     * @param object the <code>Object</code> whose name to output
     */
    protected void appendClassName(final StringBuffer buffer, final Object object) {
        if (useClassName && null != object) {
            register(object);
            if (useShortClassName) {
                buffer.append(getShortClassName(object.getClass()));
            } else {
                buffer.append(object.getClass().getName());
            }
        }
    }

    /**
     * <p>Append the {@link System#identityHashCode(Object)}.</p>
     *
     * @param buffer the <code>StringBuffer</code> to populate
     * @param object the <code>Object</code> whose id to output
     */
    protected void appendIdentityHashCode(final StringBuffer buffer, final Object object) {
        if (this.isUseIdentityHashCode() && null != object) {
            register(object);
            buffer.append(Symbol.C_AT);
            buffer.append(Integer.toHexString(System.identityHashCode(object)));
        }
    }

    /**
     * <p>Append to the <code>toString</code> the content start.</p>
     *
     * @param buffer the <code>StringBuffer</code> to populate
     */
    protected void appendContentStart(final StringBuffer buffer) {
        buffer.append(contentStart);
    }

    /**
     * <p>Append to the <code>toString</code> the content end.</p>
     *
     * @param buffer the <code>StringBuffer</code> to populate
     */
    protected void appendContentEnd(final StringBuffer buffer) {
        buffer.append(contentEnd);
    }

    /**
     * <p>Append to the <code>toString</code> an indicator for <code>null</code>.</p>
     *
     * <p>The default indicator is <code>'&lt;null&gt;'</code>.</p>
     *
     * @param buffer    the <code>StringBuffer</code> to populate
     * @param fieldName the field name, typically not used as already appended
     */
    protected void appendNullText(final StringBuffer buffer, final String fieldName) {
        buffer.append(nullText);
    }

    /**
     * <p>Append to the <code>toString</code> the field separator.</p>
     *
     * @param buffer the <code>StringBuffer</code> to populate
     */
    protected void appendFieldSeparator(final StringBuffer buffer) {
        buffer.append(fieldSeparator);
    }

    /**
     * <p>Append to the <code>toString</code> the field start.</p>
     *
     * @param buffer    the <code>StringBuffer</code> to populate
     * @param fieldName the field name
     */
    protected void appendFieldStart(final StringBuffer buffer, final String fieldName) {
        if (useFieldNames && null != fieldName) {
            buffer.append(fieldName);
            buffer.append(fieldNameValueSeparator);
        }
    }

    /**
     * <p>Append to the <code>toString</code> the field end.</p>
     *
     * @param buffer    the <code>StringBuffer</code> to populate
     * @param fieldName the field name, typically not used as already appended
     */
    protected void appendFieldEnd(final StringBuffer buffer, final String fieldName) {
        appendFieldSeparator(buffer);
    }

    /**
     * <p>Append to the <code>toString</code> a size summary.</p>
     *
     * <p>The size summary is used to summarize the contents of
     * <code>Collections</code>, <code>Maps</code> and arrays.</p>
     *
     * <p>The output consists of a prefix, the passed in size
     * and a suffix.</p>
     *
     * <p>The default format is <code>'&lt;size=n&gt;'</code>.</p>
     *
     * @param buffer    the <code>StringBuffer</code> to populate
     * @param fieldName the field name, typically not used as already appended
     * @param size      the size to append
     */
    protected void appendSummarySize(final StringBuffer buffer, final String fieldName, final int size) {
        buffer.append(sizeStartText);
        buffer.append(size);
        buffer.append(sizeEndText);
    }

    /**
     * <p>Is this field to be output in full detail.</p>
     *
     * <p>This method converts a detail request into a detail level.
     * The calling code may request full detail (<code>true</code>),
     * but a subclass might ignore that and always return
     * <code>false</code>. The calling code may pass in
     * <code>null</code> indicating that it doesn't care about
     * the detail level. In this case the default detail level is
     * used.</p>
     *
     * @param fullDetailRequest the detail level requested
     * @return whether full detail is to be shown
     */
    protected boolean isFullDetail(final Boolean fullDetailRequest) {
        if (null == fullDetailRequest) {
            return defaultFullDetail;
        }
        return fullDetailRequest.booleanValue();
    }

    /**
     * <p>Gets the short class name for a class.</p>
     *
     * <p>The short class name is the classname excluding
     * the package name.</p>
     *
     * @param cls the <code>Class</code> to get the short name of
     * @return the short name
     */
    protected String getShortClassName(final Class<?> cls) {
        return ClassKit.getShortClassName(cls);
    }

    /**
     * <p>Gets whether to use the class name.</p>
     *
     * @return the current useClassName flag
     */
    protected boolean isUseClassName() {
        return useClassName;
    }

    /**
     * <p>Sets whether to use the class name.</p>
     *
     * @param useClassName the new useClassName flag
     */
    protected void setUseClassName(final boolean useClassName) {
        this.useClassName = useClassName;
    }


    /**
     * <p>Gets whether to output short or long class names.</p>
     *
     * @return the current useShortClassName flag
     */
    protected boolean isUseShortClassName() {
        return useShortClassName;
    }

    /**
     * <p>Sets whether to output short or long class names.</p>
     *
     * @param useShortClassName the new useShortClassName flag
     */
    protected void setUseShortClassName(final boolean useShortClassName) {
        this.useShortClassName = useShortClassName;
    }


    /**
     * <p>Gets whether to use the identity hash code.</p>
     *
     * @return the current useIdentityHashCode flag
     */
    protected boolean isUseIdentityHashCode() {
        return useIdentityHashCode;
    }

    /**
     * <p>Sets whether to use the identity hash code.</p>
     *
     * @param useIdentityHashCode the new useIdentityHashCode flag
     */
    protected void setUseIdentityHashCode(final boolean useIdentityHashCode) {
        this.useIdentityHashCode = useIdentityHashCode;
    }


    /**
     * <p>Gets whether to use the field names passed in.</p>
     *
     * @return the current useFieldNames flag
     */
    protected boolean isUseFieldNames() {
        return useFieldNames;
    }

    /**
     * <p>Sets whether to use the field names passed in.</p>
     *
     * @param useFieldNames the new useFieldNames flag
     */
    protected void setUseFieldNames(final boolean useFieldNames) {
        this.useFieldNames = useFieldNames;
    }


    /**
     * <p>Gets whether to use full detail when the caller doesn't
     * specify.</p>
     *
     * @return the current defaultFullDetail flag
     */
    protected boolean isDefaultFullDetail() {
        return defaultFullDetail;
    }

    /**
     * <p>Sets whether to use full detail when the caller doesn't
     * specify.</p>
     *
     * @param defaultFullDetail the new defaultFullDetail flag
     */
    protected void setDefaultFullDetail(final boolean defaultFullDetail) {
        this.defaultFullDetail = defaultFullDetail;
    }


    /**
     * <p>Gets whether to output array content detail.</p>
     *
     * @return the current array content detail setting
     */
    protected boolean isArrayContentDetail() {
        return arrayContentDetail;
    }

    /**
     * <p>Sets whether to output array content detail.</p>
     *
     * @param arrayContentDetail the new arrayContentDetail flag
     */
    protected void setArrayContentDetail(final boolean arrayContentDetail) {
        this.arrayContentDetail = arrayContentDetail;
    }


    /**
     * <p>Gets the array start text.</p>
     *
     * @return the current array start text
     */
    protected String getArrayStart() {
        return arrayStart;
    }

    /**
     * <p>Sets the array start text.</p>
     *
     * <p><code>null</code> is accepted, but will be converted to
     * an empty String.</p>
     *
     * @param arrayStart the new array start text
     */
    protected void setArrayStart(String arrayStart) {
        if (null == arrayStart) {
            arrayStart = Normal.EMPTY;
        }
        this.arrayStart = arrayStart;
    }


    /**
     * <p>Gets the array end text.</p>
     *
     * @return the current array end text
     */
    protected String getArrayEnd() {
        return arrayEnd;
    }

    /**
     * <p>Sets the array end text.</p>
     *
     * <p><code>null</code> is accepted, but will be converted to
     * an empty String.</p>
     *
     * @param arrayEnd the new array end text
     */
    protected void setArrayEnd(String arrayEnd) {
        if (null == arrayEnd) {
            arrayEnd = Normal.EMPTY;
        }
        this.arrayEnd = arrayEnd;
    }

    /**
     * <p>Gets the array separator text.</p>
     *
     * @return the current array separator text
     */
    protected String getArraySeparator() {
        return arraySeparator;
    }

    /**
     * <p>Sets the array separator text.</p>
     *
     * <p><code>null</code> is accepted, but will be converted to
     * an empty String.</p>
     *
     * @param arraySeparator the new array separator text
     */
    protected void setArraySeparator(String arraySeparator) {
        if (null == arraySeparator) {
            arraySeparator = Normal.EMPTY;
        }
        this.arraySeparator = arraySeparator;
    }

    /**
     * <p>Gets the content start text.</p>
     *
     * @return the current content start text
     */
    protected String getContentStart() {
        return contentStart;
    }

    /**
     * <p>Sets the content start text.</p>
     *
     * <p><code>null</code> is accepted, but will be converted to
     * an empty String.</p>
     *
     * @param contentStart the new content start text
     */
    protected void setContentStart(String contentStart) {
        if (null == contentStart) {
            contentStart = Normal.EMPTY;
        }
        this.contentStart = contentStart;
    }

    /**
     * <p>Gets the content end text.</p>
     *
     * @return the current content end text
     */
    protected String getContentEnd() {
        return contentEnd;
    }

    /**
     * <p>Sets the content end text.</p>
     *
     * <p><code>null</code> is accepted, but will be converted to
     * an empty String.</p>
     *
     * @param contentEnd the new content end text
     */
    protected void setContentEnd(String contentEnd) {
        if (null == contentEnd) {
            contentEnd = Normal.EMPTY;
        }
        this.contentEnd = contentEnd;
    }

    /**
     * <p>Gets the field name value separator text.</p>
     *
     * @return the current field name value separator text
     */
    protected String getFieldNameValueSeparator() {
        return fieldNameValueSeparator;
    }

    /**
     * <p>Sets the field name value separator text.</p>
     *
     * <p><code>null</code> is accepted, but will be converted to
     * an empty String.</p>
     *
     * @param fieldNameValueSeparator the new field name value separator text
     */
    protected void setFieldNameValueSeparator(String fieldNameValueSeparator) {
        if (null == fieldNameValueSeparator) {
            fieldNameValueSeparator = Normal.EMPTY;
        }
        this.fieldNameValueSeparator = fieldNameValueSeparator;
    }

    /**
     * <p>Gets the field separator text.</p>
     *
     * @return the current field separator text
     */
    protected String getFieldSeparator() {
        return fieldSeparator;
    }

    /**
     * <p>Sets the field separator text.</p>
     *
     * <p><code>null</code> is accepted, but will be converted to
     * an empty String.</p>
     *
     * @param fieldSeparator the new field separator text
     */
    protected void setFieldSeparator(String fieldSeparator) {
        if (null == fieldSeparator) {
            fieldSeparator = Normal.EMPTY;
        }
        this.fieldSeparator = fieldSeparator;
    }

    /**
     * <p>Gets whether the field separator should be added at the start
     * of each buffer.</p>
     *
     * @return the fieldSeparatorAtStart flag
     */
    protected boolean isFieldSeparatorAtStart() {
        return fieldSeparatorAtStart;
    }

    /**
     * <p>Sets whether the field separator should be added at the start
     * of each buffer.</p>
     *
     * @param fieldSeparatorAtStart the fieldSeparatorAtStart flag
     */
    protected void setFieldSeparatorAtStart(final boolean fieldSeparatorAtStart) {
        this.fieldSeparatorAtStart = fieldSeparatorAtStart;
    }

    /**
     * <p>Gets whether the field separator should be added at the end
     * of each buffer.</p>
     *
     * @return fieldSeparatorAtEnd flag
     */
    protected boolean isFieldSeparatorAtEnd() {
        return fieldSeparatorAtEnd;
    }

    /**
     * <p>Sets whether the field separator should be added at the end
     * of each buffer.</p>
     *
     * @param fieldSeparatorAtEnd the fieldSeparatorAtEnd flag
     */
    protected void setFieldSeparatorAtEnd(final boolean fieldSeparatorAtEnd) {
        this.fieldSeparatorAtEnd = fieldSeparatorAtEnd;
    }

    /**
     * <p>Gets the text to output when <code>null</code> found.</p>
     *
     * @return the current text to output when null found
     */
    protected String getNullText() {
        return nullText;
    }

    /**
     * <p>Sets the text to output when <code>null</code> found.</p>
     *
     * <p><code>null</code> is accepted, but will be converted to
     * an empty String.</p>
     *
     * @param nullText the new text to output when null found
     */
    protected void setNullText(String nullText) {
        if (null == nullText) {
            nullText = Normal.EMPTY;
        }
        this.nullText = nullText;
    }

    /**
     * <p>Gets the start text to output when a <code>Collection</code>,
     * <code>Map</code> or array size is output.</p>
     *
     * <p>This is output before the size value.</p>
     *
     * @return the current start of size text
     */
    protected String getSizeStartText() {
        return sizeStartText;
    }

    /**
     * <p>Sets the start text to output when a <code>Collection</code>,
     * <code>Map</code> or array size is output.</p>
     *
     * <p>This is output before the size value.</p>
     *
     * <p><code>null</code> is accepted, but will be converted to
     * an empty String.</p>
     *
     * @param sizeStartText the new start of size text
     */
    protected void setSizeStartText(String sizeStartText) {
        if (null == sizeStartText) {
            sizeStartText = Normal.EMPTY;
        }
        this.sizeStartText = sizeStartText;
    }

    /**
     * <p>Gets the end text to output when a <code>Collection</code>,
     * <code>Map</code> or array size is output.</p>
     *
     * <p>This is output after the size value.</p>
     *
     * @return the current end of size text
     */
    protected String getSizeEndText() {
        return sizeEndText;
    }

    /**
     * <p>Sets the end text to output when a <code>Collection</code>,
     * <code>Map</code> or array size is output.</p>
     *
     * <p>This is output after the size value.</p>
     *
     * <p><code>null</code> is accepted, but will be converted to
     * an empty String.</p>
     *
     * @param sizeEndText the new end of size text
     */
    protected void setSizeEndText(String sizeEndText) {
        if (null == sizeEndText) {
            sizeEndText = Normal.EMPTY;
        }
        this.sizeEndText = sizeEndText;
    }

    /**
     * <p>Gets the start text to output when an <code>Object</code> is
     * output in summary mode.</p>
     *
     * <p>This is output before the size value.</p>
     *
     * @return the current start of summary text
     */
    protected String getSummaryObjectStartText() {
        return summaryObjectStartText;
    }

    /**
     * <p>Sets the start text to output when an <code>Object</code> is
     * output in summary mode.</p>
     *
     * <p>This is output before the size value.</p>
     *
     * <p><code>null</code> is accepted, but will be converted to
     * an empty String.</p>
     *
     * @param summaryObjectStartText the new start of summary text
     */
    protected void setSummaryObjectStartText(String summaryObjectStartText) {
        if (null == summaryObjectStartText) {
            summaryObjectStartText = Normal.EMPTY;
        }
        this.summaryObjectStartText = summaryObjectStartText;
    }

    /**
     * <p>Gets the end text to output when an <code>Object</code> is
     * output in summary mode.</p>
     *
     * <p>This is output after the size value.</p>
     *
     * @return the current end of summary text
     */
    protected String getSummaryObjectEndText() {
        return summaryObjectEndText;
    }

    /**
     * <p>Sets the end text to output when an <code>Object</code> is
     * output in summary mode.</p>
     *
     * <p>This is output after the size value.</p>
     *
     * <p><code>null</code> is accepted, but will be converted to
     * an empty String.</p>
     *
     * @param summaryObjectEndText the new end of summary text
     */
    protected void setSummaryObjectEndText(String summaryObjectEndText) {
        if (null == summaryObjectEndText) {
            summaryObjectEndText = Normal.EMPTY;
        }
        this.summaryObjectEndText = summaryObjectEndText;
    }

    private static final class DefaultToStringStyle extends ToStringStyle {

        private static final long serialVersionUID = 1L;

        DefaultToStringStyle() {
            super();
        }

        private Object readResolve() {
            return DEFAULT_STYLE;
        }

    }

    private static final class NoFieldNameToStringStyle extends ToStringStyle {

        private static final long serialVersionUID = 1L;

        NoFieldNameToStringStyle() {
            super();
            this.setUseFieldNames(false);
        }

        private Object readResolve() {
            return NO_FIELD_NAMES_STYLE;
        }

    }

    private static final class ShortPrefixToStringStyle extends ToStringStyle {

        private static final long serialVersionUID = 1L;

        ShortPrefixToStringStyle() {
            super();
            this.setUseShortClassName(true);
            this.setUseIdentityHashCode(false);
        }

        private Object readResolve() {
            return SHORT_PREFIX_STYLE;
        }

    }

    private static final class SimpleToStringStyle extends ToStringStyle {

        private static final long serialVersionUID = 1L;

        SimpleToStringStyle() {
            super();
            this.setUseClassName(false);
            this.setUseIdentityHashCode(false);
            this.setUseFieldNames(false);
            this.setContentStart(Normal.EMPTY);
            this.setContentEnd(Normal.EMPTY);
        }

        private Object readResolve() {
            return SIMPLE_STYLE;
        }

    }

    private static final class MultiLineToStringStyle extends ToStringStyle {

        private static final long serialVersionUID = 1L;

        MultiLineToStringStyle() {
            super();
            this.setContentStart(Symbol.BRACKET_LEFT);
            this.setFieldSeparator(System.lineSeparator() + Symbol.SPACE);
            this.setFieldSeparatorAtStart(true);
            this.setContentEnd(System.lineSeparator() + Symbol.BRACKET_RIGHT);
        }

        private Object readResolve() {
            return MULTI_LINE_STYLE;
        }

    }

    private static final class NoClassNameToStringStyle extends ToStringStyle {

        private static final long serialVersionUID = 1L;

        NoClassNameToStringStyle() {
            super();
            this.setUseClassName(false);
            this.setUseIdentityHashCode(false);
        }

        private Object readResolve() {
            return NO_CLASS_NAME_STYLE;
        }

    }

    private static final class JsonToStringStyle extends ToStringStyle {

        private static final long serialVersionUID = 1L;

        private static final String FIELD_NAME_QUOTE = Symbol.DOUBLE_QUOTES;

        JsonToStringStyle() {
            super();

            this.setUseClassName(false);
            this.setUseIdentityHashCode(false);

            this.setContentStart(Symbol.BRACE_LEFT);
            this.setContentEnd(Symbol.BRACE_RIGHT);

            this.setArrayStart(Symbol.BRACKET_LEFT);
            this.setArrayEnd(Symbol.BRACKET_RIGHT);

            this.setFieldSeparator(Symbol.COMMA);
            this.setFieldNameValueSeparator(Symbol.COLON);

            this.setNullText(Normal.NULL);

            this.setSummaryObjectStartText("\"<");
            this.setSummaryObjectEndText(">\"");

            this.setSizeStartText("\"<size=");
            this.setSizeEndText(">\"");
        }

        @Override
        public void append(final StringBuffer buffer, final String fieldName,
                           final Object[] array, final Boolean fullDetail) {
            if (null == fieldName) {
                throw new UnsupportedOperationException(
                        "Field names are mandatory when using JsonToStringStyle");
            }
            if (!isFullDetail(fullDetail)) {
                throw new UnsupportedOperationException(
                        "FullDetail must be true when using JsonToStringStyle");
            }

            super.append(buffer, fieldName, array, fullDetail);
        }

        @Override
        public void append(final StringBuffer buffer, final String fieldName, final long[] array,
                           final Boolean fullDetail) {
            if (null == fieldName) {
                throw new UnsupportedOperationException(
                        "Field names are mandatory when using JsonToStringStyle");
            }
            if (!isFullDetail(fullDetail)) {
                throw new UnsupportedOperationException(
                        "FullDetail must be true when using JsonToStringStyle");
            }

            super.append(buffer, fieldName, array, fullDetail);
        }

        @Override
        public void append(final StringBuffer buffer, final String fieldName, final int[] array,
                           final Boolean fullDetail) {

            if (null == fieldName) {
                throw new UnsupportedOperationException(
                        "Field names are mandatory when using JsonToStringStyle");
            }
            if (!isFullDetail(fullDetail)) {
                throw new UnsupportedOperationException(
                        "FullDetail must be true when using JsonToStringStyle");
            }

            super.append(buffer, fieldName, array, fullDetail);
        }

        @Override
        public void append(final StringBuffer buffer, final String fieldName,
                           final short[] array, final Boolean fullDetail) {

            if (null == fieldName) {
                throw new UnsupportedOperationException(
                        "Field names are mandatory when using JsonToStringStyle");
            }
            if (!isFullDetail(fullDetail)) {
                throw new UnsupportedOperationException(
                        "FullDetail must be true when using JsonToStringStyle");
            }

            super.append(buffer, fieldName, array, fullDetail);
        }

        @Override
        public void append(final StringBuffer buffer, final String fieldName, final byte[] array,
                           final Boolean fullDetail) {

            if (null == fieldName) {
                throw new UnsupportedOperationException(
                        "Field names are mandatory when using JsonToStringStyle");
            }
            if (!isFullDetail(fullDetail)) {
                throw new UnsupportedOperationException(
                        "FullDetail must be true when using JsonToStringStyle");
            }

            super.append(buffer, fieldName, array, fullDetail);
        }

        @Override
        public void append(final StringBuffer buffer, final String fieldName, final char[] array,
                           final Boolean fullDetail) {

            if (null == fieldName) {
                throw new UnsupportedOperationException(
                        "Field names are mandatory when using JsonToStringStyle");
            }
            if (!isFullDetail(fullDetail)) {
                throw new UnsupportedOperationException(
                        "FullDetail must be true when using JsonToStringStyle");
            }

            super.append(buffer, fieldName, array, fullDetail);
        }

        @Override
        public void append(final StringBuffer buffer, final String fieldName,
                           final double[] array, final Boolean fullDetail) {

            if (null == fieldName) {
                throw new UnsupportedOperationException(
                        "Field names are mandatory when using JsonToStringStyle");
            }
            if (!isFullDetail(fullDetail)) {
                throw new UnsupportedOperationException(
                        "FullDetail must be true when using JsonToStringStyle");
            }

            super.append(buffer, fieldName, array, fullDetail);
        }

        @Override
        public void append(final StringBuffer buffer, final String fieldName,
                           final float[] array, final Boolean fullDetail) {

            if (null == fieldName) {
                throw new UnsupportedOperationException(
                        "Field names are mandatory when using JsonToStringStyle");
            }
            if (!isFullDetail(fullDetail)) {
                throw new UnsupportedOperationException(
                        "FullDetail must be true when using JsonToStringStyle");
            }

            super.append(buffer, fieldName, array, fullDetail);
        }

        @Override
        public void append(final StringBuffer buffer, final String fieldName,
                           final boolean[] array, final Boolean fullDetail) {

            if (null == fieldName) {
                throw new UnsupportedOperationException(
                        "Field names are mandatory when using JsonToStringStyle");
            }
            if (!isFullDetail(fullDetail)) {
                throw new UnsupportedOperationException(
                        "FullDetail must be true when using JsonToStringStyle");
            }

            super.append(buffer, fieldName, array, fullDetail);
        }

        @Override
        public void append(final StringBuffer buffer, final String fieldName, final Object value,
                           final Boolean fullDetail) {

            if (null == fieldName) {
                throw new UnsupportedOperationException(
                        "Field names are mandatory when using JsonToStringStyle");
            }
            if (!isFullDetail(fullDetail)) {
                throw new UnsupportedOperationException(
                        "FullDetail must be true when using JsonToStringStyle");
            }

            super.append(buffer, fieldName, value, fullDetail);
        }

        @Override
        protected void appendDetail(final StringBuffer buffer, final String fieldName, final char value) {
            appendValueAsString(buffer, String.valueOf(value));
        }

        @Override
        protected void appendDetail(final StringBuffer buffer, final String fieldName, final Object value) {

            if (null == value) {
                appendNullText(buffer, fieldName);
                return;
            }

            if (value instanceof String || value instanceof Character) {
                appendValueAsString(buffer, value.toString());
                return;
            }

            if (value instanceof Number || value instanceof Boolean) {
                buffer.append(value);
                return;
            }

            final String valueAsString = value.toString();
            if (isJsonObject(valueAsString) || isJsonArray(valueAsString)) {
                buffer.append(value);
                return;
            }

            appendDetail(buffer, fieldName, valueAsString);
        }

        private boolean isJsonArray(final String valueAsString) {
            return valueAsString.startsWith(getArrayStart())
                    && valueAsString.endsWith(getArrayEnd());
        }

        private boolean isJsonObject(final String valueAsString) {
            return valueAsString.startsWith(getContentStart())
                    && valueAsString.endsWith(getContentEnd());
        }

        private void appendValueAsString(final StringBuffer buffer, final String value) {
            buffer.append(Symbol.C_DOUBLE_QUOTES).append(EscapeKit.escapeJson(value)).append(Symbol.C_DOUBLE_QUOTES);
        }

        @Override
        protected void appendFieldStart(final StringBuffer buffer, final String fieldName) {

            if (null == fieldName) {
                throw new UnsupportedOperationException(
                        "Field names are mandatory when using JsonToStringStyle");
            }

            super.appendFieldStart(buffer, FIELD_NAME_QUOTE + EscapeKit.escapeJson(fieldName)
                    + FIELD_NAME_QUOTE);
        }

        private Object readResolve() {
            return JSON_STYLE;
        }

    }

}
