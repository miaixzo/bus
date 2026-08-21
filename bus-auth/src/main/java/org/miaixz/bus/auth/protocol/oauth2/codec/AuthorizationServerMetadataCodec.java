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
package org.miaixz.bus.auth.protocol.oauth2.codec;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.protocol.oauth2.*;
import org.miaixz.bus.auth.shared.jose.JwaAlgorithm;
import org.miaixz.bus.auth.shared.pkce.PkceMethod;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;
import org.miaixz.bus.fabric.protocol.http.body.PayloadBody;

/**
 * Validates and translates OAuth 2.0 Authorization Server Metadata defined by RFC 8414.
 * <p>
 * The codec preserves registered JSON names and provider-neutral extension values. Discovery URL derivation, issuer
 * trust binding, and signed metadata verification remain outside this representation boundary.
 * </p>
 *
 * @author Kimi Liu
 */
public final class AuthorizationServerMetadataCodec {

    /**
     * Maximum accepted metadata document size in bytes.
     */
    private static final long MAXIMUM_JSON_BYTES = Builder.MAXIMUM_DOCUMENT_BYTES;

    /**
     * Provider-neutral JSON service.
     */
    private final JsonProvider jsonProvider;

    /**
     * Creates a strict RFC 8414 metadata codec.
     *
     * @param jsonProvider provider-neutral JSON service
     * @throws IllegalArgumentException if the provider is {@code null}
     */
    public AuthorizationServerMetadataCodec(final JsonProvider jsonProvider) {
        this.jsonProvider = Assert.notNull(jsonProvider, "OAuth 2.x metadata JSON provider must not be null");
    }

    /**
     * Validates metadata response status, media, charset, and declared size.
     *
     * @param response response to inspect
     * @throws ValidateException if the transport representation is invalid
     */
    private static void validateResponse(final HttpResponse response) {
        if (response.code() != Http.Status.OK) {
            throw new ValidateException("OAuth 2.x metadata endpoint must return HTTP 200");
        }
        if (response.body().length() > MAXIMUM_JSON_BYTES) {
            throw new ValidateException("OAuth 2.x metadata response exceeds the maximum JSON size");
        }
        final MediaType media = response.body().media();
        if (!MediaType.APPLICATION_JSON_TYPE.isCompatible(media)) {
            throw new ValidateException("OAuth 2.x metadata response must use application/json");
        }
        final String charset = media.parameter(MediaType.CHARSET_PARAMETER);
        if (charset != null && !Charset.UTF_8.equals(media.charset())) {
            throw new ValidateException("OAuth 2.x metadata response charset must be UTF-8");
        }
    }

    /**
     * Adds one present optional string member.
     *
     * @param members mutable encoded members
     * @param name    metadata member name
     * @param value   optional text
     */
    private static void put(final Map<String, JsonValue> members, final String name, final Optional<String> value) {
        value.ifPresent(item -> members.put(name, text(item)));
    }

    /**
     * Adds one non-empty typed string array member.
     *
     * @param <T>     source element type
     * @param members mutable encoded members
     * @param name    metadata member name
     * @param values  ordered typed values
     * @param mapper  exact wire-value mapper
     */
    private static <T> void putList(
            final Map<String, JsonValue> members,
            final String name,
            final List<T> values,
            final Function<T, String> mapper) {
        if (!values.isEmpty()) {
            members.put(name, array(values, mapper));
        }
    }

    /**
     * Creates one provider-neutral JSON string.
     *
     * @param value decoded string
     * @return JSON string value
     */
    private static JsonValue.StringValue text(final String value) {
        return new JsonValue.StringValue(value);
    }

    /**
     * Converts typed values to a provider-neutral JSON string array.
     *
     * @param <T>    source element type
     * @param values ordered values
     * @param mapper exact wire-value mapper
     * @return immutable JSON array
     */
    private static <T> JsonValue.ArrayValue array(final List<T> values, final Function<T, String> mapper) {
        return new JsonValue.ArrayValue(
                values.stream().map(mapper).map(JsonValue.StringValue::new).map(JsonValue.class::cast).toList());
    }

