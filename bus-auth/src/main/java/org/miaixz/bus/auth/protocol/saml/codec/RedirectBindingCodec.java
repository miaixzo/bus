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
package org.miaixz.bus.auth.protocol.saml.codec;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.protocol.saml.AuthnRequest;
import org.miaixz.bus.auth.protocol.saml.LogoutRequest;
import org.miaixz.bus.auth.protocol.saml.LogoutResponse;
import org.miaixz.bus.auth.protocol.saml.Saml;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.UnoUrl;

/**
 * Implements the SAML 2.0 HTTP-Redirect Binding for request messages and logout-response messages.
 * <p>
 * The codec applies raw RFC 1951 DEFLATE, Base64, and RFC 3986 percent encoding in the order required by the Binding.
 * Signed query input is retained byte-for-byte so the signature validator never reconstructs it from decoded values.
 * Key resolution and cryptographic signing are supplied through the narrow asynchronous {@link SigningOperation} port.
 * </p>
 *
 * @author Kimi Liu
 */
public final class RedirectBindingCodec {

    /**
     * Strict SAML XML message codec.
     */
    private final SamlMessageCodec messageCodec;

    /**
     * Compiler-supplied key resolution and Bus cryptographic signing operation.
     */
    private final SigningOperation signingOperation;

    /**
     * Creates an HTTP-Redirect Binding codec.
     *
     * @param messageCodec     strict SAML XML codec
     * @param signingOperation asynchronous external-key signing operation
     * @throws IllegalArgumentException if a collaborator is {@code null}
     */
    public RedirectBindingCodec(final SamlMessageCodec messageCodec, final SigningOperation signingOperation) {
        this.messageCodec = Assert.notNull(messageCodec, "SAML message codec must not be null");
        this.signingOperation = Assert.notNull(signingOperation, "SAML Redirect signing operation must not be null");
    }

    /**
     * Identifies parameters admitted by the HTTP-Redirect Binding grammar.
     *
     * @param name decoded query parameter name
     * @return {@code true} for a registered Redirect Binding parameter
     */
    private static boolean bindingParameter(final String name) {
        return Saml.Parameters.REQUEST.equals(name) || Saml.Parameters.RESPONSE.equals(name)
                || Saml.Parameters.RELAY_STATE.equals(name) || Saml.Parameters.SIGNATURE_ALGORITHM.equals(name)
                || Saml.Parameters.SIGNATURE.equals(name);
    }

    /**
     * Validates endpoint, correlation, key, algorithm, context, and budget inputs.
     *
     * @param endpoint           exact redirect destination
     * @param relayState         optional opaque binding correlation value
     * @param keyId              external signing key identifier
     * @param signatureAlgorithm standard XML Signature algorithm URI
     * @param context            immutable invocation context
     * @param timeout            shared operation budget
     */
    private static void validateInvocation(
            final Endpoint endpoint,
            final Optional<String> relayState,
            final String keyId,
            final String signatureAlgorithm,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(endpoint, "SAML Redirect endpoint must not be null");
        if (endpoint.transport() != Endpoint.Transport.HTTPS || endpoint.method().isEmpty()
                || endpoint.method().getOrNull() != Http.Method.GET
                || endpoint.url().toUri().getRawFragment() != null) {
            throw new ValidateException("SAML HTTP-Redirect endpoint must be fragment-free HTTPS GET");
        }
        validateRelayState(relayState);
        Assert.notBlank(keyId, "SAML Redirect signing key identifier must not be blank");
        Assert.notBlank(signatureAlgorithm, "SAML Redirect signature algorithm must not be blank");
        Assert.notNull(context, "SAML Redirect context must not be null");
        Assert.notNull(timeout, "SAML Redirect time budget must not be null");
    }

    /**
     * Validates the optional RelayState against the specification's byte limit.
     *
     * @param relayState optional UTF-8 RelayState value
     */
    private static void validateRelayState(final Optional<String> relayState) {
        Assert.notNull(relayState, "SAML RelayState container must not be null");
        final String value = relayState.getOrNull();
        if (value != null && value.getBytes(Charset.UTF_8).length > Saml.MAXIMUM_RELAY_STATE_BYTES) {
            throw new ValidateException("SAML RelayState exceeds 80 bytes");
        }
    }

