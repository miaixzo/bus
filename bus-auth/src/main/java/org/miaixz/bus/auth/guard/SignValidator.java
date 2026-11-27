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
package org.miaixz.bus.auth.guard;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.core.xyz.*;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.crypto.center.SM2;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Computes request signatures and verifies signatures over request or canonical data supplied by callers. The caller
 * authenticates the credential used to derive a request signing key.
 *
 * @author Kimi Liu
 */
public final class SignValidator {

    /**
     * Maximum size of a downstream body accepted by the signed forwarding protocol.
     */
    public static final int FORWARD_BODY_LIMIT = (int) Normal.MEBI;

    private SignValidator() {
        // No initialization required.
    }

    /**
     * Checks whether a client timestamp falls inside the inclusive tolerance around the server clock.
     *
     * @param timestampMillis client timestamp in epoch milliseconds
     * @param serverMillis    server time in epoch milliseconds
     * @param toleranceMillis maximum accepted difference in milliseconds
     * @return whether the timestamp is within the configured window
     */
    public static boolean isTimestampWithin(long timestampMillis, long serverMillis, long toleranceMillis) {
        long difference = timestampMillis >= serverMillis ? timestampMillis - serverMillis
                : serverMillis - timestampMillis;
        return difference >= 0 && difference <= toleranceMillis;
    }

    /**
     * Applies the configured request timestamp tolerance, in minutes.
     *
     * @param timestampMillis parsed client timestamp in epoch milliseconds
     * @param serverMillis    server time in epoch milliseconds
     * @param skewMinutes     maximum accepted clock difference in minutes
     * @return whether the timestamp is within the inclusive window
     */
    public static boolean isTimestampWithinMinutes(long timestampMillis, long serverMillis, int skewMinutes) {
        return isTimestampWithin(timestampMillis, serverMillis, TimeUnit.MINUTES.toMillis(skewMinutes));
    }

    /**
     * Resolves the effective signing mode, reading the configured profile only when verification is enabled.
     *
     * @param enabled    whether request signing is enabled
     * @param configured supplier of the validated wire profile
     * @param supportsV1 whether the caller's protocol supports credential-bound signing
     * @return effective verification mode
     */
    public static RequestMode requestMode(boolean enabled, Supplier<String> configured, boolean supportsV1) {
        if (!enabled) {
            return RequestMode.DISABLED;
        }
        return switch (configured.get()) {
            case "none" -> RequestMode.SKIP;
            case "legacy" -> RequestMode.LEGACY;
            case "v1" -> supportsV1 ? RequestMode.V1 : RequestMode.UNSUPPORTED;
            default -> RequestMode.UNSUPPORTED;
        };
    }

    /**
     * Verifies a legacy or credential-bound parameter signature and clears derived key material after use.
     *
     * @param mode            effective parameter signing mode
     * @param httpMethod      actual HTTP method
     * @param parameters      parsed parameters included in the signature
     * @param lookup          protocol lookup for the logical method and timestamp
     * @param signature       supplied signature from the signed parameter map
     * @param credential      original credential selected for V1 key derivation
     * @param bearerMaxLength maximum Bearer credential length
     * @param apiKeyMaxLength maximum API key length
     * @return verification outcome for the caller to map to its protocol
     */
    public static Verification verifyRequest(
            RequestMode mode,
            Http.Method httpMethod,
            Map<String, ?> parameters,
            Function<String, String> lookup,
            Supplier<String> signature,
            Supplier<Http.Auth.Credential> credential,
            int bearerMaxLength,
            int apiKeyMaxLength) {
        String method = lookup.apply(Http.Param.METHOD);
        String timestamp = lookup.apply(Http.Param.TIMESTAMP);
        byte[] key;
        switch (mode) {
            case LEGACY -> key = (method + timestamp).getBytes(Charset.UTF_8);
            case V1 -> {
                Http.Auth.Credential selected = credential.get();
                if (selected == null) {
                    return Verification.MISSING_CREDENTIAL;
                }
                boolean bearer = EnumValue.Credential.TOKEN.equals(selected.type());
                String value = selected.value();
                if (bearer ? !isValidBearer(value, bearerMaxLength) : !isValidApiKey(value, apiKeyMaxLength)) {
                    return Verification.INVALID_CREDENTIAL;
                }
                key = deriveKey("v1", bearer ? "token" : "apikey", value, method, timestamp);
            }
            default -> throw new IllegalArgumentException("Unsupported parameter signing mode: " + mode);
        }
        try {
            return verifyParameters(key, httpMethod.value(), parameters, Http.Param.SIGN, signature.get())
                    ? Verification.VALID
                    : Verification.INVALID_SIGNATURE;
        } finally {
            Arrays.fill(key, (byte) 0);
        }
    }