    /**
     * Adds one extension after defensively checking its registered name.
     *
     * @param members mutable encoded members
     * @param name    extension member name
     * @param value   provider-neutral extension value
     * @throws ValidateException if name is registered
     */
    private static void extension(final Map<String, JsonValue> members, final String name, final JsonValue value) {
        if (registered(name)) {
            throw new ValidateException("OAuth 2.x metadata extension duplicates a registered member");
        }
        members.put(name, value);
    }

    /**
     * Identifies metadata names represented by dedicated model components.
     *
     * @param name exact JSON metadata member name
     * @return {@code true} for an OAuth metadata member owned by the typed model
     */
    private static boolean registered(final String name) {
        return switch (name) {
            case OAuth2.Metadata.ISSUER, OAuth2.Metadata.AUTHORIZATION_ENDPOINT, OAuth2.Metadata.TOKEN_ENDPOINT, OAuth2.Metadata.JWKS_URI, OAuth2.Metadata.REGISTRATION_ENDPOINT, OAuth2.Metadata.SCOPES_SUPPORTED, OAuth2.Metadata.RESPONSE_TYPES_SUPPORTED, OAuth2.Metadata.RESPONSE_MODES_SUPPORTED, OAuth2.Metadata.GRANT_TYPES_SUPPORTED, OAuth2.Metadata.TOKEN_ENDPOINT_AUTH_METHODS_SUPPORTED, OAuth2.Metadata.TOKEN_ENDPOINT_AUTH_SIGNING_ALGORITHMS_SUPPORTED, OAuth2.Metadata.SERVICE_DOCUMENTATION, OAuth2.Metadata.UI_LOCALES_SUPPORTED, OAuth2.Metadata.OP_POLICY_URI, OAuth2.Metadata.OP_TOS_URI, OAuth2.Metadata.REVOCATION_ENDPOINT, OAuth2.Metadata.REVOCATION_ENDPOINT_AUTH_METHODS_SUPPORTED, OAuth2.Metadata.REVOCATION_ENDPOINT_AUTH_SIGNING_ALGORITHMS_SUPPORTED, OAuth2.Metadata.INTROSPECTION_ENDPOINT, OAuth2.Metadata.INTROSPECTION_ENDPOINT_AUTH_METHODS_SUPPORTED, OAuth2.Metadata.INTROSPECTION_ENDPOINT_AUTH_SIGNING_ALGORITHMS_SUPPORTED, OAuth2.Metadata.CODE_CHALLENGE_METHODS_SUPPORTED, OAuth2.Metadata.SIGNED_METADATA, OAuth2.Metadata.DEVICE_AUTHORIZATION_ENDPOINT, OAuth2.Metadata.AUTHORIZATION_RESPONSE_ISSUER_SUPPORTED, OAuth2.Metadata.DPOP_SIGNING_ALGORITHMS_SUPPORTED -> true;
            default -> false;
        };
    }

    /**
     * Reads a required non-empty JSON string member.
     *
     * @param values metadata object members
     * @param name   required member name
     * @return decoded string
     * @throws ValidateException if absent, empty, null, or not a string
     */
    private static String requiredString(final Map<String, JsonValue> values, final String name) {
        final String value = string(values, name);
        if (value == null || value.isEmpty()) {
            throw new ValidateException("OAuth 2.x metadata requires non-empty string member: " + name);
        }
        return value;
    }

    /**
     * Reads an optional JSON string into the Bus optional type.
     *
     * @param values metadata object members
     * @param name   optional member name
     * @return normalized optional string
     * @throws ValidateException if a present member is not a string
     */
    private static Optional<String> optional(final Map<String, JsonValue> values, final String name) {
        return Optional.ofNullable(string(values, name));
    }

    /**
     * Reads an optional JSON string while rejecting explicit null and other types.
     *
     * @param values metadata object members
     * @param name   member name
     * @return decoded text, or {@code null} when absent
     * @throws ValidateException if a present member is not a string
     */
    private static String string(final Map<String, JsonValue> values, final String name) {
        final JsonValue value = values.get(name);
        if (value == null) {
            return null;
        }
        if (value instanceof JsonValue.StringValue text) {
            return text.value();
        }
        throw new ValidateException("OAuth 2.x metadata member must be a string: " + name);
    }