    /**
     * Deflates XML using raw RFC 1951 framing.
     *
     * @param xml complete SAML message XML
     * @return raw DEFLATE octets without a zlib wrapper
     */
    private static byte[] deflate(final byte[] xml) {
        final Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
        try {
            deflater.setInput(xml);
            deflater.finish();
            final ByteArrayOutputStream output = new ByteArrayOutputStream(xml.length);
            final byte[] buffer = new byte[4096];
            while (!deflater.finished()) {
                final int count = deflater.deflate(buffer);
                if (count == 0 && deflater.needsInput()) {
                    throw new ValidateException("SAML Redirect DEFLATE did not finish");
                }
                output.write(buffer, 0, count);
            }
            return output.toByteArray();
        } finally {
            deflater.end();
        }
    }

    /**
     * Inflates raw RFC 1951 data while stopping before a configured output ceiling is exceeded.
     *
     * @param compressed   raw DEFLATE message octets
     * @param maximumBytes maximum permitted inflated XML size
     * @return bounded inflated XML bytes
     */
    private static byte[] inflate(final byte[] compressed, final long maximumBytes) {
        final Inflater inflater = new Inflater(true);
        try {
            inflater.setInput(compressed);
            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            final byte[] buffer = new byte[4096];
            while (!inflater.finished()) {
                final int count = inflater.inflate(buffer);
                if (count == 0) {
                    if (inflater.needsDictionary() || inflater.needsInput()) {
                        throw new ValidateException("SAML Redirect DEFLATE stream is incomplete");
                    }
                }
                if ((long) output.size() + count > maximumBytes) {
                    throw new ValidateException("SAML Redirect inflated XML exceeds the configured limit");
                }
                output.write(buffer, 0, count);
            }
            return output.toByteArray();
        } catch (DataFormatException exception) {
            throw new ValidateException("SAML Redirect message is not raw DEFLATE data", exception);
        } finally {
            inflater.end();
        }
    }

    /**
     * Performs one class-token checked conversion without guessing a message type.
     *
     * @param <T>     expected SAML message type
     * @param decoded decoded document object
     * @param type    exact expected message class
     * @return decoded document narrowed to the checked message type
     */
    private static <T> SamlMessageCodec.Document<T> castDocument(final Object decoded, final Class<T> type) {
        if (!(decoded instanceof SamlMessageCodec.Document<?> document) || !type.isInstance(document.message())) {
            throw new ValidateException("SAML Redirect decoder returned an unexpected message type");
        }
        return (SamlMessageCodec.Document<T>) document;
    }

    /**
     * Parses the raw URI query without converting plus signs into spaces.
     *
     * @param requestUri original inbound request URI
     * @return immutable raw and decoded parameter slices in wire order
     */
    private static List<RawParameter> rawParameters(final String requestUri) {
        try {
            final String query = new URI(requestUri).getRawQuery();
            if (query == null || query.isEmpty()) {
                throw new ValidateException("SAML Redirect request has no query");
            }
            final List<RawParameter> result = new ArrayList<>();
            for (String pair : query.split(Symbol.AND, -1)) {
                final int separator = pair.indexOf(Symbol.C_EQUAL);
                if (separator <= 0) {
                    throw new ValidateException("SAML Redirect query parameter is malformed");
                }
                final String rawName = pair.substring(0, separator);
                final String rawValue = pair.substring(separator + 1);
                result.add(new RawParameter(rawName, rawValue, percentDecode(rawName), percentDecode(rawValue)));
            }
            return List.copyOf(result);
        } catch (URISyntaxException exception) {
            throw new ValidateException("SAML Redirect request URI is invalid", exception);
        }
    }

