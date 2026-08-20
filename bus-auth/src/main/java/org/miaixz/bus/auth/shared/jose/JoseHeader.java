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
package org.miaixz.bus.auth.shared.jose;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Represents the protected and unprotected parameter sections whose union forms one RFC 7515 or RFC 7516 JOSE Header.
 * <p>
 * Registered JOSE parameters are decoded into explicit immutable components. Unknown extension parameters retain their
 * provider-neutral JSON representation because their value grammar belongs to the defining extension. JSON objects are
 * exposed only at the JOSE serialization boundary; algorithm policy and execution-key resolution remain external.
 * </p>
 *
 * @author Kimi Liu
 */
public final class JoseHeader {

    /**
     * Registered JOSE algorithm member name.
     */
    public static final String ALGORITHM = "alg";
    /**
     * Registered JWK Set URL member name.
     */
    public static final String JWK_SET_URL = "jku";
    /**
     * Registered embedded JSON Web Key member name.
     */
    public static final String JSON_WEB_KEY = "jwk";
    /**
     * Registered key identifier member name.
     */
    public static final String KEY_ID = "kid";
    /**
     * Registered X.509 certificate URL member name.
     */
    public static final String CERTIFICATE_URL = "x5u";
    /**
     * Registered X.509 certificate chain member name.
     */
    public static final String CERTIFICATE_CHAIN = "x5c";
    /**
     * Registered SHA-1 X.509 certificate thumbprint member name.
     */
    public static final String CERTIFICATE_THUMBPRINT = "x5t";
    /**
     * Registered SHA-256 X.509 certificate thumbprint member name.
     */
    public static final String CERTIFICATE_THUMBPRINT_SHA256 = "x5t#S256";
    /**
     * Registered complete JOSE object media type member name.
     */
    public static final String TYPE = "typ";
    /**
     * Registered secured content media type member name.
     */
    public static final String CONTENT_TYPE = "cty";
    /**
     * Registered critical extension member name.
     */
    public static final String CRITICAL = "crit";
    /**
     * Registered JWE content-encryption algorithm member name.
     */
    public static final String ENCRYPTION = "enc";
    /**
     * Registered JWE compression algorithm member name.
     */
    public static final String COMPRESSION = "zip";

    /**
     * Empty immutable parameter section used by protected-only JOSE objects.
     */
    private static final Parameters EMPTY = new Parameters(Optional.empty(), Optional.empty(), Optional.empty(),
            Optional.empty(), Optional.empty(), List.of(), Optional.empty(), Optional.empty(), Optional.empty(),
            Optional.empty(), List.of(), Optional.empty(), Optional.empty(), new JsonValue.ObjectValue(Map.of()));

    /**
     * Integrity-protected registered and extension parameters.
     */
    private final Parameters protectedSection;
    /**
     * Unprotected registered and extension parameters.
     */
    private final Parameters unprotectedSection;

    /**
     * Decodes and validates two JSON wire sections without discarding unknown extension values or member placement.
     *
     * @param protectedParameters   integrity-protected JOSE parameters
     * @param unprotectedParameters unprotected JOSE parameters
     * @throws IllegalArgumentException if either section is {@code null}
     * @throws ValidateException        if a parameter has an invalid type, placement, or duplicate definition
     */
    public JoseHeader(final JsonValue.ObjectValue protectedParameters,
            final JsonValue.ObjectValue unprotectedParameters) {
        this(decode(protectedParameters, true), decode(unprotectedParameters, false));
    }

    /**
     * Creates a JOSE Header from explicit protected and unprotected parameter components.
     *
     * @param protectedSection   integrity-protected parameter components
     * @param unprotectedSection unprotected parameter components
     * @throws IllegalArgumentException if either section is {@code null}
     * @throws ValidateException        if the union violates JOSE member or critical-extension rules
     */
    public JoseHeader(final Parameters protectedSection, final Parameters unprotectedSection) {
        this.protectedSection = Assert.notNull(protectedSection, "JOSE protected section must not be null");
        this.unprotectedSection = Assert.notNull(unprotectedSection, "JOSE unprotected section must not be null");
        final JsonValue.ObjectValue protectedValues = encode(protectedSection);
        final JsonValue.ObjectValue unprotectedValues = encode(unprotectedSection);
        for (String name : protectedValues.values().keySet()) {
            if (unprotectedValues.values().containsKey(name)) {
                throw new ValidateException("JOSE header parameter appears in protected and unprotected sections");
            }
        }
        final Map<String, JsonValue> combined = combine(protectedValues, unprotectedValues);
        if (!combined.containsKey(ALGORITHM)) {
            throw new ValidateException("JOSE header must contain alg");
        }
        validateCritical(protectedSection, unprotectedSection, combined);
    }

