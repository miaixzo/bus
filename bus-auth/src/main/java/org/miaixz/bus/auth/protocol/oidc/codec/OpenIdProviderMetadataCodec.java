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
package org.miaixz.bus.auth.protocol.oidc.codec;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.protocol.oauth2.AuthorizationServerMetadata;
import org.miaixz.bus.auth.protocol.oauth2.ClientAuthenticationMethod;
import org.miaixz.bus.auth.protocol.oauth2.GrantType;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.protocol.oauth2.codec.AuthorizationServerMetadataCodec;
import org.miaixz.bus.auth.protocol.oidc.*;
import org.miaixz.bus.auth.shared.jose.JwaAlgorithm;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;
import org.miaixz.bus.fabric.protocol.http.body.PayloadBody;

/**
 * Encodes and decodes OpenID Provider Metadata using the Discovery 1.0 registered JSON representation.
 * <p>
 * Every typed member retains its exact JSON type and unknown members remain provider-neutral extension values. Dynamic
 * client registration is deliberately outside this metadata model, and the codec closes every owned response after
 * bounded UTF-8 JSON decoding.
 * </p>
 *
 * @author Kimi Liu
 */
public final class OpenIdProviderMetadataCodec {

    /**
     * Maximum accepted metadata document size.
     */
    private static final long MAXIMUM_JSON_BYTES = Builder.MAXIMUM_DOCUMENT_BYTES;

    /**
     * Maximum JSON container nesting accepted from a remote Discovery document.
     */
    private static final int MAXIMUM_JSON_DEPTH = Normal._64;

    /**
     * Externally selected provider-neutral JSON implementation.
     */
    private final JsonProvider jsonProvider;

    /**
     * Shared RFC 8414 codec responsible for all OAuth metadata members.
     */
    private final AuthorizationServerMetadataCodec oauthCodec;

    /**
     * Creates a strict OpenID Provider Metadata codec.
     *
     * @param jsonProvider externally selected provider-neutral JSON implementation
     * @throws IllegalArgumentException if {@code jsonProvider} is {@code null}
     */
    public OpenIdProviderMetadataCodec(final JsonProvider jsonProvider) {
        this.jsonProvider = Assert.notNull(jsonProvider, "OpenID Connect metadata JSON provider must not be null");
        this.oauthCodec = new AuthorizationServerMetadataCodec(jsonProvider);
    }

    /**
     * Validates response status, media type, charset, and declared size.
     *
     * @param response response to inspect
     * @throws ValidateException if a transport constraint is invalid
     */
    private static void validateResponse(final HttpResponse response) {
        if (response.code() != Http.Status.OK) {
            throw new ValidateException("OpenID Connect discovery endpoint must return HTTP 200");
        }
        if (response.body().length() > MAXIMUM_JSON_BYTES) {
            throw new ValidateException("OpenID Connect metadata response exceeds one MiB");
        }
        final MediaType media = response.body().media();
        if (!MediaType.APPLICATION_JSON_TYPE.isCompatible(media)) {
            throw new ValidateException("OpenID Connect metadata response must use application/json");
        }
        final String charset = media.parameter(MediaType.CHARSET_PARAMETER);
        if (charset != null && !Charset.UTF_8.equals(media.charset())) {
            throw new ValidateException("OpenID Connect metadata response charset must be UTF-8");
        }
    }

    /**
     * Adds one mandatory string member.
     *
     * @param members mutable output object
     * @param name    exact member name
     * @param value   string value
     */
    private static void put(final Map<String, JsonValue> members, final String name, final String value) {
        members.put(name, new JsonValue.StringValue(value));
    }

    /**
     * Adds one optional string member when present.
     *
     * @param members mutable output object
     * @param name    exact member name
     * @param value   optional string value
     */
    private static void put(final Map<String, JsonValue> members, final String name, final Optional<String> value) {
        value.ifPresent(present -> put(members, name, present));
    }

    /**
     * Adds one optional Boolean member when present.
     *
     * @param members mutable output object
     * @param name    exact member name
     * @param value   optional Boolean value
     */
    private static void putBoolean(
            final Map<String, JsonValue> members,
            final String name,
            final Optional<Boolean> value) {
        value.ifPresent(present -> members.put(name, new JsonValue.BooleanValue(present)));
    }

