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
package org.miaixz.bus.auth.vendor.oschina;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.vendor.*;
import org.miaixz.bus.auth.vendor.catalog.BuiltinVendors;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Third-party client for OSChina authorization-code exchange and profile retrieval.
 *
 * <p>
 * Both remote operations use registration-owned endpoints and the injected Fabric context. The token operation resolves
 * the client secret for the active root context and clears the caller-owned character array after use.
 * </p>
 *
 * @author Kimi Liu
 */
public class OschinaProvider extends AbstractProvider {

    /**
     * Creates an OSChina client from explicit runtime dependencies.
     *
     * @param configuration non-null registration, Fabric, clock, state-store, cache, and secret dependencies
     */
    public OschinaProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.OSCHINA);
    }

    /**
     * Parses and validates a typed OSChina response.
     *
     * @param document  JSON response document
     * @param operation diagnostic operation name that contains no credentials
     * @return parsed response
     * @throws AuthorizedException if the document is empty, malformed, or reports an OAuth error
     */
    private static Response read(final String document, final String operation) {
        try {
            final Response response = JsonKit.toPojo(document, Response.class);
            if (response == null) {
                throw new AuthorizedException("Failed to parse " + operation + " response: empty response");
            }
            if (response.error() != null) {
                throw new AuthorizedException(
                        response.error_description() == null ? "Unknown error" : response.error_description());
            }
            return response;
        } catch (AuthorizedException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new AuthorizedException("Failed to parse " + operation + " response: " + exception.getMessage());
        }
    }

    /**
     * Exchanges an authorization code through OSChina's query-authenticated token GET operation.
     *
     * @param context  root context used to resolve and clear the client secret
     * @param callback inbound callback containing the authorization code
     * @return successful message containing the mapped token fields
     * @throws NullPointerException if the context or callback is {@code null}
     * @throws AuthorizedException  if OSChina returns an error or omits the access token
     */
    @Override
    public Message<VendorTokenSet> token(final Context context, final Callback.Inbound callback) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        final Map<String, String> query = new LinkedHashMap<>();
        query.put("code", inbound.value("code").orElse(null));
        query.put("client_id", registration.clientId());
        query.put("client_secret", secret(current));
        query.put("grant_type", "authorization_code");
        query.put("redirect_uri", registration.redirectUri());
        query.put("dataType", "json");
        final Response response = read(get(endpoint(VendorEndpoint.TOKEN), query), "access token");
        if (response.access_token() == null) {
            throw new AuthorizedException("Missing access_token in response");
        }
        return Message.success(
                VendorTokenSet.builder().token(response.access_token()).refresh(response.refresh_token())
                        .uid(response.uid()).expireIn(response.expires_in()).build());
    }

    /**
     * Retrieves the OSChina profile through an access-token query GET operation.
     *
     * @param context immutable root operation context
     * @param token   non-null token set containing the OSChina access token
     * @return successful message containing the mapped identity
     * @throws NullPointerException if the context or token is {@code null}
     * @throws AuthorizedException  if OSChina returns an error or omits the profile identifier
     */
    @Override
    public Message<VendorIdentity> userInfo(final Context context, final VendorTokenSet token) {
        Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final Response response = read(
                get(
                        endpoint(VendorEndpoint.USERINFO),
                        Map.of("access_token", authorization.getToken(), "dataType", "json")),
                "user info");
        if (response.id() == null) {
            throw new AuthorizedException("Missing id in user info response");
        }
        return Message.success(
                VendorIdentity.builder().rawJson(JsonKit.toJsonString(response)).uuid(response.id())
                        .username(response.name()).nickname(response.name()).avatar(response.avatar())
                        .blog(response.url()).location(response.location()).gender(Gender.of(response.gender()))
                        .email(response.email()).token(authorization).source(descriptor().id()).build());
    }

    /**
     * Typed union of OSChina token, profile, and OAuth error fields.
     *
     * @param access_token      access token
     * @param refresh_token     refresh token
     * @param uid               token response user identifier
     * @param expires_in        token lifetime in seconds
     * @param id                profile identifier
     * @param name              display name
     * @param avatar            avatar URL
     * @param url               profile URL
     * @param location          location text
     * @param gender            gender text
     * @param email             email address
     * @param error             OAuth error code
     * @param error_description OAuth error diagnostic
     * @author Kimi Liu
     */
    private record Response(String access_token, String refresh_token, String uid, int expires_in, String id,
            String name, String avatar, String url, String location, String gender, String email, String error,
            String error_description) {
    }

}