    /**
     * Creates a JOSE Header whose complete wire parameter object is integrity protected.
     *
     * @param parameters integrity-protected header members
     * @return immutable protected-only JOSE Header
     */
    public static JoseHeader protectedOnly(final JsonValue.ObjectValue parameters) {
        return new JoseHeader(parameters, new JsonValue.ObjectValue(Map.of()));
    }

    /**
     * Creates a protected JWS Header from explicit registered values and isolated extension members.
     *
     * @param algorithm  mandatory JWS algorithm identifier
     * @param keyId      optional key identifier
     * @param key        optional embedded public JWK
     * @param type       optional complete-object media type
     * @param extensions non-registered protected extension parameters
     * @return immutable protected-only JWS Header
     */
    public static JoseHeader jws(
            final JwaAlgorithm algorithm,
            final Optional<String> keyId,
            final Optional<Jwk> key,
            final Optional<String> type,
            final JsonValue.ObjectValue extensions) {
        return new JoseHeader(new Parameters(Optional.of(Assert.notNull(algorithm, "JWS algorithm must not be null")),
                Optional.empty(), Assert.notNull(key, "JWS embedded-key container must not be null"),
                Assert.notNull(keyId, "JWS key-id container must not be null"), Optional.empty(), List.of(),
                Optional.empty(), Optional.empty(), Assert.notNull(type, "JWS type container must not be null"),
                Optional.empty(), List.of(), Optional.empty(), Optional.empty(), extensions), EMPTY);
    }

    /**
     * Decodes registered parameters from one JSON section and isolates every unregistered extension member.
     *
     * @param values           wire parameter object
     * @param protectedSection whether the object came from the integrity-protected section
     * @return explicit immutable parameter components
     */
    private static Parameters decode(final JsonValue.ObjectValue values, final boolean protectedSection) {
        Assert.notNull(values, "JOSE parameter object must not be null");
        final Map<String, JsonValue> extensions = new LinkedHashMap<>();
        for (Map.Entry<String, JsonValue> entry : values.values().entrySet()) {
            Assert.notBlank(entry.getKey(), "JOSE header parameter name must not be blank");
            if (!registered(entry.getKey())) {
                extensions.put(entry.getKey(), entry.getValue());
            }
        }
        final List<String> critical = strings(values, CRITICAL, true);
        if (!protectedSection && !critical.isEmpty()) {
            throw new ValidateException("JOSE crit parameter must be integrity protected");
        }
        return new Parameters(optionalString(values, ALGORITHM).map(JwaAlgorithm::of),
                optionalString(values, JWK_SET_URL).map(JoseHeader::absoluteUri),
                optionalObject(values, JSON_WEB_KEY).map(Jwk::new), optionalString(values, KEY_ID),
                optionalString(values, CERTIFICATE_URL).map(JoseHeader::absoluteUri),
                strings(values, CERTIFICATE_CHAIN, true), optionalString(values, CERTIFICATE_THUMBPRINT),
                optionalString(values, CERTIFICATE_THUMBPRINT_SHA256), optionalString(values, TYPE),
                optionalString(values, CONTENT_TYPE), critical,
                optionalString(values, ENCRYPTION).map(JwaAlgorithm::of), optionalString(values, COMPRESSION),
                new JsonValue.ObjectValue(extensions));
    }

