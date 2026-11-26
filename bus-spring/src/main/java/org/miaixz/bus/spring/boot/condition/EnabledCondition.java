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
import org.springframework.core.type.AnnotationMetadata;

import org.miaixz.bus.core.lang.Symbol;

/**
 * Evaluates the annotation-first activation rule used by optional Spring Boot features.
 *
 * @author Kimi Liu
 */
public class EnabledCondition extends SpringBootCondition {

    /**
     * Creates an annotation-first feature condition.
     */
    public EnabledCondition() {
        // No initialization required.
    }

    /**
     * Resolves the explicit enable state declared by the application source.
     *
     * @param registry       current Bean definition registry
     * @param annotationName fully qualified enable annotation name
     * @param member         required annotation member, empty for presence-only checks
     * @return {@code TRUE} when declared and enabled, {@code FALSE} when declared and disabled, {@code null} when
     *         absent
     */
    private static Boolean declared(BeanDefinitionRegistry registry, String annotationName, String member) {
        boolean found = false;
        for (String beanName : registry.getBeanDefinitionNames()) {
            if (!(registry.getBeanDefinition(beanName) instanceof AnnotatedBeanDefinition definition)) {
                continue;
            }
            AnnotationMetadata source = definition.getMetadata();
            if (member == null || member.isEmpty()) {
                if (source.hasAnnotation(annotationName) || source.hasMetaAnnotation(annotationName)) {
                    return Boolean.TRUE;
                }
                continue;
            }
            Map<String, Object> values = source.getAnnotationAttributes(annotationName, true);
            if (values != null) {
                found = true;
                if (Boolean.TRUE.equals(values.get(member))) {
                    return Boolean.TRUE;
                }
            }
        }
        return found ? Boolean.FALSE : null;
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
        String member = (String) attributes.get("member");
        Boolean state = declared(context.getRegistry(), annotationName, member);
        if (Boolean.TRUE.equals(state)) {
            return ConditionOutcome.match(
                    ConditionMessage.forCondition(ConditionalOnEnabled.class).found("explicit enable annotation")
                            .items(annotationName));
        }
        if (Boolean.FALSE.equals(state)) {
            return ConditionOutcome.noMatch(
                    ConditionMessage.forCondition(ConditionalOnEnabled.class)
                            .found("the enable annotation disabled this feature").items(annotationName));
        }

        String prefix = (String) attributes.get("prefix");
        String name = (String) attributes.get("name");
        if (name.isEmpty()) {
            return ConditionOutcome.noMatch(
                    ConditionMessage.forCondition(ConditionalOnEnabled.class).didNotFind("enable annotation").atAll());
        }
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

}
