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
import java.security.cert.CertificateEncodingException;
import java.time.OffsetDateTime;
import java.util.*;

import javax.xml.XMLConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;

import org.miaixz.bus.auth.FabricX.Response;
import org.miaixz.bus.auth.protocol.saml.*;
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.xml.XXE;
import org.miaixz.bus.crypto.builtin.CertificateChain;

/**
 * Encodes and securely decodes the SAML 2.0 Metadata fields consumed and emitted by the SAML runtime.
 * <p>
 * The codec accepts one {@code EntityDescriptor} containing SAML 2.0 IdP or SP SSO roles. It rejects aggregate
 * metadata, artifact services, unsupported bindings, duplicate XML IDs, unsafe XML features, and schema-order
 * violations. The original document bytes and its unique ID inventory are retained for signature validation.
 * </p>
 *
 * @author Kimi Liu
 */
public class MetadataCodec {

    /**
     * SAML 2.0 Metadata namespace.
     */
    public static final String METADATA_NAMESPACE = Saml.Namespaces.METADATA;

    /**
     * W3C XML Signature namespace.
     */
    public static final String SIGNATURE_NAMESPACE = Saml.Namespaces.SIGNATURE;

    /**
     * Maximum accepted metadata document byte length.
     */
    private final long maximumBytes;

    /**
     * Maximum accepted metadata element nesting depth.
     */
    private final int maximumDepth;

    /**
     * Creates a strict SAML Metadata codec with explicit parsing limits.
     *
     * @param maximumBytes positive maximum metadata byte length
     * @param maximumDepth positive maximum metadata element nesting depth
     * @throws ValidateException if either limit is not positive
     */
    public MetadataCodec(final long maximumBytes, final int maximumDepth) {
        if (maximumBytes <= 0L) {
            throw new ValidateException("SAML Metadata maximum byte length must be positive");
        }
        if (maximumDepth <= 0) {
            throw new ValidateException("SAML Metadata maximum depth must be positive");
        }
        this.maximumBytes = maximumBytes;
        this.maximumDepth = maximumDepth;
    }

    /**
     * Reads and validates an implemented SAML binding URI.
     *
     * @param root metadata endpoint element
     * @return HTTP-Redirect or HTTP-POST binding
     */
    private static SamlBinding binding(final Element root) {
        final SamlBinding binding = new SamlBinding(required(root, "Binding"));
        if (!binding.supported()) {
            throw new ValidateException("SAML Metadata endpoint uses an unsupported binding: " + binding.value());
        }
        return binding;
    }

    /**
     * Retains wildcard endpoint children while rejecting Metadata namespace services.
     *
     * @param root endpoint element
     * @return ordered foreign child XML elements
     */
    private static List<byte[]> endpointChildren(final Element root) {
        final List<byte[]> result = new ArrayList<>();
        for (Element child : children(root)) {
            if (METADATA_NAMESPACE.equals(child.getNamespaceURI())) {
                throw unsupported(child, root.getLocalName());
            }
            result.add(fragment(child));
        }
        return result;
    }

    /**
     * Writes an IdP role in SAML Metadata schema order.
     *
     * @param document owner DOM document
     * @param value    typed IdP role
     * @return role element
     */
    private static Element writeIdentityProvider(final org.w3c.dom.Document document, final IdpSsoDescriptor value) {
        final Element root = metadata(document, "IDPSSODescriptor");
        roleAttributes(
                root,
                value.id(),
                value.validUntil(),
                value.cacheDuration(),
                value.protocolSupportEnumeration(),
                value.errorUrl());
        optional(root, "WantAuthnRequestsSigned", lexical(value.wantAuthnRequestsSigned()));
        appendOptional(root, value.signature(), SIGNATURE_NAMESPACE, "Signature");
        extensions(root, value.extensions());
        for (KeyDescriptor key : value.keys())
            root.appendChild(writeKey(document, key));
        appendOptional(root, value.organization(), METADATA_NAMESPACE, "Organization");
        appendAll(root, value.contacts(), METADATA_NAMESPACE, "ContactPerson", false);
        for (SingleLogoutServiceEndpoint endpoint : value.singleLogoutServices()) {
            root.appendChild(
                    writeEndpoint(
                            document,
                            "SingleLogoutService",
                            endpoint.binding(),
                            endpoint.location(),
                            endpoint.responseLocation(),
                            endpoint.extensions(),
                            endpoint.attributes(),
                            null,
                            Optional.empty()));
        }
        textElements(root, "NameIDFormat", value.nameIdFormats());
        for (SingleSignOnServiceEndpoint endpoint : value.singleSignOnServices()) {
            root.appendChild(
                    writeEndpoint(
                            document,
                            "SingleSignOnService",
                            endpoint.binding(),
                            endpoint.location(),
                            endpoint.responseLocation(),
                            endpoint.extensions(),
                            endpoint.attributes(),
                            null,
                            Optional.empty()));
        }
        return root;
    }

