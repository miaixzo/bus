/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.lang.annotation.resolve.synthesize;

import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;

import org.miaixz.bus.core.center.map.multiple.RowKeyTable;
import org.miaixz.bus.core.center.map.multiple.Table;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.annotation.resolve.processor.SynthesizedAnnotationAttributeProcessor;

/**
 * A caching implementation of {@link SynthesizedAnnotationAttributeProcessor}.
 * <p>
 * A comparator must be provided at construction time. When retrieving an attribute value, synthesized annotations are
 * sorted by the comparator, and the value is taken from the first annotation (highest priority) that has the requested
 * attribute. The result is cached for subsequent calls.
 * <p>
 * When retrieving attribute values via this processor, an implicit alias effect occurs: if a child annotation and a
 * meta-annotation both have an attribute with the same name and type, the child annotation's attribute always overrides
 * the meta-annotation's attribute. This override is not propagated through
 * {@link org.miaixz.bus.core.lang.annotation.Alias} or {@link org.miaixz.bus.core.lang.annotation.Link} to related
 * attributes.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class CachingAnnotationAttributeProcessor implements SynthesizedAnnotationAttributeProcessor {

    /**
     * Cache table mapping (attributeName, attributeType) to the resolved attribute value.
     */
    private final Table<String, Class<?>, Object> valueCaches = new RowKeyTable<>();

    /**
     * Comparator used to sort synthesized annotations by priority when resolving attribute values.
     */
    private final Comparator<Hierarchical> annotationComparator;

    /**
     * Constructs a caching annotation attribute processor with the given comparator. Annotations that sort first
     * (smallest according to the comparator) are given highest priority.
     *
     * @param annotationComparator the comparator used to order synthesized annotations
     */
    public CachingAnnotationAttributeProcessor(final Comparator<Hierarchical> annotationComparator) {
        Assert.notNull(annotationComparator, "annotationComparator must not null");
        this.annotationComparator = annotationComparator;
    }

    /**
     * Constructs a caching annotation attribute processor using the default comparator. Annotations are sorted by
     * {@link SynthesizedAnnotation#getVerticalDistance()} and then
     * {@link SynthesizedAnnotation#getHorizontalDistance()}, with closer annotations given higher priority.
     */
    public CachingAnnotationAttributeProcessor() {
        this(Hierarchical.DEFAULT_HIERARCHICAL_COMPARATOR);
    }

    /**
     * Retrieves the attribute value of the specified name and type from the collection of synthesized annotations. The
     * annotation with the highest priority (smallest comparator order) that has the attribute is used. Results are
     * cached for subsequent calls.
     *
     * @param attributeName          the attribute name
     * @param attributeType          the attribute type
     * @param synthesizedAnnotations the collection of synthesized annotations to search
     * @param <T>                    the attribute return type
     * @return the attribute value, or {@code null} if not found
     */
    @Override
    public <T> T getAttributeValue(
            final String attributeName,
            final Class<T> attributeType,
            final Collection<? extends SynthesizedAnnotation> synthesizedAnnotations) {
        Object value = valueCaches.get(attributeName, attributeType);
        if (Objects.isNull(value)) {
            synchronized (valueCaches) {
                value = valueCaches.get(attributeName, attributeType);
                if (Objects.isNull(value)) {
                    value = synthesizedAnnotations.stream().filter(ma -> ma.hasAttribute(attributeName, attributeType))
                            .min(annotationComparator).map(ma -> ma.getAttributeValue(attributeName)).orElse(null);
                    valueCaches.put(attributeName, attributeType, value);
                }
            }
        }
        return (T) value;
    }

}
