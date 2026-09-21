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
package org.miaixz.bus.spring.context;

import org.miaixz.bus.core.basic.entity.Authorize;
import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Immutable, transport-free snapshot of the current execution context.
 * <p>
 * Snapshots never retain Servlet requests, Spring Beans, application contexts, or mutable context containers. The
 * authenticated subject is defensively copied at construction and access boundaries.
 *
 * @author Kimi Liu
 */
public final class ContextState {

    /**
     * Shared empty state.
     */
    private static final ContextState EMPTY = new ContextState(null, null, null, null);

    /**
     * Normalized request correlation identifier.
     */
    private final String requestId;

    /**
     * Detached authenticated subject snapshot.
     */
    private final Authorize authorize;

    /**
     * Validated Bearer-token credential.
     */
    private final Http.Auth.Credential tokenCredential;

    /**
     * Validated API-key credential.
     */
    private final Http.Auth.Credential apiKeyCredential;

    /**
     * Creates an immutable detached state.
     *
     * @param requestId        request correlation identifier
     * @param authorize        authenticated subject
     * @param tokenCredential  Bearer-token credential
     * @param apiKeyCredential API-key credential
     */
    public ContextState(String requestId, Authorize authorize, Http.Auth.Credential tokenCredential,
            Http.Auth.Credential apiKeyCredential) {
        this.requestId = normalize(requestId);
        this.authorize = copy(authorize);
        validate(tokenCredential, EnumValue.Credential.TOKEN, "token");
        validate(apiKeyCredential, EnumValue.Credential.API_KEY, "API-key");
        this.tokenCredential = tokenCredential;
        this.apiKeyCredential = apiKeyCredential;
    }

    /**
     * Returns the shared empty state.
     *
     * @return empty state
     */
    public static ContextState empty() {
        return EMPTY;
    }

    /**
     * Creates a state from explicit values.
     *
     * @param requestId        request correlation identifier
     * @param authorize        authenticated subject
     * @param tokenCredential  Bearer-token credential
     * @param apiKeyCredential API-key credential
     * @return immutable state
     */
    public static ContextState of(
            String requestId,
            Authorize authorize,
            Http.Auth.Credential tokenCredential,
            Http.Auth.Credential apiKeyCredential) {
        if (StringKit.isBlank(requestId) && authorize == null && tokenCredential == null && apiKeyCredential == null) {
            return EMPTY;
        }
        return new ContextState(requestId, authorize, tokenCredential, apiKeyCredential);
    }

    /**
     * Creates a state containing identity without credentials.
     *
     * @param requestId request correlation identifier
     * @param authorize authenticated subject
     * @return immutable state
     */
    public static ContextState of(String requestId, Authorize authorize) {
        return of(requestId, authorize, null, null);
    }

    /**
     * Defensively copies an authenticated subject.
     *
     * @param value subject to copy
     * @return detached subject, or {@code null} when absent
     */
    private static Authorize copy(Authorize value) {
        return value == null ? null : ObjectKit.clone(value);
    }

    /**
     * Validates the type and value of one optional credential.
     *
     * @param credential credential to validate
     * @param expected   required credential type
     * @param label      human-readable credential label for validation errors
     * @throws IllegalArgumentException when the credential has an unexpected type or blank value
     */
    private static void validate(Http.Auth.Credential credential, EnumValue.Credential expected, String label) {
        if (credential != null && credential.type() != expected) {
            throw new IllegalArgumentException("Expected " + label + " credential");
        }
        if (credential != null && StringKit.isBlank(credential.value())) {
            throw new IllegalArgumentException("Expected non-blank " + label + " credential value");
        }
    }

    /**
     * Trims a text value and converts blank text to {@code null}.
     *
     * @param value value to normalize
     * @return normalized value, or {@code null} when absent or blank
     */
    private static String normalize(String value) {
        return StringKit.isBlank(value) ? null : value.trim();
    }

    /**
     * Returns a copy containing the supplied authenticated subject.
     *
     * @param value authenticated subject
     * @return updated immutable state
     */
    public ContextState withAuthorize(Authorize value) {
        return of(this.requestId, value, this.tokenCredential, this.apiKeyCredential);
    }

    /**
     * Returns a copy without raw request credentials.
     *
     * @return credential-free state
     */
    public ContextState withoutCredentials() {
        return of(this.requestId, this.authorize, null, null);
    }

    /**
     * Returns a copy containing only request correlation information.
     *
     * @return request-only state
     */
    public ContextState requestOnly() {
        return of(this.requestId, null, null, null);
    }

    /**
     * Returns the request correlation identifier.
     *
     * @return request identifier, or {@code null} when absent
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Returns a defensive copy of the authenticated subject.
     *
     * @return detached authenticated subject, or {@code null} when absent
     */
    public Authorize getAuthorize() {
        return copy(authorize);
    }

    /**
     * Returns the validated Bearer-token credential.
     *
     * @return Bearer-token credential, or {@code null} when absent
     */
    public Http.Auth.Credential getTokenCredential() {
        return tokenCredential;
    }

    /**
     * Returns the validated API-key credential.
     *
     * @return API-key credential, or {@code null} when absent
     */
    public Http.Auth.Credential getApiKeyCredential() {
        return apiKeyCredential;
    }

    /**
     * Tests whether this snapshot contains no correlation, identity, or credential values.
     *
     * @return {@code true} when every context value is absent
     */
    public boolean isEmpty() {
        return requestId == null && authorize == null && tokenCredential == null && apiKeyCredential == null;
    }

}