    /**
     * Writes an SP role in SAML Metadata schema order.
     *
     * @param document owner DOM document
     * @param value    typed SP role
     * @return role element
     */
    private static Element writeServiceProvider(final org.w3c.dom.Document document, final SpSsoDescriptor value) {
        final Element root = metadata(document, "SPSSODescriptor");
        roleAttributes(
                root,
                value.id(),
                value.validUntil(),
                value.cacheDuration(),
                value.protocolSupportEnumeration(),
                value.errorUrl());
        optional(root, "AuthnRequestsSigned", lexical(value.authnRequestsSigned()));
        optional(root, "WantAssertionsSigned", lexical(value.wantAssertionsSigned()));
        appendOptional(root, value.signature(), SIGNATURE_NAMESPACE, "Signature");
        extensions(root, value.extensions());
        for (KeyDescriptor key : value.keys())
            root.appendChild(writeKey(document, key));
        appendOptional(root, value.organization(), METADATA_NAMESPACE, "Organization");
        appendAll(root, value.contacts(), METADATA_NAMESPACE, "ContactPerson", false);
        for (SingleLogoutServiceEndpoint endpoint : value.singleLogoutServices()) {
            root.appendChild(
                    writeEndpoint(
                            document,
                            "SingleLogoutService",
                            endpoint.binding(),
                            endpoint.location(),
                            endpoint.responseLocation(),
                            endpoint.extensions(),
                            endpoint.attributes(),
                            null,
                            Optional.empty()));
        }
        textElements(root, "NameIDFormat", value.nameIdFormats());
        for (AssertionConsumerServiceEndpoint endpoint : value.assertionConsumerServices()) {
            root.appendChild(
                    writeEndpoint(
                            document,
                            "AssertionConsumerService",
                            endpoint.binding(),
                            endpoint.location(),
                            endpoint.responseLocation(),
                            endpoint.extensions(),
                            endpoint.attributes(),
                            endpoint.index(),
                            endpoint.defaultEndpoint()));
        }
        appendAll(root, value.attributeConsumingServices(), METADATA_NAMESPACE, "AttributeConsumingService", false);
        return root;
    }

    /**
     * Writes common role descriptor attributes.
     *
     * @param root          target role element
     * @param id            optional XML ID
     * @param validUntil    optional expiration instant
     * @param cacheDuration optional XML duration
     * @param protocols     supported protocol namespace URIs
     * @param errorUrl      optional error URL
     */
    private static void roleAttributes(
            final Element root,
            final Optional<String> id,
            final Optional<java.time.Instant> validUntil,
            final Optional<String> cacheDuration,
            final List<String> protocols,
            final Optional<String> errorUrl) {
        optional(root, "ID", id.getOrNull());
        optional(root, "validUntil", validUntil.isPresent() ? validUntil.getOrNull().toString() : null);
        optional(root, "cacheDuration", cacheDuration.getOrNull());
        attribute(root, "protocolSupportEnumeration", String.join(Symbol.SPACE, protocols));
        optional(root, "errorURL", errorUrl.getOrNull());
    }

    /**
     * Writes one metadata key descriptor.
     *
     * @param document owner DOM document
     * @param value    typed key descriptor
     * @return KeyDescriptor element
     */
    private static Element writeKey(final org.w3c.dom.Document document, final KeyDescriptor value) {
        final Element root = metadata(document, "KeyDescriptor");
        optional(root, "use", value.use().isPresent() ? value.use().getOrNull().value() : null);
        append(root, value.keyInfo().xml(), SIGNATURE_NAMESPACE, "KeyInfo", false);
        appendAll(
                root,
                value.encryptionMethods().stream().map(KeyDescriptor.EncryptionMethod::xml).toList(),
                METADATA_NAMESPACE,
                "EncryptionMethod",
                false);
        return root;
    }

    /**
     * Writes a common or indexed Metadata endpoint.
     *
     * @param document         owner DOM document
     * @param localName        endpoint element local name
     * @param binding          binding URI
     * @param location         endpoint location URI
     * @param responseLocation optional response location URI
     * @param extensions       ordered foreign child elements
     * @param attributes       ordered foreign attributes
     * @param index            optional required index for indexed endpoints
     * @param defaultEndpoint  optional default marker
     * @return endpoint element
     */
    private static Element writeEndpoint(
            final org.w3c.dom.Document document,
            final String localName,
            final SamlBinding binding,
            final String location,
            final Optional<String> responseLocation,
            final List<byte[]> extensions,
            final Map<QName, String> attributes,
            final Integer index,
            final Optional<Boolean> defaultEndpoint) {
        final Element root = metadata(document, localName);
        attribute(root, "Binding", binding.value());
        attribute(root, "Location", location);
        optional(root, "ResponseLocation", responseLocation.getOrNull());
        if (index != null)
            attribute(root, "index", Integer.toString(index));
        optional(root, "isDefault", lexical(defaultEndpoint));
        foreignAttributes(root, attributes);
        appendAll(root, extensions, null, null, true);
        return root;
    }

    /**
     * Adds a Metadata Extensions wrapper around ordered complete child elements.
     *
     * @param parent parent metadata element
     * @param values ordered extension elements
     */
    private static void extensions(final Element parent, final List<byte[]> values) {
        if (values.isEmpty())
            return;
        final Element wrapper = metadata(parent.getOwnerDocument(), "Extensions");
        appendAll(wrapper, values, null, null, false);
        parent.appendChild(wrapper);
    }

    /**
     * Adds repeated text-only metadata children.
     *
     * @param parent    parent metadata element
     * @param localName child local name
     * @param values    ordered text values
     */
    private static void textElements(final Element parent, final String localName, final List<String> values) {
        for (String value : values) {
            final Element child = metadata(parent.getOwnerDocument(), localName);
            child.setTextContent(value);
            parent.appendChild(child);
        }
    }

    /**
     * Adds one required non-empty attribute.
     *
     * @param element target element
     * @param name    unqualified attribute name
     * @param value   required attribute value
     */
    private static void attribute(final Element element, final String name, final String value) {
        element.setAttribute(name, Assert.notBlank(value, "SAML Metadata " + name + " must not be blank"));
    }

    /**
     * Adds one optional attribute when its value is present.
     *
     * @param element target element
     * @param name    unqualified attribute name
     * @param value   nullable attribute value
     */
    private static void optional(final Element element, final String name, final Object value) {
        if (value != null)
            element.setAttribute(name, value.toString());
    }