    /**
     * Adds one string array, omitting an empty optional list.
     *
     * @param members mutable output object
     * @param name    exact member name
     * @param values  ordered string values
     */
    private static void putStrings(final Map<String, JsonValue> members, final String name, final List<String> values) {
        if (!values.isEmpty()) {
            final List<JsonValue> encoded = values.stream().map(JsonValue.StringValue::new).map(JsonValue.class::cast)
                    .toList();
            members.put(name, new JsonValue.ArrayValue(encoded));
        }
    }

    /**
     * Maps typed values to their exact wire strings and emits one array.
     *
     * @param <T>     typed metadata value
     * @param members mutable output object
     * @param name    exact member name
     * @param values  ordered typed values
     * @param mapper  exact wire-value mapper
     */
    private static <T> void putMapped(
            final Map<String, JsonValue> members,
            final String name,
            final List<T> values,
            final Function<T, String> mapper) {
        putStrings(members, name, values.stream().map(mapper).toList());
    }

    /**
     * Emits one ordered JOSE algorithm-name array.
     *
     * @param members mutable output object
     * @param name    exact member name
     * @param values  ordered algorithms
     */
    private static void putAlgorithms(
            final Map<String, JsonValue> members,
            final String name,
            final List<JwaAlgorithm> values) {
        putMapped(members, name, values, JwaAlgorithm::name);
    }

    /**
     * Reads a required non-empty JSON string member.
     *
     * @param values parsed object members
     * @param name   exact member name
     * @return non-empty string value
     */
    private static String requiredString(final Map<String, JsonValue> values, final String name) {
        final String value = optionalString(values, name);
        if (value == null || value.isEmpty()) {
            throw new ValidateException("OpenID Connect metadata requires non-empty string member: " + name);
        }
        return value;
    }

    /**
     * Reads an optional exact JSON string.
     *
     * @param values parsed object members
     * @param name   exact member name
     * @return string value or {@code null}
     */
    private static String optionalString(final Map<String, JsonValue> values, final String name) {
        final JsonValue value = values.get(name);
        if (value == null) {
            return null;
        }
        if (!(value instanceof JsonValue.StringValue string)) {
            throw new ValidateException("OpenID Connect metadata member must be a string: " + name);
        }
        return string.value();
    }

    /**
     * Reads an optional exact JSON Boolean.
     *
     * @param values parsed object members
     * @param name   exact member name
     * @return optional Boolean value
     */
    private static Optional<Boolean> optionalBoolean(final Map<String, JsonValue> values, final String name) {
        final JsonValue value = values.get(name);
        if (value == null) {
            return Optional.empty();
        }
        if (!(value instanceof JsonValue.BooleanValue flag)) {
            throw new ValidateException("OpenID Connect metadata member must be a boolean: " + name);
        }
        return Optional.of(flag.value());
    }

    /**
     * Reads one array of exact non-blank JSON strings.
     *
     * @param values   parsed object members
     * @param name     exact member name
     * @param required whether the member itself must be present
     * @return immutable ordered values, empty when optional and absent
     */
    private static List<String> strings(
            final Map<String, JsonValue> values,
            final String name,
            final boolean required) {
        final JsonValue value = values.get(name);
        if (value == null) {
            if (required) {
                throw new ValidateException("OpenID Connect metadata requires array member: " + name);
            }
            return List.of();
        }
        if (!(value instanceof JsonValue.ArrayValue array)) {
            throw new ValidateException("OpenID Connect metadata member must be an array: " + name);
        }
        final List<String> decoded = new ArrayList<>(array.values().size());
        for (JsonValue element : array.values()) {
            if (!(element instanceof JsonValue.StringValue string) || string.value().isBlank()) {
                throw new ValidateException("OpenID Connect metadata array must contain non-blank strings: " + name);
            }
            decoded.add(string.value());
        }
        return List.copyOf(decoded);
    }