    /**
     * Reads one optional or required JSON string array.
     *
     * @param values   metadata object members
     * @param name     array member name
     * @param required whether absence is invalid
     * @return immutable ordered string values
     * @throws ValidateException if missing when required or not a string array
     */
    private static List<String> strings(
            final Map<String, JsonValue> values,
            final String name,
            final boolean required) {
        final JsonValue value = values.get(name);
        if (value == null) {
            if (required) {
                throw new ValidateException("OAuth 2.x metadata requires array member: " + name);
            }
            return List.of();
        }
        if (!(value instanceof JsonValue.ArrayValue array)) {
            throw new ValidateException("OAuth 2.x metadata member must be an array: " + name);
        }
        final List<String> result = new ArrayList<>(array.values().size());
        for (JsonValue element : array.values()) {
            if (!(element instanceof JsonValue.StringValue text)) {
                throw new ValidateException("OAuth 2.x metadata arrays must contain only strings: " + name);
            }
            result.add(text.value());
        }
        return List.copyOf(result);
    }

    /**
     * Maps decoded wire strings to immutable typed values.
     *
     * @param <T>    target value type
     * @param values ordered wire values
     * @param mapper value constructor or factory
     * @return immutable ordered typed values
     */
    private static <T> List<T> map(final List<String> values, final Function<String, T> mapper) {
        return values.stream().map(mapper).toList();
    }

    /**
     * Reads an optional JSON boolean member.
     *
     * @param values metadata object members
     * @param name   boolean member name
     * @return normalized optional boolean
     * @throws ValidateException if a present member is not a boolean
     */
    private static Optional<Boolean> optionalBoolean(final Map<String, JsonValue> values, final String name) {
        final JsonValue value = values.get(name);
        if (value == null) {
            return Optional.empty();
        }
        if (value instanceof JsonValue.BooleanValue flag) {
            return Optional.of(flag.value());
        }
        throw new ValidateException("OAuth 2.x metadata member must be a boolean: " + name);
    }

    /**
     * Validates an authorization server metadata resource request.
     *
     * @param request immutable Fabric HTTP request
     * @throws IllegalArgumentException if request is {@code null}
     * @throws ValidateException        if method, URL, or body is invalid
     */
    public void validateRequest(final HttpRequest request) {
        Assert.notNull(request, "OAuth 2.x metadata HTTP request must not be null");
        if (request.method() != Http.Method.GET) {
            throw new ValidateException("OAuth 2.x metadata resource requires HTTP GET");
        }
        if (!request.url().query().isEmpty() || request.url().fragment() != null) {
            throw new ValidateException("OAuth 2.x metadata resource request must not contain query or fragment");
        }
        if (request.body().length() != 0L) {
            throw new ValidateException("OAuth 2.x metadata resource GET must not contain a body");
        }
    }

