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
package org.miaixz.bus.spring.boot.condition;

import java.util.Map;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import org.miaixz.bus.core.lang.Symbol;

/**
 * Evaluates the annotation-first activation rule used by optional Spring Boot features.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class EnabledCondition extends SpringBootCondition {

    /**
     * Creates an annotation-first feature condition.
     */
    public EnabledCondition() {
        // No initialization required.
    }

    /**
     * Matches when the application declares the feature's enable annotation or the secondary property is true.
     *
     * @param context  current condition context
     * @param metadata metadata of the feature configuration under evaluation
     * @return condition outcome with its activation source
     */
    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(ConditionalOnEnabled.class.getName(), true);
        if (attributes == null) {
            return ConditionOutcome.noMatch(ConditionMessage.empty());
        }

        String annotationName = (String) attributes.get("annotation");
        if (hasAnnotation(context.getRegistry(), annotationName)) {
            return ConditionOutcome.match(
                    ConditionMessage.forCondition(ConditionalOnEnabled.class).found("explicit enable annotation")
                            .items(annotationName));
        }

        String prefix = (String) attributes.get("prefix");
        String name = (String) attributes.get("name");
        String propertyName = prefix.isEmpty() ? name : prefix + Symbol.DOT + name;
        Boolean propertyValue = context.getEnvironment().getProperty(propertyName, Boolean.class);
        if (Boolean.TRUE.equals(propertyValue)) {
            return ConditionOutcome.match(
                    ConditionMessage.forCondition(ConditionalOnEnabled.class).found("enabled property")
                            .items(propertyName));
        }
        if (propertyValue == null && Boolean.TRUE.equals(attributes.get("matchIfMissing"))) {
            return ConditionOutcome.match(
                    ConditionMessage.forCondition(ConditionalOnEnabled.class).didNotFind("property")
                            .items(propertyName));
        }
        return ConditionOutcome.noMatch(
                ConditionMessage.forCondition(ConditionalOnEnabled.class)
                        .didNotFind("explicit enable annotation or enabled property").atAll());
    }

    /**
     * Returns whether a registered application source directly or transitively declares the enable annotation.
     *
     * @param registry       current Bean definition registry
     * @param annotationName fully qualified enable annotation name
     * @return {@code true} when the annotation is present
     */
    private static boolean hasAnnotation(BeanDefinitionRegistry registry, String annotationName) {
        for (String beanName : registry.getBeanDefinitionNames()) {
            if (registry.getBeanDefinition(beanName) instanceof AnnotatedBeanDefinition definition
                    && (definition.getMetadata().hasAnnotation(annotationName)
                            || definition.getMetadata().hasMetaAnnotation(annotationName))) {
                return true;
            }
        }
        return false;
    }

}