    /**
     * Reads a present string array or applies an immutable Discovery 1.0 semantic default.
     *
     * @param values       parsed object members
     * @param name         exact optional member name
     * @param defaultValue standard value sequence used only when the member is absent
     * @return decoded present values or the supplied immutable default
     * @throws ValidateException if a present member is not an array of non-blank strings
     */
    private static List<String> stringsOrDefault(
            final Map<String, JsonValue> values,
            final String name,
            final List<String> defaultValue) {
        Assert.notNull(defaultValue, "OpenID Connect metadata default list must not be null");
        return values.containsKey(name) ? strings(values, name, true) : defaultValue;
    }

    /**
     * Maps decoded strings to strongly typed metadata values.
     *
     * @param <T>    typed metadata value
     * @param values ordered wire strings
     * @param mapper value constructor
     * @return immutable typed values
     */
    private static <T> List<T> map(final List<String> values, final Function<String, T> mapper) {
        return values.stream().map(mapper).toList();
    }

    /**
     * Reads one JOSE algorithm array.
     *
     * @param values   parsed object members
     * @param name     exact member name
     * @param required whether the member must be present
     * @return immutable algorithm values
     */
    private static List<JwaAlgorithm> algorithms(
            final Map<String, JsonValue> values,
            final String name,
            final boolean required) {
        return map(strings(values, name, required), JwaAlgorithm::of);
    }

    /**
     * Identifies OAuth metadata members delegated to the RFC 8414 codec.
     *
     * @param name exact metadata member name
     * @return {@code true} for an OAuth metadata member
     */
    private static boolean oauthMember(final String name) {
        return switch (name) {
            case OAuth2.Metadata.ISSUER, OAuth2.Metadata.AUTHORIZATION_ENDPOINT, OAuth2.Metadata.TOKEN_ENDPOINT, OAuth2.Metadata.JWKS_URI, OAuth2.Metadata.REGISTRATION_ENDPOINT, OAuth2.Metadata.SCOPES_SUPPORTED, OAuth2.Metadata.RESPONSE_TYPES_SUPPORTED, OAuth2.Metadata.RESPONSE_MODES_SUPPORTED, OAuth2.Metadata.GRANT_TYPES_SUPPORTED, OAuth2.Metadata.TOKEN_ENDPOINT_AUTH_METHODS_SUPPORTED, OAuth2.Metadata.TOKEN_ENDPOINT_AUTH_SIGNING_ALGORITHMS_SUPPORTED, OAuth2.Metadata.SERVICE_DOCUMENTATION, OAuth2.Metadata.UI_LOCALES_SUPPORTED, OAuth2.Metadata.OP_POLICY_URI, OAuth2.Metadata.OP_TOS_URI, OAuth2.Metadata.REVOCATION_ENDPOINT, OAuth2.Metadata.REVOCATION_ENDPOINT_AUTH_METHODS_SUPPORTED, OAuth2.Metadata.REVOCATION_ENDPOINT_AUTH_SIGNING_ALGORITHMS_SUPPORTED, OAuth2.Metadata.INTROSPECTION_ENDPOINT, OAuth2.Metadata.INTROSPECTION_ENDPOINT_AUTH_METHODS_SUPPORTED, OAuth2.Metadata.INTROSPECTION_ENDPOINT_AUTH_SIGNING_ALGORITHMS_SUPPORTED, OAuth2.Metadata.CODE_CHALLENGE_METHODS_SUPPORTED, OAuth2.Metadata.SIGNED_METADATA, OAuth2.Metadata.DEVICE_AUTHORIZATION_ENDPOINT, OAuth2.Metadata.AUTHORIZATION_RESPONSE_ISSUER_SUPPORTED, OAuth2.Metadata.DPOP_SIGNING_ALGORITHMS_SUPPORTED -> true;
            default -> false;
        };
    }

