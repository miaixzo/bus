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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;

import org.miaixz.bus.auth.protocol.saml.*;
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xml.XXE;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Encodes and securely decodes the SAML 2.0 protocol and assertion messages used by the implemented browser profiles.
 * <p>
 * Decoding retains the complete original XML bytes and the document-wide unique XML ID inventory. Signature validation
 * therefore runs against the original parsed document rather than a reconstructed serialization. The codec disables DTD
 * and external entity processing through bus-core XML hardening, enforces explicit byte and tree depth limits, and
 * rejects duplicate IDs before constructing a typed message.
 * </p>
 *
 * @author Kimi Liu
 */
public final class SamlMessageCodec {

    /**
     * Maximum accepted XML byte length.
     */
    private final long maximumBytes;

    /**
     * Maximum accepted XML element nesting depth.
     */
    private final int maximumDepth;

    /**
     * Creates a strict SAML message codec with explicit resource limits.
     *
     * @param maximumBytes positive maximum encoded XML size
     * @param maximumDepth positive maximum element nesting depth
     * @throws ValidateException if a limit is not positive
     */
    public SamlMessageCodec(final long maximumBytes, final int maximumDepth) {
        if (maximumBytes <= 0L) {
            throw new ValidateException("SAML maximum XML byte length must be positive");
        }
        if (maximumDepth <= 0) {
            throw new ValidateException("SAML maximum XML depth must be positive");
        }
        this.maximumBytes = maximumBytes;
        this.maximumDepth = maximumDepth;
    }

    /**
     * Creates one empty namespace-aware DOM document using bus-core XXE hardening.
     *
     * @return new empty DOM document
     */
    private static org.w3c.dom.Document document() {
        try {
            final DocumentBuilderFactory factory = XXE.disableXXE(DocumentBuilderFactory.newInstance());
            factory.setNamespaceAware(true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            return factory.newDocumentBuilder().newDocument();
        } catch (Exception exception) {
            throw new ValidateException("SAML XML document builder is unavailable", exception);
        }
    }

    /**
     * Serializes a DOM node with secure transformation and no indentation.
     *
     * @param node DOM document or element
     * @return deterministic UTF-8 XML bytes
     */
    static byte[] serialize(final Node node) {
        try {
            final TransformerFactory factory = XXE.disableXXE(TransformerFactory.newInstance());
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            final Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, Charset.UTF_8.name());
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            transformer.transform(new DOMSource(node), new StreamResult(output));
            return output.toByteArray();
        } catch (Exception exception) {
            throw new ValidateException("SAML XML serialization failed", exception);
        }
    }

