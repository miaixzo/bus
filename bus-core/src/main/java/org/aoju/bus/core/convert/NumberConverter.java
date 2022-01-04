/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.convert;

import org.aoju.bus.core.toolkit.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;

/**
 * 数字转换器
 * 支持类型为：
 * <ul>
 * <li><code>java.lang.Byte</code></li>
 * <li><code>java.lang.Short</code></li>
 * <li><code>java.lang.Integer</code></li>
 * <li><code>java.lang.Long</code></li>
 * <li><code>java.lang.Float</code></li>
 * <li><code>java.lang.Double</code></li>
 * <li><code>java.math.BigDecimal</code></li>
 * <li><code>java.math.BigInteger</code></li>
 * </ul>
 *
 * @author Kimi Liu
 * @version 6.3.3
 * @since JDK 1.8+
 */
public class NumberConverter extends AbstractConverter<Number> {

    private static final long serialVersionUID = 1L;

    private final Class<? extends Number> targetType;

    public NumberConverter() {
        this.targetType = Number.class;
    }

    /**
     * 构造
     *
     * @param clazz 需要转换的数字类型,默认 {@link Number}
     */
    public NumberConverter(Class<? extends Number> clazz) {
        this.targetType = (null == clazz) ? Number.class : clazz;
    }

    /**
     * 转换对象为数字，支持的对象包括：
     * <ul>
     *     <li>Number对象</li>
     *     <li>Boolean</li>
     *     <li>byte[]</li>
     *     <li>String</li>
     * </ul>
     *
     * @param value      对象值
     * @param targetType 目标的数字类型
     * @param toStrFunc  转换为字符串的函数
     * @return 转换后的数字
     */
    protected static Number convert(Object value, Class<? extends Number> targetType, Function<Object, String> toStrFunc) {
        // 枚举转换为数字默认为其顺序
        if (value instanceof Enum) {
            return convert(((Enum<?>) value).ordinal(), targetType, toStrFunc);
        }

        if (value instanceof byte[]) {
            return ByteKit.getNumber((byte[]) value, targetType);
        }

        if (Byte.class == targetType) {
            if (value instanceof Number) {
                return ((Number) value).byteValue();
            } else if (value instanceof Boolean) {
                return BooleanKit.toByteObj((Boolean) value);
            }
            final String valueStr = toStrFunc.apply(value);
            try {
                return StringKit.isBlank(valueStr) ? null : Byte.valueOf(valueStr);
            } catch (NumberFormatException e) {
                return MathKit.parseNumber(valueStr).byteValue();
            }
        } else if (Short.class == targetType) {
            if (value instanceof Number) {
                return ((Number) value).shortValue();
            } else if (value instanceof Boolean) {
                return BooleanKit.toShortObj((Boolean) value);
            }
            final String valueStr = toStrFunc.apply((value));
            try {
                return StringKit.isBlank(valueStr) ? null : Short.valueOf(valueStr);
            } catch (NumberFormatException e) {
                return MathKit.parseNumber(valueStr).shortValue();
            }
        } else if (Integer.class == targetType) {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            } else if (value instanceof Boolean) {
                return BooleanKit.toInteger((Boolean) value);
            } else if (value instanceof Date) {
                return (int) ((Date) value).getTime();
            } else if (value instanceof Calendar) {
                return (int) ((Calendar) value).getTimeInMillis();
            } else if (value instanceof TemporalAccessor) {
                return (int) DateKit.toInstant((TemporalAccessor) value).toEpochMilli();
            }
            final String valueStr = toStrFunc.apply((value));
            return StringKit.isBlank(valueStr) ? null : MathKit.parseInt(valueStr);
        } else if (AtomicInteger.class == targetType) {
            final Number number = convert(value, Integer.class, toStrFunc);
            if (null != number) {
                return new AtomicInteger(number.intValue());
            }
        } else if (Long.class == targetType) {
            if (value instanceof Number) {
                return ((Number) value).longValue();
            } else if (value instanceof Boolean) {
                return BooleanKit.toLongObj((Boolean) value);
            } else if (value instanceof Date) {
                return ((Date) value).getTime();
            } else if (value instanceof Calendar) {
                return ((Calendar) value).getTimeInMillis();
            } else if (value instanceof TemporalAccessor) {
                return DateKit.toInstant((TemporalAccessor) value).toEpochMilli();
            }
            final String valueStr = toStrFunc.apply((value));
            return StringKit.isBlank(valueStr) ? null : MathKit.parseLong(valueStr);
        } else if (AtomicLong.class == targetType) {
            final Number number = convert(value, Long.class, toStrFunc);
            if (null != number) {
                return new AtomicLong(number.longValue());
            }
        } else if (LongAdder.class == targetType) {
            final Number number = convert(value, Long.class, toStrFunc);
            if (null != number) {
                final LongAdder longValue = new LongAdder();
                longValue.add(number.longValue());
                return longValue;
            }
        } else if (Float.class == targetType) {
            if (value instanceof Number) {
                return ((Number) value).floatValue();
            } else if (value instanceof Boolean) {
                return BooleanKit.toFloatObj((Boolean) value);
            }
            final String valueStr = toStrFunc.apply((value));
            return StringKit.isBlank(valueStr) ? null : MathKit.parseFloat(valueStr);
        } else if (Double.class == targetType) {
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            } else if (value instanceof Boolean) {
                return BooleanKit.toDoubleObj((Boolean) value);
            }
            final String valueStr = toStrFunc.apply((value));
            return StringKit.isBlank(valueStr) ? null : MathKit.parseDouble(valueStr);
        } else if (DoubleAdder.class == targetType) {
            final Number number = convert(value, Long.class, toStrFunc);
            if (null != number) {
                final DoubleAdder doubleAdder = new DoubleAdder();
                doubleAdder.add(number.doubleValue());
                return doubleAdder;
            }
        } else if (BigDecimal.class == targetType) {
            return toBigDecimal(value, toStrFunc);
        } else if (BigInteger.class == targetType) {
            return toBigInteger(value, toStrFunc);
        } else if (Number.class == targetType) {
            if (value instanceof Number) {
                return (Number) value;
            } else if (value instanceof Boolean) {
                return BooleanKit.toInteger((Boolean) value);
            }
            final String valueStr = toStrFunc.apply((value));
            return StringKit.isBlank(valueStr) ? null : MathKit.parseNumber(valueStr);
        }

        throw new UnsupportedOperationException(StringKit.format("Unsupport Number type: {}", targetType.getName()));
    }

    /**
     * 转换为BigDecimal
     * 如果给定的值为空,或者转换失败,返回默认值
     * 转换失败不会报错
     *
     * @param value 被转换的值
     * @param func  转换为字符串的函数规则
     * @return 结果
     */
    private static BigDecimal toBigDecimal(Object value, Function<Object, String> func) {
        if (value instanceof Number) {
            return MathKit.toBigDecimal((Number) value);
        } else if (value instanceof Boolean) {
            return ((boolean) value) ? BigDecimal.ONE : BigDecimal.ZERO;
        }
        return MathKit.toBigDecimal(func.apply(value));
    }

    /**
     * 转换为BigInteger
     * 如果给定的值为空,或者转换失败,返回默认值
     * 转换失败不会报错
     *
     * @param value 被转换的值
     * @param func  转换为字符串的函数规则
     * @return 结果
     */
    private static BigInteger toBigInteger(Object value, Function<Object, String> func) {
        if (value instanceof Long) {
            return BigInteger.valueOf((Long) value);
        } else if (value instanceof Boolean) {
            return (boolean) value ? BigInteger.ONE : BigInteger.ZERO;
        }

        return MathKit.toBigInteger(func.apply(value));
    }

    @Override
    public Class<Number> getTargetType() {
        return (Class<Number>) this.targetType;
    }

    @Override
    protected Number convertInternal(Object value) {
        return convert(value, this.targetType, this::convertString);
    }

    @Override
    protected String convertString(Object value) {
        String result = StringKit.trim(super.convertString(value));
        if (StringKit.isNotEmpty(result)) {
            final char c = Character.toUpperCase(result.charAt(result.length() - 1));
            if (c == 'D' || c == 'L' || c == 'F') {
                // 类型标识形式（例如123.6D）
                return StringKit.subPre(result, -1);
            }
        }

        return result;
    }

}