    /**
     * Identifies OpenID Connect metadata members represented by explicit components.
     *
     * @param name exact metadata member name
     * @return {@code true} for an OpenID Connect discovery member
     */
    private static boolean openIdMember(final String name) {
        return switch (name) {
            case OpenIdConnect.Metadata.USERINFO_ENDPOINT, OpenIdConnect.Metadata.ACR_VALUES_SUPPORTED, OpenIdConnect.Metadata.SUBJECT_TYPES_SUPPORTED, OpenIdConnect.Metadata.ID_TOKEN_SIGNING_ALGORITHMS_SUPPORTED, OpenIdConnect.Metadata.ID_TOKEN_ENCRYPTION_ALGORITHMS_SUPPORTED, OpenIdConnect.Metadata.ID_TOKEN_ENCRYPTION_METHODS_SUPPORTED, OpenIdConnect.Metadata.USERINFO_SIGNING_ALGORITHMS_SUPPORTED, OpenIdConnect.Metadata.USERINFO_ENCRYPTION_ALGORITHMS_SUPPORTED, OpenIdConnect.Metadata.USERINFO_ENCRYPTION_METHODS_SUPPORTED, OpenIdConnect.Metadata.DISPLAY_VALUES_SUPPORTED, OpenIdConnect.Metadata.CLAIM_TYPES_SUPPORTED, OpenIdConnect.Metadata.CLAIMS_SUPPORTED, OpenIdConnect.Metadata.CLAIMS_LOCALES_SUPPORTED, OpenIdConnect.Metadata.CLAIMS_PARAMETER_SUPPORTED, OpenIdConnect.Metadata.REQUEST_PARAMETER_SUPPORTED, OpenIdConnect.Metadata.REQUEST_URI_PARAMETER_SUPPORTED, OpenIdConnect.Metadata.REQUIRE_REQUEST_URI_REGISTRATION, OpenIdConnect.Metadata.END_SESSION_ENDPOINT -> true;
            default -> false;
        };
    }

    /**
     * Validates an OpenID Provider Metadata resource request.
     *
     * @param request immutable Fabric HTTP request
     * @throws IllegalArgumentException if {@code request} is {@code null}
     * @throws ValidateException        if method, query, fragment, or body is invalid
     */
    public void validateRequest(final HttpRequest request) {
        Assert.notNull(request, "OpenID Connect metadata HTTP request must not be null");
        if (request.method() != Http.Method.GET || !request.url().query().isEmpty() || request.url().fragment() != null
                || request.body().length() != 0L) {
            throw new ValidateException(
                    "OpenID Connect metadata resource requires GET without query, fragment, or body");
        }
    }