    /**
     * Verifies a signature over protocol canonical fields and the SHA-256 digest of the body.
     *
     * @param credential supplier of the selected credential, if present
     * @param fallback   protocol signing material used when the client supplies no credential
     * @param canonical  protocol fields including the delimiter before the body digest
     * @param body       exact request bytes, or null for an empty body
     * @param signature  supplied request signature
     * @return whether suitable signing material and a matching signature were supplied
     */
    public static boolean verifyBody(
            Supplier<String> credential,
            String fallback,
            String canonical,
            byte[] body,
            String signature) {
        String payload = canonical + Builder.sha256Hex(body == null ? Normal.EMPTY_BYTE_ARRAY : body);
        String selected = credential.get();
        String secret = StringKit.isNotBlank(selected) ? selected : fallback;
        return StringKit.isNotBlank(secret) && verifyCanonical(secret, payload, signature);
    }

    /**
     * Signs the exact authorized HTTP method, raw destination and downstream body.
     *
     * @param permit compact authorization issued for this request
     * @param secret base64url one-request key received only over the private verification connection
     * @param method authorized HTTP method
     * @param path   exact downstream raw path
     * @param query  exact downstream raw query, or null if absent
     * @param body   exact JSON bytes, or null for a GET without a body
     * @return base64url proof for the downstream request
     * @throws IllegalArgumentException if the signing key, request method, or body is invalid
     */
    public static String forwardProof(
            String permit,
            String secret,
            Http.Method method,
            String path,
            String query,
            byte[] body) {
        if (method == Http.Method.GET ? body != null
                : method != Http.Method.POST || body == null || body.length == 0 || body.length > FORWARD_BODY_LIMIT) {
            throw new IllegalArgumentException("Invalid signed request body");
        }
        var encoding = java.util.Base64.getUrlEncoder().withoutPadding();
        byte[] key = java.util.Base64.getUrlDecoder().decode(secret);
        try {
            if (key.length != Normal._32)
                throw new IllegalArgumentException("Invalid request signing key");
            var coordinates = body == null ? Arrays.asList(method.value(), path, query, permit)
                    : Arrays.asList(method.value(), path, query, permit, Builder.sha256Hex(body));
            byte[] value = JsonKit.toJsonString(coordinates).getBytes(Charset.UTF_8);
            return encoding.encodeToString(hmacSha256(key, value));
        } finally {
            Arrays.fill(key, (byte) Normal._0);
        }
    }

    /**
     * Derives a V1 signing key using the supplied signature and credential domains.
     *
     * @param profile        signature domain identifier
     * @param credentialType credential domain identifier
     * @param credential     bounded bearer or API key selected by the caller
     * @param method         logical API method
     * @param timestamp      request timestamp
     * @return signing key owned by the caller, which clears it after use
     */
    public static byte[] deriveKey(
            String profile,
            String credentialType,
            String credential,
            String method,
            String timestamp) {
        MessageDigest digest = Builder.sha256().getRaw();
        update(digest, profile);
        update(digest, credentialType);
        update(digest, credential);
        update(digest, method);
        update(digest, timestamp);
        return digest.digest();
    }

