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
package org.miaixz.bus.spring;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.miaixz.bus.core.basic.entity.Authorize;
import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.core.lang.annotation.Nullable;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Instance facade for framework-neutral authenticated context state.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ContextBuilder {

    /**
     * State carrier owned by one application context.
     */
    private final ContextManager manager;

    /**
     * Ordered, side-effect-free authenticated context providers.
     */
    private final List<ContextProvider> providers;

    /**
     * Creates a context facade with an isolated manager and no providers.
     */
    public ContextBuilder() {
        this(new ContextManager(), List.of());
    }

    /**
     * Creates an application-context-scoped facade.
     *
     * @param manager   isolated state manager
     * @param providers ordered provider candidates
     */
    public ContextBuilder(ContextManager manager, List<ContextProvider> providers) {
        this.manager = Objects.requireNonNull(manager, "manager");
        List<ContextProvider> ordered = new ArrayList<>(providers == null ? List.of() : providers);
        ordered.sort(Comparator.comparingInt(ContextProvider::getOrder));
        this.providers = List.copyOf(ordered);
    }

    /**
     * Generates and installs a request correlation identifier.
     */
    public void setRequestId() {
        setRequestId(ID.objectId());
    }

    /**
     * Installs an explicit request correlation identifier.
     *
     * @param requestId request correlation identifier, or {@code null} to clear it
     */
    public void setRequestId(@Nullable String requestId) {
        ContextState current = this.manager.capture();
        this.manager.restore(
                ContextState.of(
                        normalize(requestId),
                        current.getAuthorize(),
                        current.getTokenCredential(),
                        current.getApiKeyCredential()));
    }

    /**
     * Returns the current request identifier without reading a transport request.
     *
     * @return request identifier, or {@code null} when absent
     */
    @Nullable
    public String getRequestId() {
        return this.manager.capture().getRequestId();
    }

    /**
     * Resolves and returns the authenticated subject.
     *
     * @return detached authenticated subject, or {@code null} when unavailable
     */
    @Nullable
    public Authorize getAuthorize() {
        Authorize current = this.manager.capture().getAuthorize();
        if (current != null) {
            return current;
        }
        Authorize selected = null;
        for (ContextProvider provider : this.providers) {
            Authorize candidate = provider.getAuthorize();
            if (candidate == null) {
                continue;
            }
            if (selected == null) {
                selected = ObjectKit.clone(candidate);
            } else if (!equivalent(selected, candidate)) {
                throw new IllegalStateException("Conflicting authenticated context providers");
            }
        }
        if (selected != null) {
            setAuthorize(selected);
        }
        return selected == null ? null : ObjectKit.clone(selected);
    }

    /**
     * Installs a defensive copy of the authenticated subject.
     *
     * @param authorize authenticated subject, or {@code null} to clear it
     */
    public void setAuthorize(@Nullable Authorize authorize) {
        ContextState current = this.manager.capture();
        this.manager.restore(
                ContextState.of(
                        current.getRequestId(),
                        authorize,
                        current.getTokenCredential(),
                        current.getApiKeyCredential()));
    }

    /**
     * Captures the current immutable state after refreshing authenticated provider state.
     *
     * @return detached immutable context state
     */
    public ContextState capture() {
        getAuthorize();
        return this.manager.capture();
    }

    /**
     * Installs a state and returns an idempotent parent-restoring scope.
     *
     * @param state context state, or {@code null} for the empty state
     * @return lexical scope restoring the prior state on close
     */
    public ContextScope install(@Nullable ContextState state) {
        return new ContextScope(this.manager, state);
    }

    /**
     * Restores an exact state.
     *
     * @param state state to restore, or {@code null} to clear it
     */
    public void restore(@Nullable ContextState state) {
        this.manager.restore(state);
    }

    /**
     * Returns the authenticated tenant identifier only.
     *
     * @return normalized tenant identifier, or {@code null}
     */
    @Nullable
    public String getTenantId() {
        Authorize authorize = getAuthorize();
        return authorize == null ? null : normalize(authorize.getX_tenant_id());
    }

    /**
     * Returns the preferred request credential captured at the integration boundary.
     * <p>
     * A token takes precedence when both a token and an API key are present.
     *
     * @return preferred credential, or {@code null} when absent
     */
    @Nullable
    public Http.Auth.Credential getCredential() {
        ContextState current = this.manager.capture();
        Http.Auth.Credential credential = current.getTokenCredential();
        return credential == null ? current.getApiKeyCredential() : credential;
    }

    /**
     * Returns the normalized token captured at the integration boundary.
     *
     * @return token value, or {@code null} when absent
     */
    @Nullable
    public String getToken() {
        return credentialValue(this.manager.capture().getTokenCredential());
    }

    /**
     * Returns the normalized API key captured at the integration boundary.
     *
     * @return API-key value, or {@code null} when absent
     */
    @Nullable
    public String getApiKey() {
        return credentialValue(this.manager.capture().getApiKeyCredential());
    }

    /**
     * Installs or clears the token credential while preserving all other context values.
     *
     * @param credential token credential, or {@code null} to clear it
     * @throws IllegalArgumentException when the supplied credential is not a token or has a blank value
     */
    public void setTokenCredential(@Nullable Http.Auth.Credential credential) {
        ContextState current = this.manager.capture();
        this.manager.restore(
                ContextState
                        .of(current.getRequestId(), current.getAuthorize(), credential, current.getApiKeyCredential()));
    }

    /**
     * Installs or clears the API-key credential while preserving all other context values.
     *
     * @param credential API-key credential, or {@code null} to clear it
     * @throws IllegalArgumentException when the supplied credential is not an API key or has a blank value
     */
    public void setApiKeyCredential(@Nullable Http.Auth.Credential credential) {
        ContextState current = this.manager.capture();
        this.manager.restore(
                ContextState
                        .of(current.getRequestId(), current.getAuthorize(), current.getTokenCredential(), credential));
    }

    /**
     * Clears the state owned by this application context.
     */
    public void clear() {
        this.manager.clear();
    }

    /**
     * Compares every declared authenticated subject field without exposing values in errors.
     *
     * @param left  first authenticated subject
     * @param right second authenticated subject
     * @return {@code true} when all non-static fields are equivalent
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
     * Normalizes identity strings while retaining non-string structured values.
     *
     * @param value authenticated context field value
     * @return normalized string or unchanged structured value
     */
    private static Object normalizedValue(Object value) {
        return value instanceof String string ? normalize(string) : value;
    }

    /**
     * Returns a normalized credential value without logging or formatting the secret.
     *
     * @param credential captured credential
     * @return normalized credential value, or {@code null}
     */
    private static String credentialValue(Http.Auth.Credential credential) {
        return credential == null ? null : normalize(credential.value());
    }

    /**
     * Trims identifiers and treats blank input as absent.
     *
     * @param value candidate identifier
     * @return trimmed identifier, or {@code null} when blank
     */
    private static String normalize(String value) {
        return StringKit.isBlank(value) ? null : value.trim();
    }

}