    /**
     * Encodes one complete metadata document as HTTP 200 application/json.
     *
     * @param request  originating Fabric HTTP request
     * @param metadata standard OpenID Provider Metadata
     * @return complete cache-prevented HTTP response
     */
    public HttpResponse encodeResponse(final HttpRequest request, final OpenIdProviderMetadata metadata) {
        Assert.notNull(request, "OpenID Connect metadata HTTP request must not be null");
        Assert.notNull(metadata, "OpenID Connect Provider Metadata must not be null");
        final Map<String, JsonValue> members;
        try (HttpResponse oauth = oauthCodec.encodeResponse(request, metadata.authorizationServerMetadata())) {
            final JsonValue value = jsonProvider.readValue(oauth.bytes(MAXIMUM_JSON_BYTES), MAXIMUM_JSON_DEPTH, true);
            if (!(value instanceof JsonValue.ObjectValue object)) {
                throw new ValidateException("OAuth authorization server metadata encoder returned a non-object body");
            }
            members = new LinkedHashMap<>(object.values());
        }
        put(members, OpenIdConnect.Metadata.USERINFO_ENDPOINT, metadata.userInfoEndpoint());
        putStrings(members, OpenIdConnect.Metadata.ACR_VALUES_SUPPORTED, metadata.acrValuesSupported());
        putMapped(
                members,
                OpenIdConnect.Metadata.SUBJECT_TYPES_SUPPORTED,
                metadata.subjectTypesSupported(),
                SubjectType::value);
        putAlgorithms(
                members,
                OpenIdConnect.Metadata.ID_TOKEN_SIGNING_ALGORITHMS_SUPPORTED,
                metadata.idTokenSigningAlgValuesSupported());
        putAlgorithms(
                members,
                OpenIdConnect.Metadata.ID_TOKEN_ENCRYPTION_ALGORITHMS_SUPPORTED,
                metadata.idTokenEncryptionAlgValuesSupported());
        putAlgorithms(
                members,
                OpenIdConnect.Metadata.ID_TOKEN_ENCRYPTION_METHODS_SUPPORTED,
                metadata.idTokenEncryptionEncValuesSupported());
        putAlgorithms(
                members,
                OpenIdConnect.Metadata.USERINFO_SIGNING_ALGORITHMS_SUPPORTED,
                metadata.userInfoSigningAlgValuesSupported());
        putAlgorithms(
                members,
                OpenIdConnect.Metadata.USERINFO_ENCRYPTION_ALGORITHMS_SUPPORTED,
                metadata.userInfoEncryptionAlgValuesSupported());
        putAlgorithms(
                members,
                OpenIdConnect.Metadata.USERINFO_ENCRYPTION_METHODS_SUPPORTED,
                metadata.userInfoEncryptionEncValuesSupported());
        putMapped(
                members,
                OpenIdConnect.Metadata.DISPLAY_VALUES_SUPPORTED,
                metadata.displayValuesSupported(),
                Display::value);
        putMapped(
                members,
                OpenIdConnect.Metadata.CLAIM_TYPES_SUPPORTED,
                metadata.claimTypesSupported(),
                ClaimType::value);
        putStrings(members, OpenIdConnect.Metadata.CLAIMS_SUPPORTED, metadata.claimsSupported());
        putStrings(members, OpenIdConnect.Metadata.CLAIMS_LOCALES_SUPPORTED, metadata.claimsLocalesSupported());
        putBoolean(members, OpenIdConnect.Metadata.CLAIMS_PARAMETER_SUPPORTED, metadata.claimsParameterSupported());
        putBoolean(members, OpenIdConnect.Metadata.REQUEST_PARAMETER_SUPPORTED, metadata.requestParameterSupported());
        putBoolean(
                members,
                OpenIdConnect.Metadata.REQUEST_URI_PARAMETER_SUPPORTED,
                metadata.requestUriParameterSupported());
        putBoolean(
                members,
                OpenIdConnect.Metadata.REQUIRE_REQUEST_URI_REGISTRATION,
                metadata.requireRequestUriRegistration());
        put(members, OpenIdConnect.Metadata.END_SESSION_ENDPOINT, metadata.endSessionEndpoint());
        members.putAll(metadata.extensions().values());
        final byte[] body = jsonProvider.writeValue(new JsonValue.ObjectValue(members));
        return HttpResponse.builder().request(request).code(Http.Status.OK).headers(
                Headers.of(Http.Header.CACHE_CONTROL, Http.Cache.NO_STORE, Http.Header.PRAGMA, Http.Cache.NO_CACHE))
                .body(PayloadBody.of(Payload.of(body), MediaType.APPLICATION_JSON_TYPE)).build();
    }