    /**
     * Creates an insertion-ordered unique parameter index and rejects unknown names.
     *
     * @param parameters raw query parameters in wire order
     * @return immutable unique index keyed by decoded parameter name
     */
    private static Map<String, RawParameter> index(final List<RawParameter> parameters) {
        final Map<String, RawParameter> result = new LinkedHashMap<>();
        for (RawParameter parameter : parameters) {
            if (!bindingParameter(parameter.decodedName())
                    || result.putIfAbsent(parameter.decodedName(), parameter) != null) {
                throw new ValidateException("SAML Redirect query contains an unknown or duplicate parameter");
            }
        }
        return Map.copyOf(result);
    }

    /**
     * Verifies that the boundary's decoded parameter view exactly matches the raw GET query.
     *
     * @param captured boundary-captured decoded callback parameters
     * @param raw      parameters independently decoded from the original URI
     */
    private static void verifyCapturedParameters(
            final List<Callback.Parameter> captured,
            final List<RawParameter> raw) {
        if (captured.size() != raw.size()) {
            throw new ValidateException("SAML Redirect callback parameter views do not match");
        }
        for (int index = 0; index < raw.size(); index++) {
            if (!captured.get(index).name().equals(raw.get(index).decodedName())
                    || !captured.get(index).value().equals(raw.get(index).decodedValue())) {
                throw new ValidateException("SAML Redirect callback parameter views do not match");
            }
        }
    }

    /**
     * Enforces the exact signed parameter order defined by the Redirect Binding.
     *
     * @param parameters  raw query parameters in wire order
     * @param messageName expected SAMLRequest or SAMLResponse parameter name
     */
    private static void requireSignedOrder(final List<RawParameter> parameters, final String messageName) {
        final List<String> names = parameters.stream().map(RawParameter::decodedName).toList();
        final List<String> expected = names.contains(Saml.Parameters.RELAY_STATE)
                ? List.of(
                        messageName,
                        Saml.Parameters.RELAY_STATE,
                        Saml.Parameters.SIGNATURE_ALGORITHM,
                        Saml.Parameters.SIGNATURE)
                : List.of(messageName, Saml.Parameters.SIGNATURE_ALGORITHM, Saml.Parameters.SIGNATURE);
        if (!names.equals(expected)) {
            throw new ValidateException("SAML Redirect signed parameters are outside the required order");
        }
    }

    /**
     * Reconstructs signed input only by concatenating the preserved raw parameter slices.
     *
     * @param parameters  raw query parameters in wire order
     * @param messageName expected first signed message parameter
     * @return exact ASCII query substring covered by the signature
     */
    private static String signedInput(final List<RawParameter> parameters, final String messageName) {
        final StringBuilder result = new StringBuilder();
        for (RawParameter parameter : parameters) {
            if (Saml.Parameters.SIGNATURE.equals(parameter.decodedName())) {
                break;
            }
            if (!result.isEmpty()) {
                result.append(Symbol.C_AND);
            }
            result.append(parameter.rawName()).append(Symbol.C_EQUAL).append(parameter.rawValue());
        }
        if (!result.toString().startsWith(messageName + Symbol.C_EQUAL)) {
            throw new ValidateException("SAML Redirect signed input does not begin with its message parameter");
        }
        return result.toString();
    }

    /**
     * Decodes canonical Base64 and rejects alternate lexical encodings.
     *
     * @param value canonical Base64 lexical value
     * @param label diagnostic binding component label
     * @return decoded octets
     */
    private static byte[] base64(final String value, final String label) {
        try {
            final byte[] decoded = Base64.getDecoder().decode(value);
            if (!Base64.getEncoder().encodeToString(decoded).equals(value)) {
                throw new ValidateException(label + " is not canonical Base64");
            }
            return decoded;
        } catch (IllegalArgumentException exception) {
            throw new ValidateException(label + " is not valid Base64", exception);
        }
    }