    /**
     * Encodes typed metadata as a standard HTTP 200 JSON resource response.
     *
     * @param request  originating metadata resource request
     * @param metadata validated RFC 8414 metadata
     * @return complete HTTP 200 JSON response
     * @throws IllegalArgumentException if an argument is {@code null}
     * @throws ValidateException        if an extension uses a registered metadata name
     */
    public HttpResponse encodeResponse(final HttpRequest request, final AuthorizationServerMetadata metadata) {
        Assert.notNull(request, "OAuth 2.x metadata HTTP request must not be null");
        Assert.notNull(metadata, "OAuth 2.x authorization server metadata must not be null");
        final Map<String, JsonValue> members = new LinkedHashMap<>();
        members.put(OAuth2.Metadata.ISSUER, text(metadata.issuer()));
        put(members, OAuth2.Metadata.AUTHORIZATION_ENDPOINT, metadata.authorizationEndpoint());
        put(members, OAuth2.Metadata.TOKEN_ENDPOINT, metadata.tokenEndpoint());
        put(members, OAuth2.Metadata.JWKS_URI, metadata.jwksUri());
        putList(members, OAuth2.Metadata.SCOPES_SUPPORTED, metadata.scopesSupported(), Function.identity());
        members.put(
                OAuth2.Metadata.RESPONSE_TYPES_SUPPORTED,
                array(metadata.responseTypesSupported(), ResponseType::value));
        putList(
                members,
                OAuth2.Metadata.RESPONSE_MODES_SUPPORTED,
                metadata.responseModesSupported(),
                Function.identity());
        putList(members, OAuth2.Metadata.GRANT_TYPES_SUPPORTED, metadata.grantTypesSupported(), GrantType::value);
        putList(
                members,
                OAuth2.Metadata.TOKEN_ENDPOINT_AUTH_METHODS_SUPPORTED,
                metadata.tokenEndpointAuthMethodsSupported(),
                ClientAuthenticationMethod::value);
        putList(
                members,
                OAuth2.Metadata.TOKEN_ENDPOINT_AUTH_SIGNING_ALGORITHMS_SUPPORTED,
                metadata.tokenEndpointAuthSigningAlgValuesSupported(),
                JwaAlgorithm::name);
        put(members, OAuth2.Metadata.SERVICE_DOCUMENTATION, metadata.serviceDocumentation());
        putList(members, OAuth2.Metadata.UI_LOCALES_SUPPORTED, metadata.uiLocalesSupported(), Function.identity());
        put(members, OAuth2.Metadata.OP_POLICY_URI, metadata.opPolicyUri());
        put(members, OAuth2.Metadata.OP_TOS_URI, metadata.opTosUri());
        put(members, OAuth2.Metadata.REVOCATION_ENDPOINT, metadata.revocationEndpoint());
        putList(
                members,
                OAuth2.Metadata.REVOCATION_ENDPOINT_AUTH_METHODS_SUPPORTED,
                metadata.revocationEndpointAuthMethodsSupported(),
                ClientAuthenticationMethod::value);
        putList(
                members,
                OAuth2.Metadata.REVOCATION_ENDPOINT_AUTH_SIGNING_ALGORITHMS_SUPPORTED,
                metadata.revocationEndpointAuthSigningAlgValuesSupported(),
                JwaAlgorithm::name);
        put(members, OAuth2.Metadata.INTROSPECTION_ENDPOINT, metadata.introspectionEndpoint());
        putList(
                members,
                OAuth2.Metadata.INTROSPECTION_ENDPOINT_AUTH_METHODS_SUPPORTED,
                metadata.introspectionEndpointAuthMethodsSupported(),
                ClientAuthenticationMethod::value);
        putList(
                members,
                OAuth2.Metadata.INTROSPECTION_ENDPOINT_AUTH_SIGNING_ALGORITHMS_SUPPORTED,
                metadata.introspectionEndpointAuthSigningAlgValuesSupported(),
                JwaAlgorithm::name);
        putList(
                members,
                OAuth2.Metadata.CODE_CHALLENGE_METHODS_SUPPORTED,
                metadata.codeChallengeMethodsSupported(),
                PkceMethod::value);
        put(members, OAuth2.Metadata.SIGNED_METADATA, metadata.signedMetadata());
        put(members, OAuth2.Metadata.DEVICE_AUTHORIZATION_ENDPOINT, metadata.deviceAuthorizationEndpoint());
        metadata.authorizationResponseIssParameterSupported().ifPresent(
                value -> members.put(
                        OAuth2.Metadata.AUTHORIZATION_RESPONSE_ISSUER_SUPPORTED,
                        new JsonValue.BooleanValue(value)));
        putList(
                members,
                OAuth2.Metadata.DPOP_SIGNING_ALGORITHMS_SUPPORTED,
                metadata.dpopSigningAlgValuesSupported(),
                JwaAlgorithm::name);
        metadata.extensions().values().forEach((name, value) -> extension(members, name, value));
        final byte[] body = jsonProvider.writeValue(new JsonValue.ObjectValue(members));
        return HttpResponse.builder().request(request).code(Http.Status.OK)
                .body(PayloadBody.of(Payload.of(body), MediaType.APPLICATION_JSON_TYPE)).build();
    }

