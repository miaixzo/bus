/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicReference;

import javax.xml.datatype.XMLGregorianCalendar;

import org.miaixz.bus.core.center.set.ConcurrentHashSet;
import org.miaixz.bus.core.lang.exception.ConvertException;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.core.lang.tuple.Triplet;
import org.miaixz.bus.core.lang.tuple.Tuple;
import org.miaixz.bus.core.xyz.StreamKit;
import org.miaixz.bus.core.xyz.TypeKit;

/**
 * A type-registration-based converter providing two registration methods, in order of priority:
 * <ol>
 * <li>Register by matching, using {@link #register(MatcherConverter)}. Once a given target type and value satisfies
 * {@link MatcherConverter#match(Type, Class, Object)}, the corresponding converter is called for conversion.</li>
 * <li>Register by type, using {@link #register(Type, Converter)}. When the target type matches, the converter is
 * called.</li>
 * </ol>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RegisterConverter extends ConverterWithRoot implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852271620270L;

    /**
     * Default type converter map.
     */
    private final Map<Class<?>, Converter> defaultConverterMap;
    /**
     * User-defined type converter set, stores converters with custom matching rules for certain types of objects.
     */
    private volatile Set<MatcherConverter> converterSet;
    /**
     * User-defined precise type converter map, mainly stores converters for explicitly defined types (no subclasses).
     */
    private volatile Map<Type, Converter> customConverterMap;

    /**
     * Constructs a new RegisterConverter.
     *
     * @param rootConverter the root converter, used for sub-converter conversions
     */
    public RegisterConverter(final Converter rootConverter) {
        super(rootConverter);
        this.defaultConverterMap = initDefault(rootConverter);
    }

    /**
     * Initializes the default converters.
     *
     * @return the default converter map
     */
    private static Map<Class<?>, Converter> initDefault(final Converter rootConverter) {
        final Map<Class<?>, Converter> converterMap = new ConcurrentHashMap<>(64);

        // Wrapper class converters
        converterMap.put(Character.class, CharacterConverter.INSTANCE);
        converterMap.put(Boolean.class, BooleanConverter.INSTANCE);
        converterMap.put(AtomicBoolean.class, AtomicBooleanConverter.INSTANCE);// since 3.0.8
        final StringConverter stringConverter = new StringConverter();
        converterMap.put(CharSequence.class, stringConverter);
        converterMap.put(String.class, stringConverter);

        // URI and URL
        converterMap.put(URI.class, new URIConverter());
        converterMap.put(URL.class, new URLConverter());

        // Date and time
        converterMap.put(Calendar.class, new CalendarConverter());
        // May throw Provider org.apache.xerces.jaxp.datatype.DatatypeFactoryImpl not found, ignored here
        try {
            converterMap.put(XMLGregorianCalendar.class, new XMLGregorianCalendarConverter());
        } catch (final Exception ignore) {
            // ignore
        }

        // Date and time JDK8+ (since 5.0.0)
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

        // Other types
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

    /**
     * Convert method.
     *
     * @return the Object value
     */
    @Override
    public Object convert(final Type targetType, final Object value) throws ConvertException {
        // Standard converter
        final Converter converter = getConverter(targetType, value, true);
        if (null != converter) {
            return converter.convert(targetType, value);
        }

        // Unable to convert
        throw new ConvertException("Can not support from {}: [{}] to [{}]", value.getClass().getName(), value,
                targetType.getTypeName());
    }

    /**
     * Gets the converter.
     *
     * @param type          the type
     * @param value         the value to be converted
     * @param isCustomFirst whether custom converters have priority
     * @return the converter
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
     * Gets the default converter.
     *
     * @param type the type
     * @return the converter
     */
    public Converter getDefaultConverter(final Type type) {
        final Class<?> key = null == type ? null : TypeKit.getClass(type);
        return (null == defaultConverterMap || null == key) ? null : defaultConverterMap.get(key);
    }

    /**
     * Gets the custom converter matching the type.
     *
     * @param type  the type
     * @param value the value to be converted
     * @return the converter
     */
    public Converter getCustomConverter(final Type type, final Object value) {
        return StreamKit.of(converterSet).filter((predicate) -> predicate.match(type, value)).findFirst().orElse(null);
    }

    /**
     * Gets the custom converter for the specified type.
     *
     * @param type the type
     * @return the converter
     */
    public Converter getCustomConverter(final Type type) {
        return (null == customConverterMap) ? null : customConverterMap.get(type);
    }

    /**
     * Registers a custom converter. The registered target type must be consistent.
     *
     * @param type      the target type of the conversion
     * @param converter the converter
     * @return this
     */
    public RegisterConverter register(final Type type, final Converter converter) {
        if (null == customConverterMap) {
            synchronized (this) {
                if (null == customConverterMap) {
                    customConverterMap = new ConcurrentHashMap<>();
                }
            }
        }
        customConverterMap.put(type, converter);
        return this;
    }

    /**
     * Registers a custom converter. If it matches {@link MatcherConverter#match(Type, Class, Object)}, the converter is
     * used.
     *
     * @param converter the converter
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
