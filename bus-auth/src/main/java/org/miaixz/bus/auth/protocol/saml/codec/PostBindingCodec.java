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

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.*;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.protocol.saml.*;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.Protocol;

/**
 * Implements the SAML 2.0 HTTP-POST Binding for browser-delivered protocol messages.
 * <p>
 * The binding transports the complete, uncompressed XML document as canonical Base64 in exactly one {@code SAMLRequest}
 * or {@code SAMLResponse} form control. XML signatures remain part of the transported document; this codec neither
 * creates nor validates a separate binding signature. Decoding retains the original XML bytes for subsequent validation
 * by the SAML signature service.
 * </p>
 *
 * @author Kimi Liu
 */
public class PostBindingCodec {

    /**
     * Strict SAML protocol-message XML codec.
     */
    private final SamlMessageCodec messageCodec;

    /**
     * Creates an HTTP-POST Binding codec.
     *
     * @param messageCodec strict SAML XML codec used without compression
     * @throws IllegalArgumentException if the codec is {@code null}
     */
    public PostBindingCodec(final SamlMessageCodec messageCodec) {
        this.messageCodec = Assert.notNull(messageCodec, "SAML message codec must not be null");
    }

    /**
     * Identifies parameters admitted by the HTTP-POST Binding grammar.
     *
     * @param name decoded form-control name
     * @return {@code true} for request, response, or RelayState
     */
    private static boolean bindingParameter(final String name) {
        return Saml.Parameters.REQUEST.equals(name) || Saml.Parameters.RESPONSE.equals(name)
                || Saml.Parameters.RELAY_STATE.equals(name);
    }

    /**
     * Identifies controls admitted in an outbound response form.
     *
     * @param name form-control name
     * @return {@code true} for SAMLResponse or RelayState
     */
    private static boolean responseParameter(final String name) {
        return Saml.Parameters.RESPONSE.equals(name) || Saml.Parameters.RELAY_STATE.equals(name);
    }

    /**
     * Builds the standard response form after validating the transport values.
     *
     * @param action     trusted HTTPS destination
     * @param xml        uncompressed protocol XML
     * @param relayState optional opaque RelayState
     * @return immutable standard response form
     */
    private static PostForm responseForm(final String action, final byte[] xml, final Optional<String> relayState) {
        validateRelayState(relayState);
        final Map<String, String> fields = new LinkedHashMap<>();
        fields.put(Saml.Parameters.RESPONSE, Base64.getEncoder().encodeToString(xml));
        if (relayState.isPresent()) {
            fields.put(Saml.Parameters.RELAY_STATE, relayState.getOrNull());
        }
        return new PostForm(action, fields);
    }

    /**
     * Indexes standard form fields while rejecting duplicates and foreign controls.
     *
     * @param parameters decoded form controls in their received order
     * @return unique standard form-control map
     * @throws ValidateException if a field is duplicated or outside the binding grammar
     */
    private static Map<String, String> index(final List<Callback.Parameter> parameters) {
        Assert.notNull(parameters, "SAML POST parameter list must not be null");
        final Map<String, String> indexed = new LinkedHashMap<>();
        for (Callback.Parameter parameter : parameters) {
            final Callback.Parameter value = Assert.notNull(parameter, "SAML POST parameter must not be null");
            if (!bindingParameter(value.name())) {
                throw new ValidateException("Unsupported SAML POST form parameter: " + value.name());
            }
            if (indexed.putIfAbsent(value.name(), value.value()) != null) {
                throw new ValidateException("Duplicate SAML POST form parameter: " + value.name());
            }
        }
        return indexed;
    }

    /**
     * Rejects SAML transport parameters embedded in the destination URL query.
     *
     * @param requestUri original inbound request URI
     * @throws ValidateException if the URI is invalid or carries a SAML binding field in its query
     */
    private static void rejectSamlQueryParameters(final String requestUri) {
        Assert.notBlank(requestUri, "SAML POST request URI must not be blank");
        final String query;
        try {
            query = new URI(requestUri).getRawQuery();
        } catch (URISyntaxException exception) {
            throw new ValidateException("SAML POST request URI is invalid", exception);
        }
        if (query == null || query.isEmpty()) {
            return;
        }
        for (String component : query.split(Symbol.AND, -1)) {
            final int separator = component.indexOf(Symbol.C_EQUAL);
            final String rawName = separator < 0 ? component : component.substring(0, separator);
            final String name;
            try {
                name = URLDecoder.decode(rawName, Charset.UTF_8);
            } catch (IllegalArgumentException exception) {
                throw new ValidateException("SAML POST request query has invalid percent encoding", exception);
            }
            if (bindingParameter(name)) {
                throw new ValidateException("SAML HTTP-POST Binding fields must not appear in the URL query");
            }
        }
    }

