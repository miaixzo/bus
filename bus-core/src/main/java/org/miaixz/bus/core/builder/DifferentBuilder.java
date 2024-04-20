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
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.toolkit.ArrayKit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 协助实现{@link Differentable#diff(Object)}方法
 *
 * <pre>
 * public class Person implements Diffable&lt;Person&gt; {
 *   String name;
 *   int age;
 *   boolean smoker;
 *
 *   ...
 *
 *   public DiffResult diff(Person object) {
 *     // No need for null check, as NullPointerException correct if object is null
 *     return new DiffBuilder(this, object, ToStringStyle.SHORT_PREFIX_STYLE)
 *       .append("name", this.name, object.name)
 *       .append("age", this.age, object.age)
 *       .append("smoker", this.smoker, object.smoker)
 *       .build();
 *   }
 * }
 * </pre>
 * 传递给构造函数的{@code ToStringStyle}嵌入到返回的{@code DiffResult}中，
 * 并影响{@code DiffResult. tostring()}方法的风格。可以通过调用
 * {@link DifferentResult#toString(ToStringStyle)}覆盖此样式选择。.
 *
 * @author Kimi Liu
 * @see Differentable
 * @see Different
 * @see DifferentResult
 * @see ToStringStyle
 * @since Java 17+
 */
public class DifferentBuilder implements Builder<DifferentResult> {

    private final List<Different<?>> differents;
    private final boolean objectsTriviallyEqual;
    private final Object left;
    private final Object right;
    private final ToStringStyle style;

    /**
     * 使用指定样式为指定对象构造一个生成器
     * 如果{@code lhs == rhs}或{@code lhs.equals(rhs)}，
     * 则构建器将不计算对{@code append(…)}的任何调用，
     * 并在{@link #build()}执行时返回一个空的{@link DifferentResult}.
     *
     * @param lhs                {@code this} 对象
     * @param rhs                反对的对象
     * @param style              当输出对象时将使用该样式，{@code null}使用默认值
     * @param testTriviallyEqual 如果为真，这将测试lhs和rhs是否相同或相等。如果启用了简单的相等测试并返回true，
     *                           那么所有的append(fieldName、lhs、rhs)方法都将中止，而不创建字段{@link Different}
     *                           这个测试的结果在{@link DifferentBuilder}的整个生命周期内都不会改变。.
     */
    public DifferentBuilder(final Object lhs,
                            final Object rhs,
                            final ToStringStyle style,
                            final boolean testTriviallyEqual) {

        Assert.isTrue(null != lhs, "lhs cannot be null");
        Assert.isTrue(null != rhs, "rhs cannot be null");

        this.differents = new ArrayList<>();
        this.left = lhs;
        this.right = rhs;
        this.style = style;

        this.objectsTriviallyEqual = testTriviallyEqual && (lhs == rhs || lhs.equals(rhs));
    }

    /**
     * 使用指定样式为指定对象构造一个生成器
     *
     * @param lhs   {@code this} 对象
     * @param rhs   反对的对象
     * @param style 当输出对象时将使用该样式，{@code null}使用默认值
     */
    public DifferentBuilder(final Object lhs,
                            final Object rhs,
                            final ToStringStyle style) {
        this(lhs, rhs, style, true);
    }

    /**
     * 测试两个{@code boolean}是否相等
     *
     * @param fieldName 字段名
     * @param lhs       左边 {@code boolean}
     * @param rhs       右边 {@code boolean}
     * @return this
     */
    public DifferentBuilder append(final String fieldName,
                                   final boolean lhs,
                                   final boolean rhs) {
        validateFieldNameNotNull(fieldName);

        if (objectsTriviallyEqual) {
            return this;
        }
        if (lhs != rhs) {
            differents.add(new Different<Boolean>(fieldName) {
                private static final long serialVersionUID = 1L;

                @Override
                public Boolean getLeft() {
                    return Boolean.valueOf(lhs);
                }

                @Override
                public Boolean getRight() {
                    return Boolean.valueOf(rhs);
                }
            });
        }
        return this;
    }

    /**
     * 测试两个{@code boolean[]}是否相等
     *
     * @param fieldName 字段名
     * @param lhs       左边 {@code boolean[]}
     * @param rhs       右边 {@code boolean[]}
     * @return this
     */
    public DifferentBuilder append(final String fieldName,
                                   final boolean[] lhs,
                                   final boolean[] rhs) {
        validateFieldNameNotNull(fieldName);
        if (objectsTriviallyEqual) {
            return this;
        }
        if (!Arrays.equals(lhs, rhs)) {
            differents.add(new Different<Boolean[]>(fieldName) {
                private static final long serialVersionUID = 1L;

                @Override
                public Boolean[] getLeft() {
                    return ArrayKit.toObject(lhs);
                }

                @Override
                public Boolean[] getRight() {
                    return ArrayKit.toObject(rhs);
                }
            });
        }
        return this;
    }

    /**
     * 测试两个{@code byte}是否相等
     *
     * @param fieldName 字段名
     * @param lhs       左边 {@code byte}
     * @param rhs       右边 {@code byte}
     * @return this
     */
    public DifferentBuilder append(final String fieldName,
                                   final byte lhs,
                                   final byte rhs) {
        validateFieldNameNotNull(fieldName);
        if (objectsTriviallyEqual) {
            return this;
        }
        if (lhs != rhs) {
            differents.add(new Different<Byte>(fieldName) {
                private static final long serialVersionUID = 1L;

                @Override
                public Byte getLeft() {
                    return Byte.valueOf(lhs);
                }

                @Override
                public Byte getRight() {
                    return Byte.valueOf(rhs);
                }
            });
        }
        return this;
    }

    /**
     * 测试两个{@code byte[]}是否相等
     *
     * @param fieldName 字段名
     * @param lhs       左边 {@code byte[]}
     * @param rhs       右边 {@code byte[]}
     * @return this
     */
    public DifferentBuilder append(final String fieldName,
                                   final byte[] lhs,
                                   final byte[] rhs) {
        validateFieldNameNotNull(fieldName);

        if (objectsTriviallyEqual) {
            return this;
        }
        if (!Arrays.equals(lhs, rhs)) {
            differents.add(new Different<Byte[]>(fieldName) {
                private static final long serialVersionUID = 1L;

                @Override
                public Byte[] getLeft() {
                    return ArrayKit.toObject(lhs);
                }

                @Override
                public Byte[] getRight() {
                    return ArrayKit.toObject(rhs);
                }
            });
        }
        return this;
    }

    /**
     * 测试两个{@code char}是否相等
     *
     * @param fieldName 字段名
     * @param lhs       左边 {@code char}
     * @param rhs       右边 {@code char}
     * @return this
     */
    public DifferentBuilder append(final String fieldName,
                                   final char lhs,
                                   final char rhs) {
        validateFieldNameNotNull(fieldName);

        if (objectsTriviallyEqual) {
            return this;
        }
        if (lhs != rhs) {
            differents.add(new Different<Character>(fieldName) {
                private static final long serialVersionUID = 1L;

                @Override
                public Character getLeft() {
                    return Character.valueOf(lhs);
                }

                @Override
                public Character getRight() {
                    return Character.valueOf(rhs);
                }
            });
        }
        return this;
    }

    /**
     * 测试两个{@code char[]}是否相等
     *
     * @param fieldName 字段名
     * @param lhs       左边 {@code char[]}
     * @param rhs       右边 {@code char[]}
     * @return this
     * @throws IllegalArgumentException if field name is {@code null}
     */
    public DifferentBuilder append(final String fieldName, final char[] lhs,
                                   final char[] rhs) {
        validateFieldNameNotNull(fieldName);

        if (objectsTriviallyEqual) {
            return this;
        }
        if (!Arrays.equals(lhs, rhs)) {
            differents.add(new Different<Character[]>(fieldName) {
                private static final long serialVersionUID = 1L;

                @Override
                public Character[] getLeft() {
                    return ArrayKit.toObject(lhs);
                }

                @Override
                public Character[] getRight() {
                    return ArrayKit.toObject(rhs);
                }
            });
        }
        return this;
    }

    /**
     * 测试两个{@code char[]}是否相等
     *
     * @param fieldName 字段名
     * @param lhs       左边 {@code double}
     * @param rhs       右边 {@code double}
     * @return this
     */
    public DifferentBuilder append(final String fieldName, final double lhs,
                                   final double rhs) {
        validateFieldNameNotNull(fieldName);

        if (objectsTriviallyEqual) {
            return this;
        }
        if (Double.doubleToLongBits(lhs) != Double.doubleToLongBits(rhs)) {
            differents.add(new Different<Double>(fieldName) {
                private static final long serialVersionUID = 1L;

                @Override
                public Double getLeft() {
                    return Double.valueOf(lhs);
                }

                @Override
                public Double getRight() {
                    return Double.valueOf(rhs);
                }
            });
        }
        return this;
    }

    /**
     * 测试两个 {@code double[]}是否相等
     *
     * @param fieldName 字段名
     * @param lhs       左边 {@code double[]}
     * @param rhs       右边 {@code double[]}
     * @return this
     */
    public DifferentBuilder append(final String fieldName, final double[] lhs,
                                   final double[] rhs) {
        validateFieldNameNotNull(fieldName);

        if (objectsTriviallyEqual) {
            return this;
        }
        if (!Arrays.equals(lhs, rhs)) {
            differents.add(new Different<Double[]>(fieldName) {
                private static final long serialVersionUID = 1L;

                @Override
                public Double[] getLeft() {
                    return ArrayKit.toObject(lhs);
                }

                @Override
                public Double[] getRight() {
                    return ArrayKit.toObject(rhs);
                }
            });
        }
        return this;
    }

    /**
     * 测试两个 {@code float}是否相等
     *
     * @param fieldName 字段名
     * @param lhs       左边 {@code float}
     * @param rhs       右边 {@code float}
     * @return this
     */
    public DifferentBuilder append(final String fieldName, final float lhs,
                                   final float rhs) {
        validateFieldNameNotNull(fieldName);

        if (objectsTriviallyEqual) {
            return this;
        }
        if (Float.floatToIntBits(lhs) != Float.floatToIntBits(rhs)) {
            differents.add(new Different<Float>(fieldName) {
                private static final long serialVersionUID = 1L;

                @Override
                public Float getLeft() {
                    return Float.valueOf(lhs);
                }

                @Override
                public Float getRight() {
                    return Float.valueOf(rhs);
                }
            });
        }
        return this;
    }

    /**
     * 测试两个 {@code float[]}是否相等
     *
     * @param fieldName 字段名
     * @param lhs       左边 {@code float[]}
     * @param rhs       右边 {@code float[]}
     * @return this
     */
    public DifferentBuilder append(final String fieldName, final float[] lhs,
                                   final float[] rhs) {
        validateFieldNameNotNull(fieldName);

        if (objectsTriviallyEqual) {
            return this;
        }
        if (!Arrays.equals(lhs, rhs)) {
            differents.add(new Different<Float[]>(fieldName) {
                private static final long serialVersionUID = 1L;

                @Override
                public Float[] getLeft() {
                    return ArrayKit.toObject(lhs);
                }

                @Override
                public Float[] getRight() {
                    return ArrayKit.toObject(rhs);
                }
            });
        }
        return this;
    }

    /**
     * 测试两个 {@code int}是否相等
     *
     * @param fieldName 字段名
     * @param lhs       左边 {@code int}
     * @param rhs       右边 {@code int}
     * @return this
     */
    public DifferentBuilder append(final String fieldName, final int lhs,
                                   final int rhs) {
        validateFieldNameNotNull(fieldName);

        if (objectsTriviallyEqual) {
            return this;
        }
        if (lhs != rhs) {
            differents.add(new Different<Integer>(fieldName) {
                private static final long serialVersionUID = 1L;

                @Override
                public Integer getLeft() {
                    return Integer.valueOf(lhs);
                }

                @Override
                public Integer getRight() {
                    return Integer.valueOf(rhs);
                }
            });
        }
        return this;
    }

    /**
     * 测试两个 {@code int[]}是否相等
     *
     * @param fieldName 字段名
     * @param lhs       左边 {@code int[]}
     * @param rhs       右边 {@code int[]}
     * @return this
     */
    public DifferentBuilder append(final String fieldName, final int[] lhs,
                                   final int[] rhs) {
        validateFieldNameNotNull(fieldName);

        if (objectsTriviallyEqual) {
            return this;
        }
        if (!Arrays.equals(lhs, rhs)) {
            differents.add(new Different<Integer[]>(fieldName) {
                private static final long serialVersionUID = 1L;

                @Override
                public Integer[] getLeft() {
                    return ArrayKit.toObject(lhs);
                }

                @Override
                public Integer[] getRight() {
                    return ArrayKit.toObject(rhs);
                }
            });
        }
        return this;
    }

    /**
     * 测试两个 {@code long}是否相等
     *
     * @param fieldName 字段名
     * @param lhs       左边 {@code long}
     * @param rhs       右边 {@code long}
     * @return this
     */
    public DifferentBuilder append(final String fieldName, final long lhs,
                                   final long rhs) {
        validateFieldNameNotNull(fieldName);

        if (objectsTriviallyEqual) {
            return this;
        }
        if (lhs != rhs) {
            differents.add(new Different<Long>(fieldName) {
                private static final long serialVersionUID = 1L;

                @Override
                public Long getLeft() {
                    return Long.valueOf(lhs);
                }

                @Override
                public Long getRight() {
                    return Long.valueOf(rhs);
                }
            });
        }
        return this;
    }

    /**
     * 测试两个 {@code long[]}是否相等
     *
     * @param fieldName 字段名
     * @param lhs       左边 {@code long[]}
     * @param rhs       右边 {@code long[]}
     * @return this
     */
    public DifferentBuilder append(final String fieldName, final long[] lhs,
                                   final long[] rhs) {
        validateFieldNameNotNull(fieldName);

        if (objectsTriviallyEqual) {
            return this;
        }
        if (!Arrays.equals(lhs, rhs)) {
            differents.add(new Different<Long[]>(fieldName) {
                private static final long serialVersionUID = 1L;

                @Override
                public Long[] getLeft() {
                    return ArrayKit.toObject(lhs);
                }

                @Override
                public Long[] getRight() {
                    return ArrayKit.toObject(rhs);
                }
            });
        }
        return this;
    }

    /**
     * 测试两个 {@code short}是否相等
     *
     * @param fieldName 字段名
     * @param lhs       左边 {@code short}
     * @param rhs       右边 {@code short}
     * @return this
     */
    public DifferentBuilder append(final String fieldName, final short lhs,
                                   final short rhs) {
        validateFieldNameNotNull(fieldName);

        if (objectsTriviallyEqual) {
            return this;
        }
        if (lhs != rhs) {
            differents.add(new Different<Short>(fieldName) {
                private static final long serialVersionUID = 1L;

                @Override
                public Short getLeft() {
                    return Short.valueOf(lhs);
                }

                @Override
                public Short getRight() {
                    return Short.valueOf(rhs);
                }
            });
        }
        return this;
    }

    /**
     * 测试两个 {@code short[]}是否相等
     *
     * @param fieldName 字段名
     * @param lhs       左边 {@code short[]}
     * @param rhs       右边 {@code short[]}
     * @return this
     */
    public DifferentBuilder append(final String fieldName, final short[] lhs,
                                   final short[] rhs) {
        validateFieldNameNotNull(fieldName);

        if (objectsTriviallyEqual) {
            return this;
        }
        if (!Arrays.equals(lhs, rhs)) {
            differents.add(new Different<Short[]>(fieldName) {
                private static final long serialVersionUID = 1L;

                @Override
                public Short[] getLeft() {
                    return ArrayKit.toObject(lhs);
                }

                @Override
                public Short[] getRight() {
                    return ArrayKit.toObject(rhs);
                }
            });
        }
        return this;
    }

    /**
     * 测试两个 {@code Objects}是否相等
     *
     * @param fieldName 字段名
     * @param lhs       左边 {@code Object}
     * @param rhs       右边 {@code Object}
     * @return this
     * @throws IllegalArgumentException if field name is {@code null}
     */
    public DifferentBuilder append(final String fieldName, final Object lhs,
                                   final Object rhs) {
        validateFieldNameNotNull(fieldName);
        if (objectsTriviallyEqual) {
            return this;
        }
        if (lhs == rhs) {
            return this;
        }

        Object objectToTest;
        if (null != lhs) {
            objectToTest = lhs;
        } else {
            // rhs cannot be null, as lhs != rhs
            objectToTest = rhs;
        }

        if (objectToTest.getClass().isArray()) {
            if (objectToTest instanceof boolean[]) {
                return append(fieldName, (boolean[]) lhs, (boolean[]) rhs);
            }
            if (objectToTest instanceof byte[]) {
                return append(fieldName, (byte[]) lhs, (byte[]) rhs);
            }
            if (objectToTest instanceof char[]) {
                return append(fieldName, (char[]) lhs, (char[]) rhs);
            }
            if (objectToTest instanceof double[]) {
                return append(fieldName, (double[]) lhs, (double[]) rhs);
            }
            if (objectToTest instanceof float[]) {
                return append(fieldName, (float[]) lhs, (float[]) rhs);
            }
            if (objectToTest instanceof int[]) {
                return append(fieldName, (int[]) lhs, (int[]) rhs);
            }
            if (objectToTest instanceof long[]) {
                return append(fieldName, (long[]) lhs, (long[]) rhs);
            }
            if (objectToTest instanceof short[]) {
                return append(fieldName, (short[]) lhs, (short[]) rhs);
            }

            return append(fieldName, (Object[]) lhs, (Object[]) rhs);
        }

        if (null != lhs && lhs.equals(rhs)) {
            return this;
        }

        differents.add(new Different<Object>(fieldName) {
            private static final long serialVersionUID = 1L;

            @Override
            public Object getLeft() {
                return lhs;
            }

            @Override
            public Object getRight() {
                return rhs;
            }
        });

        return this;
    }

    /**
     * 测试两个 {@code Object[]}是否相等
     *
     * @param fieldName 字段名
     * @param lhs       左边 {@code Object[]}
     * @param rhs       右边 {@code Object[]}
     * @return this
     */
    public DifferentBuilder append(final String fieldName, final Object[] lhs,
                                   final Object[] rhs) {
        validateFieldNameNotNull(fieldName);
        if (objectsTriviallyEqual) {
            return this;
        }

        if (!Arrays.equals(lhs, rhs)) {
            differents.add(new Different<Object[]>(fieldName) {
                private static final long serialVersionUID = 1L;

                @Override
                public Object[] getLeft() {
                    return lhs;
                }

                @Override
                public Object[] getRight() {
                    return rhs;
                }
            });
        }

        return this;
    }

    /**
     * 附加来自另一个{@code DiffResult}的差异.
     * 如果您想要比较本身是可扩散的属性，并且想要知道它的哪一部分是不同的，
     * 那么这个方法是很有用的.
     *
     * <pre>
     * public class Person implements Diffable&lt;Person&gt; {
     *   String name;
     *   Address address; // implements Diffable&lt;Address&gt;
     *
     *   ...
     *
     *   public DiffResult diff(Person object) {
     *     return new DiffBuilder(this, object, ToStringStyle.SHORT_PREFIX_STYLE)
     *       .append("name", this.name, object.name)
     *       .append("address", this.address.diff(object.address))
     *       .build();
     *   }
     * }
     * </pre>
     *
     * @param fieldName       字段名
     * @param differentResult 要附加的{@code DiffResult}
     * @return this
     */
    public DifferentBuilder append(final String fieldName,
                                   final DifferentResult differentResult) {
        validateFieldNameNotNull(fieldName);
        Assert.isTrue(null != differentResult, "Diff result cannot be null");
        if (objectsTriviallyEqual) {
            return this;
        }

        for (final Different<?> different : differentResult.getDifferents()) {
            append(fieldName + Symbol.DOT + different.getFieldName(),
                    different.getLeft(), different.getRight());
        }

        return this;
    }

    @Override
    public DifferentResult build() {
        return new DifferentResult(left, right, differents, style);
    }

    private void validateFieldNameNotNull(final String fieldName) {
        Assert.isTrue(null != fieldName, "Field name cannot be null");
    }

}
