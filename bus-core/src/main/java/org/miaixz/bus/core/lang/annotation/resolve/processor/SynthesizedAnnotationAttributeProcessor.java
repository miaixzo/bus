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
package org.miaixz.bus.core.lang.annotation.resolve.processor;

import java.util.Collection;

import org.miaixz.bus.core.lang.annotation.resolve.synthesize.SynthesizedAggregateAnnotation;
import org.miaixz.bus.core.lang.annotation.resolve.synthesize.SynthesizedAnnotation;

/**
 * Synthesized annotation attribute processor. Used to retrieve the corresponding attribute value from a specified type
 * of synthesized annotation within {@link SynthesizedAggregateAnnotation}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@FunctionalInterface
public interface SynthesizedAnnotationAttributeProcessor {

    /**
     * Retrieves the attribute value of the specified name and type from a collection of synthesized annotations.
     *
     * @param attributeName          The attribute name
     * @param attributeType          The attribute type
     * @param synthesizedAnnotations The collection of synthesized annotations
     * @param <R>                    The attribute return type
     * @return The attribute value
     */
    <R> R getAttributeValue(
            String attributeName,
            Class<R> attributeType,
            Collection<? extends SynthesizedAnnotation> synthesizedAnnotations);

}
