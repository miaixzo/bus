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

import org.miaixz.bus.core.center.map.concurrent.SafeConcurrentHashMap;
import org.miaixz.bus.core.center.set.ConcurrentHashSet;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ConvertException;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.core.lang.tuple.Triplet;
import org.miaixz.bus.core.lang.tuple.Tuple;
import org.miaixz.bus.core.xyz.StreamKit;
import org.miaixz.bus.core.xyz.TypeKit;

import javax.xml.datatype.XMLGregorianCalendar;
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
public class RegisterConverter implements Converter, Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * 用户自定义类型转换器，存储自定义匹配规则的一类对象的转换器
     */
    private volatile Set<MatcherConverter> converterSet;
    /**
     * 用户自定义精确类型转换器<br>
     * 主要存储类型明确（无子类）的转换器
     */
    private volatile Map<Type, Converter> customConverterMap;
    /**
     * 默认类型转换器
     */
    private Map<Class<?>, Converter> defaultConverterMap;

    /**
     * 构造
     */
    public RegisterConverter() {
        final Map<Class<?>, Converter> map = new SafeConcurrentHashMap<>(64);

        // 包装类转换器
        map.put(Character.class, new CharacterConverter());
        map.put(Boolean.class, new BooleanConverter());
        map.put(AtomicBoolean.class, new AtomicBooleanConverter());
        map.put(CharSequence.class, new StringConverter());
        map.put(String.class, new StringConverter());

        // URI and URL
        map.put(URI.class, new URIConverter());
        map.put(URL.class, new URLConverter());

        // 日期时间
        map.put(Calendar.class, new CalendarConverter());
        map.put(XMLGregorianCalendar.class, new XMLGregorianCalendarConverter());

        // 日期时间
        map.put(TemporalAccessor.class, TemporalAccessorConverter.INSTANCE);
        map.put(Instant.class, TemporalAccessorConverter.INSTANCE);
        map.put(LocalDateTime.class, TemporalAccessorConverter.INSTANCE);
        map.put(LocalDate.class, TemporalAccessorConverter.INSTANCE);
        map.put(LocalTime.class, TemporalAccessorConverter.INSTANCE);
        map.put(ZonedDateTime.class, TemporalAccessorConverter.INSTANCE);
        map.put(OffsetDateTime.class, TemporalAccessorConverter.INSTANCE);
        map.put(OffsetTime.class, TemporalAccessorConverter.INSTANCE);
        map.put(DayOfWeek.class, TemporalAccessorConverter.INSTANCE);
        map.put(Month.class, TemporalAccessorConverter.INSTANCE);
        map.put(MonthDay.class, TemporalAccessorConverter.INSTANCE);

        map.put(Period.class, new PeriodConverter());
        map.put(Duration.class, new DurationConverter());

        // Reference
        map.put(WeakReference.class, ReferenceConverter.INSTANCE);
        map.put(SoftReference.class, ReferenceConverter.INSTANCE);
        map.put(AtomicReference.class, new AtomicReferenceConverter());

        // AtomicXXXArray
        map.put(AtomicIntegerArray.class, new AtomicIntegerArrayConverter());
        map.put(AtomicLongArray.class, new AtomicLongArrayConverter());

        // 其它类型
        map.put(TimeZone.class, new TimeZoneConverter());
        map.put(ZoneId.class, new ZoneIdConverter());
        map.put(Locale.class, new LocaleConverter());
        map.put(Charset.class, new CharsetConverter());
        map.put(Path.class, new PathConverter());
        map.put(Currency.class, new CurrencyConverter());
        map.put(UUID.class, new UUIDConverter());
        map.put(StackTraceElement.class, new StackTraceElementConverter());
        map.put(java.util.Optional.class, new OptionalConverter());
        map.put(Optional.class, new OptConverter());
        map.put(Pair.class, PairConverter.INSTANCE);
        map.put(Triplet.class, TripleConverter.INSTANCE);
        map.put(Tuple.class, TupleConverter.INSTANCE);

        this.defaultConverterMap = map;
    }

    /**
     * 获得单例
     *
     * @return this
     */
    public static RegisterConverter getInstance() {
        return RegisterConverter.SingletonHolder.INSTANCE;
    }

    @Override
    public Object convert(final Type targetType, final Object value) throws ConvertException {
        // 标准转换器
        final Converter converter = getConverter(targetType, value, true);
        if (null != converter) {
            return converter.convert(targetType, value);
        }

        // 无法转换
        throw new ConvertException("Can not convert from {}: [{}] to [{}]", value.getClass().getName(), value,
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

    /**
     * 类级的内部类，也就是静态的成员式内部类，该内部类的实例与外部类的实例 没有绑定关系，而且只有被调用到才会装载，从而实现了延迟加载
     */
    private static class SingletonHolder {
        /**
         * 静态初始化器，由JVM来保证线程安全
         */
        private static final RegisterConverter INSTANCE = new RegisterConverter();
    }

}