    /**
     * Encodes explicit parameter components into their registered JOSE wire names.
     *
     * @param section parameter components to encode
     * @return detached immutable JSON object
     */
    private static JsonValue.ObjectValue encode(final Parameters section) {
        final Map<String, JsonValue> values = new LinkedHashMap<>();
        section.algorithm().ifPresent(value -> values.put(ALGORITHM, string(value.name())));
        section.jwkSetUrl().ifPresent(value -> values.put(JWK_SET_URL, string(value.toASCIIString())));
        section.jsonWebKey().ifPresent(value -> values.put(JSON_WEB_KEY, value.parameters()));
        section.keyId().ifPresent(value -> values.put(KEY_ID, string(value)));
        section.certificateUrl().ifPresent(value -> values.put(CERTIFICATE_URL, string(value.toASCIIString())));
        putStrings(values, CERTIFICATE_CHAIN, section.certificateChain());
        section.certificateThumbprint().ifPresent(value -> values.put(CERTIFICATE_THUMBPRINT, string(value)));
        section.certificateThumbprintSha256()
                .ifPresent(value -> values.put(CERTIFICATE_THUMBPRINT_SHA256, string(value)));
        section.type().ifPresent(value -> values.put(TYPE, string(value)));
        section.contentType().ifPresent(value -> values.put(CONTENT_TYPE, string(value)));
        putStrings(values, CRITICAL, section.critical());
        section.encryption().ifPresent(value -> values.put(ENCRYPTION, string(value.name())));
        section.compression().ifPresent(value -> values.put(COMPRESSION, string(value)));
        values.putAll(section.extensions().values());
        return new JsonValue.ObjectValue(values);
    }

    /**
     * Returns a validated optional JSON string without applying type coercion.
     *
     * @param values source parameter object
     * @param name   exact registered member name
     * @return non-blank string when present
     */
    private static Optional<String> optionalString(final JsonValue.ObjectValue values, final String name) {
        final JsonValue value = values.values().get(name);
        if (value == null) {
            return Optional.empty();
        }
        if (!(value instanceof JsonValue.StringValue string) || string.value().isBlank()) {
            throw new ValidateException("JOSE registered string parameter must contain a non-blank string");
        }
        return Optional.of(string.value());
    }