    /**
     * Computes the legacy or V1 parameter signature using a key provided by the caller.
     *
     * @param key                signing key
     * @param httpMethod         actual HTTP method
     * @param parameters         parsed request parameters
     * @param signatureParameter name of the signature field excluded from the canonical input
     * @return standard Base64 request signature
     */
    public static String signParameters(
            byte[] key,
            String httpMethod,
            Map<String, ?> parameters,
            String signatureParameter) {
        Objects.requireNonNull(httpMethod);
        Objects.requireNonNull(signatureParameter);
        String encodedParameters = parameters.entrySet().stream()
                .filter(entry -> entry.getKey() != null && !signatureParameter.equalsIgnoreCase(entry.getKey()))
                .map(entry -> Map.entry(entry.getKey(), normalize(entry.getValue())))
                .filter(entry -> StringKit.isNotEmpty(entry.getValue())).sorted(Map.Entry.comparingByKey())
                .map(
                        entry -> UrlEncoder.encodeAll(entry.getKey(), Charset.UTF_8)
                                + UrlEncoder.encodeAll(entry.getValue(), Charset.UTF_8))
                .collect(Collectors.joining());
        byte[] canonical = (httpMethod + Symbol.LF + encodedParameters).getBytes(Charset.UTF_8);
        return Base64.encode(hmacSha256(key, canonical));
    }

    /**
     * Checks a parameter signature against the request inputs selected by the caller.
     *
     * @param key                signing key
     * @param httpMethod         actual HTTP method
     * @param parameters         parsed request parameters
     * @param signatureParameter name of the signature field excluded from the canonical input
     * @param signature          signature supplied with the request
     * @return whether the signature matches
     */
    public static boolean verifyParameters(
            byte[] key,
            String httpMethod,
            Map<String, ?> parameters,
            String signatureParameter,
            String signature) {
        return parameters != null && signature != null
                && matches(signParameters(key, httpMethod, parameters, signatureParameter), signature);
    }

    /**
     * Checks an HMAC signature over the exact canonical text prepared by a protocol adapter.
     *
     * @param key       signing secret
     * @param canonical canonical request text
     * @param signature standard Base64 signature supplied with the request
     * @return whether the signature matches
     */
    public static boolean verifyCanonical(String key, String canonical, String signature) {
        if (signature == null)
            return false;
        String expected = Base64.encode(hmacSha256(ByteKit.toBytes(key), canonical.getBytes(Charset.UTF_8)));
        return matches(expected, signature);
    }

    /**
     * Computes HMAC-SHA256. Native Image uses its built-in JCE provider because providers added at runtime cannot be
     * verified by the image.
     *
     * @param key   signing key
     * @param value canonical bytes to sign
     * @return raw MAC bytes
     */
    private static byte[] hmacSha256(byte[] key, byte[] value) {
        if (!Keys.IS_GRAALVM_NATIVE) {
            return Builder.hmacSha256(key).digest(value);
        }
        String algorithm = Algorithm.HMACSHA256.getValue();
        try {
            Mac mac = Mac.getInstance(algorithm, "SunJCE");
            mac.init(new SecretKeySpec(key, algorithm));
            return mac.doFinal(value);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Unable to compute request signature", e);
        }
    }

    /**
     * Verifies an SM2 signature encoded as canonical hexadecimal.
     *
     * @param verifier  verifier configured with the trusted public key
     * @param canonical exact canonical bytes to verify
     * @param signature canonical hexadecimal signature
     * @return whether the signature has the expected format and verifies
     */
    public static boolean verifySm2(SM2 verifier, byte[] canonical, String signature) {
        Objects.requireNonNull(verifier);
        Objects.requireNonNull(canonical);
        if (signature == null || signature.isEmpty())
            return false;
        byte[] proof;
        try {
            proof = HexKit.decode(signature);
        } catch (InternalException invalid) {
            return false;
        }
        return HexKit.encodeString(proof).equals(signature) && verifier.verify(canonical, proof);
    }

