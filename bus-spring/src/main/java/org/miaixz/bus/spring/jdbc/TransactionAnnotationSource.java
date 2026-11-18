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
package org.miaixz.bus.spring.jdbc;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.annotation.TransactionAnnotationParser;
import org.springframework.transaction.interceptor.*;

import org.miaixz.bus.core.lang.annotation.Transactional;

/**
 * Resolves Bus annotations using the platform's inherited method and class lookup rules.
 *
 * @author Kimi Liu
 */
public final class TransactionAnnotationSource extends AnnotationTransactionAttributeSource {

    /**
     * Installs the Bus annotation parser and allows the platform to inspect nonpublic transactional methods.
     */
    public TransactionAnnotationSource() {
        super(new Parser());
        setPublicMethodsOnly(false);
    }

    /**
     * Converts a merged Bus transaction declaration to the engine's transaction attributes.
     *
     * @author Kimi Liu
     */
    private static final class Parser implements TransactionAnnotationParser {

        @Override
        public TransactionAttribute parseTransactionAnnotation(AnnotatedElement element) {
            Transactional annotation = AnnotatedElementUtils.findMergedAnnotation(element, Transactional.class);
            if (annotation == null)
                return null;
            RuleBasedTransactionAttribute attribute = new RuleBasedTransactionAttribute();
            attribute.setPropagationBehavior(annotation.propagation().value());
            attribute.setIsolationLevel(annotation.isolation().value());
            attribute.setTimeout(annotation.timeout());
            attribute.setReadOnly(annotation.readOnly());
            attribute.setQualifier(annotation.transactionManager());
            var rules = new ArrayList<RollbackRuleAttribute>();
            for (Class<? extends Throwable> type : annotation.rollbackFor())
                rules.add(new RollbackRuleAttribute(type));
            for (Class<? extends Throwable> type : annotation.noRollbackFor())
                rules.add(new NoRollbackRuleAttribute(type));
            attribute.setRollbackRules(rules);
            return attribute;
        }

    }

}
