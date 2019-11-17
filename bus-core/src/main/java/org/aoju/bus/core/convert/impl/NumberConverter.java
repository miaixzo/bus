/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
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
package org.aoju.bus.core.convert.impl;

import org.aoju.bus.core.convert.AbstractConverter;
import org.aoju.bus.core.utils.BooleanUtils;
import org.aoju.bus.core.utils.NumberUtils;
import org.aoju.bus.core.utils.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

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
 * @version 5.2.1
 * @since JDK 1.8+
 */
public class NumberConverter extends AbstractConverter<Number> {

    private Class<? extends Number> targetType;

    public NumberConverter() {
        this.targetType = Number.class;
    }

    /**
     * 构造
     *
     * @param clazz 需要转换的数字类型，默认 {@link Number}
     */
    public NumberConverter(Class<? extends Number> clazz) {
        this.targetType = (null == clazz) ? Number.class : clazz;
    }

    @Override
    protected Number convertInternal(Object value) {
        final Class<?> targetType = this.targetType;
        if (Byte.class == targetType) {
            if (value instanceof Number) {
                return Byte.valueOf(((Number) value).byteValue());
            } else if (value instanceof Boolean) {
                return BooleanUtils.toByteObj((Boolean) value);
            }
            final String valueStr = convertToStr(value);
            return StringUtils.isBlank(valueStr) ? null : Byte.valueOf(valueStr);

        } else if (Short.class == targetType) {
            if (value instanceof Number) {
                return Short.valueOf(((Number) value).shortValue());
            } else if (value instanceof Boolean) {
                return BooleanUtils.toShortObj((Boolean) value);
            }
            final String valueStr = convertToStr(value);
            return StringUtils.isBlank(valueStr) ? null : Short.valueOf(valueStr);

        } else if (Integer.class == targetType) {
            if (value instanceof Number) {
                return Integer.valueOf(((Number) value).intValue());
            } else if (value instanceof Boolean) {
                return BooleanUtils.toInteger((Boolean) value);
            }
            final String valueStr = convertToStr(value);
            return StringUtils.isBlank(valueStr) ? null : Integer.valueOf(NumberUtils.parseInt(valueStr));

        } else if (AtomicInteger.class == targetType) {
            int intValue;
            if (value instanceof Number) {
                intValue = ((Number) value).intValue();
            } else if (value instanceof Boolean) {
                intValue = BooleanUtils.toInt((Boolean) value);
            }
            final String valueStr = convertToStr(value);
            if (StringUtils.isBlank(valueStr)) {
                return null;
            }
            intValue = NumberUtils.parseInt(valueStr);
            return new AtomicInteger(intValue);
        } else if (Long.class == targetType) {
            if (value instanceof Number) {
                return Long.valueOf(((Number) value).longValue());
            } else if (value instanceof Boolean) {
                return BooleanUtils.toLongObj((Boolean) value);
            }
            final String valueStr = convertToStr(value);
            return StringUtils.isBlank(valueStr) ? null : Long.valueOf(NumberUtils.parseLong(valueStr));

        } else if (AtomicLong.class == targetType) {
            long longValue;
            if (value instanceof Number) {
                longValue = ((Number) value).longValue();
            } else if (value instanceof Boolean) {
                longValue = BooleanUtils.toLong((Boolean) value);
            }
            final String valueStr = convertToStr(value);
            if (StringUtils.isBlank(valueStr)) {
                return null;
            }
            longValue = NumberUtils.parseLong(valueStr);
            return new AtomicLong(longValue);

        } else if (Float.class == targetType) {
            if (value instanceof Number) {
                return Float.valueOf(((Number) value).floatValue());
            } else if (value instanceof Boolean) {
                return BooleanUtils.toFloatObj((Boolean) value);
            }
            final String valueStr = convertToStr(value);
            return StringUtils.isBlank(valueStr) ? null : Float.valueOf(valueStr);

        } else if (Double.class == targetType) {
            if (value instanceof Number) {
                return Double.valueOf(((Number) value).doubleValue());
            } else if (value instanceof Boolean) {
                return BooleanUtils.toDoubleObj((Boolean) value);
            }
            final String valueStr = convertToStr(value);
            return StringUtils.isBlank(valueStr) ? null : Double.valueOf(valueStr);

        } else if (BigDecimal.class == targetType) {
            return toBigDecimal(value);

        } else if (BigInteger.class == targetType) {
            return toBigInteger(value);

        } else if (Number.class == targetType) {
            if (value instanceof Number) {
                return (Number) value;
            } else if (value instanceof Boolean) {
                return BooleanUtils.toInteger((Boolean) value);
            }
            final String valueStr = convertToStr(value);
            return StringUtils.isBlank(valueStr) ? null : NumberUtils.parseNumber(valueStr);
        }

        throw new UnsupportedOperationException(StringUtils.format("Unsupport Number type: {}", this.targetType.getName()));
    }

    /**
     * 转换为BigDecimal
     * 如果给定的值为空，或者转换失败，返回默认值
     * 转换失败不会报错
     *
     * @param value 被转换的值
     * @return 结果
     */
    private BigDecimal toBigDecimal(Object value) {
        if (value instanceof Long) {
            return new BigDecimal((Long) value);
        } else if (value instanceof Integer) {
            return new BigDecimal((Integer) value);
        } else if (value instanceof BigInteger) {
            return new BigDecimal((BigInteger) value);
        } else if (value instanceof Boolean) {
            return new BigDecimal((boolean) value ? 1 : 0);
        }

        //对于Double类型，先要转换为String，避免精度问题
        final String valueStr = convertToStr(value);
        if (StringUtils.isBlank(valueStr)) {
            return null;
        }
        return new BigDecimal(valueStr);
    }

    /**
     * 转换为BigInteger
     * 如果给定的值为空，或者转换失败，返回默认值
     * 转换失败不会报错
     *
     * @param value 被转换的值
     * @return 结果
     */
    private BigInteger toBigInteger(Object value) {
        if (value instanceof Long) {
            return BigInteger.valueOf((Long) value);
        } else if (value instanceof Boolean) {
            return BigInteger.valueOf((boolean) value ? 1 : 0);
        }
        final String valueStr = convertToStr(value);
        if (StringUtils.isBlank(valueStr)) {
            return null;
        }
        return new BigInteger(valueStr);
    }

    @Override
    protected String convertToStr(Object value) {
        return StringUtils.trim(super.convertToStr(value));
    }

    @Override
    public Class<Number> getTargetType() {
        return (Class<Number>) this.targetType;
    }

}