    /**
     * Percent-encodes one UTF-8 value using only RFC 3986 unreserved characters.
     *
     * @param value decoded Unicode query value
     * @return canonical uppercase-hex percent encoding
     */
    private static String percent(final String value) {
        final StringBuilder result = new StringBuilder();
        for (byte item : value.getBytes(Charset.UTF_8)) {
            final int character = item & 0xff;
            if ((character >= 'A' && character <= 'Z') || (character >= 'a' && character <= 'z')
                    || (character >= Symbol.C_ZERO && character <= Symbol.C_NINE) || character == Symbol.C_MINUS
                    || character == Symbol.C_DOT || character == Symbol.C_UNDERLINE || character == Symbol.C_TILDE) {
                result.append((char) character);
            } else {
                result.append(Symbol.C_PERCENT).append(Character.toUpperCase(Character.forDigit(character >>> 4, 16)))
                        .append(Character.toUpperCase(Character.forDigit(character & 0x0f, 16)));
            }
        }
        return result.toString();
    }

    /**
     * Strictly percent-decodes UTF-8 without application/x-www-form-urlencoded plus semantics.
     *
     * @param value raw ASCII query component
     * @return strictly decoded Unicode value
     */
    private static String percentDecode(final String value) {
        final ByteArrayOutputStream output = new ByteArrayOutputStream(value.length());
        for (int index = 0; index < value.length();) {
            final char character = value.charAt(index);
            if (character == Symbol.C_PERCENT) {
                if (index + 2 >= value.length()) {
                    throw new ValidateException("SAML Redirect percent encoding is truncated");
                }
                final int high = Character.digit(value.charAt(index + 1), 16);
                final int low = Character.digit(value.charAt(index + 2), 16);
                if (high < 0 || low < 0) {
                    throw new ValidateException("SAML Redirect percent encoding is invalid");
                }
                output.write((high << 4) | low);
                index += 3;
            } else {
                if (character > 0x7f) {
                    throw new ValidateException("SAML Redirect raw query must use ASCII percent encoding");
                }
                output.write(character);
                index++;
            }
        }
        try {
            return Charset.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(output.toByteArray()))
                    .toString();
        } catch (CharacterCodingException exception) {
            throw new ValidateException("SAML Redirect percent encoding is not UTF-8", exception);
        }
    }

    /**
     * Builds an absolute URL from a completed encoded SAML query.
     *
     * @param endpoint validated redirect destination
     * @param query    complete encoded SAML query
     * @return completed stage containing the URL outcome
     */
    private static CompletionStage<Outcome<UnoUrl>> url(final Endpoint endpoint, final String query) {
        return completed(urlValue(endpoint, query));
    }

    /**
     * Builds one URL outcome while preserving a configured endpoint's existing query.
     *
     * @param endpoint validated redirect destination
     * @param query    complete encoded SAML query
     * @return successful URL or a sanitized construction failure
     */
    private static Outcome<UnoUrl> urlValue(final Endpoint endpoint, final String query) {
        try {
            final String base = endpoint.url().encoded();
            return Outcome.succeeded(
                    UnoUrl.parse(
                            base + (base.contains(Symbol.QUESTION_MARK) ? Symbol.C_AND : Symbol.C_QUESTION_MARK)
                                    + query));
        } catch (RuntimeException exception) {
            return Outcome.failed(failure(ErrorCode._400, "SAML Redirect URL construction failed"));
        }
    }

    /**
     * Creates a safe framework failure.
     *
     * @param code        shared Bus error code
     * @param description non-sensitive failure description
     * @return failure without SAML message, endpoint, or key material
     */
    private static Outcome.Failure failure(final Errors code, final String description) {
        return new Outcome.Failure(code, description, new JsonValue.ObjectValue(Map.of()));
    }

    /**
     * Creates a type-inferred completed stage.
     *
     * @param <T>     outcome success type
     * @param outcome already classified operation outcome
     * @return completed asynchronous stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Encodes an Authentication Request for browser navigation.
     *
     * @param endpoint           identity-provider HTTP-Redirect SSO endpoint
     * @param request            standard Authentication Request
     * @param relayState         optional opaque RelayState
     * @param sign               whether to append SigAlg and Signature
     * @param keyId              external signing key identifier
     * @param signatureAlgorithm XML Signature algorithm URI
     * @param context            immutable invocation context
     * @param timeout            shared end-to-end time budget
     * @return stage containing an absolute redirect URL or closed framework failure
     */
    public CompletionStage<Outcome<UnoUrl>> encode(
            final Endpoint endpoint,
            final AuthnRequest request,
            final Optional<String> relayState,
            final boolean sign,
            final String keyId,
            final String signatureAlgorithm,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(request, "SAML Authentication Request must not be null");
        return encode(
                endpoint,
                Saml.Parameters.REQUEST,
                messageCodec.encode(request),
                relayState,
                sign,
                keyId,
                signatureAlgorithm,
                context,
                timeout);
    }

    /**
     * Encodes a Logout Request for browser navigation.
     *
     * @param endpoint           identity-provider HTTP-Redirect SingleLogoutService endpoint
     * @param request            standard Logout Request
     * @param relayState         optional opaque RelayState
     * @param sign               whether to append SigAlg and Signature
     * @param keyId              external signing key identifier
     * @param signatureAlgorithm XML Signature algorithm URI
     * @param context            immutable invocation context
     * @param timeout            shared end-to-end time budget
     * @return stage containing an absolute redirect URL or closed framework failure
     */
    public CompletionStage<Outcome<UnoUrl>> encode(
            final Endpoint endpoint,
            final LogoutRequest request,
            final Optional<String> relayState,
            final boolean sign,
            final String keyId,
            final String signatureAlgorithm,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(request, "SAML Logout Request must not be null");
        return encode(
                endpoint,
                Saml.Parameters.REQUEST,
                messageCodec.encode(request),
                relayState,
                sign,
                keyId,
                signatureAlgorithm,
                context,
                timeout);
    }

    /**
     * Decodes one inbound Redirect Binding message while preserving exact signature input.
     *
     * @param inbound     raw callback transport captured by the external route
     * @param messageType exact supported SAML message class
     * @param <T>         decoded message type
     * @return typed secure document, RelayState, and optional binding signature
     * @throws ValidateException if method, query grammar, encoding, compression, or message type is invalid
     */
    public <T> Decoded<T> decode(final Callback.Inbound inbound, final Class<T> messageType) {
        Assert.notNull(inbound, "SAML Redirect callback must not be null");
        Assert.notNull(messageType, "SAML Redirect message type must not be null");
        if (inbound.method() != Http.Method.GET) {
            throw new ValidateException("SAML HTTP-Redirect Binding requires GET");
        }
        final List<RawParameter> parameters = rawParameters(inbound.requestUri());
        verifyCapturedParameters(inbound.parameters(), parameters);
        final Map<String, RawParameter> indexed = index(parameters);
        final boolean response = messageType == LogoutResponse.class;
        final String messageName = response ? Saml.Parameters.RESPONSE : Saml.Parameters.REQUEST;
        if (!indexed.containsKey(messageName)
                || indexed.containsKey(response ? Saml.Parameters.REQUEST : Saml.Parameters.RESPONSE)) {
            throw new ValidateException("SAML Redirect query contains an invalid request/response parameter choice");
        }
        final byte[] compressed = base64(indexed.get(messageName).decodedValue(), "SAML Redirect message");
        final byte[] xml = inflate(compressed, messageCodec.maximumBytes());
        final SamlMessageCodec.Document<T> document = decodeDocument(xml, messageType);
        final Optional<String> relayState = indexed.containsKey(Saml.Parameters.RELAY_STATE)
                ? Optional.of(indexed.get(Saml.Parameters.RELAY_STATE).decodedValue())
                : Optional.empty();
        validateRelayState(relayState);
        final boolean hasAlgorithm = indexed.containsKey(Saml.Parameters.SIGNATURE_ALGORITHM);
        final boolean hasSignature = indexed.containsKey(Saml.Parameters.SIGNATURE);
        if (hasAlgorithm != hasSignature) {
            throw new ValidateException("SAML Redirect SigAlg and Signature must be present together");
        }
        final Optional<Signature> signature;
        if (hasSignature) {
            requireSignedOrder(parameters, messageName);
            final String signed = signedInput(parameters, messageName);
            signature = Optional.of(
                    new Signature(indexed.get(Saml.Parameters.SIGNATURE_ALGORITHM).decodedValue(),
                            base64(indexed.get(Saml.Parameters.SIGNATURE).decodedValue(), "SAML Redirect Signature"),
                            signed.getBytes(Charset.US_ASCII)));
        } else {
            signature = Optional.empty();
        }
        return new Decoded<>(document, relayState, signature);
    }

    /**
     * Builds the unsigned query and optionally delegates its exact bytes for cryptographic signing.
     *
     * @param endpoint           exact fragment-free HTTPS GET destination
     * @param messageName        SAMLRequest or SAMLResponse query parameter name
     * @param xml                securely encoded SAML message XML
     * @param relayState         optional opaque binding correlation value
     * @param sign               whether an XML Signature query value must be produced
     * @param keyId              external signing key identifier
     * @param signatureAlgorithm standard XML Signature algorithm URI
     * @param context            immutable invocation context
     * @param timeout            shared decreasing operation budget
     * @return stage containing the complete redirect URL or a closed failure
     */
    private CompletionStage<Outcome<UnoUrl>> encode(
            final Endpoint endpoint,
            final String messageName,
            final byte[] xml,
            final Optional<String> relayState,
            final boolean sign,
            final String keyId,
            final String signatureAlgorithm,
            final Context context,
            final Timeout.Budget timeout) {
        validateInvocation(endpoint, relayState, keyId, signatureAlgorithm, context, timeout);
        if (timeout.expired()) {
            return completed(
                    Outcome.failed(failure(ErrorCode._408, "SAML Redirect encoding has no remaining time budget")));
        }
        final StringBuilder query = new StringBuilder(messageName).append(Symbol.C_EQUAL)
                .append(percent(Base64.getEncoder().encodeToString(deflate(xml))));
        if (relayState.isPresent()) {
            query.append(Symbol.C_AND).append(Saml.Parameters.RELAY_STATE).append(Symbol.C_EQUAL)
                    .append(percent(relayState.getOrNull()));
        }
        if (!sign) {
            return url(endpoint, query.toString());
        }
        query.append(Symbol.C_AND).append(Saml.Parameters.SIGNATURE_ALGORITHM).append(Symbol.C_EQUAL)
                .append(percent(signatureAlgorithm));
        final byte[] signedInput = query.toString().getBytes(Charset.US_ASCII);
        final CompletionStage<Outcome<byte[]>> signed = signingOperation
                .sign(keyId, signatureAlgorithm, signedInput, context, timeout);
        if (signed == null) {
            return completed(Outcome.failed(failure(ErrorCode._500, "SAML Redirect signer returned no result stage")));
        }
        return signed.thenApply(outcome -> switch (outcome) {
            case Outcome.Succeeded<byte[]> success -> {
                final byte[] signature = Assert.notNull(success.value(), "SAML Redirect signature must not be null");
                if (signature.length == 0) {
                    yield Outcome.failed(failure(ErrorCode._500, "SAML Redirect signature must not be empty"));
                }
                yield urlValue(
                        endpoint,
                        query + Symbol.AND + Saml.Parameters.SIGNATURE + Symbol.C_EQUAL
                                + percent(Base64.getEncoder().encodeToString(signature)));
            }
            case Outcome.Rejected<byte[]> rejected -> Outcome.rejected(rejected.failure());
            case Outcome.Failed<byte[]> failed -> Outcome.failed(failed.failure());
        });
    }

    /**
     * Dispatches decompressed XML to the exact supported typed decoder.
     *
     * @param <T>  supported SAML message type
     * @param xml  decompressed SAML message XML
     * @param type exact requested message class
     * @return securely decoded typed document
     */
    private <T> SamlMessageCodec.Document<T> decodeDocument(final byte[] xml, final Class<T> type) {
        final Object decoded;
        if (type == AuthnRequest.class) {
            decoded = messageCodec.decodeAuthnRequest(xml);
        } else if (type == LogoutRequest.class) {
            decoded = messageCodec.decodeLogoutRequest(xml);
        } else if (type == LogoutResponse.class) {
            decoded = messageCodec.decodeLogoutResponse(xml);
        } else {
            throw new ValidateException("SAML Redirect Binding does not support the requested message type");
        }
        return castDocument(decoded, type);
    }

    /**
     * Resolves the explicit signing key and signs exact Redirect Binding input.
     */
    @FunctionalInterface
    public interface SigningOperation {

        /**
         * Signs exact ASCII Redirect Binding input with one externally resolved key.
         *
         * @param keyId     exact external key identifier
         * @param algorithm standard XML Signature algorithm URI
         * @param input     exact percent-encoded query bytes
         * @param context   immutable invocation context
         * @param timeout   shared operation budget
         * @return stage containing raw signature bytes or a closed framework failure
         */
        CompletionStage<Outcome<byte[]>> sign(
                String keyId,
                String algorithm,
                byte[] input,
                Context context,
                Timeout.Budget timeout);

    }

    /**
     * Carries one decoded Redirect Binding message and its binding-level correlation and signature evidence.
     *
     * @param document   securely decoded original SAML document
     * @param relayState optional opaque RelayState
     * @param signature  optional exact Redirect Binding signature evidence
     * @param <T>        standard message type
     */
    public record Decoded<T>(SamlMessageCodec.Document<T> document, Optional<String> relayState,
            Optional<Signature> signature) {

        /**
         * Validates and normalizes the decoded binding result.
         */
        public Decoded {
            Assert.notNull(document, "Decoded SAML document must not be null");
            Assert.notNull(relayState, "Decoded SAML RelayState container must not be null");
            Assert.notNull(signature, "Decoded SAML signature container must not be null");
            relayState = Optional.ofNullable(relayState.getOrNull());
            signature = Optional.ofNullable(signature.getOrNull());
        }

    }

    /**
     * Retains the exact Redirect Binding signature inputs for cryptographic verification.
     *
     * @param algorithm   standard XML Signature algorithm URI
     * @param value       raw decoded signature bytes
     * @param signedInput exact preserved ASCII input bytes
     */
    public record Signature(String algorithm, byte[] value, byte[] signedInput) {

        /**
         * Takes defensive ownership of non-empty signature material.
         */
        public Signature {
            Assert.notBlank(algorithm, "SAML Redirect signature algorithm must not be blank");
            value = bytes(value, "SAML Redirect signature");
            signedInput = bytes(signedInput, "SAML Redirect signed input");
        }

        /**
         * Copies one required non-empty byte sequence.
         *
         * @param source required signature-related octets
         * @param label  diagnostic signature component label
         * @return detached non-empty byte sequence
         */
        private static byte[] bytes(final byte[] source, final String label) {
            final byte[] result = Assert.notNull(source, label + " must not be null");
            if (result.length == 0) {
                throw new ValidateException(label + " must not be empty");
            }
            return result.clone();
        }

        /**
         * Returns a defensive copy of raw signature bytes.
         *
         * @return signature octets owned by the caller
         */
        @Override
        public byte[] value() {
            return value.clone();
        }

        /**
         * Returns a defensive copy of exact signed input.
         *
         * @return canonical Redirect-binding signed input owned by the caller
         */
        @Override
        public byte[] signedInput() {
            return signedInput.clone();
        }

    }

    /**
     * Preserves one raw and decoded query parameter slice.
     *
     * @param rawName      exact percent-encoded parameter name
     * @param rawValue     exact percent-encoded parameter value
     * @param decodedName  strict UTF-8 decoded parameter name
     * @param decodedValue strict UTF-8 decoded parameter value
     */
    private record RawParameter(String rawName, String rawValue, String decodedName, String decodedValue) {

        /**
         * Validates non-null raw and decoded parameter values.
         */
        private RawParameter {
            Assert.notNull(rawName, "SAML raw query name must not be null");
            Assert.notNull(rawValue, "SAML raw query value must not be null");
            Assert.notNull(decodedName, "SAML decoded query name must not be null");
            Assert.notNull(decodedValue, "SAML decoded query value must not be null");
        }

    }

}