    /**
     * Enforces the SAML binding limit on the UTF-8 representation of RelayState.
     *
     * @param relayState optional opaque value
     * @throws ValidateException if the value exceeds eighty UTF-8 bytes
     */
    private static void validateRelayState(final Optional<String> relayState) {
        Assert.notNull(relayState, "SAML RelayState container must not be null");
        final String value = relayState.getOrNull();
        if (value != null && value.getBytes(Charset.UTF_8).length > Saml.MAXIMUM_RELAY_STATE_BYTES) {
            throw new ValidateException("SAML RelayState must not exceed 80 UTF-8 bytes");
        }
    }

    /**
     * Encodes a SAML Response into an immutable browser POST form.
     *
     * @param action     trusted HTTPS assertion-consumer endpoint
     * @param response   standard SAML Response document
     * @param relayState optional opaque RelayState returned unchanged
     * @return immutable form action and standard form controls
     * @throws ValidateException if the action or RelayState violates the binding profile
     */
    public PostForm encode(final String action, final Response response, final Optional<String> relayState) {
        Assert.notNull(response, "SAML Response must not be null");
        return responseForm(action, messageCodec.encode(response), relayState);
    }

    /**
     * Encodes a SAML Logout Response into an immutable browser POST form.
     *
     * @param action     trusted HTTPS SingleLogoutService response endpoint
     * @param response   standard SAML Logout Response document
     * @param relayState optional opaque RelayState returned unchanged
     * @return immutable form action and standard form controls
     * @throws ValidateException if the action or RelayState violates the binding profile
     */
    public PostForm encode(final String action, final LogoutResponse response, final Optional<String> relayState) {
        Assert.notNull(response, "SAML Logout Response must not be null");
        return responseForm(action, messageCodec.encode(response), relayState);
    }

    /**
     * Decodes one inbound HTTP-POST Binding message without altering its XML serialization.
     *
     * @param inbound     raw callback transport captured by the external HTTP endpoint
     * @param messageType exact expected SAML message class
     * @param <T>         expected standard SAML message type
     * @return secure original document and optional RelayState
     * @throws ValidateException if the method, fields, encoding, size, or message type is invalid
     */
    public <T> Decoded<T> decode(final Callback.Inbound inbound, final Class<T> messageType) {
        Assert.notNull(inbound, "SAML POST callback must not be null");
        Assert.notNull(messageType, "SAML POST message type must not be null");
        if (inbound.method() != Http.Method.POST) {
            throw new ValidateException("SAML HTTP-POST Binding requires POST");
        }
        rejectSamlQueryParameters(inbound.requestUri());
        final Map<String, String> fields = index(inbound.parameters());
        final boolean requestType = messageType == AuthnRequest.class || messageType == LogoutRequest.class;
        final boolean responseType = messageType == Response.class || messageType == LogoutResponse.class;
        if (!requestType && !responseType) {
            throw new ValidateException("Unsupported SAML HTTP-POST message type: " + messageType.getName());
        }
        final String messageName = requestType ? Saml.Parameters.REQUEST : Saml.Parameters.RESPONSE;
        final String conflictingName = requestType ? Saml.Parameters.RESPONSE : Saml.Parameters.REQUEST;
        if (!fields.containsKey(messageName) || fields.containsKey(conflictingName)) {
            throw new ValidateException("SAML POST form contains an invalid request/response parameter choice");
        }
        final Optional<String> relayState = fields.containsKey(Saml.Parameters.RELAY_STATE)
                ? Optional.of(fields.get(Saml.Parameters.RELAY_STATE))
                : Optional.empty();
        validateRelayState(relayState);
        final byte[] xml = canonicalBase64(fields.get(messageName));
        return new Decoded<>(decodeDocument(xml, messageType), relayState);
    }

    /**
     * Decodes canonical standard Base64 and enforces the configured XML byte ceiling before XML parsing.
     *
     * @param encoded canonical Base64 message value
     * @return decoded uncompressed XML bytes
     * @throws ValidateException if Base64 is non-canonical, invalid, empty, or exceeds the XML limit
     */
    private byte[] canonicalBase64(final String encoded) {
        Assert.notNull(encoded, "SAML POST message value must not be null");
        final long maximumEncoded = ((messageCodec.maximumBytes() + 2L) / 3L) * 4L;
        if (encoded.isEmpty() || encoded.length() > maximumEncoded || (encoded.length() & 3) != 0) {
            throw new ValidateException("SAML POST message Base64 length is invalid");
        }
        final byte[] xml;
        try {
            xml = Base64.getDecoder().decode(encoded);
        } catch (IllegalArgumentException exception) {
            throw new ValidateException("SAML POST message is not valid Base64", exception);
        }
        if (!Base64.getEncoder().encodeToString(xml).equals(encoded)) {
            throw new ValidateException("SAML POST message must use canonical Base64");
        }
        if (xml.length == 0 || xml.length > messageCodec.maximumBytes()) {
            throw new ValidateException("SAML POST XML length exceeds the configured limit");
        }
        return xml;
    }

