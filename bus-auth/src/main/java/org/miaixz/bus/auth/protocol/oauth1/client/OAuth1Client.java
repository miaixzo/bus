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
package org.miaixz.bus.auth.protocol.oauth1.client;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.protocol.oauth1.*;
import org.miaixz.bus.auth.shared.ExecutionServices;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.UnoUrl;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;

/**
 * Provides the four RFC 5849 client operations through one immutable OAuth 1.0 facade.
 * <p>
 * Network operations are delegated to their single-operation clients. Resource owner authorization is different: it
 * creates the user-agent redirect URI and does not pretend that the later callback response already exists.
 * </p>
 *
 * @author Kimi Liu
 */
public final class OAuth1Client {

    /**
     * Client settings that contain the registered resource owner authorization endpoint.
     */
    private final OAuth1ClientSettings settings;

    /**
     * Single-operation temporary credentials client.
     */
    private final TemporaryCredentialsClient temporaryCredentialsClient;

    /**
     * Single-operation token credentials client.
     */
    private final TokenCredentialsClient tokenCredentialsClient;

    /**
     * Single-operation protected resource client.
     */
    private final ProtectedResourceClient protectedResourceClient;

    /**
     * Creates an OAuth 1.0 client for one compiled Source registration.
     *
     * @param namespaceId namespace that isolates dynamic credential bindings
     * @param sourceId    compiled Source registration identifier
     * @param settings    validated OAuth 1.0 client settings
     * @param services    externally owned runtime dependencies
     * @throws IllegalArgumentException if an argument is {@code null} or an identifier is blank
     */
    public OAuth1Client(final String namespaceId, final String sourceId, final OAuth1ClientSettings settings,
            final ExecutionServices services) {
        Assert.notBlank(namespaceId, "OAuth 1.0 namespace id must not be blank");
        Assert.notBlank(sourceId, "OAuth 1.0 Source id must not be blank");
        this.settings = Assert.notNull(settings, "OAuth 1.0 client settings must not be null");
        final ExecutionServices runtime = Assert.notNull(services, "OAuth 1.0 execution services must not be null");
        this.temporaryCredentialsClient = new TemporaryCredentialsClient(namespaceId, sourceId, settings, runtime);
        this.tokenCredentialsClient = new TokenCredentialsClient(namespaceId, sourceId, settings, runtime);
        this.protectedResourceClient = new ProtectedResourceClient(namespaceId, sourceId, settings, runtime);
    }

    /**
     * Obtains temporary credentials according to RFC 5849 section 2.1.
     *
     * @param request standard temporary credentials request
     * @param context immutable invocation context
     * @param timeout shared end-to-end time budget
     * @return stage containing the standard response or a closed framework failure
     */
    public CompletionStage<Outcome<TemporaryCredentialsResponse>> temporaryCredentials(
            final TemporaryCredentialsRequest request,
            final Context context,
            final Timeout.Budget timeout) {
        return temporaryCredentialsClient.temporaryCredentials(request, context, timeout);
    }

    /**
     * Creates the resource owner authorization URI according to RFC 5849 section 2.2.
     *
     * @param request standard resource owner authorization request
     * @param context immutable invocation context
     * @param timeout shared end-to-end time budget
     * @return completed stage containing an absolute authorization URI or a closed framework failure
     */
    public CompletionStage<Outcome<UnoUrl>> authorize(
            final ResourceOwnerAuthorizationRequest request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(request, "Resource owner authorization request must not be null");
        Assert.notNull(context, "OAuth 1.0 invocation context must not be null");
        Assert.notNull(timeout, "OAuth 1.0 time budget must not be null");
        if (timeout.expired()) {
            return CompletableFuture.completedFuture(
                    Outcome.failed(
                            new Outcome.Failure(ErrorCode._408,
                                    "OAuth 1.0 resource owner authorization has no remaining time budget",
                                    new JsonValue.ObjectValue(Map.of()))));
        }
        UnoUrl location = settings.resourceOwnerAuthorizationEndpoint().url();
        if (location.queryParameter(OAuth1.Parameters.TOKEN) != null) {
            throw new ValidateException("Resource owner authorization endpoint must not contain oauth_token");
        }
        location = location.withQuery(OAuth1.Parameters.TOKEN, request.oauthToken());
        for (OAuth1Parameter parameter : request.parameters()) {
            location = location.withQuery(parameter.name(), parameter.value());
        }
        return CompletableFuture.completedFuture(Outcome.succeeded(location));
    }

    /**
     * Obtains token credentials according to RFC 5849 section 2.3.
     *
     * @param request standard token credentials request
     * @param context immutable invocation context
     * @param timeout shared end-to-end time budget
     * @return stage containing the standard response or a closed framework failure
     */
    public CompletionStage<Outcome<TokenCredentialsResponse>> tokenCredentials(
            final TokenCredentialsRequest request,
            final Context context,
            final Timeout.Budget timeout) {
        return tokenCredentialsClient.tokenCredentials(request, context, timeout);
    }

    /**
     * Accesses a protected resource according to RFC 5849 section 3.
     *
     * @param request exact protected resource request before OAuth signing
     * @param context immutable invocation context
     * @param timeout shared end-to-end time budget
     * @return stage containing the caller-owned Fabric HTTP response or a closed framework failure
     */
    public CompletionStage<Outcome<HttpResponse>> access(
            final ProtectedResourceRequest request,
            final Context context,
            final Timeout.Budget timeout) {
        return protectedResourceClient.access(request, context, timeout);
    }

}
