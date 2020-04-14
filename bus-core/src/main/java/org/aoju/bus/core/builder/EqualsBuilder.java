/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.core.builder;

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.tuple.Pair;
import org.aoju.bus.core.utils.ArrayUtils;
import org.aoju.bus.core.utils.ClassUtils;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * {@link Object#equals(Object)} 方法的构建器
 * 两个对象equals必须保证hashCode值相等
 * ,hashCode值相等不能保证一定相等
 *
 * <p>使用方法如下：</p>
 * <pre>
 * public boolean equals(Object obj) {
 *   if (obj == null) { return false; }
 *   if (obj == this) { return true; }
 *   if (obj.getClass() != getClass()) {
 *     return false;
 *   }
 *   MyClass rhs = (MyClass) obj;
 *   return new EqualsBuilder()
 *                 .appendSuper(super.equals(obj))
 *                 .append(field1, rhs.field1)
 *                 .append(field2, rhs.field2)
 *                 .append(field3, rhs.field3)
 *                 .isEquals();
 *  }
 *
 * public boolean equals(Object obj) {
 *   return EqualsBuilder.reflectionEquals(this, obj);
 * }
 * </pre>
 *
 * @author Kimi Liu
 * @version 5.8.5
 * @since JDK 1.8+
 */
public class EqualsBuilder implements Builder<Boolean> {

    /**
     * 反射方法用于检测循环对象引用和避免无限循环的对象注册表
     *
     * @since 3.0.0
     */
    private static final ThreadLocal<Set<Pair<HashKey, HashKey>>> REGISTRY = new ThreadLocal<>();

    /**
     * 是否equals,此值随着构建会变更,默认true
     */
    private boolean isEquals = true;
    private boolean testTransients = false;
    private boolean testRecursive = false;
    private List<Class<?>> bypassReflectionClasses;
    private Class<?> reflectUpToClass = null;
    private String[] excludeFields = null;


    public EqualsBuilder() {
        bypassReflectionClasses = new ArrayList<>();
        bypassReflectionClasses.add(String.class);
    }

    /**
     * 返回当前线程中的反射方法遍历的对象对的注册表
     *
     * @return 设置要遍历的对象的注册表
     * @since 3.0.0
     */
    static Set<Pair<HashKey, HashKey>> getRegistry() {
        return REGISTRY.get();
    }

    /**
     * 将值对转换为寄存器对
     *
     * @param lhs 当前对象
     * @param rhs 其他对象
     * @return the pair
     */
    static Pair<HashKey, HashKey> getRegisterPair(final Object lhs, final Object rhs) {
        final HashKey left = new HashKey(lhs);
        final HashKey right = new HashKey(rhs);
        return Pair.of(left, right);
    }

    /**
     * 如果注册表包含给定的对象对，则返回true。
     * 用于反射方法避免无限循环。
     * 对象可能被交换，因此如果对象对需要进行检查
     * 按给定或交换的顺序注册
     *
     * @param lhs this对象在注册表中查找
     * @param rhs 要在registry上查找的另一个对象
     * @return 如果注册表包含给定的对象，布尔true.
     * @since 3.0.0
     */
    static boolean isRegistered(final Object lhs, final Object rhs) {
        final Set<Pair<HashKey, HashKey>> registry = getRegistry();
        final Pair<HashKey, HashKey> pair = getRegisterPair(lhs, rhs);
        final Pair<HashKey, HashKey> swappedPair = Pair.of(pair.getRight(), pair.getLeft());

        return registry != null
                && (registry.contains(pair) || registry.contains(swappedPair));
    }

    /**
     * 注册给定的对象对
     * 用于反射方法避免无限循环
     *
     * @param lhs 要注册的对象
     * @param rhs 另一个要注册的对象
     */
    private static void register(final Object lhs, final Object rhs) {
        Set<Pair<HashKey, HashKey>> registry = getRegistry();
        if (registry == null) {
            registry = new HashSet<>();
            REGISTRY.set(registry);
        }
        final Pair<HashKey, HashKey> pair = getRegisterPair(lhs, rhs);
        registry.add(pair);
    }

    /**
     * 注销给定的对象对
     * 使用反射方法避免无限循环
     *
     * @param lhs 要注销此对象
     * @param rhs 另一个要注册的对象
     * @since 3.0.0
     */
    private static void unregister(final Object lhs, final Object rhs) {
        final Set<Pair<HashKey, HashKey>> registry = getRegistry();
        if (registry != null) {
            final Pair<HashKey, HashKey> pair = getRegisterPair(lhs, rhs);
            registry.remove(pair);
            if (registry.isEmpty()) {
                REGISTRY.remove();
            }
        }
    }

    /**
     * 反射检查两个对象是否equals,此方法检查对象及其父对象的属性（包括私有属性）是否相等
     *
     * @param lhs           此对象
     * @param rhs           另一个对象
     * @param excludeFields 排除的字段集合,如果有不参与计算equals的字段加入此集合即可
     * @return 两个对象是否equals, 是返回<code>true</code>
     */
    public static boolean reflectionEquals(final Object lhs, final Object rhs, final Collection<String> excludeFields) {
        return reflectionEquals(lhs, rhs, ReflectionToStringBuilder.toNoNullStringArray(excludeFields));
    }

    /**
     * 反射检查两个对象是否equals,此方法检查对象及其父对象的属性（包括私有属性）是否相等
     *
     * @param lhs           此对象
     * @param rhs           另一个对象
     * @param excludeFields 排除的字段集合,如果有不参与计算equals的字段加入此集合即可
     * @return 两个对象是否equals, 是返回<code>true</code>
     */
    public static boolean reflectionEquals(final Object lhs, final Object rhs, final String... excludeFields) {
        return reflectionEquals(lhs, rhs, false, null, excludeFields);
    }

    /**
     * 此方法使用反射来确定两个对象是否相等
     *
     * @param lhs            对象
     * @param rhs            其他对象
     * @param testTransients 是否测试忽略
     * @return true如果两个对象已测试相等.
     * @see EqualsExclude
     */
    public static boolean reflectionEquals(final Object lhs, final Object rhs, final boolean testTransients) {
        return reflectionEquals(lhs, rhs, testTransients, null);
    }

    /**
     * 此方法使用反射来确定两个对象是否相等
     *
     * @param lhs              对象
     * @param rhs              其他对象
     * @param testTransients   是否测试忽略
     * @param reflectUpToClass 要反映到(包括)的超类可以是null
     * @param excludeFields    要从测试中排除的字段名的数组
     * @return true如果两个对象已测试相等.
     */
    public static boolean reflectionEquals(final Object lhs, final Object rhs, final boolean testTransients, final Class<?> reflectUpToClass,
                                           final String... excludeFields) {
        return reflectionEquals(lhs, rhs, testTransients, reflectUpToClass, false, excludeFields);
    }

    /**
     * 此方法使用反射来确定两个对象是否相等
     *
     * @param lhs              对象
     * @param rhs              其他对象
     * @param testTransients   是否测试忽略
     * @param reflectUpToClass 要反映到(包括)的超类可以是null
     * @param testRecursive    是否递归地调用非基元字段上的反射等于.
     * @param excludeFields    要从测试中排除的字段名的数组
     * @return true如果两个对象已测试相等
     */
    public static boolean reflectionEquals(final Object lhs, final Object rhs, final boolean testTransients, final Class<?> reflectUpToClass,
                                           final boolean testRecursive, final String... excludeFields) {
        if (lhs == rhs) {
            return true;
        }
        if (lhs == null || rhs == null) {
            return false;
        }
        return new EqualsBuilder()
                .setExcludeFields(excludeFields)
                .setReflectUpToClass(reflectUpToClass)
                .setTestTransients(testTransients)
                .setTestRecursive(testRecursive)
                .reflectionAppend(lhs, rhs)
                .isEquals();
    }

    /**
     * 设置反射比较对象时是否测试忽略.
     *
     * @param testTransients 是否测试忽略
     * @return EqualsBuilder -调用链.
     */
    public EqualsBuilder setTestTransients(final boolean testTransients) {
        this.testTransients = testTransients;
        return this;
    }

    /**
     * 置反射比较对象时是否测试忽略.
     *
     * @param testRecursive 是否做递归测试
     * @return EqualsBuilder -调用链.
     */
    public EqualsBuilder setTestRecursive(final boolean testRecursive) {
        this.testRecursive = testRecursive;
        return this;
    }

    public EqualsBuilder setBypassReflectionClasses(List<Class<?>> bypassReflectionClasses) {
        this.bypassReflectionClasses = bypassReflectionClasses;
        return this;
    }

    public EqualsBuilder setReflectUpToClass(final Class<?> reflectUpToClass) {
        this.reflectUpToClass = reflectUpToClass;
        return this;
    }

    public EqualsBuilder setExcludeFields(final String... excludeFields) {
        this.excludeFields = excludeFields;
        return this;
    }

    /**
     * 使用反射测试两个对象
     *
     * @param lhs 左边对象
     * @param rhs 右边对象
     * @return EqualsBuilder -调用链.
     */
    public EqualsBuilder reflectionAppend(final Object lhs, final Object rhs) {
        if (!isEquals) {
            return this;
        }
        if (lhs == rhs) {
            return this;
        }
        if (lhs == null || rhs == null) {
            isEquals = false;
            return this;
        }

        final Class<?> lhsClass = lhs.getClass();
        final Class<?> rhsClass = rhs.getClass();
        Class<?> testClass;
        if (lhsClass.isInstance(rhs)) {
            testClass = lhsClass;
            if (!rhsClass.isInstance(lhs)) {
                testClass = rhsClass;
            }
        } else if (rhsClass.isInstance(lhs)) {
            testClass = rhsClass;
            if (!lhsClass.isInstance(rhs)) {
                testClass = lhsClass;
            }
        } else {
            isEquals = false;
            return this;
        }

        try {
            if (testClass.isArray()) {
                append(lhs, rhs);
            } else {
                if (bypassReflectionClasses != null
                        && (bypassReflectionClasses.contains(lhsClass) || bypassReflectionClasses.contains(rhsClass))) {
                    isEquals = lhs.equals(rhs);
                } else {
                    reflectionAppend(lhs, rhs, testClass);
                    while (testClass.getSuperclass() != null && testClass != reflectUpToClass) {
                        testClass = testClass.getSuperclass();
                        reflectionAppend(lhs, rhs, testClass);
                    }
                }
            }
        } catch (final IllegalArgumentException e) {
            isEquals = false;
            return this;
        }
        return this;
    }

    /**
     * 附加由给定类的给定对象定义的字段和值
     *
     * @param lhs   左边对象
     * @param rhs   右边对象
     * @param clazz 要附加详细信息的类
     */
    private void reflectionAppend(
            final Object lhs,
            final Object rhs,
            final Class<?> clazz) {

        if (isRegistered(lhs, rhs)) {
            return;
        }

        try {
            register(lhs, rhs);
            final Field[] fields = clazz.getDeclaredFields();
            AccessibleObject.setAccessible(fields, true);
            for (int i = 0; i < fields.length && isEquals; i++) {
                final Field f = fields[i];
                if (!ArrayUtils.contains(excludeFields, f.getName())
                        && !f.getName().contains(Symbol.DOLLAR)
                        && (testTransients || !Modifier.isTransient(f.getModifiers()))
                        && !Modifier.isStatic(f.getModifiers())
                        && !f.isAnnotationPresent(EqualsExclude.class)) {
                    try {
                        append(f.get(lhs), f.get(rhs));
                    } catch (final IllegalAccessException e) {
                        throw new InternalError("Unexpected IllegalAccessException");
                    }
                }
            }
        } finally {
            unregister(lhs, rhs);
        }
    }

    public EqualsBuilder appendSuper(final boolean superEquals) {
        if (!isEquals) {
            return this;
        }
        isEquals = superEquals;
        return this;
    }

    public EqualsBuilder append(final Object lhs, final Object rhs) {
        if (!isEquals) {
            return this;
        }
        if (lhs == rhs) {
            return this;
        }
        if (lhs == null || rhs == null) {
            this.setEquals(false);
            return this;
        }
        final Class<?> lhsClass = lhs.getClass();
        if (lhsClass.isArray()) {
            appendArray(lhs, rhs);
        } else {
            if (testRecursive && !ClassUtils.isPrimitiveOrWrapper(lhsClass)) {
                reflectionAppend(lhs, rhs);
            } else {
                isEquals = lhs.equals(rhs);
            }
        }
        return this;
    }

    private void appendArray(final Object lhs, final Object rhs) {
        if (lhs.getClass() != rhs.getClass()) {
            this.setEquals(false);
        } else if (lhs instanceof long[]) {
            append((long[]) lhs, (long[]) rhs);
        } else if (lhs instanceof int[]) {
            append((int[]) lhs, (int[]) rhs);
        } else if (lhs instanceof short[]) {
            append((short[]) lhs, (short[]) rhs);
        } else if (lhs instanceof char[]) {
            append((char[]) lhs, (char[]) rhs);
        } else if (lhs instanceof byte[]) {
            append((byte[]) lhs, (byte[]) rhs);
        } else if (lhs instanceof double[]) {
            append((double[]) lhs, (double[]) rhs);
        } else if (lhs instanceof float[]) {
            append((float[]) lhs, (float[]) rhs);
        } else if (lhs instanceof boolean[]) {
            append((boolean[]) lhs, (boolean[]) rhs);
        } else {
            // Not an array of primitives
            append((Object[]) lhs, (Object[]) rhs);
        }
    }

    public EqualsBuilder append(final long lhs, final long rhs) {
        if (!isEquals) {
            return this;
        }
        isEquals = lhs == rhs;
        return this;
    }

    public EqualsBuilder append(final int lhs, final int rhs) {
        if (!isEquals) {
            return this;
        }
        isEquals = lhs == rhs;
        return this;
    }

    public EqualsBuilder append(final short lhs, final short rhs) {
        if (!isEquals) {
            return this;
        }
        isEquals = lhs == rhs;
        return this;
    }

    public EqualsBuilder append(final char lhs, final char rhs) {
        if (!isEquals) {
            return this;
        }
        isEquals = lhs == rhs;
        return this;
    }

    public EqualsBuilder append(final byte lhs, final byte rhs) {
        if (!isEquals) {
            return this;
        }
        isEquals = lhs == rhs;
        return this;
    }

    public EqualsBuilder append(final double lhs, final double rhs) {
        if (!isEquals) {
            return this;
        }
        return append(Double.doubleToLongBits(lhs), Double.doubleToLongBits(rhs));
    }

    public EqualsBuilder append(final float lhs, final float rhs) {
        if (!isEquals) {
            return this;
        }
        return append(Float.floatToIntBits(lhs), Float.floatToIntBits(rhs));
    }

    public EqualsBuilder append(final boolean lhs, final boolean rhs) {
        if (!isEquals) {
            return this;
        }
        isEquals = lhs == rhs;
        return this;
    }

    public EqualsBuilder append(final Object[] lhs, final Object[] rhs) {
        if (!isEquals) {
            return this;
        }
        if (lhs == rhs) {
            return this;
        }
        if (lhs == null || rhs == null) {
            this.setEquals(false);
            return this;
        }
        if (lhs.length != rhs.length) {
            this.setEquals(false);
            return this;
        }
        for (int i = 0; i < lhs.length && isEquals; ++i) {
            append(lhs[i], rhs[i]);
        }
        return this;
    }

    public EqualsBuilder append(final long[] lhs, final long[] rhs) {
        if (!isEquals) {
            return this;
        }
        if (lhs == rhs) {
            return this;
        }
        if (lhs == null || rhs == null) {
            this.setEquals(false);
            return this;
        }
        if (lhs.length != rhs.length) {
            this.setEquals(false);
            return this;
        }
        for (int i = 0; i < lhs.length && isEquals; ++i) {
            append(lhs[i], rhs[i]);
        }
        return this;
    }

    public EqualsBuilder append(final int[] lhs, final int[] rhs) {
        if (!isEquals) {
            return this;
        }
        if (lhs == rhs) {
            return this;
        }
        if (lhs == null || rhs == null) {
            this.setEquals(false);
            return this;
        }
        if (lhs.length != rhs.length) {
            this.setEquals(false);
            return this;
        }
        for (int i = 0; i < lhs.length && isEquals; ++i) {
            append(lhs[i], rhs[i]);
        }
        return this;
    }

    public EqualsBuilder append(final short[] lhs, final short[] rhs) {
        if (!isEquals) {
            return this;
        }
        if (lhs == rhs) {
            return this;
        }
        if (lhs == null || rhs == null) {
            this.setEquals(false);
            return this;
        }
        if (lhs.length != rhs.length) {
            this.setEquals(false);
            return this;
        }
        for (int i = 0; i < lhs.length && isEquals; ++i) {
            append(lhs[i], rhs[i]);
        }
        return this;
    }

    public EqualsBuilder append(final char[] lhs, final char[] rhs) {
        if (!isEquals) {
            return this;
        }
        if (lhs == rhs) {
            return this;
        }
        if (lhs == null || rhs == null) {
            this.setEquals(false);
            return this;
        }
        if (lhs.length != rhs.length) {
            this.setEquals(false);
            return this;
        }
        for (int i = 0; i < lhs.length && isEquals; ++i) {
            append(lhs[i], rhs[i]);
        }
        return this;
    }

    public EqualsBuilder append(final byte[] lhs, final byte[] rhs) {
        if (!isEquals) {
            return this;
        }
        if (lhs == rhs) {
            return this;
        }
        if (lhs == null || rhs == null) {
            this.setEquals(false);
            return this;
        }
        if (lhs.length != rhs.length) {
            this.setEquals(false);
            return this;
        }
        for (int i = 0; i < lhs.length && isEquals; ++i) {
            append(lhs[i], rhs[i]);
        }
        return this;
    }

    public EqualsBuilder append(final double[] lhs, final double[] rhs) {
        if (!isEquals) {
            return this;
        }
        if (lhs == rhs) {
            return this;
        }
        if (lhs == null || rhs == null) {
            this.setEquals(false);
            return this;
        }
        if (lhs.length != rhs.length) {
            this.setEquals(false);
            return this;
        }
        for (int i = 0; i < lhs.length && isEquals; ++i) {
            append(lhs[i], rhs[i]);
        }
        return this;
    }

    public EqualsBuilder append(final float[] lhs, final float[] rhs) {
        if (!isEquals) {
            return this;
        }
        if (lhs == rhs) {
            return this;
        }
        if (lhs == null || rhs == null) {
            this.setEquals(false);
            return this;
        }
        if (lhs.length != rhs.length) {
            this.setEquals(false);
            return this;
        }
        for (int i = 0; i < lhs.length && isEquals; ++i) {
            append(lhs[i], rhs[i]);
        }
        return this;
    }

    public EqualsBuilder append(final boolean[] lhs, final boolean[] rhs) {
        if (!isEquals) {
            return this;
        }
        if (lhs == rhs) {
            return this;
        }
        if (lhs == null || rhs == null) {
            this.setEquals(false);
            return this;
        }
        if (lhs.length != rhs.length) {
            this.setEquals(false);
            return this;
        }
        for (int i = 0; i < lhs.length && isEquals; ++i) {
            append(lhs[i], rhs[i]);
        }
        return this;
    }

    public boolean isEquals() {
        return this.isEquals;
    }

    protected void setEquals(final boolean isEquals) {
        this.isEquals = isEquals;
    }

    @Override
    public Boolean build() {
        return Boolean.valueOf(isEquals());
    }

    public void reset() {
        this.isEquals = true;
    }

}