    /**
     * Decodes and closes one bounded HTTP 200 OpenID Provider Metadata response.
     *
     * @param response owned Fabric HTTP response
     * @return validated standard metadata model
     * @throws IllegalArgumentException if {@code response} is {@code null}
     * @throws ValidateException        if HTTP, media, JSON, or registered-member shape is invalid
     */
    public OpenIdProviderMetadata decode(final HttpResponse response) {
        final HttpResponse encoded = Assert.notNull(response, "OpenID Connect metadata HTTP response must not be null");
        try (encoded) {
            validateResponse(encoded);
            final JsonValue root = jsonProvider.readValue(encoded.bytes(MAXIMUM_JSON_BYTES), MAXIMUM_JSON_DEPTH, true);
            if (!(root instanceof JsonValue.ObjectValue object)) {
                throw new ValidateException("OpenID Connect metadata JSON root must be an object");
            }
            final Map<String, JsonValue> values = object.values();
            final Map<String, JsonValue> oauthMembers = new LinkedHashMap<>();
            final Map<String, JsonValue> extensions = new LinkedHashMap<>();
            values.forEach((name, value) -> {
                if (oauthMember(name)) {
                    oauthMembers.put(name, value);
                } else if (!openIdMember(name)) {
                    extensions.put(name, value);
                }
            });
            if (!oauthMembers.containsKey(OAuth2.Metadata.RESPONSE_MODES_SUPPORTED)) {
                putStrings(
                        oauthMembers,
                        OAuth2.Metadata.RESPONSE_MODES_SUPPORTED,
                        List.of(OpenIdConnect.ResponseModes.QUERY, OpenIdConnect.ResponseModes.FRAGMENT));
            }
            if (!oauthMembers.containsKey(OAuth2.Metadata.GRANT_TYPES_SUPPORTED)) {
                putStrings(
                        oauthMembers,
                        OAuth2.Metadata.GRANT_TYPES_SUPPORTED,
                        List.of(GrantType.AUTHORIZATION_CODE.value(), "implicit"));
            }
            if (!oauthMembers.containsKey(OAuth2.Metadata.TOKEN_ENDPOINT_AUTH_METHODS_SUPPORTED)) {
                putStrings(
                        oauthMembers,
                        OAuth2.Metadata.TOKEN_ENDPOINT_AUTH_METHODS_SUPPORTED,
                        List.of(ClientAuthenticationMethod.CLIENT_SECRET_BASIC.value()));
            }
            final byte[] oauthBody = jsonProvider.writeValue(new JsonValue.ObjectValue(oauthMembers));
            final HttpResponse oauthResponse = HttpResponse.builder().request(encoded.request()).code(Http.Status.OK)
                    .body(PayloadBody.of(Payload.of(oauthBody), MediaType.APPLICATION_JSON_TYPE)).build();
            final AuthorizationServerMetadata oauth = oauthCodec.decode(oauthResponse);
            return new OpenIdProviderMetadata(oauth,
                    Optional.ofNullable(optionalString(values, OpenIdConnect.Metadata.USERINFO_ENDPOINT)),
                    strings(values, OpenIdConnect.Metadata.ACR_VALUES_SUPPORTED, false),
                    map(strings(values, OpenIdConnect.Metadata.SUBJECT_TYPES_SUPPORTED, true), SubjectType::new),
                    algorithms(values, OpenIdConnect.Metadata.ID_TOKEN_SIGNING_ALGORITHMS_SUPPORTED, true),
                    algorithms(values, OpenIdConnect.Metadata.ID_TOKEN_ENCRYPTION_ALGORITHMS_SUPPORTED, false),
                    algorithms(values, OpenIdConnect.Metadata.ID_TOKEN_ENCRYPTION_METHODS_SUPPORTED, false),
                    algorithms(values, OpenIdConnect.Metadata.USERINFO_SIGNING_ALGORITHMS_SUPPORTED, false),
                    algorithms(values, OpenIdConnect.Metadata.USERINFO_ENCRYPTION_ALGORITHMS_SUPPORTED, false),
                    algorithms(values, OpenIdConnect.Metadata.USERINFO_ENCRYPTION_METHODS_SUPPORTED, false),
                    map(strings(values, OpenIdConnect.Metadata.DISPLAY_VALUES_SUPPORTED, false), Display::new),
                    map(strings(values, OpenIdConnect.Metadata.CLAIM_TYPES_SUPPORTED, false), ClaimType::new),
                    strings(values, OpenIdConnect.Metadata.CLAIMS_SUPPORTED, false),
                    strings(values, OpenIdConnect.Metadata.CLAIMS_LOCALES_SUPPORTED, false),
                    optionalBoolean(values, OpenIdConnect.Metadata.CLAIMS_PARAMETER_SUPPORTED),
                    optionalBoolean(values, OpenIdConnect.Metadata.REQUEST_PARAMETER_SUPPORTED),
                    optionalBoolean(values, OpenIdConnect.Metadata.REQUEST_URI_PARAMETER_SUPPORTED),
                    optionalBoolean(values, OpenIdConnect.Metadata.REQUIRE_REQUEST_URI_REGISTRATION),
                    Optional.ofNullable(optionalString(values, OpenIdConnect.Metadata.END_SESSION_ENDPOINT)),
                    new JsonValue.ObjectValue(extensions));
        }
    }

}