    /**
     * Returns the lexical representation of an optional XML boolean.
     *
     * @param value optional boolean
     * @return nullable canonical lexical value
     */
    private static String lexical(final Optional<Boolean> value) {
        return value.isPresent() ? Boolean.toString(value.getOrNull()) : null;
    }

    /**
     * Writes foreign qualified attributes without replacing core Metadata attributes.
     *
     * @param element    target element
     * @param attributes expanded-name attribute map
     */
    private static void foreignAttributes(final Element element, final Map<QName, String> attributes) {
        int sequence = 0;
        for (Map.Entry<QName, String> entry : attributes.entrySet()) {
            final QName name = entry.getKey();
            final String prefix = name.getPrefix().isEmpty() ? "ext" + sequence++ : name.getPrefix();
            element.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:" + prefix, name.getNamespaceURI());
            element.setAttributeNS(
                    name.getNamespaceURI(),
                    prefix + Symbol.C_COLON + name.getLocalPart(),
                    entry.getValue());
        }
    }

    /**
     * Appends one optional validated XML fragment.
     *
     * @param parent    destination parent
     * @param value     optional complete XML element
     * @param namespace required namespace
     * @param localName required local name
     */
    private static void appendOptional(
            final Element parent,
            final Optional<byte[]> value,
            final String namespace,
            final String localName) {
        if (value.isPresent())
            append(parent, value.getOrNull(), namespace, localName, false);
    }

    /**
     * Appends ordered validated XML fragments.
     *
     * @param parent    destination parent
     * @param values    complete XML elements
     * @param namespace required namespace, or {@code null} for any namespace
     * @param localName required local name, or {@code null} for any local name
     * @param foreign   whether the fragment must be outside the Metadata namespace
     */
    private static void appendAll(
            final Element parent,
            final List<byte[]> values,
            final String namespace,
            final String localName,
            final boolean foreign) {
        for (byte[] value : values)
            append(parent, value, namespace, localName, foreign);
    }

    /**
     * Securely parses and imports one retained complete XML element.
     *
     * @param parent    destination parent
     * @param value     complete XML element bytes
     * @param namespace required namespace, or {@code null}
     * @param localName required local name, or {@code null}
     * @param foreign   whether Metadata namespace content is prohibited
     */
    private static void append(
            final Element parent,
            final byte[] value,
            final String namespace,
            final String localName,
            final boolean foreign) {
        final Element source = parseStatic(value).getDocumentElement();
        if (namespace != null && !is(source, namespace, localName)) {
            throw new ValidateException("SAML Metadata retained XML has an unexpected root QName");
        }
        if (foreign && METADATA_NAMESPACE.equals(source.getNamespaceURI())) {
            throw new ValidateException("SAML Metadata endpoint extension must use a foreign namespace");
        }
        parent.appendChild(parent.getOwnerDocument().importNode(source, true));
    }

    /**
     * Returns children retained inside one Metadata Extensions wrapper.
     *
     * @param wrapper Extensions element
     * @return ordered complete child XML elements
     */
    private static List<byte[]> extensionChildren(final Element wrapper) {
        rejectUnknownAttributes(wrapper, Set.of());
        final List<byte[]> result = new ArrayList<>();
        for (Element child : children(wrapper))
            result.add(fragment(child));
        if (result.isEmpty())
            throw new ValidateException("SAML Metadata Extensions must not be empty");
        return result;
    }

    /**
     * Creates a namespace-aware metadata element.
     *
     * @param document  owner DOM document
     * @param localName local element name
     * @return Metadata namespace element
     */
    private static Element metadata(final org.w3c.dom.Document document, final String localName) {
        return document.createElementNS(METADATA_NAMESPACE, "md:" + localName);
    }

    /**
     * Creates a namespace-aware XML Signature element.
     *
     * @param document  owner DOM document
     * @param localName local element name
     * @return XML Signature namespace element
     */
    private static Element signature(final org.w3c.dom.Document document, final String localName) {
        return document.createElementNS(SIGNATURE_NAMESPACE, "ds:" + localName);
    }

    /**
     * Creates a hardened namespace-aware DOM document builder.
     *
     * @return empty DOM document
     */
    private static org.w3c.dom.Document document() {
        try {
            final DocumentBuilderFactory factory = factory();
            return factory.newDocumentBuilder().newDocument();
        } catch (Exception exception) {
            throw new ValidateException("Cannot create a secure SAML Metadata XML document", exception);
        }
    }

    /**
     * Parses one complete XML document with DTD and external resolution disabled.
     *
     * @param xml complete XML bytes
     * @return namespace-aware DOM document
     */
    private static org.w3c.dom.Document parseStatic(final byte[] xml) {
        try {
            return factory().newDocumentBuilder().parse(new ByteArrayInputStream(xml));
        } catch (Exception exception) {
            throw new ValidateException("SAML Metadata XML cannot be parsed securely", exception);
        }
    }

