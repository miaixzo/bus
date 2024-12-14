/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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

import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.time.*;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicReference;

import javax.xml.datatype.XMLGregorianCalendar;

import org.miaixz.bus.core.center.map.concurrent.SafeConcurrentHashMap;
import org.miaixz.bus.core.center.set.ConcurrentHashSet;
import org.miaixz.bus.core.lang.exception.ConvertException;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.core.lang.tuple.Triplet;
import org.miaixz.bus.core.lang.tuple.Tuple;
import org.miaixz.bus.core.xyz.StreamKit;
import org.miaixz.bus.core.xyz.TypeKit;

/**
 * 基于类型注册的转换器，提供两种注册方式，按照优先级依次为：
 * <ol>
 * <li>按照匹配注册，使用{@link #register(MatcherConverter)}。
 * 注册后一旦给定的目标类型和值满足{@link MatcherConverter#match(Type, Class, Object)}，即可调用对应转换器转换。</li>
 * <li>按照类型注册，使用{@link #register(Type, Converter)}，目标类型一致，即可调用转换。</li>
 * </ol>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RegisterConverter extends ConverterWithRoot implements Serializable {

    private static final long serialVersionUID = -1L;
    /**
     * 默认类型转换器
     */
    private final Map<Class<?>, Converter> defaultConverterMap;
    /**
     * 用户自定义类型转换器，存储自定义匹配规则的一类对象的转换器
     */
    private volatile Set<MatcherConverter> converterSet;
    /**
     * 用户自定义精确类型转换器 主要存储类型明确（无子类）的转换器
     */
    private volatile Map<Type, Converter> customConverterMap;

    /**
     * 构造
     *
     * @param rootConverter 根转换器，用于子转换器转换
     */
    public RegisterConverter(final Converter rootConverter) {
        super(rootConverter);
        this.defaultConverterMap = initDefault(rootConverter);
    }

    /**
     * 初始化默认转换器
     *
     * @return 默认转换器
     */
    private static Map<Class<?>, Converter> initDefault(final Converter rootConverter) {
        final Map<Class<?>, Converter> converterMap = new SafeConcurrentHashMap<>(64);

        // 包装类转换器
        converterMap.put(Character.class, CharacterConverter.INSTANCE);
        converterMap.put(Boolean.class, BooleanConverter.INSTANCE);
        converterMap.put(AtomicBoolean.class, AtomicBooleanConverter.INSTANCE);// since 3.0.8
        final StringConverter stringConverter = new StringConverter();
        converterMap.put(CharSequence.class, stringConverter);
        converterMap.put(String.class, stringConverter);

        // URI and URL
        converterMap.put(URI.class, new URIConverter());
        converterMap.put(URL.class, new URLConverter());

        // 日期时间
        converterMap.put(Calendar.class, new CalendarConverter());
        // 可能抛出Provider org.apache.xerces.jaxp.datatype.DatatypeFactoryImpl not found，此处忽略
        try {
            converterMap.put(XMLGregorianCalendar.class, new XMLGregorianCalendarConverter());
        } catch (final Exception ignore) {
            // ignore
        }

        // 日期时间 JDK8+(since 5.0.0)
        converterMap.put(TemporalAccessor.class, TemporalAccessorConverter.INSTANCE);
        converterMap.put(Instant.class, TemporalAccessorConverter.INSTANCE);
        converterMap.put(LocalDateTime.class, TemporalAccessorConverter.INSTANCE);
        converterMap.put(LocalDate.class, TemporalAccessorConverter.INSTANCE);
        converterMap.put(LocalTime.class, TemporalAccessorConverter.INSTANCE);
        converterMap.put(ZonedDateTime.class, TemporalAccessorConverter.INSTANCE);
        converterMap.put(OffsetDateTime.class, TemporalAccessorConverter.INSTANCE);
        converterMap.put(OffsetTime.class, TemporalAccessorConverter.INSTANCE);
        converterMap.put(DayOfWeek.class, TemporalAccessorConverter.INSTANCE);
        converterMap.put(Month.class, TemporalAccessorConverter.INSTANCE);
        converterMap.put(MonthDay.class, TemporalAccessorConverter.INSTANCE);

        converterMap.put(Period.class, new PeriodConverter());
        converterMap.put(Duration.class, new DurationConverter());

        // Reference
        final ReferenceConverter referenceConverter = new ReferenceConverter(rootConverter);
        converterMap.put(WeakReference.class, referenceConverter);
        converterMap.put(SoftReference.class, referenceConverter);
        converterMap.put(AtomicReference.class, new AtomicReferenceConverter(rootConverter));

        // AtomicXXXArray
        converterMap.put(AtomicIntegerArray.class, new AtomicIntegerArrayConverter());
        converterMap.put(AtomicLongArray.class, new AtomicLongArrayConverter());

        // 其它类型
        converterMap.put(Locale.class, new LocaleConverter());
        converterMap.put(Charset.class, new CharsetConverter());
        converterMap.put(Path.class, new PathConverter());
        converterMap.put(Currency.class, new CurrencyConverter());
        converterMap.put(UUID.class, new UUIDConverter());
        converterMap.put(StackTraceElement.class, new StackTraceElementConverter());
        converterMap.put(Optional.class, new OptionalConverter());
        converterMap.put(org.miaixz.bus.core.lang.Optional.class, new OptConverter());
        converterMap.put(Pair.class, new PairConverter(rootConverter));
        converterMap.put(Triplet.class, new TripletConverter(rootConverter));
        converterMap.put(Tuple.class, TupleConverter.INSTANCE);

        return converterMap;
    }

    @Override
    public Object convert(final Type targetType, final Object value) throws ConvertException {
        // 标准转换器
        final Converter converter = getConverter(targetType, value, true);
        if (null != converter) {
            return converter.convert(targetType, value);
        }

        // 无法转换
        throw new ConvertException("Can not support from {}: [{}] to [{}]", value.getClass().getName(), value,
                targetType.getTypeName());
    }

    /**
     * 获得转换器
     *
     * @param type          类型
     * @param value         转换的值
     * @param isCustomFirst 是否自定义转换器优先
     * @return 转换器
     */
    public Converter getConverter(final Type type, final Object value, final boolean isCustomFirst) {
        Converter converter;
        if (isCustomFirst) {
            converter = this.getCustomConverter(type, value);
            if (null == converter) {
                converter = this.getCustomConverter(type);
            }
            if (null == converter) {
                converter = this.getDefaultConverter(type);
            }
        } else {
            converter = this.getDefaultConverter(type);
            if (null == converter) {
                converter = this.getCustomConverter(type, value);
            }
            if (null == converter) {
                converter = this.getCustomConverter(type);
            }
        }
        return converter;
    }

    /**
     * 获得默认转换器
     *
     * @param type 类型
     * @return 转换器
     */
    public Converter getDefaultConverter(final Type type) {
        final Class<?> key = null == type ? null : TypeKit.getClass(type);
        return (null == defaultConverterMap || null == key) ? null : defaultConverterMap.get(key);
    }

    /**
     * 获得匹配类型的自定义转换器
     *
     * @param type  类型
     * @param value 被转换的值
     * @return 转换器
     */
    public Converter getCustomConverter(final Type type, final Object value) {
        return StreamKit.of(converterSet).filter((predicate) -> predicate.match(type, value)).findFirst().orElse(null);
    }

    /**
     * 获得指定类型对应的自定义转换器
     *
     * @param type 类型
     * @return 转换器
     */
    public Converter getCustomConverter(final Type type) {
        return (null == customConverterMap) ? null : customConverterMap.get(type);
    }

    /**
     * 登记自定义转换器，登记的目标类型必须一致
     *
     * @param type      转换的目标类型
     * @param converter 转换器
     * @return this
     */
    public RegisterConverter register(final Type type, final Converter converter) {
        if (null == customConverterMap) {
            synchronized (this) {
                if (null == customConverterMap) {
                    customConverterMap = new SafeConcurrentHashMap<>();
                }
            }
        }
        customConverterMap.put(type, converter);
        return this;
    }

    /**
     * 登记自定义转换器，符合{@link MatcherConverter#match(Type, Class, Object)}则使用其转换器
     *
     * @param converter 转换器
     * @return this
     */
    public RegisterConverter register(final MatcherConverter converter) {
        if (null == this.converterSet) {
            synchronized (this) {
                if (null == this.converterSet) {
                    this.converterSet = new ConcurrentHashSet<>();
                }
            }
        }
        this.converterSet.add(converter);
        return this;
    }

}