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
package org.miaixz.bus.spring.context.resolver;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.miaixz.bus.core.basic.entity.Authorize;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.spring.context.ContextState;
import org.miaixz.bus.spring.context.spi.ContextProvider;

/**
 * Builds one final immutable state from normalized credentials and ordered authentication providers.
 *
 * @author Kimi Liu
 */
public final class ContextResolver {

    /**
     * Immutable authentication providers ordered from highest to lowest precedence.
     */
    private final List<ContextProvider> providers;

    /**
     * Creates a resolver from the registered authentication providers.
     *
     * @param providers providers to sort and freeze; {@code null} is treated as an empty list
     */
    public ContextResolver(List<ContextProvider> providers) {
        List<ContextProvider> ordered = new ArrayList<>(providers == null ? List.of() : providers);
        ordered.sort(Comparator.comparingInt(ContextProvider::getOrder));
        this.providers = List.copyOf(ordered);
    }

    /**
     * Compares all non-static subject fields after normalizing textual values.
     *
     * @param left  first authenticated subject
     * @param right second authenticated subject
     * @return {@code true} when both subjects contain equivalent values
     * @throws IllegalStateException when reflective field access fails
     */
    private static boolean equivalent(Authorize left, Authorize right) {
        Class<?> type = Authorize.class;
        while (type != null && type != Object.class) {
            for (Field field : type.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                try {
                    field.setAccessible(true);
                    Object leftValue = normalizedValue(field.get(left));
                    Object rightValue = normalizedValue(field.get(right));
                    if (!Objects.deepEquals(leftValue, rightValue)) {
                        return false;
                    }
                } catch (IllegalAccessException exception) {
                    throw new IllegalStateException("Unable to compare authenticated context", exception);
                }
            }
            type = type.getSuperclass();
        }
        return true;
    }

    /**
     * Normalizes text before provider-result comparison while retaining non-text values unchanged.
     *
     * @param value field value to normalize
     * @return normalized comparison value
     */
    private static Object normalizedValue(Object value) {
        return value instanceof String string ? (StringKit.isBlank(string) ? null : string.trim()) : value;
    }

    /**
     * Resolves one immutable context snapshot from normalized request-boundary values.
     *
     * @param requestId        request correlation identifier
     * @param tokenCredential  Bearer-token credential
     * @param apiKeyCredential API-key credential
     * @return resolved immutable context snapshot
     * @throws IllegalStateException when providers return conflicting authenticated subjects
     */
    public ContextState resolve(
            String requestId,
            Http.Auth.Credential tokenCredential,
            Http.Auth.Credential apiKeyCredential) {
        ContextState initial = ContextState.of(requestId, null, tokenCredential, apiKeyCredential);
        Authorize authorize = resolveAuthorize(initial);
        return authorize == null ? initial : initial.withAuthorize(authorize);
    }

    /**
     * Invokes every ordered provider and selects a consistent authenticated subject.
     *
     * @param initial initial context containing correlation and credential values
     * @return detached authenticated subject, or {@code null} when no provider authenticates the request
     * @throws IllegalStateException when two providers return conflicting subjects
     */
    private Authorize resolveAuthorize(ContextState initial) {
        Authorize selected = null;
        for (ContextProvider provider : providers) {
            Authorize candidate = provider.getAuthorize(initial);
            if (candidate == null) {
                continue;
            }
            if (selected == null) {
                selected = ObjectKit.clone(candidate);
            } else if (!equivalent(selected, candidate)) {
                throw new IllegalStateException("Conflicting authenticated context providers");
            }
        }
        return selected;
    }

}
