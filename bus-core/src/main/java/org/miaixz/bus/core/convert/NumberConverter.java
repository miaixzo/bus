/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.convert;

import java.io.Serial;
import java.lang.reflect.Type;
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

import org.miaixz.bus.core.lang.exception.ConvertException;
import org.miaixz.bus.core.xyz.*;

/**
 * Converter for numeric types. Supported types:
 * <ul>
 * <li>{@code java.lang.Byte}</li>
 * <li>{@code java.lang.Short}</li>
 * <li>{@code java.lang.Integer}</li>
 * <li>{@code java.util.concurrent.atomic.AtomicInteger}</li>
 * <li>{@code java.lang.Long}</li>
 * <li>{@code java.util.concurrent.atomic.AtomicLong}</li>
 * <li>{@code java.lang.Float}</li>
 * <li>{@code java.lang.Double}</li>
 * <li>{@code java.math.BigDecimal}</li>
 * <li>{@code java.math.BigInteger}</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NumberConverter extends AbstractConverter implements MatcherConverter {

    @Serial
    private static final long serialVersionUID = 2852269279995L;

    /**
     * Singleton instance
     */
    public static final NumberConverter INSTANCE = new NumberConverter();

    /**
     * Converts an object to a number, supporting:
     * <ul>
     * <li>Number objects</li>
     * <li>Boolean</li>
     * <li>byte[]</li>
     * <li>String</li>
     * </ul>
     *
     * @param value      the object value
     * @param targetType the target number type
     * @param toStrFunc  the function to convert to string
     * @return the converted number
     */
    protected static Number convert(
            final Object value,
            final Class<? extends Number> targetType,
            final Function<Object, String> toStrFunc) {
        // Convert enum to number, defaulting to its ordinal
        if (value instanceof Enum) {
            return convert(((Enum<?>) value).ordinal(), targetType, toStrFunc);
        }

        if (value instanceof byte[]) {
            return ByteKit.toNumber((byte[]) value, targetType, ByteKit.DEFAULT_ORDER);
        }

        if (Byte.class == targetType) {
            if (value instanceof Number) {
                return ((Number) value).byteValue();
            } else if (value instanceof Boolean) {
                return BooleanKit.toByteObject((Boolean) value);
            }
            final String values = toStrFunc.apply(value);
            try {
                return Byte.valueOf(values);
            } catch (final NumberFormatException e) {
                return MathKit.parseNumber(values).byteValue();
            }
        } else if (Short.class == targetType) {
            if (value instanceof Number) {
                return ((Number) value).shortValue();
            } else if (value instanceof Boolean) {
                return BooleanKit.toShortObject((Boolean) value);
            }
            final String values = toStrFunc.apply((value));
            try {
                return Short.valueOf(values);
            } catch (final NumberFormatException e) {
                return MathKit.parseNumber(values).shortValue();
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
            final String values = toStrFunc.apply((value));
            return MathKit.parseInt(values);
        } else if (AtomicInteger.class == targetType) {
            final Number number = convert(value, Integer.class, toStrFunc);
            if (null != number) {
                return new AtomicInteger(number.intValue());
            }
        } else if (Long.class == targetType) {
            if (value instanceof Number) {
                return ((Number) value).longValue();
            } else if (value instanceof Boolean) {
                return BooleanKit.toLongObject((Boolean) value);
            } else if (value instanceof Date) {
                return ((Date) value).getTime();
            } else if (value instanceof Calendar) {
                return ((Calendar) value).getTimeInMillis();
            } else if (value instanceof TemporalAccessor) {
                return DateKit.toInstant((TemporalAccessor) value).toEpochMilli();
            }
            final String values = toStrFunc.apply((value));
            return MathKit.parseLong(values);
        } else if (AtomicLong.class == targetType) {
            final Number number = convert(value, Long.class, toStrFunc);
            if (null != number) {
                return new AtomicLong(number.longValue());
            }
        } else if (LongAdder.class == targetType) {
            // Added in JDK 8
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
                return BooleanKit.toFloatObject((Boolean) value);
            }
            final String values = toStrFunc.apply((value));
            return MathKit.parseFloat(values);
        } else if (Double.class == targetType) {
            if (value instanceof Number) {
                return MathKit.toDouble((Number) value);
            } else if (value instanceof Boolean) {
                return BooleanKit.toDoubleObject((Boolean) value);
            }
            final String values = toStrFunc.apply((value));
            return MathKit.parseDouble(values);
        } else if (DoubleAdder.class == targetType) {
            // Added in JDK 8
            final Number number = convert(value, Double.class, toStrFunc);
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
            final String values = toStrFunc.apply((value));
            return MathKit.parseNumber(values);
        }

        throw new UnsupportedOperationException(StringKit.format("Unsupport Number type: {}", targetType.getName()));
    }

    /**
     * Converts to BigDecimal. Returns default value if the given value is null or conversion fails. Conversion failure
     * will not throw an exception.
     *
     * @param value     the value to convert
     * @param toStrFunc the function to convert to string
     * @return the result
     */
    private static BigDecimal toBigDecimal(final Object value, final Function<Object, String> toStrFunc) {
        if (value instanceof Number) {
            return MathKit.toBigDecimal((Number) value);
        } else if (value instanceof Boolean) {
            return ((boolean) value) ? BigDecimal.ONE : BigDecimal.ZERO;
        }

        // For Double type, convert to String first to avoid precision issues
        return MathKit.toBigDecimal(toStrFunc.apply(value));
    }

    /**
     * Converts to BigInteger. Returns default value if the given value is null or conversion fails. Conversion failure
     * will not throw an exception.
     *
     * @param value     the value to convert
     * @param toStrFunc the function to convert to string
     * @return the result
     */
    private static BigInteger toBigInteger(final Object value, final Function<Object, String> toStrFunc) {
        if (value instanceof Long) {
            return BigInteger.valueOf((Long) value);
        } else if (value instanceof Boolean) {
            return (boolean) value ? BigInteger.ONE : BigInteger.ZERO;
        }

        return MathKit.toBigInteger(toStrFunc.apply(value));
    }

    @Override
    public boolean match(final Type targetType, final Class<?> rawType, final Object value) {
        return Number.class.isAssignableFrom(rawType);
    }

    @Override
    protected Number convertInternal(final Class<?> targetClass, final Object value) {
        return convert(value, (Class<? extends Number>) targetClass, this::convertToString);
    }

    @Override
    protected String convertToString(final Object value) {
        final String result = StringKit.trim(super.convertToString(value));
        if (StringKit.isEmpty(result)) {
            throw new ConvertException("Can not support empty value to Number!");
        }

        if (result.length() > 1) {
            // Check trailing identifier for non-single characters
            final char c = Character.toUpperCase(result.charAt(result.length() - 1));
            if (c == 'D' || c == 'L' || c == 'F') {
                // Type identifier form (e.g., 123.6D)
                return StringKit.subPre(result, -1);
            }
        }
        return result;
    }

}
