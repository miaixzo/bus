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
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.LinkedHashSet;
import java.util.Set;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ConvertException;
import org.miaixz.bus.core.xyz.StreamKit;
import org.miaixz.bus.core.xyz.TypeKit;

/**
 * Converter for special types. Returns {@code null} if type doesn't match to continue with other conversion rules. For
 * special objects (such as collections, Maps, Enums, arrays), implements conversion. Note: Converter lookup in this
 * class is done by traversal.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SpecialConverter extends ConverterWithRoot implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852271836876L;

    /**
     * Set of type converters. This set does not add new values after initialization, so it is thread-safe for singleton
     * use
     */
    private final Set<MatcherConverter> converterSet;

    /**
     * Constructs a new SpecialConverter
     *
     * @param rootConverter the parent converter
     */
    public SpecialConverter(final Converter rootConverter) {
        super(rootConverter);
        this.converterSet = initDefault(Assert.notNull(rootConverter));
    }

    /**
     * Finds a matching converter from the specified set
     *
     * @param type the type
     * @return the converter
     */
    private static Converter getConverterFromSet(
            final Set<? extends MatcherConverter> converterSet,
            final Type type,
            final Class<?> rawType,
            final Object value) {
        return StreamKit.of(converterSet).filter((predicate) -> predicate.match(type, rawType, value)).findFirst()
                .orElse(null);
    }

    /**
     * Initializes default converters
     *
     * @param rootConverter the root converter for recursive sub-object conversion
     * @return the set of converters
     */
    private static Set<MatcherConverter> initDefault(final Converter rootConverter) {
        final Set<MatcherConverter> converterSet = new LinkedHashSet<>(64);

        // Collection conversion (with generic parameters, cannot default cast)
        converterSet.add(CollectionConverter.INSTANCE);
        // Map type (with generic parameters, cannot default cast)
        converterSet.add(new MapConverter(rootConverter));
        // Entry class (with generic parameters, cannot default cast)
        converterSet.add(new EntryConverter(rootConverter));
        // Default casting
        converterSet.add(CastConverter.INSTANCE);
        // Date, java.sql dates and custom dates unified processing
        converterSet.add(DateConverter.INSTANCE);
        // Primitive type conversion
        converterSet.add(PrimitiveConverter.INSTANCE);
        // Numeric type conversion
        converterSet.add(NumberConverter.INSTANCE);
        // Enum conversion
        converterSet.add(EnumConverter.INSTANCE);
        // Array conversion
        converterSet.add(ArrayConverter.INSTANCE);
        // Record
        converterSet.add(RecordConverter.INSTANCE);
        // Kotlin Bean
        converterSet.add(KBeanConverter.INSTANCE);
        // Class
        converterSet.add(ClassConverter.INSTANCE);
        // // Empty value to empty Bean
        converterSet.add(EmptyBeanConverter.INSTANCE);

        // Date related
        converterSet.add(TimeZoneConverter.INSTANCE);
        converterSet.add(ZoneIdConverter.INSTANCE);

        return converterSet;
    }

    /**
     * Convert method.
     *
     * @return the Object value
     */
    @Override
    public Object convert(final Type targetType, final Object value) throws ConvertException {
        return convert(targetType, TypeKit.getClass(targetType), value);
    }

    /**
     * Converts value
     *
     * @param targetType the target type
     * @param rawType    the target raw type (i.e., target Class)
     * @param value      the value to convert
     * @return the converted value, returns {@code null} if no converter found
     * @throws ConvertException if conversion fails, i.e., corresponding converter found but conversion failed
     */
    public Object convert(final Type targetType, final Class<?> rawType, final Object value) throws ConvertException {
        final Converter converter = getConverter(targetType, rawType, value);
        return null == converter ? null : converter.convert(targetType, value);
    }

    /**
     * Gets the matching converter
     *
     * @param type    the type
     * @param rawType the Class of target type
     * @param value   the value to convert
     * @return the converter
     */
    public Converter getConverter(final Type type, final Class<?> rawType, final Object value) {
        return getConverterFromSet(this.converterSet, type, rawType, value);
    }

}
