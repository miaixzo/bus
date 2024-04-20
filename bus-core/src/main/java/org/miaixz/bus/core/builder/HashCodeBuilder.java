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


import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.toolkit.ArrayKit;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * 协助实现{@link Object#hashCode()}方法
 *
 *
 * <p>
 * 下面是采取的方法。添加数据字段时，将当前总数乘以乘数，然后添加该数据类型的相关值。
 * 例如，如果当前的hashCode是17，而乘数是37，整数45将创建一个674的散列代码，17 * 37 + 45.
 * </p>
 *
 * <pre>
 * public class Person {
 *   String name;
 *   int age;
 *   boolean smoker;
 *   ...
 *
 *   public int hashCode() {
 *     return new HashCodeBuilder(17, 37).
 *       append(name).
 *       append(age).
 *       append(smoker).
 *       toHashCode();
 *   }
 * }
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HashCodeBuilder implements Builder<Integer> {

    /**
     * T在反射哈希代码构建中使用默认的初始值.
     */
    private static final int DEFAULT_INITIAL_VALUE = 17;

    /**
     * 在反射散列代码构建中使用的默认乘法器值.
     */
    private static final int DEFAULT_MULTIPLIER_VALUE = 37;

    /**
     * 反射方法用于检测循环对象引用和避免无限循环的对象注册表
     */
    private static final ThreadLocal<Set<HashKey>> REGISTRY = new ThreadLocal<>();

    /**
     * 用于构建hashCode的常量.
     */
    private final int iConstant;
    /**
     * 运行hashCode的total.
     */
    private int iTotal = 0;

    public HashCodeBuilder() {
        iConstant = 37;
        iTotal = 17;
    }

    /**
     * 必须输入两个随机选择的奇数。理想情况下，
     * 每个类应该是不同的，但是这不是很重要.
     *
     * @param initialOddNumber    用作初值的奇数
     * @param multiplierOddNumber 用作乘法器的奇数
     */
    public HashCodeBuilder(final int initialOddNumber, final int multiplierOddNumber) {
        Assert.isTrue(initialOddNumber % 2 != 0, "HashCodeBuilder requires an odd initial value");
        Assert.isTrue(multiplierOddNumber % 2 != 0, "HashCodeBuilder requires an odd multiplier");
        iConstant = multiplierOddNumber;
        iTotal = initialOddNumber;
    }

    static Set<HashKey> getRegistry() {
        return REGISTRY.get();
    }

    static boolean isRegistered(final Object value) {
        final Set<HashKey> registry = getRegistry();
        return null != registry && registry.contains(new HashKey(value));
    }

    /**
     * 附加由给定类的给定对象定义的字段和值
     *
     * @param object        要附加详细信息的对象
     * @param clazz         要附加详细信息的类
     * @param builder       要附加到的生成器
     * @param useTransients 是否使用忽略字段
     * @param excludeFields 在计算哈希码时排除的字符串字段名的集合
     */
    private static void reflectionAppend(final Object object, final Class<?> clazz, final HashCodeBuilder builder, final boolean useTransients,
                                         final String[] excludeFields) {
        if (isRegistered(object)) {
            return;
        }
        try {
            register(object);
            final Field[] fields = clazz.getDeclaredFields();
            AccessibleObject.setAccessible(fields, true);
            for (final Field field : fields) {
                if (!ArrayKit.contains(excludeFields, field.getName())
                        && !field.getName().contains(Symbol.DOLLAR)
                        && (useTransients || !Modifier.isTransient(field.getModifiers()))
                        && !Modifier.isStatic(field.getModifiers())
                        && !field.isAnnotationPresent(HashCodeExclude.class)) {
                    try {
                        final Object fieldValue = field.get(object);
                        builder.append(fieldValue);
                    } catch (final IllegalAccessException e) {
                        throw new InternalError("Unexpected IllegalAccessException");
                    }
                }
            }
        } finally {
            unregister(object);
        }
    }

    public static int reflectionHashCode(final int initialNonZeroOddNumber, final int multiplierNonZeroOddNumber, final Object object) {
        return reflectionHashCode(initialNonZeroOddNumber, multiplierNonZeroOddNumber, object, false, null);
    }

    public static int reflectionHashCode(final int initialNonZeroOddNumber, final int multiplierNonZeroOddNumber, final Object object,
                                         final boolean testTransients) {
        return reflectionHashCode(initialNonZeroOddNumber, multiplierNonZeroOddNumber, object, testTransients, null);
    }

    /**
     * 使用反射从{@code Object}的字段构建有效的散列代码
     *
     * <p>
     * 它使用<code>AccessibleObject.setAccessible</code> 获得对私有字段的访问权。
     * 这意味着如果权限没有正确设置，则在安全管理器下运行时将抛出安全异常。它也没有显式测试那么有效.
     * </p>
     *
     * @param <T>                        所涉及对象的类型
     * @param initialNonZeroOddNumber    initialNonZeroOddNumber非零,奇数作为初始值。如果在散列代码中没有找到包含的字段，则返回该值
     * @param multiplierNonZeroOddNumber 用作乘法器的非零的奇数
     * @param object                     用于创建hashCode的对象
     * @param testTransients             是否包含瞬态字段
     * @param reflectUpToClass           反映到(包括)的超类可以是null
     * @param excludeFields              在计算哈希码时要排除的字段名数组
     * @return int散列码
     */
    public static <T> int reflectionHashCode(final int initialNonZeroOddNumber,
                                             final int multiplierNonZeroOddNumber,
                                             final T object,
                                             final boolean testTransients,
                                             final Class<? super T> reflectUpToClass,
                                             final String... excludeFields) {
        Assert.isTrue(null != object, "The object to build a hash code for must not be null");
        final HashCodeBuilder builder = new HashCodeBuilder(initialNonZeroOddNumber, multiplierNonZeroOddNumber);
        Class<?> clazz = object.getClass();
        reflectionAppend(object, clazz, builder, testTransients, excludeFields);
        while (null != clazz.getSuperclass() && clazz != reflectUpToClass) {
            clazz = clazz.getSuperclass();
            reflectionAppend(object, clazz, builder, testTransients, excludeFields);
        }
        return builder.toHashCode();
    }

    public static int reflectionHashCode(final Object object, final boolean testTransients) {
        return reflectionHashCode(DEFAULT_INITIAL_VALUE, DEFAULT_MULTIPLIER_VALUE, object,
                testTransients, null);
    }

    public static int reflectionHashCode(final Object object, final Collection<String> excludeFields) {
        return reflectionHashCode(object, ReflectionToStringBuilder.toNoNullStringArray(excludeFields));
    }

    public static int reflectionHashCode(final Object object, final String... excludeFields) {
        return reflectionHashCode(DEFAULT_INITIAL_VALUE, DEFAULT_MULTIPLIER_VALUE, object, false,
                null, excludeFields);
    }

    private static void register(final Object value) {
        Set<HashKey> registry = getRegistry();
        if (null == registry) {
            registry = new HashSet<>();
            REGISTRY.set(registry);
        }
        registry.add(new HashKey(value));
    }

    private static void unregister(final Object value) {
        final Set<HashKey> registry = getRegistry();
        if (null != registry) {
            registry.remove(new HashKey(value));
            if (registry.isEmpty()) {
                REGISTRY.remove();
            }
        }
    }

    public HashCodeBuilder append(final boolean value) {
        iTotal = iTotal * iConstant + (value ? 0 : 1);
        return this;
    }

    public HashCodeBuilder append(final boolean[] array) {
        if (null == array) {
            iTotal = iTotal * iConstant;
        } else {
            for (final boolean element : array) {
                append(element);
            }
        }
        return this;
    }

    public HashCodeBuilder append(final byte value) {
        iTotal = iTotal * iConstant + value;
        return this;
    }

    public HashCodeBuilder append(final byte[] array) {
        if (null == array) {
            iTotal = iTotal * iConstant;
        } else {
            for (final byte element : array) {
                append(element);
            }
        }
        return this;
    }

    public HashCodeBuilder append(final char value) {
        iTotal = iTotal * iConstant + value;
        return this;
    }

    public HashCodeBuilder append(final char[] array) {
        if (null == array) {
            iTotal = iTotal * iConstant;
        } else {
            for (final char element : array) {
                append(element);
            }
        }
        return this;
    }

    public HashCodeBuilder append(final double value) {
        return append(Double.doubleToLongBits(value));
    }

    public HashCodeBuilder append(final double[] array) {
        if (null == array) {
            iTotal = iTotal * iConstant;
        } else {
            for (final double element : array) {
                append(element);
            }
        }
        return this;
    }

    public HashCodeBuilder append(final float value) {
        iTotal = iTotal * iConstant + Float.floatToIntBits(value);
        return this;
    }

    public HashCodeBuilder append(final float[] array) {
        if (null == array) {
            iTotal = iTotal * iConstant;
        } else {
            for (final float element : array) {
                append(element);
            }
        }
        return this;
    }

    public HashCodeBuilder append(final int value) {
        iTotal = iTotal * iConstant + value;
        return this;
    }

    public HashCodeBuilder append(final int[] array) {
        if (null == array) {
            iTotal = iTotal * iConstant;
        } else {
            for (final int element : array) {
                append(element);
            }
        }
        return this;
    }

    public HashCodeBuilder append(final long value) {
        iTotal = iTotal * iConstant + ((int) (value ^ (value >> Normal._32)));
        return this;
    }

    public HashCodeBuilder append(final long[] array) {
        if (null == array) {
            iTotal = iTotal * iConstant;
        } else {
            for (final long element : array) {
                append(element);
            }
        }
        return this;
    }

    public HashCodeBuilder append(final Object object) {
        if (null == object) {
            iTotal = iTotal * iConstant;

        } else {
            if (object.getClass().isArray()) {
                appendArray(object);
            } else {
                iTotal = iTotal * iConstant + object.hashCode();
            }
        }
        return this;
    }

    private void appendArray(final Object object) {
        if (object instanceof long[]) {
            append((long[]) object);
        } else if (object instanceof int[]) {
            append((int[]) object);
        } else if (object instanceof short[]) {
            append((short[]) object);
        } else if (object instanceof char[]) {
            append((char[]) object);
        } else if (object instanceof byte[]) {
            append((byte[]) object);
        } else if (object instanceof double[]) {
            append((double[]) object);
        } else if (object instanceof float[]) {
            append((float[]) object);
        } else if (object instanceof boolean[]) {
            append((boolean[]) object);
        } else {
            append((Object[]) object);
        }
    }

    public HashCodeBuilder append(final Object[] array) {
        if (null == array) {
            iTotal = iTotal * iConstant;
        } else {
            for (final Object element : array) {
                append(element);
            }
        }
        return this;
    }

    public HashCodeBuilder append(final short value) {
        iTotal = iTotal * iConstant + value;
        return this;
    }

    public HashCodeBuilder append(final short[] array) {
        if (null == array) {
            iTotal = iTotal * iConstant;
        } else {
            for (final short element : array) {
                append(element);
            }
        }
        return this;
    }

    public HashCodeBuilder appendSuper(final int superHashCode) {
        iTotal = iTotal * iConstant + superHashCode;
        return this;
    }

    public int toHashCode() {
        return iTotal;
    }

    @Override
    public Integer build() {
        return toHashCode();
    }

    @Override
    public int hashCode() {
        return toHashCode();
    }

}
