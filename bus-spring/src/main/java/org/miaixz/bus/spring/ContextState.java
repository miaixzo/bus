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

import org.miaixz.bus.core.basic.entity.Authorize;
import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Immutable snapshot of the framework request context.
 * <p>
 * Snapshot instances copy authorization data and never retain servlet objects, request caches, or thread-local
 * containers, allowing the captured values to cross execution boundaries safely.
 *
 * @author Kimi Liu
 */
public class ContextState {

    /**
     * Shared empty immutable state.
     */
    private static final ContextState EMPTY = new ContextState(null, null, null, null);

    /**
     * Captured request correlation identifier.
     */
    private final String requestId;

    /**
     * Detached captured authorization information.
     */
    private final Authorize authorize;

    /**
     * Captured token credential with its request-source metadata.
     */
    private final Http.Auth.Credential tokenCredential;

    /**
     * Captured API-key credential with its request-source metadata.
     */
    private final Http.Auth.Credential apiKeyCredential;

    /**
     * Creates an immutable detached state.
     *
     * @param requestId        request correlation identifier
     * @param authorize        authorization information
     * @param tokenCredential  token credential
     * @param apiKeyCredential API-key credential
     */
    public ContextState(String requestId, Authorize authorize, Http.Auth.Credential tokenCredential,
            Http.Auth.Credential apiKeyCredential) {
        this.requestId = requestId;
        this.authorize = copy(authorize);
        this.tokenCredential = tokenCredential;
        this.apiKeyCredential = apiKeyCredential;
    }

    /**
     * Returns the empty snapshot.
     *
     * @return the empty snapshot
     */
    public static ContextState empty() {
        return EMPTY;
    }

    /**
     * Creates a snapshot from explicit immutable context values.
     *
     * @param requestId request correlation identifier
     * @param authorize authenticated authorization information
     * @return a detached snapshot
     */
    public static ContextState of(String requestId, Authorize authorize) {
        return of(requestId, authorize, null, null);
    }

    /**
     * Creates a snapshot containing identity and request credentials.
     *
     * @param requestId        request correlation identifier
     * @param authorize        authenticated authorization information
     * @param tokenCredential  resolved token credential
     * @param apiKeyCredential resolved API-key credential
     * @return a detached snapshot
     * @throws IllegalArgumentException when a credential has the wrong type or a blank value
     */
    public static ContextState of(
            String requestId,
            Authorize authorize,
            Http.Auth.Credential tokenCredential,
            Http.Auth.Credential apiKeyCredential) {
        validate(tokenCredential, EnumValue.Credential.TOKEN, "token");
        validate(apiKeyCredential, EnumValue.Credential.API_KEY, "API-key");
        if (requestId == null && authorize == null && tokenCredential == null && apiKeyCredential == null) {
            return EMPTY;
        }
        return new ContextState(requestId, authorize, tokenCredential, apiKeyCredential);
    }

    /**
     * Gets the captured request identifier.
     *
     * @return the request identifier, or {@code null}
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Gets a detached copy of the captured authorization information.
     *
     * @return authorization information, or {@code null}
     */
    public Authorize getAuthorize() {
        return copy(authorize);
    }

    /**
     * Gets the captured token credential without exposing its value through diagnostics.
     *
     * @return token credential, or {@code null}
     */
    public Http.Auth.Credential getTokenCredential() {
        return tokenCredential;
    }

    /**
     * Gets the captured API-key credential without exposing its value through diagnostics.
     *
     * @return API-key credential, or {@code null}
     */
    public Http.Auth.Credential getApiKeyCredential() {
        return apiKeyCredential;
    }

    /**
     * Tests whether this snapshot contains no context values.
     *
     * @return {@code true} when all context values are absent
     */
    public boolean isEmpty() {
        return requestId == null && authorize == null && tokenCredential == null && apiKeyCredential == null;
    }

    /**
     * Creates a defensive copy of authorization information.
     *
     * @param authorize source authorization information
     * @return detached copy, or {@code null}
     */
    private static Authorize copy(Authorize authorize) {
        return authorize == null ? null : ObjectKit.clone(authorize);
    }

    /**
     * Verifies that a captured credential matches its immutable state slot.
     *
     * @param credential credential to validate
     * @param expected   required credential type
     * @param label      safe diagnostic label
     */
    private static void validate(Http.Auth.Credential credential, EnumValue.Credential expected, String label) {
        if (credential != null && credential.type() != expected) {
            throw new IllegalArgumentException("Expected " + label + " credential");
        }
        if (credential != null && StringKit.isBlank(credential.value())) {
            throw new IllegalArgumentException("Expected non-blank " + label + " credential value");
        }
    }

}
