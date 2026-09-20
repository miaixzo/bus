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
import org.miaixz.bus.core.lang.annotation.Nullable;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.lang.exception.TokenException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Static, read-only facade for the context installed around the current execution.
 * <p>
 * Every method is an in-memory lookup. This class never reads transport objects, resolves Spring Beans, authenticates
 * users, refreshes credentials, or performs network I/O. Raw credential values returned by this API are sensitive and
 * must never be written to logs.
 *
 * @author Kimi Liu
 */
public final class ContextBuilder {

    /**
     * Prevents instantiation of the static context facade.
     */
    private ContextBuilder() {
        // No initialization required.
    }

    /**
     * Tests whether an execution context is currently installed.
     *
     * @return {@code true} when a lexical context scope is active
     */
    public static boolean isPresent() {
        return ContextCarrier.isPresent();
    }

    /**
     * Returns the current immutable state, or the shared empty state outside a scope.
     *
     * @return current state
     */
    public static ContextState current() {
        return ContextCarrier.current();
    }

    /**
     * Captures the current immutable state for explicit propagation.
     *
     * @return current state
     */
    public static ContextState capture() {
        return current();
    }

    /**
     * Returns the correlation identifier associated with the current execution.
     *
     * @return current request identifier, or {@code null} when no identifier is available
     */
    @Nullable
    public static String getRequestId() {
        return current().getRequestId();
    }

    /**
     * Returns a detached copy of the authenticated subject in the current context.
     *
     * @return authenticated subject, or {@code null} when the current execution is anonymous
     */
    @Nullable
    public static Authorize getAuthorize() {
        return current().getAuthorize();
    }

    /**
     * Returns the authenticated subject in the current context.
     *
     * @return detached authenticated subject
     * @throws AuthorizedException when the current execution has no authenticated subject
     */
    public static Authorize requireAuthorize() {
        Authorize authorize = getAuthorize();
        if (authorize == null) {
            throw new AuthorizedException("No authenticated subject is available in the current context");
        }
        return authorize;
    }

    /**
     * Returns the normalized tenant identifier of the authenticated subject.
     *
     * @return tenant identifier, or {@code null} when no authenticated tenant is available
     */
    @Nullable
    public static String getTenantId() {
        Authorize authorize = getAuthorize();
        return authorize == null ? null : normalize(authorize.getX_tenant_id());
    }

    /**
     * Returns the preferred raw credential for the current execution.
     * <p>
     * A Bearer token takes precedence over an API key when both credential types are present.
     *
     * @return current credential, or {@code null} when no credential is available
     */
    @Nullable
    public static Http.Auth.Credential getCredential() {
        ContextState state = current();
        Http.Auth.Credential credential = state.getTokenCredential();
        return credential == null ? state.getApiKeyCredential() : credential;
    }

    /**
     * Returns the normalized Bearer-token value for the current execution.
     * <p>
     * The returned value is sensitive and must not be written to application logs.
     *
     * @return Bearer-token value, or {@code null} when unavailable
     */
    @Nullable
    public static String getToken() {
        return value(current().getTokenCredential());
    }

    /**
     * Returns the normalized Bearer-token value for the current execution.
     *
     * @return non-blank Bearer-token value
     * @throws TokenException when no Bearer token is available
     */
    public static String requireToken() {
        String token = getToken();
        if (token == null) {
            throw new TokenException("No Bearer token is available in the current context");
        }
        return token;
    }

    /**
     * Returns the normalized API-key value for the current execution.
     * <p>
     * The returned value is sensitive and must not be written to application logs.
     *
     * @return API-key value, or {@code null} when unavailable
     */
    @Nullable
    public static String getApiKey() {
        return value(current().getApiKeyCredential());
    }

    /**
     * Returns the normalized API-key value for the current execution.
     *
     * @return non-blank API-key value
     * @throws AuthorizedException when no API key is available
     */
    public static String requireApiKey() {
        String apiKey = getApiKey();
        if (apiKey == null) {
            throw new AuthorizedException("No API key is available in the current context");
        }
        return apiKey;
    }

    /**
     * Extracts and normalizes the secret value from a credential.
     *
     * @param credential credential to inspect
     * @return normalized credential value, or {@code null} when absent or blank
     */
    private static String value(Http.Auth.Credential credential) {
        return credential == null ? null : normalize(credential.value());
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

}