    /**
     * Returns a validated optional JSON object without applying type coercion.
     *
     * @param values source parameter object
     * @param name   exact registered member name
     * @return object value when present
     */
    private static Optional<JsonValue.ObjectValue> optionalObject(
            final JsonValue.ObjectValue values,
            final String name) {
        final JsonValue value = values.values().get(name);
        if (value == null) {
            return Optional.empty();
        }
        if (!(value instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException("JOSE registered object parameter must contain a JSON object");
        }
        return Optional.of(object);
    }

    /**
     * Returns a validated string-array member while preserving element order.
     *
     * @param values   source parameter object
     * @param name     exact registered member name
     * @param nonEmpty whether an explicitly present array must contain at least one value
     * @return immutable string values, or an empty list when absent
     */
    private static List<String> strings(final JsonValue.ObjectValue values, final String name, final boolean nonEmpty) {
        final JsonValue value = values.values().get(name);
        if (value == null) {
            return List.of();
        }
        if (!(value instanceof JsonValue.ArrayValue array) || nonEmpty && array.values().isEmpty()) {
            throw new ValidateException("JOSE registered array parameter has an invalid JSON value");
        }
        final List<String> result = new ArrayList<>(array.values().size());
        for (JsonValue element : array.values()) {
            if (!(element instanceof JsonValue.StringValue string) || string.value().isBlank()) {
                throw new ValidateException("JOSE registered array entries must be non-blank strings");
            }
            result.add(string.value());
        }
        return List.copyOf(result);
    }

    /**
     * Converts and validates an absolute registered URI value.
     *
     * @param value exact URI text
     * @return absolute immutable URI
     */
    private static URI absoluteUri(final String value) {
        try {
            final URI uri = new URI(value);
            if (!uri.isAbsolute()) {
                throw new ValidateException("JOSE URL parameter must be an absolute URI");
            }
            return uri;
        } catch (URISyntaxException cause) {
            throw new ValidateException("JOSE URL parameter syntax is invalid", cause);
        }
    }

    /**
     * Adds a non-empty string-array parameter to a JSON output object.
     *
     * @param target destination JSON members
     * @param name   exact registered member name
     * @param values validated component values
     */
    private static void putStrings(final Map<String, JsonValue> target, final String name, final List<String> values) {
        if (values.isEmpty()) {
            return;
        }
        final List<JsonValue> encoded = values.stream().map(JsonValue.StringValue::new).map(JsonValue.class::cast)
                .toList();
        target.put(name, new JsonValue.ArrayValue(encoded));
    }

    /**
     * Creates one JSON string value.
     *
     * @param value validated component text
     * @return JSON string value
     */
    private static JsonValue.StringValue string(final String value) {
        return new JsonValue.StringValue(value);
    }

    /**
     * Identifies names registered by the JOSE Header and JWA specifications without maintaining a parallel field set.
     *
     * @param name exact case-sensitive member name
     * @return {@code true} when the name belongs to an explicit component
     */
    private static boolean registered(final String name) {
        return switch (name) {
            case ALGORITHM, JWK_SET_URL, JSON_WEB_KEY, KEY_ID, CERTIFICATE_URL, CERTIFICATE_CHAIN, CERTIFICATE_THUMBPRINT, CERTIFICATE_THUMBPRINT_SHA256, TYPE, CONTENT_TYPE, CRITICAL, ENCRYPTION, COMPRESSION -> true;
            default -> false;
        };
    }

    /**
     * Combines two parameter objects while preserving protected members before unprotected members.
     *
     * @param protectedValues   encoded protected section
     * @param unprotectedValues encoded unprotected section
     * @return mutable ordered union used only for validation or detached output
     */
    private static Map<String, JsonValue> combine(
            final JsonValue.ObjectValue protectedValues,
            final JsonValue.ObjectValue unprotectedValues) {
        final Map<String, JsonValue> combined = new LinkedHashMap<>(
                protectedValues.values().size() + unprotectedValues.values().size());
        combined.putAll(protectedValues.values());
        combined.putAll(unprotectedValues.values());
        return combined;
    }

    /**
     * Enforces protected placement and extension-only membership for the RFC 7515 critical parameter.
     *
     * @param protectedValues   protected component section
     * @param unprotectedValues unprotected component section
     * @param combined          complete encoded header union
     */
    private static void validateCritical(
            final Parameters protectedValues,
            final Parameters unprotectedValues,
            final Map<String, JsonValue> combined) {
        if (!unprotectedValues.critical().isEmpty()) {
            throw new ValidateException("JOSE crit parameter must be integrity protected");
        }
        if (protectedValues.critical().isEmpty()) {
            return;
        }
        final Set<String> names = new LinkedHashSet<>();
        for (String name : protectedValues.critical()) {
            if (!names.add(name)) {
                throw new ValidateException("JOSE crit parameter must not contain duplicate names");
            }
            if (registered(name)) {
                throw new ValidateException("JOSE crit parameter must contain extension names only");
            }
            if (!combined.containsKey(name)) {
                throw new ValidateException("JOSE crit entry must identify an existing header parameter");
            }
        }
    }

    /**
     * Returns the encoded integrity-protected parameter object required by JOSE serializers.
     *
     * @return detached immutable protected parameter object
     */
    public JsonValue.ObjectValue protectedParameters() {
        return encode(protectedSection);
    }

    /**
     * Returns the encoded unprotected parameter object required by JOSE serializers.
     *
     * @return detached immutable unprotected parameter object
     */
    public JsonValue.ObjectValue unprotectedParameters() {
        return encode(unprotectedSection);
    }

    /**
     * Returns the ordered encoded union of protected and unprotected parameters.
     *
     * @return detached immutable complete JOSE Header object
     */
    public JsonValue.ObjectValue parameters() {
        return new JsonValue.ObjectValue(combine(protectedParameters(), unprotectedParameters()));
    }

    /**
     * Looks up one complete JOSE Header wire member without applying extension value coercion.
     *
     * @param name exact case-sensitive member name
     * @return registered or extension JSON value when present
     */
    public Optional<JsonValue> parameter(final String name) {
        Assert.notBlank(name, "JOSE header parameter name must not be blank");
        final JsonValue.ObjectValue protectedValues = protectedParameters();
        return Optional.ofNullable(
                protectedValues.values().containsKey(name) ? protectedValues.values().get(name)
                        : unprotectedParameters().values().get(name));
    }

    /**
     * Returns the mandatory case-sensitive JOSE algorithm identifier.
     *
     * @return exact algorithm wire identifier
     */
    public String algorithm() {
        return protectedSection.algorithm().or(() -> unprotectedSection.algorithm())
                .orElseThrow(() -> new ValidateException("Required JOSE alg parameter is absent")).name();
    }

    /**
     * Returns the optional JWE content-encryption algorithm identifier.
     *
     * @return exact encryption wire identifier when present
     */
    public Optional<String> encryption() {
        return protectedSection.encryption().or(() -> unprotectedSection.encryption()).map(JwaAlgorithm::name);
    }

    /**
     * Returns the optional key identifier hint without changing case.
     *
     * @return key identifier when present
     */
    public Optional<String> keyId() {
        return protectedSection.keyId().or(() -> unprotectedSection.keyId());
    }

    /**
     * Returns the optional media type of the complete JOSE object.
     *
     * @return complete-object media type when present
     */
    public Optional<String> type() {
        return protectedSection.type().or(() -> unprotectedSection.type());
    }

    /**
     * Returns the optional media type of the secured content.
     *
     * @return secured-content media type when present
     */
    public Optional<String> contentType() {
        return protectedSection.contentType().or(() -> unprotectedSection.contentType());
    }

    /**
     * Returns the ordered critical extension names declared in the protected section.
     *
     * @return immutable critical extension names
     */
    public List<String> critical() {
        return protectedSection.critical();
    }

    /**
     * Confirms that every declared critical extension is implemented by the current consumer.
     *
     * @param understood exact case-sensitive extension names understood and processed by the consumer
     * @throws IllegalArgumentException if {@code understood} is {@code null}
     * @throws ValidateException        if a declared critical extension is not understood
     */
    public void validateCritical(final Set<String> understood) {
        Assert.notNull(understood, "Understood JOSE critical parameters must not be null");
        for (String name : critical()) {
            if (!understood.contains(name)) {
                throw new ValidateException("JOSE critical header parameter is not understood");
            }
        }
    }

    /**
     * Requires selected members to occur in the integrity-protected section when present.
     *
     * @param names exact case-sensitive member names requiring integrity protection
     * @throws IllegalArgumentException if {@code names} is {@code null}
     * @throws ValidateException        if a selected member occurs only in the unprotected section
     */
    public void requireProtected(final Set<String> names) {
        Assert.notNull(names, "Required protected JOSE parameter names must not be null");
        final JsonValue.ObjectValue unprotectedValues = unprotectedParameters();
        for (String name : names) {
            Assert.notBlank(name, "Required protected JOSE parameter name must not be blank");
            if (unprotectedValues.values().containsKey(name)) {
                throw new ValidateException("JOSE header parameter must be integrity protected");
            }
        }
    }

    /**
     * Compares both typed parameter sections without exposing their internal representation.
     *
     * @param other comparison value
     * @return {@code true} when both JOSE sections are equal
     */
    @Override
    public boolean equals(final Object other) {
        return this == other || other instanceof JoseHeader header && protectedSection.equals(header.protectedSection)
                && unprotectedSection.equals(header.unprotectedSection);
    }

    /**
     * Returns a hash derived from both typed parameter sections.
     *
     * @return stable value hash
     */
    @Override
    public int hashCode() {
        return Objects.hash(protectedSection, unprotectedSection);
    }

    /**
     * Returns a structural summary without rendering extension or certificate values.
     *
     * @return non-sensitive JOSE Header summary
     */
    @Override
    public String toString() {
        return "JoseHeader[algorithm=" + algorithm() + ",protected=" + protectedParameters().values().keySet()
                + ",unprotected=" + unprotectedParameters().values().keySet() + "]";
    }

    /**
     * Holds the registered JOSE parameters and unregistered extension values from one serialization section.
     *
     * @param algorithm                   optional JOSE algorithm identifier
     * @param jwkSetUrl                   optional JWK Set URL
     * @param jsonWebKey                  optional embedded JSON Web Key
     * @param keyId                       optional key identifier hint
     * @param certificateUrl              optional X.509 certificate URL
     * @param certificateChain            ordered base64 DER certificate chain
     * @param certificateThumbprint       optional SHA-1 certificate thumbprint
     * @param certificateThumbprintSha256 optional SHA-256 certificate thumbprint
     * @param type                        optional complete-object media type
     * @param contentType                 optional secured-content media type
     * @param critical                    ordered critical extension names
     * @param encryption                  optional JWE content-encryption algorithm
     * @param compression                 optional JWE compression algorithm
     * @param extensions                  unregistered extension parameters
     * @author Kimi Liu
     */
    public record Parameters(Optional<JwaAlgorithm> algorithm, Optional<URI> jwkSetUrl, Optional<Jwk> jsonWebKey,
            Optional<String> keyId, Optional<URI> certificateUrl, List<String> certificateChain,
            Optional<String> certificateThumbprint, Optional<String> certificateThumbprintSha256, Optional<String> type,
            Optional<String> contentType, List<String> critical, Optional<JwaAlgorithm> encryption,
            Optional<String> compression, JsonValue.ObjectValue extensions) {

        /**
         * Validates, detaches, and freezes all registered components and extension members.
         *
         * @throws IllegalArgumentException if a component or container is {@code null}
         * @throws ValidateException        if text, URI, array, or extension ownership is invalid
         */
        public Parameters {
            Assert.notNull(algorithm, "JOSE algorithm container must not be null");
            Assert.notNull(jwkSetUrl, "JOSE JWK Set URL container must not be null");
            Assert.notNull(jsonWebKey, "JOSE embedded JWK container must not be null");
            Assert.notNull(keyId, "JOSE key-id container must not be null");
            Assert.notNull(certificateUrl, "JOSE certificate URL container must not be null");
            Assert.notNull(certificateChain, "JOSE certificate-chain list must not be null");
            Assert.notNull(certificateThumbprint, "JOSE certificate-thumbprint container must not be null");
            Assert.notNull(
                    certificateThumbprintSha256,
                    "JOSE SHA-256 certificate-thumbprint container must not be null");
            Assert.notNull(type, "JOSE type container must not be null");
            Assert.notNull(contentType, "JOSE content-type container must not be null");
            Assert.notNull(critical, "JOSE critical-extension list must not be null");
            Assert.notNull(encryption, "JOSE encryption container must not be null");
            Assert.notNull(compression, "JOSE compression container must not be null");
            Assert.notNull(extensions, "JOSE extension object must not be null");
            keyId.ifPresent(value -> Assert.notBlank(value, "JOSE key id must not be blank"));
            jwkSetUrl.ifPresent(value -> requireAbsolute(value, "JOSE JWK Set URL"));
            certificateUrl.ifPresent(value -> requireAbsolute(value, "JOSE certificate URL"));
            certificateThumbprint
                    .ifPresent(value -> Assert.notBlank(value, "JOSE certificate thumbprint must not be blank"));
            certificateThumbprintSha256.ifPresent(
                    value -> Assert.notBlank(value, "JOSE SHA-256 certificate thumbprint must not be blank"));
            type.ifPresent(value -> Assert.notBlank(value, "JOSE type must not be blank"));
            contentType.ifPresent(value -> Assert.notBlank(value, "JOSE content type must not be blank"));
            compression.ifPresent(value -> Assert.notBlank(value, "JOSE compression algorithm must not be blank"));
            certificateChain = validatedStrings(certificateChain, "JOSE certificate chain");
            critical = validatedStrings(critical, "JOSE critical extension");
            for (String name : extensions.values().keySet()) {
                Assert.notBlank(name, "JOSE extension parameter name must not be blank");
                if (registered(name)) {
                    throw new ValidateException("JOSE extensions must not replace a registered parameter");
                }
            }
            extensions = new JsonValue.ObjectValue(extensions.values());
        }

        /**
         * Requires an absolute URI for a directly constructed registered URL parameter.
         *
         * @param value URI component under validation
         * @param label safe semantic label
         */
        private static void requireAbsolute(final URI value, final String label) {
            Assert.notNull(value, label + " must not be null");
            if (!value.isAbsolute()) {
                throw new ValidateException(label + " must be absolute");
            }
        }

        /**
         * Validates and freezes one ordered list of non-blank JOSE string values.
         *
         * @param values source list
         * @param label  safe diagnostic label
         * @return immutable validated list
         */
        private static List<String> validatedStrings(final List<String> values, final String label) {
            final List<String> copy = List.copyOf(values);
            for (String value : copy) {
                Assert.notBlank(value, label + " value must not be blank");
            }
            return copy;
        }

    }

}
