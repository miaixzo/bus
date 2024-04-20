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

import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.core.toolkit.ArrayKit;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * {@link Object#equals(Object)} 方法的构建器
 * 两个对象equals必须保证hashCode值相等
 * ,hashCode值相等不能保证一定相等
 *
 * <p>使用方法如下：</p>
 * <pre>
 * public boolean equals(Object object) {
 *   if (null == object) { return false; }
 *   if (object == this) { return true; }
 *   if (object.getClass() != getClass()) {
 *     return false;
 *   }
 *   MyClass rhs = (MyClass) object;
 *   return new EqualsBuilder()
 *                 .appendSuper(super.equals(object))
 *                 .append(field1, rhs.field1)
 *                 .append(field2, rhs.field2)
 *                 .append(field3, rhs.field3)
 *                 .isEquals();
 *  }
 *
 * public boolean equals(Object object) {
 *   return EqualsBuilder.reflectionEquals(this, object);
 * }
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class EqualsBuilder implements Builder<Boolean> {

    private static final long serialVersionUID = 1L;

    /**
     * 反射方法用于检测循环对象引用和避免无限循环的对象注册表
     */
    private static final ThreadLocal<Set<Pair<HashKey, HashKey>>> REGISTRY = new ThreadLocal<>();

    /**
     * 是否equals，此值随着构建会变更，默认true
     */
    private boolean isEquals = true;

    /**
     * 构造，初始状态值为true
     */
    public EqualsBuilder() {

    }

    /**
     * 返回当前线程中的反射方法遍历的对象对的注册表
     *
     * @return 设置要遍历的对象的注册表
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
     */
    static boolean isRegistered(final Object lhs, final Object rhs) {
        final Set<Pair<HashKey, HashKey>> registry = getRegistry();
        final Pair<HashKey, HashKey> pair = getRegisterPair(lhs, rhs);
        final Pair<HashKey, HashKey> swappedPair = Pair.of(pair.getKey(), pair.getValue());

        return null != registry
                && (registry.contains(pair) || registry.contains(swappedPair));
    }

    /**
     * 注册给定的对象对
     * 用于反射方法避免无限循环
     *
     * @param lhs 要注册的对象
     * @param rhs 另一个要注册的对象
     */
    static void register(final Object lhs, final Object rhs) {
        synchronized (EqualsBuilder.class) {
            if (null == getRegistry()) {
                REGISTRY.set(new HashSet<>());
            }
        }

        final Set<Pair<HashKey, HashKey>> registry = getRegistry();
        final Pair<HashKey, HashKey> pair = getRegisterPair(lhs, rhs);
        registry.add(pair);
    }

    /**
     * 注销给定的对象对
     * 使用反射方法避免无限循环
     *
     * @param lhs 要注销此对象
     * @param rhs 另一个要注册的对象
     */
    static void unregister(final Object lhs, final Object rhs) {
        Set<Pair<HashKey, HashKey>> registry = getRegistry();
        if (null != registry) {
            final Pair<HashKey, HashKey> pair = getRegisterPair(lhs, rhs);
            registry.remove(pair);
            synchronized (EqualsBuilder.class) {
                registry = getRegistry();
                if (null != registry && registry.isEmpty()) {
                    REGISTRY.remove();
                }
            }
        }
    }

    /**
     * 反射检查两个对象是否equals,此方法检查对象及其父对象的属性(包括私有属性)是否相等
     *
     * @param lhs           此对象
     * @param rhs           另一个对象
     * @param excludeFields 排除的字段集合,如果有不参与计算equals的字段加入此集合即可
     * @return 两个对象是否equals, 是返回<code>true</code>
     */
    public static boolean reflectionEquals(final Object lhs, final Object rhs, final Collection<String> excludeFields) {
        return reflectionEquals(lhs, rhs, ArrayKit.toArray(excludeFields, String.class));
    }

    /**
     * 反射检查两个对象是否equals,此方法检查对象及其父对象的属性(包括私有属性)是否相等
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
     * @return true如果两个对象已测试相等
     */
    public static boolean reflectionEquals(final Object lhs, final Object rhs, final boolean testTransients, final Class<?> reflectUpToClass,
                                           final String... excludeFields) {
        if (lhs == rhs) {
            return true;
        }
        if (null == lhs || null == rhs) {
            return false;
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
            return false;
        }
        final EqualsBuilder equalsBuilder = new EqualsBuilder();
        try {
            if (testClass.isArray()) {
                equalsBuilder.append(lhs, rhs);
            } else {
                reflectionAppend(lhs, rhs, testClass, equalsBuilder, testTransients, excludeFields);
                while (null != testClass.getSuperclass() && testClass != reflectUpToClass) {
                    testClass = testClass.getSuperclass();
                    reflectionAppend(lhs, rhs, testClass, equalsBuilder, testTransients, excludeFields);
                }
            }
        } catch (final IllegalArgumentException e) {
            return false;
        }
        return equalsBuilder.isEquals();
    }

    /**
     * 附加由给定类的给定对象定义的字段和值
     *
     * @param lhs           左边对象
     * @param rhs           右边对象
     * @param clazz         要附加详细信息的类
     * @param builder       要附加的构建器
     * @param useTransients 是否包含修饰符
     * @param excludeFields 要从比较中排除的字段名称数组
     */
    private static void reflectionAppend(
            final Object lhs,
            final Object rhs,
            final Class<?> clazz,
            final EqualsBuilder builder,
            final boolean useTransients,
            final String[] excludeFields) {

        if (isRegistered(lhs, rhs)) {
            return;
        }

        try {
            register(lhs, rhs);
            final Field[] fields = clazz.getDeclaredFields();
            AccessibleObject.setAccessible(fields, true);
            for (int i = 0; i < fields.length && builder.isEquals; i++) {
                final Field f = fields[i];
                if (false == ArrayKit.contains(excludeFields, f.getName())
                        && (f.getName().indexOf('$') == -1)
                        && (useTransients || !Modifier.isTransient(f.getModifiers()))
                        && (!Modifier.isStatic(f.getModifiers()))) {
                    try {
                        builder.append(f.get(lhs), f.get(rhs));
                    } catch (final IllegalAccessException e) {
                        throw new InternalError("Unexpected IllegalAccessException");
                    }
                }
            }
        } finally {
            unregister(lhs, rhs);
        }
    }

    /**
     * 将<code>super.equals()</code>的结果添加到此构建器
     *
     * @param superEquals 调用<code> super.equals()</code>的结果
     * @return EqualsBuilder - 自定义返回链
     */
    public EqualsBuilder appendSuper(final boolean superEquals) {
        if (isEquals == false) {
            return this;
        }
        isEquals = superEquals;
        return this;
    }

    /**
     * 使用两个<code>equals</code>方法比较两个<code>Object</code>是否相等
     *
     * @param lhs 左边对象
     * @param rhs 右边对象
     * @return EqualsBuilder - 自定义返回链
     */
    public EqualsBuilder append(final Object lhs, final Object rhs) {
        if (isEquals == false) {
            return this;
        }
        if (lhs == rhs) {
            return this;
        }
        if (lhs == null || rhs == null) {
            return setEquals(false);
        }
        if (ArrayKit.isArray(lhs)) {
            // 判断数组的equals
            return setEquals(ArrayKit.equals(lhs, rhs));
        }

        // The simple case, not an array, just test the element
        return setEquals(lhs.equals(rhs));
    }

    /**
     * 比较两个<code>long</code>是否相等
     *
     * @param lhs 左边对象
     * @param rhs 右边对象
     * @return EqualsBuilder - 自定义返回链
     */
    public EqualsBuilder append(final long lhs, final long rhs) {
        if (isEquals == false) {
            return this;
        }
        isEquals = (lhs == rhs);
        return this;
    }

    /**
     * 比较两个<code>int</code>是否相等
     *
     * @param lhs 左边对象
     * @param rhs 右边对象
     * @return EqualsBuilder - 自定义返回链
     */
    public EqualsBuilder append(final int lhs, final int rhs) {
        if (isEquals == false) {
            return this;
        }
        isEquals = (lhs == rhs);
        return this;
    }

    /**
     * 比较两个<code>short</code>是否相等
     *
     * @param lhs 左边对象
     * @param rhs 右边对象
     * @return EqualsBuilder - 自定义返回链
     */
    public EqualsBuilder append(final short lhs, final short rhs) {
        if (isEquals == false) {
            return this;
        }
        isEquals = (lhs == rhs);
        return this;
    }

    /**
     * 比较两个<code>char</code>是否相等
     *
     * @param lhs 左边对象
     * @param rhs 右边对象
     * @return EqualsBuilder - 自定义返回链
     */
    public EqualsBuilder append(final char lhs, final char rhs) {
        if (isEquals == false) {
            return this;
        }
        isEquals = (lhs == rhs);
        return this;
    }

    /**
     * 比较两个<code>byte</code>是否相等
     *
     * @param lhs 左边对象
     * @param rhs 右边对象
     * @return EqualsBuilder - 自定义返回链
     */
    public EqualsBuilder append(final byte lhs, final byte rhs) {
        if (isEquals == false) {
            return this;
        }
        isEquals = (lhs == rhs);
        return this;
    }

    /**
     * 通过比较<code>doubleToLong</code>返回的位的模式是否相等来比较两个<code>double</code>是否相等
     *
     * @param lhs 左边对象
     * @param rhs 右边对象
     * @return EqualsBuilder - 自定义返回链
     */
    public EqualsBuilder append(final double lhs, final double rhs) {
        if (isEquals == false) {
            return this;
        }
        return append(Double.doubleToLongBits(lhs), Double.doubleToLongBits(rhs));
    }

    /**
     * <p>Test if two <code>float</code>s are equal byt testing that the
     * pattern of bits returned by doubleToLong are equal.</p>
     *
     * <p>This handles NaNs, Infinities, and <code>-0.0</code>.</p>
     *
     * <p>It is compatible with the hash code generated by
     * <code>HashCodeBuilder</code>.</p>
     *
     * @param lhs 左边对象 <code>float</code>
     * @param rhs 右边对象 <code>float</code>
     * @return EqualsBuilder - 自定义返回链
     */
    public EqualsBuilder append(final float lhs, final float rhs) {
        if (isEquals == false) {
            return this;
        }
        return append(Float.floatToIntBits(lhs), Float.floatToIntBits(rhs));
    }

    /**
     * 比较两个<code>boolean</code>是否相等
     *
     * @param lhs 左边对象 <code>boolean</code>
     * @param rhs 右边对象 <code>boolean</code>
     * @return EqualsBuilder - 自定义返回链
     */
    public EqualsBuilder append(final boolean lhs, final boolean rhs) {
        if (isEquals == false) {
            return this;
        }
        isEquals = (lhs == rhs);
        return this;
    }

    /**
     * 如果已选中的字段全部相等，则返回<code>true</code>
     *
     * @return boolean
     */
    public boolean isEquals() {
        return this.isEquals;
    }

    /**
     * 设置<code>isEquals</code>值
     *
     * @param isEquals 设定值
     * @return this
     */
    protected EqualsBuilder setEquals(boolean isEquals) {
        this.isEquals = isEquals;
        return this;
    }

    /**
     * 如果已选中的字段全部相等，则返回<code>true</code>
     *
     * @return 如果所有已检查的字段都相等，则<code>true</code>否则<code>false</code>
     */
    @Override
    public Boolean build() {
        return isEquals();
    }

    /**
     * 重置EqualsBuilder，以便您可以再次使用同一对象
     */
    public void reset() {
        this.isEquals = true;
    }

}
