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

import java.util.Map;

import org.miaixz.bus.core.center.map.ForestMap;
import org.miaixz.bus.core.center.map.LinkedForestMap;
import org.miaixz.bus.core.center.map.TreeEntry;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.annotation.Alias;
import org.miaixz.bus.core.lang.annotation.resolve.attribute.AnnotationAttribute;
import org.miaixz.bus.core.lang.annotation.resolve.attribute.ForceAliasedAnnotationAttribute;
import org.miaixz.bus.core.lang.annotation.resolve.synthesize.AnnotationSynthesizer;
import org.miaixz.bus.core.lang.annotation.resolve.synthesize.SynthesizedAnnotation;
import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * Post-processor for annotation objects whose attributes have the {@link Alias} annotation.
 * <p>
 * When this processor completes, the target annotation attributes pointed to by the {@link Alias} annotation will be
 * wrapped and replaced with {@link ForceAliasedAnnotationAttribute}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class AliasAnnotationPostProcessor implements SynthesizedAnnotationPostProcessor {

    /**
     * Returns the lowest priority order so this processor runs first.
     *
     * @return {@link Integer#MIN_VALUE}
     */
    @Override
    public int order() {
        return Integer.MIN_VALUE;
    }

    /**
     * Processes all attributes annotated with {@link Alias} in the given synthesized annotation, replacing target
     * attributes with {@link ForceAliasedAnnotationAttribute} wrappers.
     *
     * @param synthesizedAnnotation the synthesized annotation being processed
     * @param synthesizer           the annotation synthesizer
     */
    @Override
    public void process(final SynthesizedAnnotation synthesizedAnnotation, final AnnotationSynthesizer synthesizer) {
        final Map<String, AnnotationAttribute> attributeMap = synthesizedAnnotation.getAttributes();

        // Record the mapping between aliases and attributes
        final ForestMap<String, AnnotationAttribute> attributeAliasMappings = new LinkedForestMap<>(false);
        attributeMap.forEach((attributeName, attribute) -> {
            final String alias = Optional.ofNullable(attribute.getAnnotation(Alias.class)).map(Alias::value)
                    .orElse(null);
            if (ObjectKit.isNull(alias)) {
                return;
            }
            final AnnotationAttribute aliasAttribute = attributeMap.get(alias);
            Assert.notNull(aliasAttribute, "no method for alias: [{}]", alias);
            attributeAliasMappings.putLinkedNodes(alias, aliasAttribute, attributeName, attribute);
        });

        // Process aliases: resolve each attribute to its root alias target
        attributeMap.forEach((attributeName, attribute) -> {
            final AnnotationAttribute resolvedAttribute = Optional.ofNullable(attributeName)
                    .map(attributeAliasMappings::getRootNode).map(TreeEntry::getValue).orElse(attribute);
            Assert.isTrue(
                    ObjectKit.isNull(resolvedAttribute) || ClassKit
                            .isAssignable(attribute.getAttributeType(), resolvedAttribute.getAttributeType()),
                    "return type of the root alias method [{}] is inconsistent with the original [{}]",
                    resolvedAttribute.getClass(),
                    attribute.getAttributeType());
            if (attribute != resolvedAttribute) {
                attributeMap.put(attributeName, new ForceAliasedAnnotationAttribute(attribute, resolvedAttribute));
            }
        });
        synthesizedAnnotation.setAttributes(attributeMap);
    }

}