    /**
     * Decodes one standard metadata document and closes the owned HTTP response on every path.
     *
     * @param response owned Fabric HTTP response
     * @return validated RFC 8414 metadata model
     * @throws IllegalArgumentException if response is {@code null}
     * @throws ValidateException        if status, media, JSON shape, or a registered member is invalid
     */
    public AuthorizationServerMetadata decode(final HttpResponse response) {
        final HttpResponse encoded = Assert.notNull(response, "OAuth 2.x metadata HTTP response must not be null");
        try (encoded) {
            validateResponse(encoded);
            final JsonValue value = jsonProvider.readValue(encoded.bytes(MAXIMUM_JSON_BYTES));
            if (!(value instanceof JsonValue.ObjectValue object)) {
                throw new ValidateException("OAuth 2.x metadata JSON root must be an object");
            }
            final Map<String, JsonValue> values = object.values();
            if (values.containsKey(OAuth2.Metadata.REGISTRATION_ENDPOINT)) {
                throw new ValidateException(
                        "OAuth 2.x metadata codec cannot represent a dynamic client registration endpoint");
            }
            final Map<String, JsonValue> extensions = new LinkedHashMap<>();
            values.forEach((name, member) -> {
                if (!registered(name)) {
                    extensions.put(name, member);
                }
            });
            return new AuthorizationServerMetadata(requiredString(values, OAuth2.Metadata.ISSUER),
                    optional(values, OAuth2.Metadata.AUTHORIZATION_ENDPOINT),
                    optional(values, OAuth2.Metadata.TOKEN_ENDPOINT), optional(values, OAuth2.Metadata.JWKS_URI),
                    strings(values, OAuth2.Metadata.SCOPES_SUPPORTED, false),
                    map(strings(values, OAuth2.Metadata.RESPONSE_TYPES_SUPPORTED, true), ResponseType::new),
                    strings(values, OAuth2.Metadata.RESPONSE_MODES_SUPPORTED, false),
                    map(strings(values, OAuth2.Metadata.GRANT_TYPES_SUPPORTED, false), GrantType::new),
                    map(
                            strings(values, OAuth2.Metadata.TOKEN_ENDPOINT_AUTH_METHODS_SUPPORTED, false),
                            ClientAuthenticationMethod::new),
                    map(
                            strings(values, OAuth2.Metadata.TOKEN_ENDPOINT_AUTH_SIGNING_ALGORITHMS_SUPPORTED, false),
                            JwaAlgorithm::of),
                    optional(values, OAuth2.Metadata.SERVICE_DOCUMENTATION),
                    strings(values, OAuth2.Metadata.UI_LOCALES_SUPPORTED, false),
                    optional(values, OAuth2.Metadata.OP_POLICY_URI), optional(values, OAuth2.Metadata.OP_TOS_URI),
                    optional(values, OAuth2.Metadata.REVOCATION_ENDPOINT),
                    map(
                            strings(values, OAuth2.Metadata.REVOCATION_ENDPOINT_AUTH_METHODS_SUPPORTED, false),
                            ClientAuthenticationMethod::new),
                    map(
                            strings(
                                    values,
                                    OAuth2.Metadata.REVOCATION_ENDPOINT_AUTH_SIGNING_ALGORITHMS_SUPPORTED,
                                    false),
                            JwaAlgorithm::of),
                    optional(values, OAuth2.Metadata.INTROSPECTION_ENDPOINT),
                    map(
                            strings(values, OAuth2.Metadata.INTROSPECTION_ENDPOINT_AUTH_METHODS_SUPPORTED, false),
                            ClientAuthenticationMethod::new),
                    map(
                            strings(
                                    values,
                                    OAuth2.Metadata.INTROSPECTION_ENDPOINT_AUTH_SIGNING_ALGORITHMS_SUPPORTED,
                                    false),
                            JwaAlgorithm::of),
                    map(strings(values, OAuth2.Metadata.CODE_CHALLENGE_METHODS_SUPPORTED, false), PkceMethod::of),
                    optional(values, OAuth2.Metadata.SIGNED_METADATA),
                    optional(values, OAuth2.Metadata.DEVICE_AUTHORIZATION_ENDPOINT),
                    optionalBoolean(values, OAuth2.Metadata.AUTHORIZATION_RESPONSE_ISSUER_SUPPORTED),
                    map(strings(values, OAuth2.Metadata.DPOP_SIGNING_ALGORITHMS_SUPPORTED, false), JwaAlgorithm::of),
                    new JsonValue.ObjectValue(extensions));
        }
    }

}