    /**
     * Dispatches XML decoding only to the explicitly selected standard message model.
     *
     * @param xml         original uncompressed XML bytes
     * @param messageType exact expected SAML model class
     * @param <T>         expected message type
     * @return typed secure document retaining the original XML
     * @throws ValidateException if the selected type is unsupported
     */
    private <T> SamlMessageCodec.Document<T> decodeDocument(final byte[] xml, final Class<T> messageType) {
        if (messageType == AuthnRequest.class) {
            return (SamlMessageCodec.Document<T>) messageCodec.decodeAuthnRequest(xml);
        }
        if (messageType == LogoutRequest.class) {
            return (SamlMessageCodec.Document<T>) messageCodec.decodeLogoutRequest(xml);
        }
        if (messageType == Response.class) {
            return (SamlMessageCodec.Document<T>) messageCodec.decodeResponse(xml);
        }
        if (messageType == LogoutResponse.class) {
            return (SamlMessageCodec.Document<T>) messageCodec.decodeLogoutResponse(xml);
        }
        throw new ValidateException("Unsupported SAML HTTP-POST message type: " + messageType.getName());
    }

    /**
     * Describes one immutable browser form produced by the SAML HTTP-POST Binding.
     *
     * @param action trusted absolute HTTPS form destination
     * @param fields immutable standard {@code SAMLResponse} and optional {@code RelayState} controls
     * @author Kimi Liu
     */
    public record PostForm(String action, Map<String, String> fields) {

        /**
         * Validates the form destination and freezes its exact standard controls.
         *
         * @throws IllegalArgumentException if a required value is null or blank
         * @throws ValidateException        if the action is not a safe HTTPS URI or fields violate the response grammar
         */
        public PostForm {
            Assert.notBlank(action, "SAML POST form action must not be blank");
            URI target;
            try {
                target = new URI(action);
            } catch (URISyntaxException exception) {
                throw new ValidateException("SAML POST form action is invalid", exception);
            }
            if (!target.isAbsolute() || !Protocol.HTTPS.name.equalsIgnoreCase(target.getScheme())
                    || target.getHost() == null || target.getRawUserInfo() != null || target.getRawFragment() != null) {
                throw new ValidateException(
                        "SAML POST form action must be an absolute HTTPS URI without user info or fragment");
            }
            Assert.notNull(fields, "SAML POST form fields must not be null");
            final Map<String, String> copy = new LinkedHashMap<>();
            for (Map.Entry<String, String> entry : fields.entrySet()) {
                final String name = Assert.notBlank(entry.getKey(), "SAML POST field name must not be blank");
                final String value = Assert.notNull(entry.getValue(), "SAML POST field value must not be null");
                if (!responseParameter(name)) {
                    throw new ValidateException("SAML response form contains an unsupported field: " + name);
                }
                copy.put(name, value);
            }
            if (!copy.containsKey(Saml.Parameters.RESPONSE)) {
                throw new ValidateException("SAML response form must contain SAMLResponse");
            }
            if (copy.get(Saml.Parameters.RESPONSE).isEmpty()) {
                throw new ValidateException("SAML response form SAMLResponse must not be empty");
            }
            validateRelayState(
                    copy.containsKey(Saml.Parameters.RELAY_STATE) ? Optional.of(copy.get(Saml.Parameters.RELAY_STATE))
                            : Optional.empty());
            fields = Collections.unmodifiableMap(copy);
        }

    }

    /**
     * Carries one decoded SAML HTTP-POST document and its unchanged RelayState.
     *
     * @param document   typed secure document retaining the original XML bytes
     * @param relayState optional opaque RelayState
     * @param <T>        decoded standard SAML message type
     * @author Kimi Liu
     */
    public record Decoded<T>(SamlMessageCodec.Document<T> document, Optional<String> relayState) {

        /**
         * Validates the decoded binding result and normalizes its optional container.
         *
         * @throws IllegalArgumentException if a required component or container is {@code null}
         * @throws ValidateException        if RelayState exceeds the binding limit
         */
        public Decoded {
            document = Assert.notNull(document, "SAML POST document must not be null");
            Assert.notNull(relayState, "SAML POST RelayState container must not be null");
            relayState = Optional.ofNullable(relayState.getOrNull());
            validateRelayState(relayState);
        }

    }

}