    /**
     * Configures the shared secure DOM parser baseline.
     *
     * @return hardened namespace-aware document builder factory
     */
    private static DocumentBuilderFactory factory() {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        XXE.disableXXE(factory);
        factory.setNamespaceAware(true);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, Normal.EMPTY);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, Normal.EMPTY);
        } catch (Exception exception) {
            throw new ValidateException("Cannot configure secure SAML Metadata XML parsing", exception);
        }
        return factory;
    }

    /**
     * Serializes a complete DOM tree without changing its semantic namespace content.
     *
     * @param source document or element to serialize
     * @return UTF-8 XML bytes without an XML declaration
     */
    private static byte[] serialize(final Node source) {
        try {
            final TransformerFactory factory = TransformerFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, Normal.EMPTY);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, Normal.EMPTY);
            final Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, Charset.UTF_8.name());
            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            transformer.transform(new DOMSource(source), new StreamResult(output));
            return output.toByteArray();
        } catch (Exception exception) {
            throw new ValidateException("SAML Metadata XML cannot be serialized securely", exception);
        }
    }

    /**
     * Serializes one complete element for immutable wildcard retention.
     *
     * @param element source element
     * @return complete UTF-8 XML element bytes
     */
    private static byte[] fragment(final Element element) {
        return serialize(element);
    }

    /**
     * Returns element children while rejecting non-whitespace mixed text.
     *
     * @param parent parent element
     * @return ordered child elements
     */
    private static List<Element> children(final Element parent) {
        final List<Element> result = new ArrayList<>();
        final NodeList nodes = parent.getChildNodes();
        for (int index = 0; index < nodes.getLength(); index++) {
            final Node node = nodes.item(index);
            if (node instanceof Element element) {
                result.add(element);
            } else if ((node.getNodeType() == Node.TEXT_NODE || node.getNodeType() == Node.CDATA_SECTION_NODE)
                    && !node.getTextContent().isBlank()) {
                throw new ValidateException("SAML Metadata structural element contains unexpected text");
            }
        }
        return result;
    }

    /**
     * Returns element children without interpreting legal character content in wildcard or leaf elements.
     *
     * @param parent parent element
     * @return ordered child elements
     */
    private static List<Element> elementChildren(final Element parent) {
        final List<Element> result = new ArrayList<>();
        final NodeList nodes = parent.getChildNodes();
        for (int index = 0; index < nodes.getLength(); index++) {
            if (nodes.item(index) instanceof Element element)
                result.add(element);
        }
        return result;
    }

    /**
     * Requires an exact expanded element name.
     *
     * @param element   element to verify
     * @param namespace required namespace URI
     * @param localName required local name
     */
    private static void require(final Element element, final String namespace, final String localName) {
        if (!is(element, namespace, localName)) {
            throw new ValidateException("SAML Metadata root must be md:" + localName);
        }
    }

    /**
     * Compares an element by expanded name.
     *
     * @param element   candidate element
     * @param namespace namespace URI
     * @param localName local name
     * @return whether the expanded names are identical
     */
    private static boolean is(final Element element, final String namespace, final String localName) {
        return namespace.equals(element.getNamespaceURI()) && localName.equals(element.getLocalName());
    }

    /**
     * Requires one non-blank unqualified attribute.
     *
     * @param element source element
     * @param name    attribute name
     * @return required lexical value
     */
    private static String required(final Element element, final String name) {
        if (!element.hasAttribute(name)) {
            throw new ValidateException("SAML Metadata " + element.getLocalName() + " requires " + name);
        }
        return Assert.notBlank(
                element.getAttribute(name),
                "SAML Metadata " + element.getLocalName() + Symbol.C_SPACE + name + " must not be blank");
    }

    /**
     * Reads one optional unqualified attribute without trimming its lexical value.
     *
     * @param element source element
     * @param name    attribute name
     * @return optional exact lexical value
     */
    private static Optional<String> text(final Element element, final String name) {
        return element.hasAttribute(name) ? Optional.of(element.getAttribute(name)) : Optional.empty();
    }

    /**
     * Reads and validates one optional XML Schema dateTime attribute.
     *
     * @param element source element
     * @param name    attribute name
     * @return optional instant
     */
    private static Optional<java.time.Instant> instant(final Element element, final String name) {
        if (!element.hasAttribute(name))
            return Optional.empty();
        try {
            return Optional.of(OffsetDateTime.parse(element.getAttribute(name)).toInstant());
        } catch (RuntimeException exception) {
            throw new ValidateException("SAML Metadata " + name + " is not an XML Schema dateTime", exception);
        }
    }

    /**
     * Reads and validates one optional XML Schema duration attribute.
     *
     * @param element source element
     * @param name    attribute name
     * @return optional exact duration lexical value
     */
    private static Optional<String> duration(final Element element, final String name) {
        final Optional<String> value = text(element, name);
        if (value.isPresent()) {
            try {
                DatatypeFactory.newInstance().newDuration(value.getOrNull());
            } catch (Exception exception) {
                throw new ValidateException("SAML Metadata " + name + " is not an XML Schema duration", exception);
            }
        }
        return value;
    }

    /**
     * Reads one optional XML Schema boolean attribute.
     *
     * @param element source element
     * @param name    attribute name
     * @return optional boolean value
     */
    private static Optional<Boolean> bool(final Element element, final String name) {
        if (!element.hasAttribute(name))
            return Optional.empty();
        return switch (element.getAttribute(name)) {
            case Normal.TRUE, Symbol.ONE -> Optional.of(true);
            case Normal.FALSE, Symbol.ZERO -> Optional.of(false);
            default -> throw new ValidateException("SAML Metadata " + name + " is not an XML Schema boolean");
        };
    }

    /**
     * Reads one required non-negative integer attribute.
     *
     * @param element source element
     * @param name    attribute name
     * @return parsed integer value
     */
    private static int integer(final Element element, final String name) {
        try {
            return Integer.parseInt(required(element, name));
        } catch (NumberFormatException exception) {
            throw new ValidateException("SAML Metadata " + name + " is not an integer", exception);
        }
    }

    /**
     * Splits an XML whitespace-separated URI list.
     *
     * @param value required lexical list
     * @return immutable non-empty token list
     */
    private static List<String> tokens(final String value) {
        return List.of(value.trim().split("\\s+"));
    }

    /**
     * Reads required element text while prohibiting nested elements.
     *
     * @param element text-only element
     * @param label   diagnostic label
     * @return non-blank text content
     */
    private static String requiredContent(final Element element, final String label) {
        if (!elementChildren(element).isEmpty()) {
            throw new ValidateException("SAML Metadata " + label + " must be text-only");
        }
        return Assert.notBlank(element.getTextContent(), "SAML Metadata " + label + " must not be blank");
    }

    /**
     * Preserves attributes whose expanded names belong to foreign namespaces.
     *
     * @param element source element
     * @param known   unqualified attributes represented by typed fields
     * @return immutable foreign attribute map
     */
    private static Map<QName, String> foreignAttributes(final Element element, final Set<String> known) {
        final Map<QName, String> result = new LinkedHashMap<>();
        final NamedNodeMap attributes = element.getAttributes();
        for (int index = 0; index < attributes.getLength(); index++) {
            final Attr attribute = (Attr) attributes.item(index);
            if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(attribute.getNamespaceURI()))
                continue;
            if ((attribute.getNamespaceURI() == null || attribute.getNamespaceURI().isEmpty())
                    && known.contains(attribute.getName()))
                continue;
            if (attribute.getNamespaceURI() == null || attribute.getNamespaceURI().isEmpty()) {
                throw new ValidateException("Unsupported SAML Metadata attribute: " + attribute.getName());
            }
            result.put(
                    new QName(attribute.getNamespaceURI(), attribute.getLocalName(),
                            attribute.getPrefix() == null ? Normal.EMPTY : attribute.getPrefix()),
                    attribute.getValue());
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Rejects unknown unqualified attributes while permitting schema wildcard foreign attributes.
     *
     * @param element source element
     * @param known   supported unqualified attribute names
     */
    private static void rejectUnknownAttributes(final Element element, final Set<String> known) {
        foreignAttributes(element, known);
    }

    /**
     * Enforces non-decreasing SAML schema child order.
     *
     * @param previous preceding child rank
     * @param current  current child rank
     * @param parent   parent element label
     * @return current rank
     */
    private static int ordered(final int previous, final int current, final String parent) {
        if (current < previous)
            throw new ValidateException("SAML Metadata " + parent + " child order is invalid");
        return current;
    }

    /**
     * Creates an unsupported-core-element validation failure.
     *
     * @param element unsupported child
     * @param parent  parent label
     * @return validation failure ready to throw
     */
    private static ValidateException unsupported(final Element element, final String parent) {
        return new ValidateException("Unsupported SAML Metadata " + parent + " child: {" + element.getNamespaceURI()
                + Symbol.C_BRACE_RIGHT + element.getLocalName());

    }

    /**
     * Throws a duplicate singleton-element validation failure.
     *
     * @param label duplicated element label
     */
    private static void duplicate(final String label) {
        throw new ValidateException("Duplicate SAML Metadata " + label);
    }

    /**
     * Determines whether a Fabric media type represents XML.
     *
     * @param media response media type
     * @return whether the type is application/xml, text/xml, or a structured +xml type
     */
    private static boolean xml(final MediaType media) {
        if (media == null)
            return false;
        final String type = media.type();
        final String subtype = media.subtype();
        return subtype != null && (("application".equalsIgnoreCase(type) && "xml".equalsIgnoreCase(subtype))
                || ("text".equalsIgnoreCase(type) && "xml".equalsIgnoreCase(subtype))
                || subtype.toLowerCase(java.util.Locale.ROOT).endsWith("+xml"));
    }

    /**
     * Parses the constrained SAML Metadata key-use lexical value.
     *
     * @param value exact {@code signing} or {@code encryption} value
     * @return matching typed key use
     * @throws ValidateException if the value is outside the SAML Metadata enumeration
     */
    private static KeyDescriptor.Use parseKeyUse(final String value) {
        for (KeyDescriptor.Use use : KeyDescriptor.Use.values()) {
            if (use.value().equals(value)) {
                return use;
            }
        }
        throw new ValidateException("SAML KeyDescriptor use must be signing or encryption");
    }

    /**
     * Decodes a successful XML HTTP response and closes the owned response body.
     *
     * @param response Fabric HTTP response owned by this invocation
     * @return typed metadata retaining the exact HTTP body bytes
     * @throws ProtocolException if status or media type is not acceptable
     * @throws ValidateException if the metadata document is unsafe or invalid
     */
    public SamlMessageCodec.Document<EntityDescriptor> decode(final Response response) {
        Assert.notNull(response, "SAML Metadata HTTP response must not be null");
        try {
            if (!response.successful()) {
                throw new ProtocolException("SAML Metadata endpoint returned HTTP status " + response.code());
            }
            final MediaType media = response.body().media();
            if (!xml(media)) {
                throw new ProtocolException("SAML Metadata response must use an XML media type");
            }
            return decode(response.bytes(maximumBytes));
        } finally {
            response.close();
        }
    }

    /**
     * Decodes one complete SAML {@code EntityDescriptor} document.
     *
     * @param xml original metadata XML bytes
     * @return immutable typed descriptor, original bytes, and unique ID inventory
     * @throws ValidateException if the XML or supported metadata grammar is invalid
     */
    public SamlMessageCodec.Document<EntityDescriptor> decode(final byte[] xml) {
        final byte[] source = bounded(xml, "SAML Metadata XML");
        final org.w3c.dom.Document document = parse(source);
        final Element root = document.getDocumentElement();
        require(root, METADATA_NAMESPACE, "EntityDescriptor");
        final Set<String> ids = validateTree(root, 1);
        return new SamlMessageCodec.Document<>(entity(root), source, ids);
    }

    /**
     * Encodes one typed SAML entity descriptor in schema order.
     *
     * @param descriptor validated SAML entity descriptor
     * @return complete UTF-8 metadata XML bytes
     * @throws ValidateException if retained wildcard XML is unsafe or output exceeds the configured limit
     */
    public byte[] encode(final EntityDescriptor descriptor) {
        final EntityDescriptor value = Assert.notNull(descriptor, "SAML EntityDescriptor must not be null");
        final org.w3c.dom.Document document = document();
        final Element root = metadata(document, "EntityDescriptor");
        document.appendChild(root);
        attribute(root, "entityID", value.entityId());
        optional(root, "validUntil", value.validUntil().isPresent() ? value.validUntil().getOrNull().toString() : null);
        optional(root, "cacheDuration", value.cacheDuration().getOrNull());
        optional(root, "ID", value.id().getOrNull());
        foreignAttributes(root, value.attributes());
        appendOptional(root, value.signature(), SIGNATURE_NAMESPACE, "Signature");
        extensions(root, value.extensions());
        for (IdpSsoDescriptor role : value.identityProviders()) {
            root.appendChild(writeIdentityProvider(document, role));
        }
        for (SpSsoDescriptor role : value.serviceProviders()) {
            root.appendChild(writeServiceProvider(document, role));
        }
        appendOptional(root, value.organization(), METADATA_NAMESPACE, "Organization");
        appendAll(root, value.contacts(), METADATA_NAMESPACE, "ContactPerson", false);
        appendAll(root, value.additionalMetadataLocations(), METADATA_NAMESPACE, "AdditionalMetadataLocation", false);
        validateTree(root, 1);
        return bounded(serialize(document), "Encoded SAML Metadata XML");
    }

    /**
     * Creates standards-compliant {@code ds:KeyInfo/ds:X509Data} from a Bus certificate chain.
     *
     * @param chain non-empty trusted Bus certificate chain
     * @return complete UTF-8 {@code ds:KeyInfo} element bytes
     * @throws ValidateException if the chain is empty or a certificate cannot be DER encoded
     */
    public byte[] keyInfo(final CertificateChain chain) {
        final CertificateChain value = Assert.notNull(chain, "SAML Metadata certificate chain must not be null");
        if (value.empty()) {
            throw new ValidateException("SAML Metadata certificate chain must not be empty");
        }
        final org.w3c.dom.Document document = document();
        final Element root = signature(document, "KeyInfo");
        document.appendChild(root);
        final Element data = signature(document, "X509Data");
        root.appendChild(data);
        for (java.security.cert.Certificate certificate : value.certificates()) {
            final Element item = signature(document, "X509Certificate");
            try {
                item.setTextContent(Base64.getEncoder().encodeToString(certificate.getEncoded()));
            } catch (CertificateEncodingException exception) {
                throw new ValidateException("SAML Metadata certificate cannot be DER encoded", exception);
            }
            data.appendChild(item);
        }
        return bounded(serialize(document), "SAML Metadata KeyInfo XML");
    }

    /**
     * Reads an entity descriptor in the exact supported schema order.
     *
     * @param root validated EntityDescriptor root
     * @return typed entity descriptor
     */
    private EntityDescriptor entity(final Element root) {
        rejectUnknownAttributes(root, Set.of("entityID", "validUntil", "cacheDuration", "ID"));
        final List<byte[]> extensions = new ArrayList<>();
        final List<IdpSsoDescriptor> identityProviders = new ArrayList<>();
        final List<SpSsoDescriptor> serviceProviders = new ArrayList<>();
        final List<byte[]> contacts = new ArrayList<>();
        final List<byte[]> additional = new ArrayList<>();
        byte[] signature = null;
        byte[] organization = null;
        int rank = 0;
        for (Element child : children(root)) {
            final int current;
            if (is(child, SIGNATURE_NAMESPACE, "Signature")) {
                current = 1;
                if (signature != null)
                    duplicate("EntityDescriptor Signature");
                signature = fragment(child);
            } else if (is(child, METADATA_NAMESPACE, "Extensions")) {
                current = 2;
                if (!extensions.isEmpty())
                    duplicate("EntityDescriptor Extensions");
                extensions.addAll(extensionChildren(child));
            } else if (is(child, METADATA_NAMESPACE, "IDPSSODescriptor")) {
                current = 3;
                identityProviders.add(identityProvider(child));
            } else if (is(child, METADATA_NAMESPACE, "SPSSODescriptor")) {
                current = 3;
                serviceProviders.add(serviceProvider(child));
            } else if (is(child, METADATA_NAMESPACE, "Organization")) {
                current = 4;
                if (organization != null)
                    duplicate("EntityDescriptor Organization");
                organization = fragment(child);
            } else if (is(child, METADATA_NAMESPACE, "ContactPerson")) {
                current = 5;
                contacts.add(fragment(child));
            } else if (is(child, METADATA_NAMESPACE, "AdditionalMetadataLocation")) {
                current = 6;
                additional.add(fragment(child));
            } else {
                throw unsupported(child, "EntityDescriptor");
            }
            rank = ordered(rank, current, "EntityDescriptor");
        }
        return new EntityDescriptor(required(root, "entityID"), instant(root, "validUntil"),
                text(root, "cacheDuration"), text(root, "ID"), Optional.ofNullable(signature), extensions,
                identityProviders, serviceProviders, Optional.ofNullable(organization), contacts, additional,
                foreignAttributes(root, Set.of("entityID", "validUntil", "cacheDuration", "ID")));
    }

    /**
     * Reads one executable IdP SSO role descriptor.
     *
     * @param root IDPSSODescriptor element
     * @return typed IdP role
     */
    private IdpSsoDescriptor identityProvider(final Element root) {
        final Set<String> known = Set.of(
                "ID",
                "validUntil",
                "cacheDuration",
                "protocolSupportEnumeration",
                "errorURL",
                "WantAuthnRequestsSigned");
        rejectUnknownAttributes(root, known);
        final List<byte[]> extensions = new ArrayList<>();
        final List<KeyDescriptor> keys = new ArrayList<>();
        final List<byte[]> contacts = new ArrayList<>();
        final List<SingleLogoutServiceEndpoint> logout = new ArrayList<>();
        final List<String> formats = new ArrayList<>();
        final List<SingleSignOnServiceEndpoint> signOn = new ArrayList<>();
        byte[] signature = null;
        byte[] organization = null;
        int rank = 0;
        for (Element child : children(root)) {
            final int current;
            if (is(child, SIGNATURE_NAMESPACE, "Signature")) {
                current = 1;
                if (signature != null)
                    duplicate("IDPSSODescriptor Signature");
                signature = fragment(child);
            } else if (is(child, METADATA_NAMESPACE, "Extensions")) {
                current = 2;
                if (!extensions.isEmpty())
                    duplicate("IDPSSODescriptor Extensions");
                extensions.addAll(extensionChildren(child));
            } else if (is(child, METADATA_NAMESPACE, "KeyDescriptor")) {
                current = 3;
                keys.add(key(child));
            } else if (is(child, METADATA_NAMESPACE, "Organization")) {
                current = 4;
                if (organization != null)
                    duplicate("IDPSSODescriptor Organization");
                organization = fragment(child);
            } else if (is(child, METADATA_NAMESPACE, "ContactPerson")) {
                current = 5;
                contacts.add(fragment(child));
            } else if (is(child, METADATA_NAMESPACE, "SingleLogoutService")) {
                current = 6;
                logout.add(logoutEndpoint(child));
            } else if (is(child, METADATA_NAMESPACE, "NameIDFormat")) {
                current = 7;
                formats.add(requiredContent(child, "NameIDFormat"));
            } else if (is(child, METADATA_NAMESPACE, "SingleSignOnService")) {
                current = 8;
                signOn.add(signOnEndpoint(child));
            } else {
                throw unsupported(child, "IDPSSODescriptor");
            }
            rank = ordered(rank, current, "IDPSSODescriptor");
        }
        return new IdpSsoDescriptor(text(root, "ID"), instant(root, "validUntil"), duration(root, "cacheDuration"),
                tokens(required(root, "protocolSupportEnumeration")), text(root, "errorURL"),
                Optional.ofNullable(signature), extensions, keys, Optional.ofNullable(organization), contacts, logout,
                formats, signOn, bool(root, "WantAuthnRequestsSigned"));
    }

    /**
     * Reads one executable SP SSO role descriptor.
     *
     * @param root SPSSODescriptor element
     * @return typed SP role
     */
    private SpSsoDescriptor serviceProvider(final Element root) {
        final Set<String> known = Set.of(
                "ID",
                "validUntil",
                "cacheDuration",
                "protocolSupportEnumeration",
                "errorURL",
                "AuthnRequestsSigned",
                "WantAssertionsSigned");
        rejectUnknownAttributes(root, known);
        final List<byte[]> extensions = new ArrayList<>();
        final List<KeyDescriptor> keys = new ArrayList<>();
        final List<byte[]> contacts = new ArrayList<>();
        final List<SingleLogoutServiceEndpoint> logout = new ArrayList<>();
        final List<String> formats = new ArrayList<>();
        final List<AssertionConsumerServiceEndpoint> consumers = new ArrayList<>();
        final List<byte[]> attributes = new ArrayList<>();
        byte[] signature = null;
        byte[] organization = null;
        int rank = 0;
        for (Element child : children(root)) {
            final int current;
            if (is(child, SIGNATURE_NAMESPACE, "Signature")) {
                current = 1;
                if (signature != null)
                    duplicate("SPSSODescriptor Signature");
                signature = fragment(child);
            } else if (is(child, METADATA_NAMESPACE, "Extensions")) {
                current = 2;
                if (!extensions.isEmpty())
                    duplicate("SPSSODescriptor Extensions");
                extensions.addAll(extensionChildren(child));
            } else if (is(child, METADATA_NAMESPACE, "KeyDescriptor")) {
                current = 3;
                keys.add(key(child));
            } else if (is(child, METADATA_NAMESPACE, "Organization")) {
                current = 4;
                if (organization != null)
                    duplicate("SPSSODescriptor Organization");
                organization = fragment(child);
            } else if (is(child, METADATA_NAMESPACE, "ContactPerson")) {
                current = 5;
                contacts.add(fragment(child));
            } else if (is(child, METADATA_NAMESPACE, "SingleLogoutService")) {
                current = 6;
                logout.add(logoutEndpoint(child));
            } else if (is(child, METADATA_NAMESPACE, "NameIDFormat")) {
                current = 7;
                formats.add(requiredContent(child, "NameIDFormat"));
            } else if (is(child, METADATA_NAMESPACE, "AssertionConsumerService")) {
                current = 8;
                consumers.add(consumerEndpoint(child));
            } else if (is(child, METADATA_NAMESPACE, "AttributeConsumingService")) {
                current = 9;
                attributes.add(fragment(child));
            } else {
                throw unsupported(child, "SPSSODescriptor");
            }
            rank = ordered(rank, current, "SPSSODescriptor");
        }
        return new SpSsoDescriptor(text(root, "ID"), instant(root, "validUntil"), duration(root, "cacheDuration"),
                tokens(required(root, "protocolSupportEnumeration")), text(root, "errorURL"),
                Optional.ofNullable(signature), extensions, keys, Optional.ofNullable(organization), contacts, logout,
                formats, consumers, attributes, bool(root, "AuthnRequestsSigned"), bool(root, "WantAssertionsSigned"));
    }

    /**
     * Reads one signing or encryption key descriptor.
     *
     * @param root KeyDescriptor element
     * @return typed key descriptor retaining complete KeyInfo XML
     */
    private KeyDescriptor key(final Element root) {
        rejectUnknownAttributes(root, Set.of("use"));
        byte[] keyInfo = null;
        final List<byte[]> encryptionMethods = new ArrayList<>();
        int rank = 0;
        for (Element child : children(root)) {
            final int current;
            if (is(child, SIGNATURE_NAMESPACE, "KeyInfo")) {
                current = 1;
                if (keyInfo != null)
                    duplicate("KeyDescriptor KeyInfo");
                keyInfo = fragment(child);
            } else if (is(child, METADATA_NAMESPACE, "EncryptionMethod")) {
                current = 2;
                encryptionMethods.add(fragment(child));
            } else {
                throw unsupported(child, "KeyDescriptor");
            }
            rank = ordered(rank, current, "KeyDescriptor");
        }
        if (keyInfo == null) {
            throw new ValidateException("SAML KeyDescriptor requires ds:KeyInfo");
        }
        final Optional<String> use = text(root, "use");
        final Optional<KeyDescriptor.Use> typedUse = use.isEmpty() ? Optional.empty()
                : Optional.of(parseKeyUse(use.getOrNull()));
        return new KeyDescriptor(typedUse, new KeyDescriptor.KeyInfo(keyInfo),
                encryptionMethods.stream().map(KeyDescriptor.EncryptionMethod::new).toList());
    }

    /**
     * Reads one SingleSignOnService endpoint.
     *
     * @param root endpoint element
     * @return typed endpoint restricted to implemented bindings
     */
    private SingleSignOnServiceEndpoint signOnEndpoint(final Element root) {
        return new SingleSignOnServiceEndpoint(binding(root), required(root, "Location"),
                text(root, "ResponseLocation"), endpointChildren(root),
                foreignAttributes(root, Set.of("Binding", "Location", "ResponseLocation")));
    }

    /**
     * Reads one SingleLogoutService endpoint.
     *
     * @param root endpoint element
     * @return typed endpoint restricted to implemented bindings
     */
    private SingleLogoutServiceEndpoint logoutEndpoint(final Element root) {
        return new SingleLogoutServiceEndpoint(binding(root), required(root, "Location"),
                text(root, "ResponseLocation"), endpointChildren(root),
                foreignAttributes(root, Set.of("Binding", "Location", "ResponseLocation")));
    }

    /**
     * Reads one indexed AssertionConsumerService endpoint.
     *
     * @param root endpoint element
     * @return typed indexed endpoint restricted to HTTP-POST
     */
    private AssertionConsumerServiceEndpoint consumerEndpoint(final Element root) {
        final SamlBinding binding = binding(root);
        if (!SamlBinding.HTTP_POST.equals(binding)) {
            throw new ValidateException("SAML runtime requires AssertionConsumerService to use HTTP-POST");
        }
        return new AssertionConsumerServiceEndpoint(binding, required(root, "Location"), text(root, "ResponseLocation"),
                endpointChildren(root),
                foreignAttributes(root, Set.of("Binding", "Location", "ResponseLocation", "index", "isDefault")),
                integer(root, "index"), bool(root, "isDefault"));
    }

    /**
     * Parses one bounded metadata document using hardened JAXP options.
     *
     * @param xml bounded source XML
     * @return parsed DOM document
     */
    private org.w3c.dom.Document parse(final byte[] xml) {
        return parseStatic(xml);
    }

    /**
     * Enforces the configured metadata byte ceiling and takes a defensive copy.
     *
     * @param value source bytes
     * @param label diagnostic label
     * @return bounded copied bytes
     */
    private byte[] bounded(final byte[] value, final String label) {
        final byte[] source = Assert.notNull(value, label + " must not be null");
        if (source.length == 0 || source.length > maximumBytes) {
            throw new ValidateException(label + " must contain between 1 and " + maximumBytes + " bytes");
        }
        return source.clone();
    }

    /**
     * Validates element depth and document-wide XML ID uniqueness.
     *
     * @param root  current root element
     * @param depth current one-based element depth
     * @return immutable unique ID inventory
     */
    private Set<String> validateTree(final Element root, final int depth) {
        final Set<String> ids = new LinkedHashSet<>();
        validateTree(root, depth, ids);
        return Collections.unmodifiableSet(ids);
    }

    /**
     * Recursively validates resource depth and collects unique ID attributes.
     *
     * @param element current element
     * @param depth   current one-based depth
     * @param ids     mutable document-wide ID inventory
     */
    private void validateTree(final Element element, final int depth, final Set<String> ids) {
        if (depth > maximumDepth) {
            throw new ValidateException("SAML Metadata XML exceeds the configured depth limit");
        }
        final NamedNodeMap attributes = element.getAttributes();
        for (int index = 0; index < attributes.getLength(); index++) {
            final Node attribute = attributes.item(index);
            if ("ID".equals(attribute.getLocalName()) || "id".equals(attribute.getLocalName())
                    || "Id".equals(attribute.getLocalName())) {
                final String id = Assert.notBlank(attribute.getNodeValue(), "SAML Metadata XML ID must not be blank");
                if (!ids.add(id))
                    throw new ValidateException("SAML Metadata XML contains a duplicate ID");
            }
        }
        for (Element child : elementChildren(element))
            validateTree(child, depth + 1, ids);
    }

}