    /**
     * Adds an XML Schema type and scalar text to one AttributeValue element.
     *
     * @param element AttributeValue element
     * @param type    qualified XML Schema type
     * @param text    scalar lexical value
     */
    private static void typed(final Element element, final String type, final String text) {
        element.setAttributeNS(
                XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
                Saml.Xml.XMLNS_XSI,
                Saml.Namespaces.SCHEMA_INSTANCE);
        element.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, Saml.Xml.XMLNS_XS, Saml.Namespaces.SCHEMA);
        element.setAttributeNS(Saml.Namespaces.SCHEMA_INSTANCE, Saml.Xml.XSI_TYPE, type);
        element.setTextContent(text);
    }

    /**
     * Requires an element to have one exact expanded QName.
     *
     * @param element   candidate element
     * @param namespace required namespace URI
     * @param localName required local name
     */
    static void require(final Element element, final String namespace, final String localName) {
        if (!namespace.equals(element.getNamespaceURI()) || !localName.equals(element.getLocalName())) {
            throw new ValidateException("Unexpected SAML XML element {" + element.getNamespaceURI() + Symbol.BRACE_RIGHT
                    + element.getLocalName());
        }
    }

    /**
     * Converts a SAML XML dateTime lexical value to an Instant without accepting a local time zone.
     *
     * @param value XML dateTime lexical value
     * @param label safe field label
     * @return parsed instant
     */
    static Instant instant(final String value, final String label) {
        try {
            return OffsetDateTime.parse(Assert.notBlank(value, label + " must not be blank")).toInstant();
        } catch (RuntimeException exception) {
            throw new ValidateException(label + " must be an XML Schema dateTime with a UTC offset", exception);
        }
    }

    /**
     * Returns the XML byte ceiling shared with SAML transport binding codecs.
     *
     * @return positive maximum decoded XML byte count
     */
    long maximumBytes() {
        return maximumBytes;
    }

    /**
     * Encodes a standard Authentication Request as UTF-8 SAML protocol XML.
     *
     * @param message standard Authentication Request
     * @return deterministic XML bytes
     */
    public byte[] encode(final AuthnRequest message) {
        return serialize(
                SamlXmlWriter.authnRequest(
                        document(),
                        Assert.notNull(message, "SAML Authentication Request must not be null")));
    }

    /**
     * Encodes a standard SAML Response as UTF-8 protocol XML.
     *
     * @param message standard SAML Response
     * @return deterministic XML bytes
     */
    public byte[] encode(final Response message) {
        return serialize(SamlXmlWriter.response(document(), Assert.notNull(message, "SAML Response must not be null")));
    }

    /**
     * Encodes a standard Logout Request as UTF-8 SAML protocol XML.
     *
     * @param message standard Logout Request
     * @return deterministic XML bytes
     */
    public byte[] encode(final LogoutRequest message) {
        return serialize(
                SamlXmlWriter
                        .logoutRequest(document(), Assert.notNull(message, "SAML Logout Request must not be null")));
    }

    /**
     * Encodes a standard Logout Response as UTF-8 SAML protocol XML.
     *
     * @param message standard Logout Response
     * @return deterministic XML bytes
     */
    public byte[] encode(final LogoutResponse message) {
        return serialize(
                SamlXmlWriter
                        .logoutResponse(document(), Assert.notNull(message, "SAML Logout Response must not be null")));
    }

    /**
     * Encodes a standard SAML Assertion as UTF-8 assertion XML.
     *
     * @param message standard SAML Assertion
     * @return deterministic XML bytes
     */
    public byte[] encode(final Assertion message) {
        return serialize(
                SamlXmlWriter.assertion(document(), Assert.notNull(message, "SAML Assertion must not be null")));
    }

    /**
     * Securely decodes an Authentication Request and retains its original signed document.
     *
     * @param xml raw UTF-8 XML bytes
     * @return typed request with original document and unique ID inventory
     */
    public Document<AuthnRequest> decodeAuthnRequest(final byte[] xml) {
        return decode(xml, Saml.Namespaces.PROTOCOL, Saml.Xml.AUTHN_REQUEST, SamlXmlReader::authnRequest);
    }

    /**
     * Securely decodes a SAML Response and retains its original signed document.
     *
     * @param xml raw UTF-8 XML bytes
     * @return typed response with original document and unique ID inventory
     */
    public Document<Response> decodeResponse(final byte[] xml) {
        return decode(xml, Saml.Namespaces.PROTOCOL, Saml.Xml.RESPONSE, SamlXmlReader::response);
    }

    /**
     * Securely decodes a Logout Request and retains its original signed document.
     *
     * @param xml raw UTF-8 XML bytes
     * @return typed request with original document and unique ID inventory
     */
    public Document<LogoutRequest> decodeLogoutRequest(final byte[] xml) {
        return decode(xml, Saml.Namespaces.PROTOCOL, Saml.Xml.LOGOUT_REQUEST, SamlXmlReader::logoutRequest);
    }

    /**
     * Securely decodes a Logout Response and retains its original signed document.
     *
     * @param xml raw UTF-8 XML bytes
     * @return typed response with original document and unique ID inventory
     */
    public Document<LogoutResponse> decodeLogoutResponse(final byte[] xml) {
        return decode(xml, Saml.Namespaces.PROTOCOL, Saml.Xml.LOGOUT_RESPONSE, SamlXmlReader::logoutResponse);
    }

    /**
     * Securely decodes a decrypted Assertion and retains its original signed document.
     *
     * @param xml raw UTF-8 XML bytes
     * @return typed assertion with original document and unique ID inventory
     */
    public Document<Assertion> decodeAssertion(final byte[] xml) {
        return decode(xml, Saml.Namespaces.ASSERTION, Saml.Xml.ASSERTION, SamlXmlReader::assertion);
    }

    /**
     * Encodes one provider-neutral JSON scalar as a complete SAML AttributeValue element.
     * <p>
     * Arrays and objects are rejected because AssertionIssuer expands arrays into repeated AttributeValue elements and
     * no interoperable standard mapping exists for arbitrary JSON objects. JSON null becomes an explicit
     * {@code xsi:nil="true"} value.
     * </p>
     *
     * @param value provider-neutral scalar value
     * @return complete UTF-8 AttributeValue XML element
     * @throws ValidateException if the value is an array or object
     */
    public byte[] attributeValue(final JsonValue value) {
        Assert.notNull(value, "SAML AttributeValue source must not be null");
        final org.w3c.dom.Document document = document();
        final Element element = document
                .createElementNS(Saml.Namespaces.ASSERTION, Saml.Xml.ASSERTION_PREFIX + Saml.Xml.ATTRIBUTE_VALUE);
        document.appendChild(element);
        switch (value) {
            case JsonValue.StringValue string -> typed(element, Saml.Xml.XS_STRING, string.value());
            case JsonValue.NumberValue number -> typed(element, Saml.Xml.XS_DECIMAL, number.value().toPlainString());
            case JsonValue.BooleanValue bool -> typed(element, Saml.Xml.XS_BOOLEAN, Boolean.toString(bool.value()));
            case JsonValue.NullValue ignored -> {
                element.setAttributeNS(
                        XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
                        Saml.Xml.XMLNS_XSI,
                        Saml.Namespaces.SCHEMA_INSTANCE);
                element.setAttributeNS(Saml.Namespaces.SCHEMA_INSTANCE, Saml.Xml.XSI_NIL, Normal.TRUE);
            }
            case JsonValue.ArrayValue ignored -> throw new ValidateException(
                    "SAML AttributeValue arrays must be expanded by the assertion issuer");
            case JsonValue.ObjectValue ignored -> throw new ValidateException(
                    "SAML AttributeValue has no standard arbitrary JSON object mapping");
        }
        return serialize(document);
    }

    /**
     * Parses, validates, and converts one XML document through an exact root-specific reader.
     *
     * @param xml       raw XML bytes
     * @param namespace required root namespace
     * @param localName required root local name
     * @param reader    typed root reader
     * @param <T>       decoded standard message type
     * @return immutable decoded document
     */
    private <T> Document<T> decode(
            final byte[] xml,
            final String namespace,
            final String localName,
            final Reader<T> reader) {
        final byte[] source = checkedBytes(xml);
        try {
            final DocumentBuilderFactory factory = XXE.disableXXE(DocumentBuilderFactory.newInstance());
            factory.setNamespaceAware(true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            final org.w3c.dom.Document parsed = factory.newDocumentBuilder().parse(new ByteArrayInputStream(source));
            final Element root = Assert.notNull(parsed.getDocumentElement(), "SAML XML root must not be null");
            require(root, namespace, localName);
            final LinkedHashSet<String> ids = new LinkedHashSet<>();
            inspect(root, 1, ids);
            final T message = reader.read(root);
            return new Document<>(message, source, ids);
        } catch (ValidateException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ValidateException("SAML XML document is malformed or unsafe", exception);
        }
    }

    /**
     * Validates XML size and takes a defensive copy before parsing.
     *
     * @param xml source bytes
     * @return owned non-empty bytes
     */
    private byte[] checkedBytes(final byte[] xml) {
        final byte[] source = Assert.notNull(xml, "SAML XML bytes must not be null");
        if (source.length == 0 || source.length > maximumBytes) {
            throw new ValidateException("SAML XML byte length is outside the configured limit");
        }
        return source.clone();
    }

    /**
     * Traverses one element tree to enforce depth and document-wide ID uniqueness.
     *
     * @param element current element
     * @param depth   current one-based element depth
     * @param ids     accumulated unique ID values
     */
    private void inspect(final Element element, final int depth, final Set<String> ids) {
        if (depth > maximumDepth) {
            throw new ValidateException("SAML XML element depth exceeds the configured limit");
        }
        final NamedNodeMap attributes = element.getAttributes();
        for (int index = 0; index < attributes.getLength(); index++) {
            final Node node = attributes.item(index);
            if (node instanceof Attr attribute && (Saml.Xml.ID.equals(attribute.getLocalName())
                    || Saml.Xml.METADATA_ID.equals(attribute.getLocalName())
                    || Saml.Xml.ID_LOWER.equals(attribute.getLocalName()))) {
                final String value = Assert.notBlank(attribute.getValue(), "SAML XML ID must not be blank");
                if (!ids.add(value)) {
                    throw new ValidateException("SAML XML document contains a duplicate ID");
                }
                element.setIdAttributeNode(attribute, true);
            }
        }
        final NodeList children = element.getChildNodes();
        for (int index = 0; index < children.getLength(); index++) {
            if (children.item(index) instanceof Element child) {
                inspect(child, depth + 1, ids);
            }
        }
    }

    /**
     * Maps one nullable source value into a non-null typed result.
     *
     * @param <S> source type
     * @param <T> target type
     */
    @FunctionalInterface
    private interface Mapper<S, T> {

        /**
         * Converts one present source value.
         *
         * @param source present source value
         * @return converted target value
         */
        T map(S source);

    }

    /**
     * Converts one exact root element into its typed SAML message.
     *
     * @param <T> decoded message type
     */
    @FunctionalInterface
    private interface Reader<T> {

        /**
         * Reads one already validated root element.
         *
         * @param root exact root element
         * @return typed standard message
         */
        T read(Element root);

    }

    /**
     * Retains one securely decoded typed SAML message with its exact original XML and unique ID inventory.
     *
     * @param message typed standard SAML message
     * @param xml     complete original XML bytes
     * @param ids     immutable document-wide XML ID values
     * @param <T>     typed message type
     * @author Kimi Liu
     */
    public record Document<T>(T message, byte[] xml, Set<String> ids) {

        /**
         * Takes immutable ownership of the original XML and identifier inventory.
         *
         * @throws IllegalArgumentException if a component or ID is {@code null}
         * @throws ValidateException        if XML is empty or an ID is blank
         */
        public Document {
            Assert.notNull(message, "Decoded SAML message must not be null");
            final byte[] source = Assert.notNull(xml, "Decoded SAML XML must not be null");
            if (source.length == 0) {
                throw new ValidateException("Decoded SAML XML must not be empty");
            }
            xml = source.clone();
            Assert.notNull(ids, "Decoded SAML XML ID set must not be null");
            final Set<String> copy = new LinkedHashSet<>();
            for (String id : ids) {
                copy.add(Assert.notBlank(id, "Decoded SAML XML ID must not be blank"));
            }
            ids = Collections.unmodifiableSet(copy);
        }

        /**
         * Returns a defensive copy of the original signed XML document.
         *
         * @return copied XML bytes
         */
        @Override
        public byte[] xml() {
            return xml.clone();
        }

    }

    /**
     * Writes immutable SAML models into namespace-correct DOM trees in schema order.
     */
    private static final class SamlXmlWriter {

        /**
         * Prevents construction of the writer namespace.
         */
        private SamlXmlWriter() {
            // No initialization required.
        }

        /**
         * Writes one Authentication Request as a complete document.
         *
         * @param document empty namespace-aware target document
         * @param value    validated Authentication Request model
         * @return target document containing one AuthnRequest root
         */
        private static org.w3c.dom.Document authnRequest(
                final org.w3c.dom.Document document,
                final AuthnRequest value) {
            final Element root = protocol(document, Saml.Xml.AUTHN_REQUEST);
            document.appendChild(root);
            requestAttributes(
                    root,
                    value.id(),
                    value.version(),
                    value.issueInstant(),
                    value.destination().getOrNull(),
                    value.consent().getOrNull());
            optional(root, Saml.Xml.FORCE_AUTHN, value.forceAuthn().getOrNull());
            optional(root, Saml.Xml.IS_PASSIVE, value.passive().getOrNull());
            optional(
                    root,
                    Saml.Xml.PROTOCOL_BINDING,
                    value.protocolBinding().isPresent() ? value.protocolBinding().getOrNull().value() : null);
            optional(
                    root,
                    Saml.Xml.ASSERTION_CONSUMER_SERVICE_INDEX,
                    value.assertionConsumerServiceIndex().getOrNull());
            optional(root, Saml.Xml.ASSERTION_CONSUMER_SERVICE_URL, value.assertionConsumerServiceUrl().getOrNull());
            optional(
                    root,
                    Saml.Xml.ATTRIBUTE_CONSUMING_SERVICE_INDEX,
                    value.attributeConsumingServiceIndex().getOrNull());
            optional(root, Saml.Xml.PROVIDER_NAME, value.providerName().getOrNull());
            commonRequestChildren(
                    document,
                    root,
                    value.issuer().getOrNull(),
                    value.signature().getOrNull(),
                    value.extensions());
            append(root, subject(document, value.subject().getOrNull()));
            append(root, nameIdPolicy(document, value.nameIdPolicy().getOrNull()));
            append(root, conditions(document, value.conditions().getOrNull()));
            append(root, requestedAuthnContext(document, value.requestedAuthnContext().getOrNull()));
            append(root, fragment(document, value.scoping().getOrNull()));
            return document;
        }

        /**
         * Writes one protocol Response as a complete document.
         *
         * @param document empty namespace-aware target document
         * @param value    validated protocol Response model
         * @return target document containing one Response root
         */
        private static org.w3c.dom.Document response(final org.w3c.dom.Document document, final Response value) {
            final Element root = protocol(document, Saml.Xml.RESPONSE);
            document.appendChild(root);
            statusResponseAttributes(
                    root,
                    value.id(),
                    value.inResponseTo().getOrNull(),
                    value.version(),
                    value.issueInstant(),
                    value.destination().getOrNull(),
                    value.consent().getOrNull());
            commonRequestChildren(
                    document,
                    root,
                    value.issuer().getOrNull(),
                    value.signature().getOrNull(),
                    value.extensions());
            root.appendChild(status(document, value.status()));
            for (Response.AssertionContent content : value.assertions()) {
                switch (content) {
                    case Response.PlainAssertion plain -> root
                            .appendChild(assertionElement(document, plain.assertion()));
                    case Response.EncryptedAssertion encrypted -> root
                            .appendChild(requiredFragment(document, encrypted.xml(), "SAML EncryptedAssertion"));
                }
            }
            return document;
        }

        /**
         * Writes one Logout Request as a complete document.
         *
         * @param document empty namespace-aware target document
         * @param value    validated Logout Request model
         * @return target document containing one LogoutRequest root
         */
        private static org.w3c.dom.Document logoutRequest(
                final org.w3c.dom.Document document,
                final LogoutRequest value) {
            final Element root = protocol(document, Saml.Xml.LOGOUT_REQUEST);
            document.appendChild(root);
            requestAttributes(
                    root,
                    value.id(),
                    value.version(),
                    value.issueInstant(),
                    value.destination().getOrNull(),
                    value.consent().getOrNull());
            optional(root, Saml.Xml.REASON, value.reason().getOrNull());
            optional(root, Saml.Xml.NOT_ON_OR_AFTER, value.notOnOrAfter().getOrNull());
            commonRequestChildren(
                    document,
                    root,
                    value.issuer().getOrNull(),
                    value.signature().getOrNull(),
                    value.extensions());
            root.appendChild(identifier(document, value.identifier()));
            for (String sessionIndex : value.sessionIndexes()) {
                final Element element = protocol(document, Saml.Xml.SESSION_INDEX);
                element.setTextContent(sessionIndex);
                root.appendChild(element);
            }
            return document;
        }

        /**
         * Writes one Logout Response as a complete document.
         *
         * @param document empty namespace-aware target document
         * @param value    validated Logout Response model
         * @return target document containing one LogoutResponse root
         */
        private static org.w3c.dom.Document logoutResponse(
                final org.w3c.dom.Document document,
                final LogoutResponse value) {
            final Element root = protocol(document, Saml.Xml.LOGOUT_RESPONSE);
            document.appendChild(root);
            statusResponseAttributes(
                    root,
                    value.id(),
                    value.inResponseTo().getOrNull(),
                    value.version(),
                    value.issueInstant(),
                    value.destination().getOrNull(),
                    value.consent().getOrNull());
            commonRequestChildren(
                    document,
                    root,
                    value.issuer().getOrNull(),
                    value.signature().getOrNull(),
                    value.extensions());
            root.appendChild(status(document, value.status()));
            return document;
        }

        /**
         * Writes one Assertion as a complete document.
         *
         * @param document empty namespace-aware target document
         * @param value    validated Assertion model
         * @return target document containing one Assertion root
         */
        private static org.w3c.dom.Document assertion(final org.w3c.dom.Document document, final Assertion value) {
            document.appendChild(assertionElement(document, value));
            return document;
        }

        /**
         * Writes one Assertion element and its ordered statement choice.
         *
         * @param document owning namespace-aware document
         * @param value    validated Assertion model
         * @return complete detached Assertion element
         */
        private static Element assertionElement(final org.w3c.dom.Document document, final Assertion value) {
            final Element root = assertion(document, Saml.Xml.ASSERTION);
            root.setAttribute(Saml.Xml.VERSION, value.version());
            root.setAttribute(Saml.Xml.ID, value.id());
            root.setAttribute(Saml.Xml.ISSUE_INSTANT, value.issueInstant().toString());
            root.appendChild(issuer(document, value.issuer()));
            append(root, fragment(document, value.signature().getOrNull()));
            append(root, subject(document, value.subject().getOrNull()));
            append(root, conditions(document, value.conditions().getOrNull()));
            append(root, fragment(document, value.advice().getOrNull()));
            for (Assertion.StatementContent statement : value.statements()) {
                switch (statement) {
                    case Assertion.AuthenticationStatement authentication -> root
                            .appendChild(authnStatement(document, authentication.statement()));
                    case Assertion.AttributesStatement attributes -> root
                            .appendChild(attributeStatement(document, attributes.statement()));
                    case Assertion.OtherStatement other -> root
                            .appendChild(requiredFragment(document, other.xml(), "SAML Statement"));
                }
            }
            return root;
        }

        /**
         * Adds the common request attributes defined by RequestAbstractType.
         *
         * @param root         request element receiving attributes
         * @param id           validated XML ID
         * @param version      SAML protocol version
         * @param issueInstant request issue instant
         * @param destination  optional destination URI
         * @param consent      optional consent URI
         */
        private static void requestAttributes(
                final Element root,
                final String id,
                final String version,
                final Instant issueInstant,
                final String destination,
                final String consent) {
            root.setAttribute(Saml.Xml.ID, id);
            root.setAttribute(Saml.Xml.VERSION, version);
            root.setAttribute(Saml.Xml.ISSUE_INSTANT, issueInstant.toString());
            optional(root, Saml.Xml.DESTINATION, destination);
            optional(root, Saml.Xml.CONSENT, consent);
        }

        /**
         * Adds StatusResponseType attributes after the common request attributes.
         *
         * @param root         response element receiving attributes
         * @param id           validated XML ID
         * @param inResponseTo optional correlated request ID
         * @param version      SAML protocol version
         * @param issueInstant response issue instant
         * @param destination  optional destination URI
         * @param consent      optional consent URI
         */
        private static void statusResponseAttributes(
                final Element root,
                final String id,
                final String inResponseTo,
                final String version,
                final Instant issueInstant,
                final String destination,
                final String consent) {
            requestAttributes(root, id, version, issueInstant, destination, consent);
            optional(root, Saml.Xml.IN_RESPONSE_TO, inResponseTo);
        }

        /**
         * Writes Issuer, Signature, and Extensions children in schema order.
         *
         * @param document   owning namespace-aware document
         * @param root       request or response parent element
         * @param issuer     optional assertion Issuer
         * @param signature  optional complete XML Signature bytes
         * @param extensions ordered complete protocol extension elements
         */
        private static void commonRequestChildren(
                final org.w3c.dom.Document document,
                final Element root,
                final Issuer issuer,
                final byte[] signature,
                final List<byte[]> extensions) {
            append(root, issuer == null ? null : issuer(document, issuer));
            append(root, fragment(document, signature));
            if (!extensions.isEmpty()) {
                final Element wrapper = protocol(document, Saml.Xml.EXTENSIONS);
                for (byte[] extension : extensions) {
                    wrapper.appendChild(requiredFragment(document, extension, "SAML protocol extension"));
                }
                root.appendChild(wrapper);
            }
        }

        /**
         * Writes one assertion Issuer element.
         *
         * @param document owning namespace-aware document
         * @param value    validated Issuer model
         * @return complete Issuer element
         */
        private static Element issuer(final org.w3c.dom.Document document, final Issuer value) {
            final Element element = nameId(document, value.nameId(), Saml.Xml.ISSUER);
            return element;
        }

        /**
         * Writes one NameIDType element using the requested local name.
         *
         * @param document  owning namespace-aware document
         * @param value     validated NameID model
         * @param localName owning SAML element local name
         * @return complete NameIDType element
         */
        private static Element nameId(final org.w3c.dom.Document document, final NameID value, final String localName) {
            final Element element = assertion(document, localName);
            optional(element, Saml.Xml.NAME_QUALIFIER, value.nameQualifier().getOrNull());
            optional(element, Saml.Xml.SP_NAME_QUALIFIER, value.spNameQualifier().getOrNull());
            optional(element, Saml.Xml.FORMAT, value.format().getOrNull());
            optional(element, Saml.Xml.SP_PROVIDED_ID, value.spProvidedId().getOrNull());
            element.setTextContent(value.value());
            return element;
        }

        /**
         * Writes the SAML Subject identifier and confirmations.
         *
         * @param document owning namespace-aware document
         * @param value    optional validated Subject model
         * @return complete Subject element or {@code null} when absent
         */
        private static Element subject(final org.w3c.dom.Document document, final Subject value) {
            if (value == null) {
                return null;
            }
            final Element element = assertion(document, Saml.Xml.SUBJECT);
            append(
                    element,
                    value.identifier().isPresent() ? identifier(document, value.identifier().getOrNull()) : null);
            for (SubjectConfirmation confirmation : value.confirmations()) {
                element.appendChild(subjectConfirmation(document, confirmation));
            }
            return element;
        }

        /**
         * Writes one subject identifier CHOICE member.
         *
         * @param document   owning namespace-aware document
         * @param identifier selected Subject identifier model
         * @return complete NameID, BaseID, or EncryptedID element
         */
        private static Element identifier(final org.w3c.dom.Document document, final Subject.Identifier identifier) {
            return switch (identifier) {
                case Subject.NamedIdentifier named -> nameId(document, named.value(), Saml.Xml.NAME_ID);
                case Subject.BaseIdentifier base -> requiredFragment(document, base.xml(), "SAML BaseID");
                case Subject.EncryptedIdentifier encrypted -> requiredFragment(
                        document,
                        encrypted.xml(),
                        "SAML EncryptedID");
            };
        }

        /**
         * Writes one bearer or extension SubjectConfirmation.
         *
         * @param document owning namespace-aware document
         * @param value    validated SubjectConfirmation model
         * @return complete SubjectConfirmation element
         */
        private static Element subjectConfirmation(
                final org.w3c.dom.Document document,
                final SubjectConfirmation value) {
            final Element element = assertion(document, Saml.Xml.SUBJECT_CONFIRMATION);
            element.setAttribute(Saml.Xml.METHOD, value.method());
            append(
                    element,
                    value.identifier().isPresent() ? identifier(document, value.identifier().getOrNull()) : null);
            append(
                    element,
                    value.data().isPresent() ? subjectConfirmationData(document, value.data().getOrNull()) : null);
            return element;
        }

        /**
         * Writes one SubjectConfirmationData element and its retained wildcard nodes.
         *
         * @param document owning namespace-aware document
         * @param value    validated SubjectConfirmationData model
         * @return complete SubjectConfirmationData element
         */
        private static Element subjectConfirmationData(
                final org.w3c.dom.Document document,
                final SubjectConfirmationData value) {
            final Element element = assertion(document, Saml.Xml.SUBJECT_CONFIRMATION_DATA);
            optional(element, Saml.Xml.NOT_BEFORE, value.notBefore().getOrNull());
            optional(element, Saml.Xml.NOT_ON_OR_AFTER, value.notOnOrAfter().getOrNull());
            optional(element, Saml.Xml.RECIPIENT, value.recipient().getOrNull());
            optional(element, Saml.Xml.IN_RESPONSE_TO, value.inResponseTo().getOrNull());
            optional(element, Saml.Xml.ADDRESS, value.address().getOrNull());
            value.attributes().forEach(
                    (name, text) -> element.setAttributeNS(
                            name.getNamespaceURI(),
                            qualified(name.getPrefix(), name.getLocalPart()),
                            text));
            for (byte[] node : value.content()) {
                element.appendChild(requiredFragment(document, node, "SAML SubjectConfirmationData content"));
            }
            return element;
        }

        /**
         * Writes one requested NameIDPolicy element.
         *
         * @param document owning namespace-aware document
         * @param value    optional validated NameIDPolicy model
         * @return complete NameIDPolicy element or {@code null} when absent
         */
        private static Element nameIdPolicy(final org.w3c.dom.Document document, final NameIDPolicy value) {
            if (value == null) {
                return null;
            }
            final Element element = protocol(document, Saml.Xml.NAME_ID_POLICY);
            optional(element, Saml.Xml.FORMAT, value.format().getOrNull());
            optional(element, Saml.Xml.SP_NAME_QUALIFIER, value.spNameQualifier().getOrNull());
            optional(element, Saml.Xml.ALLOW_CREATE, value.allowCreate().getOrNull());
            return element;
        }

        /**
         * Writes assertion Conditions and their supported CHOICE entries.
         *
         * @param document owning namespace-aware document
         * @param value    optional validated Conditions model
         * @return complete Conditions element or {@code null} when absent
         */
        private static Element conditions(final org.w3c.dom.Document document, final Conditions value) {
            if (value == null) {
                return null;
            }
            final Element element = assertion(document, Saml.Xml.CONDITIONS);
            optional(element, Saml.Xml.NOT_BEFORE, value.notBefore().getOrNull());
            optional(element, Saml.Xml.NOT_ON_OR_AFTER, value.notOnOrAfter().getOrNull());
            for (Conditions.Condition condition : value.conditions()) {
                switch (condition) {
                    case Conditions.Audience audience -> element
                            .appendChild(audienceRestriction(document, audience.restriction()));
                    case Conditions.OneTimeUse ignored -> element
                            .appendChild(assertion(document, Saml.Xml.ONE_TIME_USE));
                    case Conditions.ProxyRestriction proxy -> element.appendChild(proxyRestriction(document, proxy));
                    case Conditions.Extension extension -> element
                            .appendChild(requiredFragment(document, extension.xml(), "SAML extension Condition"));
                }
            }
            return element;
        }

        /**
         * Writes one AudienceRestriction condition.
         *
         * @param document owning namespace-aware document
         * @param value    validated AudienceRestriction model
         * @return complete AudienceRestriction element
         */
        private static Element audienceRestriction(
                final org.w3c.dom.Document document,
                final AudienceRestriction value) {
            final Element element = assertion(document, Saml.Xml.AUDIENCE_RESTRICTION);
            for (String audience : value.audiences()) {
                final Element child = assertion(document, Saml.Xml.AUDIENCE);
                child.setTextContent(audience);
                element.appendChild(child);
            }
            return element;
        }

        /**
         * Writes one ProxyRestriction condition.
         *
         * @param document owning namespace-aware document
         * @param value    validated ProxyRestriction model
         * @return complete ProxyRestriction element
         */
        private static Element proxyRestriction(
                final org.w3c.dom.Document document,
                final Conditions.ProxyRestriction value) {
            final Element element = assertion(document, Saml.Xml.PROXY_RESTRICTION);
            optional(element, Saml.Xml.COUNT, value.count().getOrNull());
            for (String audience : value.audiences()) {
                final Element child = assertion(document, Saml.Xml.AUDIENCE);
                child.setTextContent(audience);
                element.appendChild(child);
            }
            return element;
        }

        /**
         * Writes one RequestedAuthnContext and its selected reference family.
         *
         * @param document owning namespace-aware document
         * @param value    optional validated RequestedAuthnContext model
         * @return complete RequestedAuthnContext element or {@code null} when absent
         */
        private static Element requestedAuthnContext(
                final org.w3c.dom.Document document,
                final RequestedAuthnContext value) {
            if (value == null) {
                return null;
            }
            final Element element = protocol(document, Saml.Xml.REQUESTED_AUTHN_CONTEXT);
            optional(
                    element,
                    Saml.Xml.COMPARISON,
                    value.comparison().map(RequestedAuthnContext.Comparison::value).getOrNull());
            for (RequestedAuthnContext.ClassReference reference : value.classReferences()) {
                final Element child = assertion(document, Saml.Xml.AUTHN_CONTEXT_CLASS_REF);
                child.setTextContent(reference.value().toASCIIString());
                element.appendChild(child);
            }
            for (RequestedAuthnContext.DeclarationReference reference : value.declarationReferences()) {
                final Element child = assertion(document, Saml.Xml.AUTHN_CONTEXT_DECL_REF);
                child.setTextContent(reference.value().toASCIIString());
                element.appendChild(child);
            }
            return element;
        }

        /**
         * Writes one standard Status tree.
         *
         * @param document owning namespace-aware document
         * @param value    validated Status model
         * @return complete Status element
         */
        private static Element status(final org.w3c.dom.Document document, final Status value) {
            final Element element = protocol(document, Saml.Xml.STATUS);
            element.appendChild(statusCode(document, value.statusCode()));
            if (value.statusMessage().isPresent()) {
                final Element message = protocol(document, Saml.Xml.STATUS_MESSAGE);
                message.setTextContent(value.statusMessage().getOrNull().value());
                element.appendChild(message);
            }
            if (value.statusDetail().isPresent()) {
                final Element detail = protocol(document, Saml.Xml.STATUS_DETAIL);
                for (byte[] child : value.statusDetail().getOrNull().elements()) {
                    detail.appendChild(requiredFragment(document, child, "SAML StatusDetail child"));
                }
                element.appendChild(detail);
            }
            return element;
        }

        /**
         * Writes one recursively nested StatusCode.
         *
         * @param document owning namespace-aware document
         * @param value    validated StatusCode model
         * @return complete recursively nested StatusCode element
         */
        private static Element statusCode(final org.w3c.dom.Document document, final StatusCode value) {
            final Element element = protocol(document, Saml.Xml.STATUS_CODE);
            element.setAttribute(Saml.Xml.VALUE, value.value());
            if (value.nested().isPresent()) {
                element.appendChild(statusCode(document, value.nested().getOrNull()));
            }
            return element;
        }

        /**
         * Writes one AuthnStatement and required AuthnContext.
         *
         * @param document owning namespace-aware document
         * @param value    validated AuthnStatement model
         * @return complete AuthnStatement element
         */
        private static Element authnStatement(final org.w3c.dom.Document document, final AuthnStatement value) {
            final Element element = assertion(document, Saml.Xml.AUTHN_STATEMENT);
            element.setAttribute(Saml.Xml.AUTHN_INSTANT, value.authnInstant().toString());
            optional(element, Saml.Xml.SESSION_INDEX, value.sessionIndex().getOrNull());
            optional(element, Saml.Xml.SESSION_NOT_ON_OR_AFTER, value.sessionNotOnOrAfter().getOrNull());
            if (value.subjectLocalityAddress().isPresent() || value.subjectLocalityDnsName().isPresent()) {
                final Element locality = assertion(document, Saml.Xml.SUBJECT_LOCALITY);
                optional(locality, Saml.Xml.ADDRESS, value.subjectLocalityAddress().getOrNull());
                optional(locality, Saml.Xml.DNS_NAME, value.subjectLocalityDnsName().getOrNull());
                element.appendChild(locality);
            }
            element.appendChild(authnContext(document, value.authnContext()));
            return element;
        }

        /**
         * Writes one AuthnContext declaration choice.
         *
         * @param document owning namespace-aware document
         * @param value    validated AuthnContext model
         * @return complete AuthnContext element
         */
        private static Element authnContext(final org.w3c.dom.Document document, final AuthnContext value) {
            final Element element = assertion(document, Saml.Xml.AUTHN_CONTEXT);
            if (value.classReference().isPresent()) {
                final Element child = assertion(document, Saml.Xml.AUTHN_CONTEXT_CLASS_REF);
                child.setTextContent(value.classReference().getOrNull());
                element.appendChild(child);
            }
            append(element, fragment(document, value.declaration().getOrNull()));
            if (value.declarationReference().isPresent()) {
                final Element child = assertion(document, Saml.Xml.AUTHN_CONTEXT_DECL_REF);
                child.setTextContent(value.declarationReference().getOrNull());
                element.appendChild(child);
            }
            for (String authority : value.authenticatingAuthorities()) {
                final Element child = assertion(document, Saml.Xml.AUTHENTICATING_AUTHORITY);
                child.setTextContent(authority);
                element.appendChild(child);
            }
            return element;
        }

        /**
         * Writes one AttributeStatement with plain or retained encrypted attributes.
         *
         * @param document owning namespace-aware document
         * @param value    validated AttributeStatement model
         * @return complete AttributeStatement element
         */
        private static Element attributeStatement(final org.w3c.dom.Document document, final AttributeStatement value) {
            final Element element = assertion(document, Saml.Xml.ATTRIBUTE_STATEMENT);
            for (AttributeStatement.AttributeContent content : value.attributes()) {
                switch (content) {
                    case AttributeStatement.PlainAttribute plain -> element
                            .appendChild(attribute(document, plain.attribute()));
                    case AttributeStatement.EncryptedAttribute encrypted -> element
                            .appendChild(requiredFragment(document, encrypted.xml(), "SAML EncryptedAttribute"));
                }
            }
            return element;
        }

        /**
         * Writes one Attribute and its complete retained AttributeValue elements.
         *
         * @param document owning namespace-aware document
         * @param value    validated Attribute model
         * @return complete Attribute element
         */
        private static Element attribute(final org.w3c.dom.Document document, final Attribute value) {
            final Element element = assertion(document, Saml.Xml.ATTRIBUTE);
            element.setAttribute(Saml.Xml.NAME, value.name());
            optional(element, Saml.Xml.NAME_FORMAT, value.nameFormat().getOrNull());
            optional(element, Saml.Xml.FRIENDLY_NAME, value.friendlyName().getOrNull());
            value.attributes().forEach(
                    (name, text) -> element.setAttributeNS(
                            name.getNamespaceURI(),
                            qualified(name.getPrefix(), name.getLocalPart()),
                            text));
            for (byte[] item : value.values()) {
                element.appendChild(requiredFragment(document, item, "SAML AttributeValue"));
            }
            return element;
        }

        /**
         * Creates one protocol-namespace element.
         *
         * @param document  owning namespace-aware document
         * @param localName SAML protocol local name
         * @return new protocol-namespace element
         */
        private static Element protocol(final org.w3c.dom.Document document, final String localName) {
            return document.createElementNS(Saml.Namespaces.PROTOCOL, Saml.Xml.PROTOCOL_PREFIX + localName);
        }

        /**
         * Creates one assertion-namespace element.
         *
         * @param document  owning namespace-aware document
         * @param localName SAML assertion local name
         * @return new assertion-namespace element
         */
        private static Element assertion(final org.w3c.dom.Document document, final String localName) {
            return document.createElementNS(Saml.Namespaces.ASSERTION, Saml.Xml.ASSERTION_PREFIX + localName);
        }

        /**
         * Adds one optional attribute using the standard lexical representation of its value.
         *
         * @param element target element
         * @param name    unqualified attribute name
         * @param value   optional scalar value
         */
        private static void optional(final Element element, final String name, final Object value) {
            if (value != null) {
                element.setAttribute(name, value instanceof Instant instant ? instant.toString() : value.toString());
            }
        }

        /**
         * Appends a child only when the optional element is present.
         *
         * @param parent target parent element
         * @param child  optional child element
         */
        private static void append(final Element parent, final Element child) {
            if (child != null) {
                parent.appendChild(child);
            }
        }

        /**
         * Imports one optional complete XML element into the target document.
         *
         * @param target owning namespace-aware document
         * @param xml    optional complete XML element bytes
         * @return imported element or {@code null} when absent
         */
        private static Element fragment(final org.w3c.dom.Document target, final byte[] xml) {
            return xml == null ? null : requiredFragment(target, xml, "SAML XML fragment");
        }

        /**
         * Securely parses and imports one required complete XML element.
         *
         * @param target owning namespace-aware document
         * @param xml    required complete XML element bytes
         * @param label  diagnostic retained-element label
         * @return deeply imported element owned by the target document
         */
        private static Element requiredFragment(
                final org.w3c.dom.Document target,
                final byte[] xml,
                final String label) {
            try {
                final DocumentBuilderFactory factory = XXE.disableXXE(DocumentBuilderFactory.newInstance());
                factory.setNamespaceAware(true);
                factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                final Element source = factory.newDocumentBuilder()
                        .parse(new ByteArrayInputStream(Assert.notNull(xml, label + " must not be null")))
                        .getDocumentElement();
                return (Element) target.importNode(source, true);
            } catch (Exception exception) {
                throw new ValidateException(label + " is not a safe complete XML element", exception);
            }
        }

        /**
         * Builds a qualified XML name while preserving a present prefix.
         *
         * @param prefix    optional namespace prefix
         * @param localName XML local name
         * @return qualified name using the prefix when present
         */
        private static String qualified(final String prefix, final String localName) {
            return prefix == null || prefix.isEmpty() ? localName : prefix + Symbol.C_COLON + localName;
        }

    }

    /**
     * Converts validated namespace-aware DOM trees into immutable SAML models without normalizing retained XML.
     */
    private static final class SamlXmlReader {

        /**
         * Prevents construction of the reader namespace.
         */
        private SamlXmlReader() {
            // No initialization required.
        }

        /**
         * Reads one Authentication Request root.
         *
         * @param root validated AuthnRequest root element
         * @return immutable Authentication Request model
         */
        private static AuthnRequest authnRequest(final Element root) {
            allowedChildren(
                    root,
                    Set.of(
                            assertionName(Saml.Xml.ISSUER),
                            signatureName(Saml.Xml.SIGNATURE),
                            protocolName(Saml.Xml.EXTENSIONS),
                            assertionName(Saml.Xml.SUBJECT),
                            protocolName(Saml.Xml.NAME_ID_POLICY),
                            assertionName(Saml.Xml.CONDITIONS),
                            protocolName(Saml.Xml.REQUESTED_AUTHN_CONTEXT),
                            protocolName(Saml.Xml.SCOPING)));
            orderedChildren(
                    root,
                    List.of(
                            Set.of(assertionName(Saml.Xml.ISSUER)),
                            Set.of(signatureName(Saml.Xml.SIGNATURE)),
                            Set.of(protocolName(Saml.Xml.EXTENSIONS)),
                            Set.of(assertionName(Saml.Xml.SUBJECT)),
                            Set.of(protocolName(Saml.Xml.NAME_ID_POLICY)),
                            Set.of(assertionName(Saml.Xml.CONDITIONS)),
                            Set.of(protocolName(Saml.Xml.REQUESTED_AUTHN_CONTEXT)),
                            Set.of(protocolName(Saml.Xml.SCOPING))));
            return new AuthnRequest(required(root, Saml.Xml.ID), required(root, Saml.Xml.VERSION),
                    instant(required(root, Saml.Xml.ISSUE_INSTANT), "SAML AuthnRequest IssueInstant"),
                    optional(root, Saml.Xml.DESTINATION), optional(root, Saml.Xml.CONSENT),
                    optionalObject(child(root, Saml.Namespaces.ASSERTION, Saml.Xml.ISSUER), SamlXmlReader::issuer),
                    optionalXml(child(root, Saml.Namespaces.SIGNATURE, Saml.Xml.SIGNATURE)),
                    extensionChildren(child(root, Saml.Namespaces.PROTOCOL, Saml.Xml.EXTENSIONS)),
                    optionalObject(child(root, Saml.Namespaces.ASSERTION, Saml.Xml.SUBJECT), SamlXmlReader::subject),
                    optionalObject(
                            child(root, Saml.Namespaces.PROTOCOL, Saml.Xml.NAME_ID_POLICY),
                            SamlXmlReader::nameIdPolicy),
                    optionalObject(
                            child(root, Saml.Namespaces.ASSERTION, Saml.Xml.CONDITIONS),
                            SamlXmlReader::conditions),
                    optionalObject(
                            child(root, Saml.Namespaces.PROTOCOL, Saml.Xml.REQUESTED_AUTHN_CONTEXT),
                            SamlXmlReader::requestedAuthnContext),
                    optionalXml(child(root, Saml.Namespaces.PROTOCOL, Saml.Xml.SCOPING)),
                    optionalBoolean(root, Saml.Xml.FORCE_AUTHN), optionalBoolean(root, Saml.Xml.IS_PASSIVE),
                    optionalObject(
                            optional(root, Saml.Xml.PROTOCOL_BINDING).getOrNull(),
                            org.miaixz.bus.auth.protocol.saml.SamlBinding::new),
                    optionalInteger(root, Saml.Xml.ASSERTION_CONSUMER_SERVICE_INDEX),
                    optional(root, Saml.Xml.ASSERTION_CONSUMER_SERVICE_URL),
                    optionalInteger(root, Saml.Xml.ATTRIBUTE_CONSUMING_SERVICE_INDEX),
                    optional(root, Saml.Xml.PROVIDER_NAME));
        }

        /**
         * Reads one standard protocol Response root.
         *
         * @param root validated Response root element
         * @return immutable protocol Response model
         */
        private static Response response(final Element root) {
            allowedChildren(
                    root,
                    Set.of(
                            assertionName(Saml.Xml.ISSUER),
                            signatureName(Saml.Xml.SIGNATURE),
                            protocolName(Saml.Xml.EXTENSIONS),
                            protocolName(Saml.Xml.STATUS),
                            assertionName(Saml.Xml.ASSERTION),
                            assertionName(Saml.Xml.ENCRYPTED_ASSERTION)));
            orderedChildren(
                    root,
                    List.of(
                            Set.of(assertionName(Saml.Xml.ISSUER)),
                            Set.of(signatureName(Saml.Xml.SIGNATURE)),
                            Set.of(protocolName(Saml.Xml.EXTENSIONS)),
                            Set.of(protocolName(Saml.Xml.STATUS)),
                            Set.of(assertionName(Saml.Xml.ASSERTION), assertionName(Saml.Xml.ENCRYPTED_ASSERTION))));
            final List<Response.AssertionContent> assertions = new ArrayList<>();
            for (Element element : elements(root)) {
                if (matches(element, Saml.Namespaces.ASSERTION, Saml.Xml.ASSERTION)) {
                    assertions.add(new Response.PlainAssertion(assertion(element)));
                } else if (matches(element, Saml.Namespaces.ASSERTION, Saml.Xml.ENCRYPTED_ASSERTION)) {
                    assertions.add(new Response.EncryptedAssertion(xml(element)));
                }
            }
            return new Response(required(root, Saml.Xml.ID), optional(root, Saml.Xml.IN_RESPONSE_TO),
                    required(root, Saml.Xml.VERSION),
                    instant(required(root, Saml.Xml.ISSUE_INSTANT), "SAML Response IssueInstant"),
                    optional(root, Saml.Xml.DESTINATION), optional(root, Saml.Xml.CONSENT),
                    optionalObject(child(root, Saml.Namespaces.ASSERTION, Saml.Xml.ISSUER), SamlXmlReader::issuer),
                    optionalXml(child(root, Saml.Namespaces.SIGNATURE, Saml.Xml.SIGNATURE)),
                    extensionChildren(child(root, Saml.Namespaces.PROTOCOL, Saml.Xml.EXTENSIONS)),
                    status(requiredChild(root, Saml.Namespaces.PROTOCOL, Saml.Xml.STATUS)), assertions);
        }

        /**
         * Reads one standard Logout Request root.
         *
         * @param root validated LogoutRequest root element
         * @return immutable Logout Request model
         */
        private static LogoutRequest logoutRequest(final Element root) {
            allowedChildren(
                    root,
                    Set.of(
                            assertionName(Saml.Xml.ISSUER),
                            signatureName(Saml.Xml.SIGNATURE),
                            protocolName(Saml.Xml.EXTENSIONS),
                            assertionName(Saml.Xml.BASE_ID),
                            assertionName(Saml.Xml.NAME_ID),
                            assertionName(Saml.Xml.ENCRYPTED_ID),
                            protocolName(Saml.Xml.SESSION_INDEX)));
            orderedChildren(
                    root,
                    List.of(
                            Set.of(assertionName(Saml.Xml.ISSUER)),
                            Set.of(signatureName(Saml.Xml.SIGNATURE)),
                            Set.of(protocolName(Saml.Xml.EXTENSIONS)),
                            Set.of(
                                    assertionName(Saml.Xml.BASE_ID),
                                    assertionName(Saml.Xml.NAME_ID),
                                    assertionName(Saml.Xml.ENCRYPTED_ID)),
                            Set.of(protocolName(Saml.Xml.SESSION_INDEX))));
            final Subject.Identifier identifier = identifier(
                    requiredChoice(
                            root,
                            Set.of(
                                    assertionName(Saml.Xml.BASE_ID),
                                    assertionName(Saml.Xml.NAME_ID),
                                    assertionName(Saml.Xml.ENCRYPTED_ID))));
            final List<String> sessionIndexes = new ArrayList<>();
            for (Element element : children(root, Saml.Namespaces.PROTOCOL, Saml.Xml.SESSION_INDEX)) {
                sessionIndexes.add(element.getTextContent());
            }
            return new LogoutRequest(required(root, Saml.Xml.ID), required(root, Saml.Xml.VERSION),
                    instant(required(root, Saml.Xml.ISSUE_INSTANT), "SAML LogoutRequest IssueInstant"),
                    optional(root, Saml.Xml.DESTINATION), optional(root, Saml.Xml.CONSENT),
                    optionalObject(child(root, Saml.Namespaces.ASSERTION, Saml.Xml.ISSUER), SamlXmlReader::issuer),
                    optionalXml(child(root, Saml.Namespaces.SIGNATURE, Saml.Xml.SIGNATURE)),
                    extensionChildren(child(root, Saml.Namespaces.PROTOCOL, Saml.Xml.EXTENSIONS)), identifier,
                    sessionIndexes, optional(root, Saml.Xml.REASON),
                    optionalInstant(root, Saml.Xml.NOT_ON_OR_AFTER, "SAML LogoutRequest NotOnOrAfter"));
        }

        /**
         * Reads one standard Logout Response root.
         *
         * @param root validated LogoutResponse root element
         * @return immutable Logout Response model
         */
        private static LogoutResponse logoutResponse(final Element root) {
            allowedChildren(
                    root,
                    Set.of(
                            assertionName(Saml.Xml.ISSUER),
                            signatureName(Saml.Xml.SIGNATURE),
                            protocolName(Saml.Xml.EXTENSIONS),
                            protocolName(Saml.Xml.STATUS)));
            orderedChildren(
                    root,
                    List.of(
                            Set.of(assertionName(Saml.Xml.ISSUER)),
                            Set.of(signatureName(Saml.Xml.SIGNATURE)),
                            Set.of(protocolName(Saml.Xml.EXTENSIONS)),
                            Set.of(protocolName(Saml.Xml.STATUS))));
            return new LogoutResponse(required(root, Saml.Xml.ID), optional(root, Saml.Xml.IN_RESPONSE_TO),
                    required(root, Saml.Xml.VERSION),
                    instant(required(root, Saml.Xml.ISSUE_INSTANT), "SAML LogoutResponse IssueInstant"),
                    optional(root, Saml.Xml.DESTINATION), optional(root, Saml.Xml.CONSENT),
                    optionalObject(child(root, Saml.Namespaces.ASSERTION, Saml.Xml.ISSUER), SamlXmlReader::issuer),
                    optionalXml(child(root, Saml.Namespaces.SIGNATURE, Saml.Xml.SIGNATURE)),
                    extensionChildren(child(root, Saml.Namespaces.PROTOCOL, Saml.Xml.EXTENSIONS)),
                    status(requiredChild(root, Saml.Namespaces.PROTOCOL, Saml.Xml.STATUS)));
        }

        /**
         * Reads one Assertion element and preserves statement ordering.
         *
         * @param root validated Assertion element
         * @return immutable Assertion model
         */
        private static Assertion assertion(final Element root) {
            require(root, Saml.Namespaces.ASSERTION, Saml.Xml.ASSERTION);
            allowedChildren(
                    root,
                    Set.of(
                            assertionName(Saml.Xml.ISSUER),
                            signatureName(Saml.Xml.SIGNATURE),
                            assertionName(Saml.Xml.SUBJECT),
                            assertionName(Saml.Xml.CONDITIONS),
                            assertionName(Saml.Xml.ADVICE),
                            assertionName(Saml.Xml.AUTHN_STATEMENT),
                            assertionName(Saml.Xml.ATTRIBUTE_STATEMENT),
                            assertionName(Saml.Xml.AUTHZ_DECISION_STATEMENT),
                            new QName(Saml.Namespaces.ASSERTION, Saml.Xml.STATEMENT)));
            orderedChildren(
                    root,
                    List.of(
                            Set.of(assertionName(Saml.Xml.ISSUER)),
                            Set.of(signatureName(Saml.Xml.SIGNATURE)),
                            Set.of(assertionName(Saml.Xml.SUBJECT)),
                            Set.of(assertionName(Saml.Xml.CONDITIONS)),
                            Set.of(assertionName(Saml.Xml.ADVICE)),
                            Set.of(
                                    assertionName(Saml.Xml.AUTHN_STATEMENT),
                                    assertionName(Saml.Xml.ATTRIBUTE_STATEMENT),
                                    assertionName(Saml.Xml.AUTHZ_DECISION_STATEMENT),
                                    assertionName(Saml.Xml.STATEMENT))));
            final List<Assertion.StatementContent> statements = new ArrayList<>();
            for (Element element : elements(root)) {
                if (matches(element, Saml.Namespaces.ASSERTION, Saml.Xml.AUTHN_STATEMENT)) {
                    statements.add(new Assertion.AuthenticationStatement(authnStatement(element)));
                } else if (matches(element, Saml.Namespaces.ASSERTION, Saml.Xml.ATTRIBUTE_STATEMENT)) {
                    statements.add(new Assertion.AttributesStatement(attributeStatement(element)));
                } else if (matches(element, Saml.Namespaces.ASSERTION, Saml.Xml.AUTHZ_DECISION_STATEMENT)
                        || matches(element, Saml.Namespaces.ASSERTION, Saml.Xml.STATEMENT)) {
                    statements.add(new Assertion.OtherStatement(xml(element)));
                }
            }
            return new Assertion(required(root, Saml.Xml.VERSION), required(root, Saml.Xml.ID),
                    instant(required(root, Saml.Xml.ISSUE_INSTANT), "SAML Assertion IssueInstant"),
                    issuer(requiredChild(root, Saml.Namespaces.ASSERTION, Saml.Xml.ISSUER)),
                    optionalXml(child(root, Saml.Namespaces.SIGNATURE, Saml.Xml.SIGNATURE)),
                    optionalObject(child(root, Saml.Namespaces.ASSERTION, Saml.Xml.SUBJECT), SamlXmlReader::subject),
                    optionalObject(
                            child(root, Saml.Namespaces.ASSERTION, Saml.Xml.CONDITIONS),
                            SamlXmlReader::conditions),
                    optionalXml(child(root, Saml.Namespaces.ASSERTION, Saml.Xml.ADVICE)), statements);
        }

        /**
         * Reads an assertion Issuer.
         *
         * @param element validated Issuer element
         * @return immutable Issuer model
         */
        private static Issuer issuer(final Element element) {
            return new Issuer(nameId(element));
        }

        /**
         * Reads a NameIDType element with any legal owning local name.
         *
         * @param element validated NameIDType element
         * @return immutable NameID model
         */
        private static NameID nameId(final Element element) {
            return new NameID(element.getTextContent(), optional(element, Saml.Xml.NAME_QUALIFIER),
                    optional(element, Saml.Xml.SP_NAME_QUALIFIER), optional(element, Saml.Xml.FORMAT),
                    optional(element, Saml.Xml.SP_PROVIDED_ID));
        }

        /**
         * Reads one SAML Subject and its identifier choice.
         *
         * @param element validated Subject element
         * @return immutable Subject model
         */
        private static Subject subject(final Element element) {
            allowedChildren(
                    element,
                    Set.of(
                            assertionName(Saml.Xml.BASE_ID),
                            assertionName(Saml.Xml.NAME_ID),
                            assertionName(Saml.Xml.ENCRYPTED_ID),
                            assertionName(Saml.Xml.SUBJECT_CONFIRMATION)));
            final Element choice = firstChoice(
                    element,
                    Set.of(
                            assertionName(Saml.Xml.BASE_ID),
                            assertionName(Saml.Xml.NAME_ID),
                            assertionName(Saml.Xml.ENCRYPTED_ID)));
            final List<SubjectConfirmation> confirmations = new ArrayList<>();
            for (Element child : children(element, Saml.Namespaces.ASSERTION, Saml.Xml.SUBJECT_CONFIRMATION)) {
                confirmations.add(subjectConfirmation(child));
            }
            return new Subject(choice == null ? Optional.empty() : Optional.of(identifier(choice)), confirmations);
        }

        /**
         * Reads one subject identifier choice.
         *
         * @param element validated NameID, BaseID, or EncryptedID element
         * @return immutable selected Subject identifier model
         */
        private static Subject.Identifier identifier(final Element element) {
            if (matches(element, Saml.Namespaces.ASSERTION, Saml.Xml.NAME_ID)) {
                return new Subject.NamedIdentifier(nameId(element));
            }
            if (matches(element, Saml.Namespaces.ASSERTION, Saml.Xml.BASE_ID)) {
                return new Subject.BaseIdentifier(xml(element));
            }
            if (matches(element, Saml.Namespaces.ASSERTION, Saml.Xml.ENCRYPTED_ID)) {
                return new Subject.EncryptedIdentifier(xml(element));
            }
            throw new ValidateException("Unsupported SAML Subject identifier element");
        }

        /**
         * Reads one SubjectConfirmation.
         *
         * @param element validated SubjectConfirmation element
         * @return immutable SubjectConfirmation model
         */
        private static SubjectConfirmation subjectConfirmation(final Element element) {
            allowedChildren(
                    element,
                    Set.of(
                            assertionName(Saml.Xml.BASE_ID),
                            assertionName(Saml.Xml.NAME_ID),
                            assertionName(Saml.Xml.ENCRYPTED_ID),
                            assertionName(Saml.Xml.SUBJECT_CONFIRMATION_DATA)));
            final Element choice = firstChoice(
                    element,
                    Set.of(
                            assertionName(Saml.Xml.BASE_ID),
                            assertionName(Saml.Xml.NAME_ID),
                            assertionName(Saml.Xml.ENCRYPTED_ID)));
            return new SubjectConfirmation(choice == null ? Optional.empty() : Optional.of(identifier(choice)),
                    optionalObject(
                            child(element, Saml.Namespaces.ASSERTION, Saml.Xml.SUBJECT_CONFIRMATION_DATA),
                            SamlXmlReader::subjectConfirmationData),
                    required(element, Saml.Xml.METHOD));
        }

        /**
         * Reads one SubjectConfirmationData element including foreign content.
         *
         * @param element validated SubjectConfirmationData element
         * @return immutable SubjectConfirmationData model
         */
        private static SubjectConfirmationData subjectConfirmationData(final Element element) {
            final List<byte[]> content = new ArrayList<>();
            for (Element child : elements(element)) {
                content.add(xml(child));
            }
            return new SubjectConfirmationData(
                    optionalInstant(element, Saml.Xml.NOT_BEFORE, "SAML SubjectConfirmationData NotBefore"),
                    optionalInstant(element, Saml.Xml.NOT_ON_OR_AFTER, "SAML SubjectConfirmationData NotOnOrAfter"),
                    optional(element, Saml.Xml.RECIPIENT), optional(element, Saml.Xml.IN_RESPONSE_TO),
                    optional(element, Saml.Xml.ADDRESS), content,
                    foreignAttributes(
                            element,
                            Set.of(
                                    Saml.Xml.NOT_BEFORE,
                                    Saml.Xml.NOT_ON_OR_AFTER,
                                    Saml.Xml.RECIPIENT,
                                    Saml.Xml.IN_RESPONSE_TO,
                                    Saml.Xml.ADDRESS)));
        }

        /**
         * Reads one NameIDPolicy element.
         *
         * @param element validated NameIDPolicy element
         * @return immutable NameIDPolicy model
         */
        private static NameIDPolicy nameIdPolicy(final Element element) {
            return new NameIDPolicy(optional(element, Saml.Xml.FORMAT), optional(element, Saml.Xml.SP_NAME_QUALIFIER),
                    optionalBoolean(element, Saml.Xml.ALLOW_CREATE));
        }

        /**
         * Reads assertion Conditions and their supported condition choice.
         *
         * @param element validated Conditions element
         * @return immutable Conditions model preserving choice order
         */
        private static Conditions conditions(final Element element) {
            final List<Conditions.Condition> conditions = new ArrayList<>();
            for (Element child : elements(element)) {
                if (matches(child, Saml.Namespaces.ASSERTION, Saml.Xml.AUDIENCE_RESTRICTION)) {
                    conditions.add(new Conditions.Audience(audienceRestriction(child)));
                } else if (matches(child, Saml.Namespaces.ASSERTION, Saml.Xml.ONE_TIME_USE)) {
                    conditions.add(new Conditions.OneTimeUse());
                } else if (matches(child, Saml.Namespaces.ASSERTION, Saml.Xml.PROXY_RESTRICTION)) {
                    conditions.add(proxyRestriction(child));
                } else {
                    conditions.add(new Conditions.Extension(xml(child)));
                }
            }
            return new Conditions(optionalInstant(element, Saml.Xml.NOT_BEFORE, "SAML Conditions NotBefore"),
                    optionalInstant(element, Saml.Xml.NOT_ON_OR_AFTER, "SAML Conditions NotOnOrAfter"), conditions);
        }

        /**
         * Reads one AudienceRestriction condition.
         *
         * @param element validated AudienceRestriction element
         * @return immutable AudienceRestriction model
         */
        private static AudienceRestriction audienceRestriction(final Element element) {
            final List<String> audiences = new ArrayList<>();
            for (Element child : children(element, Saml.Namespaces.ASSERTION, Saml.Xml.AUDIENCE)) {
                audiences.add(child.getTextContent());
            }
            return new AudienceRestriction(audiences);
        }

        /**
         * Reads one ProxyRestriction condition.
         *
         * @param element validated ProxyRestriction element
         * @return immutable ProxyRestriction model
         */
        private static Conditions.ProxyRestriction proxyRestriction(final Element element) {
            final List<String> audiences = new ArrayList<>();
            for (Element child : children(element, Saml.Namespaces.ASSERTION, Saml.Xml.AUDIENCE)) {
                audiences.add(child.getTextContent());
            }
            return new Conditions.ProxyRestriction(optionalInteger(element, Saml.Xml.COUNT), audiences);
        }

        /**
         * Reads one RequestedAuthnContext reference choice.
         *
         * @param element validated RequestedAuthnContext element
         * @return immutable RequestedAuthnContext model
         */
        private static RequestedAuthnContext requestedAuthnContext(final Element element) {
            final List<RequestedAuthnContext.ClassReference> classes = new ArrayList<>();
            final List<RequestedAuthnContext.DeclarationReference> declarations = new ArrayList<>();
            for (Element child : elements(element)) {
                if (matches(child, Saml.Namespaces.ASSERTION, Saml.Xml.AUTHN_CONTEXT_CLASS_REF)) {
                    classes.add(
                            new RequestedAuthnContext.ClassReference(
                                    absoluteUri(child.getTextContent(), "SAML AuthnContextClassRef")));
                } else if (matches(child, Saml.Namespaces.ASSERTION, Saml.Xml.AUTHN_CONTEXT_DECL_REF)) {
                    declarations.add(
                            new RequestedAuthnContext.DeclarationReference(
                                    absoluteUri(child.getTextContent(), "SAML AuthnContextDeclRef")));
                } else {
                    throw new ValidateException("Unsupported RequestedAuthnContext child element");
                }
            }
            return new RequestedAuthnContext(classes, declarations,
                    optional(element, Saml.Xml.COMPARISON).map(SamlXmlReader::comparison));
        }

        /**
         * Resolves one standard RequestedAuthnContext comparison lexical value.
         *
         * @param value exact XML attribute value
         * @return matching constrained comparison
         * @throws ValidateException if the value is not registered
         */
        private static RequestedAuthnContext.Comparison comparison(final String value) {
            return Arrays.stream(RequestedAuthnContext.Comparison.values())
                    .filter(candidate -> candidate.value().equals(value)).findFirst()
                    .orElseThrow(() -> new ValidateException("SAML RequestedAuthnContext Comparison is invalid"));
        }

        /**
         * Parses one absolute XML URI attribute or element value.
         *
         * @param value exact XML lexical value
         * @param label safe diagnostic label
         * @return absolute URI
         * @throws ValidateException if the value is not an absolute URI
         */
        private static java.net.URI absoluteUri(final String value, final String label) {
            try {
                final URI uri = new URI(Assert.notBlank(value, label + " must not be blank"));
                if (!uri.isAbsolute()) {
                    throw new ValidateException(label + " must be an absolute URI");
                }
                return uri;
            } catch (java.net.URISyntaxException exception) {
                throw new ValidateException(label + " must be a valid URI", exception);
            }
        }

        /**
         * Reads one standard Status tree.
         *
         * @param element validated Status element
         * @return immutable Status model
         */
        private static Status status(final Element element) {
            allowedChildren(
                    element,
                    Set.of(
                            protocolName(Saml.Xml.STATUS_CODE),
                            protocolName(Saml.Xml.STATUS_MESSAGE),
                            protocolName(Saml.Xml.STATUS_DETAIL)));
            final Element message = child(element, Saml.Namespaces.PROTOCOL, Saml.Xml.STATUS_MESSAGE);
            final Element detail = child(element, Saml.Namespaces.PROTOCOL, Saml.Xml.STATUS_DETAIL);
            return new Status(statusCode(requiredChild(element, Saml.Namespaces.PROTOCOL, Saml.Xml.STATUS_CODE)),
                    message == null ? Optional.empty()
                            : Optional
                                    .of(new org.miaixz.bus.auth.protocol.saml.StatusMessage(message.getTextContent())),
                    detail == null ? Optional.empty()
                            : Optional
                                    .of(new org.miaixz.bus.auth.protocol.saml.StatusDetail(extensionChildren(detail))));
        }

        /**
         * Reads one recursively nested StatusCode.
         *
         * @param element validated StatusCode element
         * @return immutable recursively nested StatusCode model
         */
        private static org.miaixz.bus.auth.protocol.saml.StatusCode statusCode(final Element element) {
            final Element nested = child(element, Saml.Namespaces.PROTOCOL, Saml.Xml.STATUS_CODE);
            return new org.miaixz.bus.auth.protocol.saml.StatusCode(required(element, Saml.Xml.VALUE),
                    nested == null ? Optional.empty() : Optional.of(statusCode(nested)));
        }

        /**
         * Reads one AuthnStatement and its required AuthnContext.
         *
         * @param element validated AuthnStatement element
         * @return immutable AuthnStatement model
         */
        private static AuthnStatement authnStatement(final Element element) {
            final Element locality = child(element, Saml.Namespaces.ASSERTION, Saml.Xml.SUBJECT_LOCALITY);
            return new AuthnStatement(
                    instant(required(element, Saml.Xml.AUTHN_INSTANT), "SAML AuthnStatement AuthnInstant"),
                    optional(element, Saml.Xml.SESSION_INDEX),
                    optionalInstant(
                            element,
                            Saml.Xml.SESSION_NOT_ON_OR_AFTER,
                            "SAML AuthnStatement SessionNotOnOrAfter"),
                    locality == null ? Optional.empty() : optional(locality, Saml.Xml.ADDRESS),
                    locality == null ? Optional.empty() : optional(locality, Saml.Xml.DNS_NAME),
                    authnContext(requiredChild(element, Saml.Namespaces.ASSERTION, Saml.Xml.AUTHN_CONTEXT)));
        }

        /**
         * Reads one AuthnContext declaration choice.
         *
         * @param element validated AuthnContext element
         * @return immutable AuthnContext model
         */
        private static AuthnContext authnContext(final Element element) {
            final Element classReference = child(element, Saml.Namespaces.ASSERTION, Saml.Xml.AUTHN_CONTEXT_CLASS_REF);
            final Element declaration = child(element, Saml.Namespaces.ASSERTION, Saml.Xml.AUTHN_CONTEXT_DECL);
            final Element declarationReference = child(
                    element,
                    Saml.Namespaces.ASSERTION,
                    Saml.Xml.AUTHN_CONTEXT_DECL_REF);
            final List<String> authorities = new ArrayList<>();
            for (Element authority : children(element, Saml.Namespaces.ASSERTION, Saml.Xml.AUTHENTICATING_AUTHORITY)) {
                authorities.add(authority.getTextContent());
            }
            return new AuthnContext(
                    classReference == null ? Optional.empty() : Optional.of(classReference.getTextContent()),
                    optionalXml(declaration), declarationReference == null ? Optional.empty()
                            : Optional.of(declarationReference.getTextContent()),
                    authorities);
        }

        /**
         * Reads one AttributeStatement and preserves its repeated choice.
         *
         * @param element validated AttributeStatement element
         * @return immutable AttributeStatement model
         */
        private static AttributeStatement attributeStatement(final Element element) {
            final List<AttributeStatement.AttributeContent> attributes = new ArrayList<>();
            for (Element child : elements(element)) {
                if (matches(child, Saml.Namespaces.ASSERTION, Saml.Xml.ATTRIBUTE)) {
                    attributes.add(new AttributeStatement.PlainAttribute(attribute(child)));
                } else if (matches(child, Saml.Namespaces.ASSERTION, Saml.Xml.ENCRYPTED_ATTRIBUTE)) {
                    attributes.add(new AttributeStatement.EncryptedAttribute(xml(child)));
                } else {
                    throw new ValidateException("Unsupported SAML AttributeStatement child element");
                }
            }
            return new AttributeStatement(attributes);
        }

        /**
         * Reads one Attribute with complete AttributeValue elements.
         *
         * @param element validated Attribute element
         * @return immutable Attribute model
         */
        private static Attribute attribute(final Element element) {
            final List<byte[]> values = new ArrayList<>();
            for (Element child : children(element, Saml.Namespaces.ASSERTION, Saml.Xml.ATTRIBUTE_VALUE)) {
                values.add(xml(child));
            }
            return new Attribute(required(element, Saml.Xml.NAME), optional(element, Saml.Xml.NAME_FORMAT),
                    optional(element, Saml.Xml.FRIENDLY_NAME),
                    foreignAttributes(element, Set.of(Saml.Xml.NAME, Saml.Xml.NAME_FORMAT, Saml.Xml.FRIENDLY_NAME)),
                    values);
        }

        /**
         * Rejects direct child elements outside the permitted expanded-name set.
         *
         * @param parent  element whose direct children are validated
         * @param allowed permitted child expanded names
         */
        private static void allowedChildren(final Element parent, final Set<QName> allowed) {
            for (Element child : elements(parent)) {
                final QName name = new QName(child.getNamespaceURI(), child.getLocalName());
                if (!allowed.contains(name)) {
                    throw new ValidateException("Unexpected SAML child element " + name);
                }
            }
        }

        /**
         * Enforces non-decreasing schema-group order for direct child elements.
         *
         * @param parent element whose direct children are checked
         * @param groups ordered expanded-name groups; names in the same group may repeat in wire order
         */
        private static void orderedChildren(final Element parent, final List<Set<QName>> groups) {
            int previous = -1;
            for (Element child : elements(parent)) {
                final QName name = new QName(child.getNamespaceURI(), child.getLocalName());
                int current = -1;
                for (int index = 0; index < groups.size(); index++) {
                    if (groups.get(index).contains(name)) {
                        current = index;
                        break;
                    }
                }
                if (current < previous) {
                    throw new ValidateException("SAML child element is outside its schema sequence: " + name);
                }
                previous = current;
            }
        }

        /**
         * Returns all direct child elements in document order.
         *
         * @param parent parent element
         * @return mutable traversal list of direct element children in wire order
         */
        private static List<Element> elements(final Element parent) {
            final List<Element> result = new ArrayList<>();
            final NodeList nodes = parent.getChildNodes();
            for (int index = 0; index < nodes.getLength(); index++) {
                if (nodes.item(index) instanceof Element element) {
                    result.add(element);
                }
            }
            return result;
        }

        /**
         * Returns direct children with one exact expanded name.
         *
         * @param parent    parent element
         * @param namespace required namespace URI
         * @param localName required local name
         * @return matching direct children in wire order
         */
        private static List<Element> children(final Element parent, final String namespace, final String localName) {
            final List<Element> result = new ArrayList<>();
            for (Element element : elements(parent)) {
                if (matches(element, namespace, localName)) {
                    result.add(element);
                }
            }
            return result;
        }

        /**
         * Returns at most one direct child with an exact expanded name.
         *
         * @param parent    parent element
         * @param namespace required namespace URI
         * @param localName required local name
         * @return matching singleton child or {@code null} when absent
         */
        private static Element child(final Element parent, final String namespace, final String localName) {
            final List<Element> matches = children(parent, namespace, localName);
            if (matches.size() > 1) {
                throw new ValidateException("SAML element contains duplicate singleton child " + localName);
            }
            return matches.isEmpty() ? null : matches.get(0);
        }

        /**
         * Returns one required singleton child.
         *
         * @param parent    parent element
         * @param namespace required namespace URI
         * @param localName required local name
         * @return required singleton child
         */
        private static Element requiredChild(final Element parent, final String namespace, final String localName) {
            final Element value = child(parent, namespace, localName);
            if (value == null) {
                throw new ValidateException("SAML element requires child " + localName);
            }
            return value;
        }

        /**
         * Returns the first optional member of one element choice and rejects multiple members.
         *
         * @param parent  parent element
         * @param choices permitted choice expanded names
         * @return selected choice element or {@code null} when absent
         */
        private static Element firstChoice(final Element parent, final Set<QName> choices) {
            Element selected = null;
            for (Element element : elements(parent)) {
                if (choices.contains(new QName(element.getNamespaceURI(), element.getLocalName()))) {
                    if (selected != null) {
                        throw new ValidateException("SAML element contains multiple members of a singleton choice");
                    }
                    selected = element;
                }
            }
            return selected;
        }

        /**
         * Returns the required member of one singleton choice.
         *
         * @param parent  parent element
         * @param choices permitted choice expanded names
         * @return required selected choice element
         */
        private static Element requiredChoice(final Element parent, final Set<QName> choices) {
            final Element selected = firstChoice(parent, choices);
            if (selected == null) {
                throw new ValidateException("SAML element omits a required choice member");
            }
            return selected;
        }

        /**
         * Reads one required non-empty XML attribute.
         *
         * @param element owning element
         * @param name    unqualified attribute name
         * @return required non-empty lexical value
         */
        private static String required(final Element element, final String name) {
            if (!element.hasAttribute(name)) {
                throw new ValidateException("SAML element requires attribute " + name);
            }
            return Assert.notEmpty(element.getAttribute(name), "SAML attribute " + name + " must not be empty");
        }

        /**
         * Reads one optional XML attribute while preserving empty schema values.
         *
         * @param element owning element
         * @param name    unqualified attribute name
         * @return optional lexical value
         */
        private static Optional<String> optional(final Element element, final String name) {
            return element.hasAttribute(name) ? Optional.of(element.getAttribute(name)) : Optional.empty();
        }

        /**
         * Reads one optional boolean attribute with exact XML Schema lexical values.
         *
         * @param element owning element
         * @param name    unqualified boolean attribute name
         * @return optional decoded boolean
         */
        private static Optional<Boolean> optionalBoolean(final Element element, final String name) {
            if (!element.hasAttribute(name)) {
                return Optional.empty();
            }
            return switch (element.getAttribute(name)) {
                case Normal.TRUE, Symbol.ONE -> Optional.of(Boolean.TRUE);
                case Normal.FALSE, Symbol.ZERO -> Optional.of(Boolean.FALSE);
                default -> throw new ValidateException("SAML boolean attribute " + name + " is invalid");
            };
        }

        /**
         * Reads one optional integer attribute.
         *
         * @param element owning element
         * @param name    unqualified integer attribute name
         * @return optional decoded integer
         */
        private static Optional<Integer> optionalInteger(final Element element, final String name) {
            if (!element.hasAttribute(name)) {
                return Optional.empty();
            }
            try {
                return Optional.of(Integer.valueOf(element.getAttribute(name)));
            } catch (NumberFormatException exception) {
                throw new ValidateException("SAML integer attribute " + name + " is invalid", exception);
            }
        }

        /**
         * Reads one optional XML dateTime attribute.
         *
         * @param element owning element
         * @param name    unqualified dateTime attribute name
         * @param label   safe diagnostic field label
         * @return optional decoded instant
         */
        private static Optional<Instant> optionalInstant(final Element element, final String name, final String label) {
            return element.hasAttribute(name) ? Optional.of(instant(element.getAttribute(name), label))
                    : Optional.empty();
        }

        /**
         * Maps one optional value without introducing Optional.
         *
         * @param <S>    source value type
         * @param <T>    mapped value type
         * @param value  nullable source value
         * @param mapper mapper invoked only for a present source
         * @return empty or mapped Bus optional
         */
        private static <S, T> Optional<T> optionalObject(final S value, final Mapper<S, T> mapper) {
            return value == null ? Optional.empty() : Optional.of(mapper.map(value));
        }

        /**
         * Retains one optional complete XML element.
         *
         * @param element optional DOM element
         * @return optional deterministic serialized element bytes
         */
        private static Optional<byte[]> optionalXml(final Element element) {
            return element == null ? Optional.empty() : Optional.of(xml(element));
        }

        /**
         * Serializes one retained DOM element without an XML declaration.
         *
         * @param element DOM element to retain
         * @return deterministic UTF-8 element bytes
         */
        private static byte[] xml(final Element element) {
            return serialize(element);
        }

        /**
         * Retains all direct element children of an extension wrapper.
         *
         * @param wrapper optional Extensions or StatusDetail wrapper
         * @return immutable serialized child elements in wire order
         */
        private static List<byte[]> extensionChildren(final Element wrapper) {
            if (wrapper == null) {
                return List.of();
            }
            final List<byte[]> result = new ArrayList<>();
            for (Element child : elements(wrapper)) {
                result.add(xml(child));
            }
            return List.copyOf(result);
        }

        /**
         * Copies foreign attributes while excluding known unqualified attributes and namespace declarations.
         *
         * @param element element containing standard and wildcard attributes
         * @param known   recognized unqualified attribute names
         * @return immutable insertion-ordered foreign attribute map
         */
        private static Map<QName, String> foreignAttributes(final Element element, final Set<String> known) {
            final Map<QName, String> result = new LinkedHashMap<>();
            final NamedNodeMap attributes = element.getAttributes();
            for (int index = 0; index < attributes.getLength(); index++) {
                final Attr attribute = (Attr) attributes.item(index);
                if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(attribute.getNamespaceURI())
                        || (attribute.getNamespaceURI() == null && known.contains(attribute.getName()))) {
                    continue;
                }
                final String namespace = attribute.getNamespaceURI();
                if (namespace == null || namespace.isEmpty()) {
                    throw new ValidateException("Unexpected unqualified SAML attribute " + attribute.getName());
                }
                result.put(
                        new QName(namespace, attribute.getLocalName(),
                                attribute.getPrefix() == null ? Normal.EMPTY : attribute.getPrefix()),
                        attribute.getValue());
            }
            return Collections.unmodifiableMap(result);
        }

        /**
         * Tests one element expanded name.
         *
         * @param element   candidate element
         * @param namespace required namespace URI
         * @param localName required local name
         * @return whether both namespace and local name match
         */
        private static boolean matches(final Element element, final String namespace, final String localName) {
            return namespace.equals(element.getNamespaceURI()) && localName.equals(element.getLocalName());
        }

        /**
         * Creates one assertion expanded name.
         *
         * @param localName assertion local name
         * @return assertion-namespace QName
         */
        private static QName assertionName(final String localName) {
            return new QName(Saml.Namespaces.ASSERTION, localName);
        }

        /**
         * Creates one protocol expanded name.
         *
         * @param localName protocol local name
         * @return protocol-namespace QName
         */
        private static QName protocolName(final String localName) {
            return new QName(Saml.Namespaces.PROTOCOL, localName);
        }

        /**
         * Creates one XML Signature expanded name.
         *
         * @param localName XML Signature local name
         * @return XML Signature namespace QName
         */
        private static QName signatureName(final String localName) {
            return new QName(Saml.Namespaces.SIGNATURE, localName);
        }

    }

}