    /**
     * Checks the bounded Bearer formats supported for V1 key derivation.
     *
     * @param token     Bearer credential
     * @param maxLength maximum accepted credential length
     * @return whether the credential has a supported V1 signing format and length
     */
    public static boolean isValidBearer(String token, int maxLength) {
        if (!isAsciiWithin(token, maxLength))
            return false;
        int first = token.indexOf(Symbol.DOT);
        if (first < 0)
            return token.length() == Normal._43 && isBase64Url(token, 0, token.length());
        int second = token.indexOf(Symbol.DOT, first + 1);
        return first > 0 && second > first + 1 && second < token.length() - 1
                && token.indexOf(Symbol.DOT, second + 1) < 0 && isBase64Url(token, 0, first)
                && isBase64Url(token, first + 1, second) && isBase64Url(token, second + 1, token.length());
    }

    /**
     * Checks the bounded ASCII API key format used for V1 key derivation.
     *
     * @param apiKey    API key credential
     * @param maxLength maximum accepted credential length
     * @return whether the credential has a supported V1 signing format and length
     */
    public static boolean isValidApiKey(String apiKey, int maxLength) {
        return isAsciiWithin(apiKey, maxLength);
    }

    /**
     * Serializes complex values and applies the original Unicode normalization.
     */
    private static String normalize(Object value) {
        String text = value instanceof Map || value instanceof Collection
                || (value != null && value.getClass().isArray()) ? JsonKit.toJsonString(value) : String.valueOf(value);
        return UnicodeKit.toString(text);
    }

    /**
     * Writes a UTF-8 field with its LF delimiter and clears the temporary bytes.
     */
    private static void update(MessageDigest digest, String value) {
        byte[] bytes = value.getBytes(Charset.UTF_8);
        try {
            digest.update(bytes);
            digest.update((byte) Symbol.C_LF);
        } finally {
            Arrays.fill(bytes, (byte) 0);
        }
    }

    /**
     * Tests whether a segment consists of unpadded Base64URL characters.
     */
    private static boolean isBase64Url(String value, int start, int end) {
        for (int index = start; index < end; index++) {
            char character = value.charAt(index);
            if (!(character >= 'A' && character <= 'Z') && !(character >= 'a' && character <= 'z')
                    && !(character >= '0' && character <= '9') && character != Symbol.C_MINUS
                    && character != Symbol.C_UNDERLINE)
                return false;
        }
        return true;
    }

    /**
     * Tests whether a credential consists of bounded printable, non-whitespace ASCII.
     */
    private static boolean isAsciiWithin(String value, int maximum) {
        if (StringKit.isEmpty(value) || value.length() > maximum)
            return false;
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            if (!CharKit.isAsciiPrintable(character) || Character.isWhitespace(character))
                return false;
        }
        return true;
    }

    /**
     * Compares signature bytes without an early exit at the first differing byte.
     */
    private static boolean matches(String expected, String signature) {
        byte[] expectedBytes = expected.getBytes(Charset.UTF_8);
        byte[] suppliedBytes = signature.getBytes(Charset.UTF_8);
        try {
            return MessageDigest.isEqual(expectedBytes, suppliedBytes);
        } finally {
            Arrays.fill(expectedBytes, (byte) 0);
            Arrays.fill(suppliedBytes, (byte) 0);
        }
    }

    /**
     * Effective request signing mode after applying the global switch and the caller's supported profiles.
     */
    public enum RequestMode {
        /**
         * The global signing switch is off.
         */
        DISABLED,
        /**
         * No verification is required.
         */
        SKIP,
        /**
         * Uses the legacy parameter or body signature.
         */
        LEGACY,
        /**
         * Uses a credential-bound parameter signature.
         */
        V1,
        /**
         * The configured profile is unavailable for this protocol.
         */
        UNSUPPORTED
    }

    /**
     * Transport-neutral verification outcome for a signed parameter request.
     */
    public enum Verification {
        /**
         * The request signature matches.
         */
        VALID,
        /**
         * Credential-bound signing requires a supplied credential.
         */
        MISSING_CREDENTIAL,
        /**
         * The supplied credential cannot be used to derive a signing key.
         */
        INVALID_CREDENTIAL,
        /**
         * The signature is missing from the signed map or does not match.
         */
        INVALID_SIGNATURE
    }

}
