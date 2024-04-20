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

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.toolkit.ArrayKit;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Comparator;

/**
 * 用于构建 {@link java.lang.Comparable#compareTo(Object)} 方法的辅助工具
 * 在Bean对象中,所有相关字段都参与比对,继承的字段不参与 使用方法如下：
 *
 * <pre>
 * public class MyClass {
 *   String field1;
 *   int field2;
 *   boolean field3;
 *
 *   ...
 *
 *   public int compareTo(Object o) {
 *     MyClass myClass = (MyClass) o;
 *     return new CompareToBuilder()
 *       .appendSuper(super.compareTo(o)
 *       .append(this.field1, myClass.field1)
 *       .append(this.field2, myClass.field2)
 *       .append(this.field3, myClass.field3)
 *       .toComparison();
 *   }
 * }
 * </pre>
 * <p>
 * 字段值按照顺序比较,如果某个字段返回非0结果,比较终止,使用{@code toComparison()}返回结果,后续比较忽略
 *
 * <p>
 * 也可以使用{@link #reflectionCompare(Object, Object) reflectionCompare} 方法通过反射比较字段,使用方法如下：
 *
 * <pre>
 * public int compareTo(Object o) {
 *   return CompareToBuilder.reflectionCompare(this, o);
 * }
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CompareBuilder implements Builder<Integer> {

    /**
     * 当前比较状态
     */
    private int comparison;

    /**
     * 构造,构造后调用append方法增加比较项,然后调用{@link #toComparison()}获取结果
     */
    public CompareBuilder() {
        comparison = 0;
    }

    /**
     * 通过反射比较两个Bean对象,对象字段可以为private 比较规则如下：
     *
     * <ul>
     * <li>static字段不比较</li>
     * <li>Transient字段不参与比较</li>
     * <li>父类字段参与比较</li>
     * </ul>
     *
     * <p>
     * 如果被比较的两个对象都为<code>null</code>,被认为相同
     *
     * @param left  第一个对象
     * @param right 第二个对象
     * @return 当left小于、等于或大于right时，为负整数、零或正整数
     */
    public static int reflectionCompare(final Object left, final Object right) {
        return reflectionCompare(left, right, false, null);
    }

    /**
     * 通过反射比较两个对象
     *
     * @param left              左边的对象
     * @param right             右边的对象
     * @param compareTransients 是否比较属性
     * @return 当left小于、等于或大于right时，为负整数、零或正整数
     */
    public static int reflectionCompare(final Object left,
                                        final Object right,
                                        final boolean compareTransients) {
        return reflectionCompare(left, right, compareTransients, null);
    }

    /**
     * 通过反射比较两个对象
     *
     * @param left          左边的对象
     * @param right         右边的对象
     * @param excludeFields 要排除的字符串字段的集合
     * @return 当left小于、等于或大于right时，为负整数、零或正整数
     */
    public static int reflectionCompare(final Object left,
                                        final Object right,
                                        final Collection<String> excludeFields) {
        return reflectionCompare(left, right, ReflectionToStringBuilder.toNoNullStringArray(excludeFields));
    }

    /**
     * 通过反射比较两个对象
     *
     * @param left          左边的对象
     * @param right         右边的对象
     * @param excludeFields 要排除的字符串字段
     * @return 当left小于、等于或大于right时，为负整数、零或正整数
     */
    public static int reflectionCompare(final Object left,
                                        final Object right,
                                        final String... excludeFields) {
        return reflectionCompare(left, right, false, null, excludeFields);
    }

    /**
     * 通过反射比较两个对象
     *
     * @param left              左边的对象
     * @param right             右边的对象
     * @param compareTransients 是否比较属性
     * @param reflectUpToClass  比较字段的最后一个超类
     * @param excludeFields     要排除的字符串字段
     * @return 当left小于、等于或大于right时，为负整数、零或正整数
     */
    public static int reflectionCompare(
            final Object left,
            final Object right,
            final boolean compareTransients,
            final Class<?> reflectUpToClass,
            final String... excludeFields) {

        if (left == right) {
            return 0;
        }
        if (null == left || null == right) {
            throw new NullPointerException();
        }
        Class<?> leftClazz = left.getClass();
        if (!leftClazz.isInstance(right)) {
            throw new ClassCastException();
        }
        final CompareBuilder compareBuilder = new CompareBuilder();
        reflectionAppend(left, right, leftClazz, compareBuilder, compareTransients, excludeFields);
        while (null != leftClazz.getSuperclass() && leftClazz != reflectUpToClass) {
            leftClazz = leftClazz.getSuperclass();
            reflectionAppend(left, right, leftClazz, compareBuilder, compareTransients, excludeFields);
        }
        return compareBuilder.toComparison();
    }

    /**
     * 附加到builder,比较left/right使用clazz.
     *
     * @param left          数值
     * @param right         数值
     * @param clazz         <code>Class</code> 定义要比较的字段
     * @param builder       <code>CompareToBuilder</code>
     * @param useTransients 是否比较 transient fields
     * @param excludeFields fields to exclude
     */
    private static void reflectionAppend(
            final Object left,
            final Object right,
            final Class<?> clazz,
            final CompareBuilder builder,
            final boolean useTransients,
            final String[] excludeFields) {

        final Field[] fields = clazz.getDeclaredFields();
        AccessibleObject.setAccessible(fields, true);
        for (int i = 0; i < fields.length && builder.comparison == 0; i++) {
            final Field f = fields[i];
            if (!ArrayKit.contains(excludeFields, f.getName())
                    && !f.getName().contains(Symbol.DOLLAR)
                    && (useTransients || !Modifier.isTransient(f.getModifiers()))
                    && !Modifier.isStatic(f.getModifiers())) {
                try {
                    builder.append(f.get(left), f.get(right));
                } catch (final IllegalAccessException e) {
                    throw new InternalError("Unexpected IllegalAccessException");
                }
            }
        }
    }

    /**
     * 附加到 builder compareTo(Object) 超类的结果
     *
     * @param superCompareTo 调用 <code>super.compareTo(Object)</code>结果
     * @return this - object
     */
    public CompareBuilder appendSuper(final int superCompareTo) {
        if (comparison != 0) {
            return this;
        }
        comparison = superCompareTo;
        return this;
    }

    /**
     * 附加到builder的深层比较,两个Object数组
     *
     * <ol>
     * <li>使用==检查数组是否相同</li>
     * <li>检查是否为null, null小于非null</li>
     * <li>检查数组长度,长度较短的数组小于长度较长的数组</li>
     * <li>使用{@link #append(Object, Object)}逐个元素检查数组内容</li>
     * </ol>
     *
     * @param left  数值
     * @param right 数值
     * @return this - object
     */
    public CompareBuilder append(final Object left, final Object right) {
        return append(left, right, null);
    }

    /**
     * 附加到builder的深层比较,两个Object数组
     *
     * <ol>
     * <li>使用==检查数组是否相同</li>
     * <li>检查是否为null, null小于非null</li>
     * <li>检查数组长度,长度较短的数组小于长度较长的数组</li>
     * <li>使用{@link #append(Object, Object, Comparator)}逐个元素检查数组内容</li>
     * </ol>
     *
     * @param left       数值
     * @param right      数值
     * @param comparator 用来比较数值元素的比较器.
     * @return this - object
     */
    public CompareBuilder append(final Object left, final Object right, final Comparator<?> comparator) {
        if (comparison != 0) {
            return this;
        }
        if (left == right) {
            return this;
        }
        if (null == left) {
            comparison = -1;
            return this;
        }
        if (null == right) {
            comparison = 1;
            return this;
        }
        if (left.getClass().isArray()) {
            appendArray(left, right, comparator);
        } else {
            if (null == comparator) {
                final Comparable<Object> comparable = (Comparable<Object>) left;
                comparison = comparable.compareTo(right);
            } else {
                final Comparator<Object> comparator2 = (Comparator<Object>) comparator;
                comparison = comparator2.compare(left, right);
            }
        }
        return this;
    }

    private void appendArray(final Object left, final Object right, final Comparator<?> comparator) {
        if (left instanceof long[]) {
            append((long[]) left, (long[]) right);
        } else if (left instanceof int[]) {
            append((int[]) left, (int[]) right);
        } else if (left instanceof short[]) {
            append((short[]) left, (short[]) right);
        } else if (left instanceof char[]) {
            append((char[]) left, (char[]) right);
        } else if (left instanceof byte[]) {
            append((byte[]) left, (byte[]) right);
        } else if (left instanceof double[]) {
            append((double[]) left, (double[]) right);
        } else if (left instanceof float[]) {
            append((float[]) left, (float[]) right);
        } else if (left instanceof boolean[]) {
            append((boolean[]) left, (boolean[]) right);
        } else {
            append((Object[]) left, (Object[]) right, comparator);
        }
    }

    /**
     * 附加到builder的深层比较,两个long值比较
     *
     * @param left  数值
     * @param right 数值
     * @return this - object
     */
    public CompareBuilder append(final long left, final long right) {
        if (comparison != 0) {
            return this;
        }
        comparison = Long.compare(left, right);
        return this;
    }

    /**
     * 附加到builder的深层比较,两个int值比较
     *
     * @param left  数值
     * @param right 数值
     * @return this - object
     */
    public CompareBuilder append(final int left, final int right) {
        if (comparison != 0) {
            return this;
        }
        comparison = Integer.compare(left, right);
        return this;
    }

    /**
     * 附加到builder的深层比较,两个short值比较
     *
     * @param left  数值
     * @param right 数值
     * @return this - object
     */
    public CompareBuilder append(final short left, final short right) {
        if (comparison != 0) {
            return this;
        }
        comparison = Short.compare(left, right);
        return this;
    }

    /**
     * 附加到builder的深层比较,两个char值比较
     *
     * @param left  数值
     * @param right 数值
     * @return this - object
     */
    public CompareBuilder append(final char left, final char right) {
        if (comparison != 0) {
            return this;
        }
        comparison = Character.compare(left, right);
        return this;
    }

    /**
     * 附加到builder的深层比较,两个byte值比较
     *
     * @param left  数值
     * @param right 数值
     * @return this - object
     */
    public CompareBuilder append(final byte left, final byte right) {
        if (comparison != 0) {
            return this;
        }
        comparison = Byte.compare(left, right);
        return this;
    }

    /**
     * 附加到builder的深层比较,两个double值比较
     *
     * @param left  数值
     * @param right 数值
     * @return this - object
     */
    public CompareBuilder append(final double left, final double right) {
        if (comparison != 0) {
            return this;
        }
        comparison = Double.compare(left, right);
        return this;
    }

    /**
     * 附加到builder的深层比较,两个float值比较
     *
     * @param left  数值
     * @param right 数值
     * @return this - object
     */
    public CompareBuilder append(final float left, final float right) {
        if (comparison != 0) {
            return this;
        }
        comparison = Float.compare(left, right);
        return this;
    }

    /**
     * 附加到builder的深层比较,两个boolean值比较
     *
     * @param left  数值
     * @param right 数值
     * @return this - object
     */
    public CompareBuilder append(final boolean left, final boolean right) {
        if (comparison != 0) {
            return this;
        }
        if (left == right) {
            return this;
        }
        if (left) {
            comparison = 1;
        } else {
            comparison = -1;
        }
        return this;
    }

    /**
     * 附加到builder的深层比较,两个Object数组
     *
     * <ol>
     * <li>使用==检查数组是否相同</li>
     * <li>检查是否为null, null小于非null</li>
     * <li>检查数组长度,长度较短的数组小于长度较长的数组</li>
     * <li>使用{@link #append(Object, Object)}逐个元素检查数组内容</li>
     * </ol>
     *
     * @param left  数组
     * @param right 数组
     * @return this - object
     */
    public CompareBuilder append(final Object[] left, final Object[] right) {
        return append(left, right, null);
    }

    /**
     * 附加到builder的深层比较,两个Object数组
     *
     * <ol>
     * <li>使用==检查数组是否相同</li>
     * <li>检查是否为null, null小于非null</li>
     * <li>检查数组长度,长度较短的数组小于长度较长的数组</li>
     * <li>使用{@link #append(Object, Object, Comparator)}逐个元素检查数组内容</li>
     * </ol>
     *
     * @param left       数组
     * @param right      数组
     * @param comparator 用来比较数组元素的比较器.
     * @return this - object
     */
    public CompareBuilder append(final Object[] left, final Object[] right, final Comparator<?> comparator) {
        if (comparison != 0) {
            return this;
        }
        if (left == right) {
            return this;
        }
        if (null == left) {
            comparison = -1;
            return this;
        }
        if (null == right) {
            comparison = 1;
            return this;
        }
        if (left.length != right.length) {
            comparison = left.length < right.length ? -1 : 1;
            return this;
        }
        for (int i = 0; i < left.length && comparison == 0; i++) {
            append(left[i], right[i], comparator);
        }
        return this;
    }

    /**
     * 附加到builder的深层比较,两个long数组.
     *
     * <ol>
     * <li>使用==检查数组是否相同</li>
     * <li>检查是否为null, null小于非null</li>
     * <li>检查数组长度,长度较短的数组小于长度较长的数组</li>
     * <li>使用{@link #append(long, long)}逐个元素检查数组内容</li>
     * </ol>
     *
     * @param left  数组
     * @param right 数组
     * @return this - object
     */
    public CompareBuilder append(final long[] left, final long[] right) {
        if (comparison != 0) {
            return this;
        }
        if (left == right) {
            return this;
        }
        if (null == left) {
            comparison = -1;
            return this;
        }
        if (null == right) {
            comparison = 1;
            return this;
        }
        if (left.length != right.length) {
            comparison = left.length < right.length ? -1 : 1;
            return this;
        }
        for (int i = 0; i < left.length && comparison == 0; i++) {
            append(left[i], right[i]);
        }
        return this;
    }

    /**
     * 附加到builder的深层比较,两个int数组.
     *
     * <ol>
     * <li>使用==检查数组是否相同</li>
     * <li>检查是否为null, null小于非null</li>
     * <li>检查数组长度,长度较短的数组小于长度较长的数组</li>
     * <li>使用{@link #append(int, int)}逐个元素检查数组内容</li>
     * </ol>
     *
     * @param left  数组
     * @param right 数组
     * @return this - object
     */
    public CompareBuilder append(final int[] left, final int[] right) {
        if (comparison != 0) {
            return this;
        }
        if (left == right) {
            return this;
        }
        if (null == left) {
            comparison = -1;
            return this;
        }
        if (null == right) {
            comparison = 1;
            return this;
        }
        if (left.length != right.length) {
            comparison = left.length < right.length ? -1 : 1;
            return this;
        }
        for (int i = 0; i < left.length && comparison == 0; i++) {
            append(left[i], right[i]);
        }
        return this;
    }

    /**
     * 附加到builder的深层比较,两个short数组.
     *
     * <ol>
     * <li>使用==检查数组是否相同</li>
     * <li>检查是否为null, null小于非null</li>
     * <li>检查数组长度,长度较短的数组小于长度较长的数组</li>
     * <li>使用{@link #append(short, short)}逐个元素检查数组内容</li>
     * </ol>
     *
     * @param left  数组
     * @param right 数组
     * @return this - object
     */
    public CompareBuilder append(final short[] left, final short[] right) {
        if (comparison != 0) {
            return this;
        }
        if (left == right) {
            return this;
        }
        if (null == left) {
            comparison = -1;
            return this;
        }
        if (null == right) {
            comparison = 1;
            return this;
        }
        if (left.length != right.length) {
            comparison = left.length < right.length ? -1 : 1;
            return this;
        }
        for (int i = 0; i < left.length && comparison == 0; i++) {
            append(left[i], right[i]);
        }
        return this;
    }

    /**
     * 附加到builder的深层比较,两个char数组.
     *
     * <ol>
     * <li>使用==检查数组是否相同</li>
     * <li>检查是否为null, null小于非null</li>
     * <li>检查数组长度,长度较短的数组小于长度较长的数组</li>
     * <li>使用{@link #append(char, char)}逐个元素检查数组内容</li>
     * </ol>
     *
     * @param left  数组
     * @param right 数组
     * @return this - object
     */
    public CompareBuilder append(final char[] left, final char[] right) {
        if (comparison != 0) {
            return this;
        }
        if (left == right) {
            return this;
        }
        if (null == left) {
            comparison = -1;
            return this;
        }
        if (null == right) {
            comparison = 1;
            return this;
        }
        if (left.length != right.length) {
            comparison = left.length < right.length ? -1 : 1;
            return this;
        }
        for (int i = 0; i < left.length && comparison == 0; i++) {
            append(left[i], right[i]);
        }
        return this;
    }

    /**
     * 附加到builder的深层比较,两个byte数组.
     *
     * <ol>
     * <li>使用==检查数组是否相同</li>
     * <li>检查是否为null, null小于非null</li>
     * <li>检查数组长度,长度较短的数组小于长度较长的数组</li>
     * <li>使用{@link #append(byte, byte)}逐个元素检查数组内容</li>
     * </ol>
     *
     * @param left  数组
     * @param right 数组
     * @return this - object
     */
    public CompareBuilder append(final byte[] left, final byte[] right) {
        if (comparison != 0) {
            return this;
        }
        if (left == right) {
            return this;
        }
        if (null == left) {
            comparison = -1;
            return this;
        }
        if (null == right) {
            comparison = 1;
            return this;
        }
        if (left.length != right.length) {
            comparison = left.length < right.length ? -1 : 1;
            return this;
        }
        for (int i = 0; i < left.length && comparison == 0; i++) {
            append(left[i], right[i]);
        }
        return this;
    }

    /**
     * 附加到builder的深层比较,两个double数组.
     *
     * <ol>
     * <li>使用==检查数组是否相同</li>
     * <li>检查是否为null, null小于非null</li>
     * <li>检查数组长度,长度较短的数组小于长度较长的数组</li>
     * <li>使用{@link #append(double, double)}逐个元素检查数组内容</li>
     * </ol>
     *
     * @param left  数组
     * @param right 数组
     * @return this - object
     */
    public CompareBuilder append(final double[] left, final double[] right) {
        if (comparison != 0) {
            return this;
        }
        if (left == right) {
            return this;
        }
        if (null == left) {
            comparison = -1;
            return this;
        }
        if (null == right) {
            comparison = 1;
            return this;
        }
        if (left.length != right.length) {
            comparison = left.length < right.length ? -1 : 1;
            return this;
        }
        for (int i = 0; i < left.length && comparison == 0; i++) {
            append(left[i], right[i]);
        }
        return this;
    }

    /**
     * 附加到builder的深层比较,两个float数组.
     *
     * <ol>
     * <li>使用==检查数组是否相同</li>
     * <li>检查是否为null, null小于非null</li>
     * <li>检查数组长度,长度较短的数组小于长度较长的数组</li>
     * <li>使用{@link #append(float, float)}逐个元素检查数组内容</li>
     * </ol>
     *
     * @param left  数组
     * @param right 数组
     * @return this - object
     */
    public CompareBuilder append(final float[] left, final float[] right) {
        if (comparison != 0) {
            return this;
        }
        if (left == right) {
            return this;
        }
        if (null == left) {
            comparison = -1;
            return this;
        }
        if (null == right) {
            comparison = 1;
            return this;
        }
        if (left.length != right.length) {
            comparison = left.length < right.length ? -1 : 1;
            return this;
        }
        for (int i = 0; i < left.length && comparison == 0; i++) {
            append(left[i], right[i]);
        }
        return this;
    }

    /**
     * 附加到builder的深层比较,两个boolean数组.
     *
     * <ol>
     * <li>使用==检查数组是否相同</li>
     * <li>检查是否为null, null小于非null</li>
     * <li>检查数组长度,长度较短的数组小于长度较长的数组</li>
     * <li>使用{@link #append(boolean, boolean)}逐个元素检查数组内容</li>
     * </ol>
     *
     * @param left  数组
     * @param right 数组
     * @return 比较器
     */
    public CompareBuilder append(final boolean[] left, final boolean[] right) {
        if (comparison != 0) {
            return this;
        }
        if (left == right) {
            return this;
        }
        if (null == left) {
            comparison = -1;
            return this;
        }
        if (null == right) {
            comparison = 1;
            return this;
        }
        if (left.length != right.length) {
            comparison = left.length < right.length ? -1 : 1;
            return this;
        }
        for (int i = 0; i < left.length && comparison == 0; i++) {
            append(left[i], right[i]);
        }
        return this;
    }

    /**
     * 返回一个负整数、一个正整数或零
     * builder判断左边小于、大于或等于右边
     *
     * @return 比较结果
     * @see #build()
     */
    public int toComparison() {
        return comparison;
    }

    /**
     * 返回一个负整数、一个正整数或零
     * builder判断左边小于、大于或等于右边
     *
     * @return 最终的比较结果为整数
     * @see #toComparison()
     */
    @Override
    public Integer build() {
        return Integer.valueOf(toComparison());
    }

}

