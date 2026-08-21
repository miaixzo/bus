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
package org.miaixz.bus.auth.protocol.saml.security;

import java.io.ByteArrayInputStream;
import java.security.PrivateKey;
import java.security.spec.MGF1ParameterSpec;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.protocol.saml.Assertion;
import org.miaixz.bus.auth.protocol.saml.Response;
import org.miaixz.bus.auth.protocol.saml.Saml;
import org.miaixz.bus.auth.protocol.saml.client.SamlClientOptions;
import org.miaixz.bus.auth.protocol.saml.codec.SamlMessageCodec;
import org.miaixz.bus.auth.resolver.KeyMaterial;
import org.miaixz.bus.auth.shared.SecurityBaseline;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.worker.KeyLoader;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xml.XXE;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Decrypts the modern SAML 2.0 {@code EncryptedAssertion} profile into validated plaintext assertions.
 * <p>
 * Execution is deliberately limited to XML Encryption 1.1 RSA-OAEP with SHA-256 key transport and AES-GCM content
 * encryption. Private keys are selected by an explicit {@code ds:KeyName}; RSA 1.5, CBC, CipherReference, key guessing,
 * nested keys, and partially decrypted responses are rejected. Every plaintext is securely parsed and its XML signature
 * is validated before it replaces the encrypted choice entry.
 * </p>
 *
 * @author Kimi Liu
 */
public final class SamlDecryptionService {

    /**
     * XML Encryption 1.0 namespace used by EncryptedData and CipherData.
     */
    private static final String XENC = Saml.Namespaces.ENCRYPTION;

    /**
     * XML Encryption 1.1 namespace used by modern algorithms and MGF.
     */
    private static final String XENC11 = Saml.Namespaces.ENCRYPTION_11;

    /**
     * XML Signature namespace used by KeyInfo, KeyName, and DigestMethod.
     */
    private static final String DS = Saml.Namespaces.SIGNATURE;

    /**
     * Required modern RSA-OAEP key transport algorithm.
     */
    private static final String RSA_OAEP = XENC11 + "rsa-oaep";

    /**
     * Required SHA-256 digest algorithm URI.
     */
    private static final String SHA256 = "http://www.w3.org/2001/04/xmlenc#sha256";

    /**
     * Required SHA-256 MGF algorithm URI.
     */
    private static final String MGF1_SHA256 = XENC11 + "mgf1sha256";

    /**
     * AES-128-GCM content-encryption algorithm URI.
     */
    private static final String AES128_GCM = XENC11 + "aes128-gcm";

    /**
     * AES-192-GCM content-encryption algorithm URI.
     */
    private static final String AES192_GCM = XENC11 + "aes192-gcm";

    /**
     * AES-256-GCM content-encryption algorithm URI.
     */
    private static final String AES256_GCM = XENC11 + "aes256-gcm";

    /**
     * XML Encryption Type URI identifying a complete encrypted element.
     */
    private static final String ELEMENT_TYPE = XENC + "Element";

    /**
     * AES-GCM initialization vector length defined by XML Encryption 1.1.
     */
    private static final int GCM_IV_BYTES = 12;

    /**
     * AES-GCM authentication tag length defined by XML Encryption 1.1.
     */
    private static final int GCM_TAG_BITS = 128;

    /**
     * External private-key loader and framework-owned key parser.
     */
    private final DriverServices services;

    /**
     * Strict plaintext SAML XML codec.
     */
    private final SamlMessageCodec messageCodec;

    /**
     * Original-plaintext assertion signature validator.
     */
    private final SamlSignatureValidator signatureValidator;

    /**
     * Shared non-relaxable algorithm and message policy.
     */
    private final SecurityBaseline securityBaseline;

    /**
     * Creates an EncryptedAssertion decryption service.
     *
     * @param services           external loaders and pure parsers
     * @param messageCodec       strict SAML plaintext codec
     * @param signatureValidator trusted assertion signature validator
     * @param securityBaseline   shared SAML security baseline
     * @throws IllegalArgumentException if a collaborator is {@code null}
     */
    public SamlDecryptionService(final DriverServices services,
            final SamlMessageCodec messageCodec, final SamlSignatureValidator signatureValidator,
            final SecurityBaseline securityBaseline) {
        this.services = Assert.notNull(services, "SAML execution services must not be null");
        this.messageCodec = Assert.notNull(messageCodec, "SAML message codec must not be null");
        this.signatureValidator = Assert.notNull(signatureValidator, "SAML signature validator must not be null");
        this.securityBaseline = Assert.notNull(securityBaseline, "SAML security baseline must not be null");
    }

    /**
     * Parses the exact supported EncryptedAssertion structure.
     *
     * @param xml complete EncryptedAssertion XML
     * @return immutable cryptographic payload
     */
    private static EncryptedPayload payload(final byte[] xml) {
        final Element wrapper = parse(xml).getDocumentElement();
        require(wrapper, Saml.Namespaces.ASSERTION, "EncryptedAssertion");
        final Element data = exactly(wrapper, XENC, "EncryptedData");
        final String type = optional(data, "Type");
        if (type != null && !ELEMENT_TYPE.equals(type)) {
            throw new ValidateException("SAML EncryptedData Type must identify an element");
        }
        final Element contentMethod = exactly(data, XENC, "EncryptionMethod");
        final String contentAlgorithm = required(contentMethod, "Algorithm");
        if (!elements(contentMethod).isEmpty()) {
            throw new ValidateException("SAML AES-GCM EncryptionMethod must not contain parameters");
        }
        final Element outerKeyInfo = exactly(data, DS, "KeyInfo");
        final Element encryptedKey = exactly(outerKeyInfo, XENC, "EncryptedKey");
        final Element keyMethod = exactly(encryptedKey, XENC, "EncryptionMethod");
        final String keyAlgorithm = required(keyMethod, "Algorithm");
        validateOaep(keyMethod);
        final Element innerKeyInfo = exactly(encryptedKey, DS, "KeyInfo");
        final String keyName = content(exactly(innerKeyInfo, DS, "KeyName"), "SAML EncryptedKey KeyName");
        final byte[] wrappedKey = cipher(exactly(encryptedKey, XENC, "CipherData"), "SAML EncryptedKey");
        final byte[] ciphertext = cipher(exactly(data, XENC, "CipherData"), "SAML EncryptedData");
        allowedChildren(wrapper, Set.of(q(XENC, "EncryptedData")));
        allowedChildren(data, Set.of(q(XENC, "EncryptionMethod"), q(DS, "KeyInfo"), q(XENC, "CipherData")));
        allowedChildren(outerKeyInfo, Set.of(q(XENC, "EncryptedKey")));
        allowedChildren(encryptedKey, Set.of(q(XENC, "EncryptionMethod"), q(DS, "KeyInfo"), q(XENC, "CipherData")));
        allowedChildren(innerKeyInfo, Set.of(q(DS, "KeyName")));
        return new EncryptedPayload(keyName, keyAlgorithm, contentAlgorithm, wrappedKey, ciphertext);
    }

    /**
     * Validates explicit SHA-256 RSA-OAEP parameters.
     *
     * @param method RSA-OAEP EncryptionMethod
     */
    private static void validateOaep(final Element method) {
        final List<Element> children = elements(method);
        String digest = null;
        String mgf = null;
        for (Element child : children) {
            if (is(child, DS, "DigestMethod") && digest == null)
                digest = required(child, "Algorithm");
            else if (is(child, XENC11, "MGF") && mgf == null)
                mgf = required(child, "Algorithm");
            else
                throw new ValidateException("SAML RSA-OAEP contains unsupported or duplicate parameters");
        }
        if ((digest == null) != (mgf == null)
                || (digest != null && (!SHA256.equals(digest) || !MGF1_SHA256.equals(mgf)))) {
            throw new ValidateException("SAML RSA-OAEP parameters must select SHA-256 digest and MGF1 together");
        }
    }

    /**
     * Extracts one canonical Base64 CipherValue and rejects CipherReference.
     *
     * @param cipherData CipherData element
     * @param label      diagnostic label
     * @return decoded ciphertext bytes
     */
    private static byte[] cipher(final Element cipherData, final String label) {
        allowedChildren(cipherData, Set.of(q(XENC, "CipherValue")));
        final String lexical = content(exactly(cipherData, XENC, "CipherValue"), label + " CipherValue");
        final String encoded = lexical.replaceAll("[\\x20\\x09\\x0D\\x0A]", Normal.EMPTY);
        try {
            final byte[] result = Base64.getDecoder().decode(encoded);
            if (result.length == 0 || !Base64.getEncoder().encodeToString(result).equals(encoded)) {
                throw new ValidateException(label + " CipherValue must use canonical Base64");
            }
            return result;
        } catch (IllegalArgumentException exception) {
            throw new ValidateException(label + " CipherValue is not valid Base64", exception);
        }
    }

    /**
     * Returns the exact AES key size selected by an allowed XML Encryption URI.
     *
     * @param algorithm content-encryption algorithm URI
     * @return key size in bytes, or zero for unsupported algorithms
     */
    private static int keyBytes(final String algorithm) {
        return switch (algorithm) {
            case AES128_GCM -> 16;
            case AES192_GCM -> 24;
            case AES256_GCM -> 32;
            default -> 0;
        };
    }

    /**
     * Creates a copy of the Response with a completely replaced assertion sequence.
     *
     * @param source     original response
     * @param assertions validated plaintext assertion choices
     * @return immutable transformed response
     */
    private static Response copy(final Response source, final List<Response.AssertionContent> assertions) {
        return new Response(source.id(), source.inResponseTo(), source.version(), source.issueInstant(),
                source.destination(), source.consent(), source.issuer(), source.signature(), source.extensions(),
                source.status(), List.copyOf(assertions));
    }

    /**
     * Appends one assertion choice to a new immutable list outcome.
     *
     * @param source accumulated assertion choices
     * @param value  next choice
     * @return successful copied accumulator
     */
    private static Outcome<List<Response.AssertionContent>> append(
            final List<Response.AssertionContent> source,
            final Response.AssertionContent value) {
        final List<Response.AssertionContent> copy = new ArrayList<>(source);
        copy.add(value);
        return Outcome.succeeded(copy);
    }

    /**
     * Securely parses one encrypted XML element.
     *
     * @param xml complete encrypted XML bytes
     * @return namespace-aware DOM document
     */
    private static org.w3c.dom.Document parse(final byte[] xml) {
        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            XXE.disableXXE(factory);
            factory.setNamespaceAware(true);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, Normal.EMPTY);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, Normal.EMPTY);
            return factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml));
        } catch (Exception exception) {
            throw new ValidateException("SAML EncryptedAssertion cannot be parsed securely", exception);
        }
    }

    /**
     * Requires an exact expanded element name.
     *
     * @param element   element to verify
     * @param namespace required namespace
     * @param localName required local name
     */
    private static void require(final Element element, final String namespace, final String localName) {
        if (!is(element, namespace, localName)) {
            throw new ValidateException("Unexpected SAML encrypted XML root element");
        }
    }

    /**
     * Returns the unique direct child with one expanded name.
     *
     * @param parent    parent element
     * @param namespace child namespace
     * @param localName child local name
     * @return unique direct child
     */
    private static Element exactly(final Element parent, final String namespace, final String localName) {
        Element result = null;
        for (Element child : elements(parent)) {
            if (is(child, namespace, localName)) {
                if (result != null)
                    throw new ValidateException("Duplicate SAML encrypted XML element: " + localName);
                result = child;
            }
        }
        if (result == null)
            throw new ValidateException("Missing SAML encrypted XML element: " + localName);
        return result;
    }

    /**
     * Returns ordered direct element children while rejecting mixed non-whitespace text.
     *
     * @param parent parent element
     * @return ordered child elements
     */
    private static List<Element> elements(final Element parent) {
        final List<Element> result = new ArrayList<>();
        final NodeList nodes = parent.getChildNodes();
        for (int index = 0; index < nodes.getLength(); index++) {
            final Node node = nodes.item(index);
            if (node instanceof Element element)
                result.add(element);
            else if ((node.getNodeType() == Node.TEXT_NODE || node.getNodeType() == Node.CDATA_SECTION_NODE)
                    && !node.getTextContent().isBlank()) {
                throw new ValidateException("SAML encrypted XML structural element contains unexpected text");
            }
        }
        return result;
    }

    /**
     * Rejects direct child elements outside an exact expanded-name set.
     *
     * @param parent  parent element
     * @param allowed allowed expanded names
     */
    private static void allowedChildren(final Element parent, final Set<String> allowed) {
        for (Element child : elements(parent)) {
            if (!allowed.contains(q(child.getNamespaceURI(), child.getLocalName()))) {
                throw new ValidateException("Unsupported SAML encrypted XML child: " + child.getLocalName());
            }
        }
    }

    /**
     * Compares one element expanded name.
     *
     * @param element   candidate element
     * @param namespace required namespace
     * @param localName required local name
     * @return whether the expanded names match
     */
    private static boolean is(final Element element, final String namespace, final String localName) {
        return namespace.equals(element.getNamespaceURI()) && localName.equals(element.getLocalName());
    }

    /**
     * Encodes an expanded name for fixed-set comparison.
     *
     * @param namespace namespace URI
     * @param localName local name
     * @return unambiguous expanded-name string
     */
    private static String q(final String namespace, final String localName) {
        return Symbol.C_BRACE_LEFT + namespace + Symbol.C_BRACE_RIGHT + localName;
    }

    /**
     * Reads one required non-blank attribute.
     *
     * @param element source element
     * @param name    attribute name
     * @return required value
     */
    private static String required(final Element element, final String name) {
        if (!element.hasAttribute(name))
            throw new ValidateException("Missing SAML encrypted XML attribute: " + name);
        return Assert.notBlank(element.getAttribute(name), "SAML encrypted XML " + name + " must not be blank");
    }

    /**
     * Reads one optional attribute.
     *
     * @param element source element
     * @param name    attribute name
     * @return nullable exact value
     */
    private static String optional(final Element element, final String name) {
        return element.hasAttribute(name) ? element.getAttribute(name) : null;
    }

    /**
     * Reads required text-only content.
     *
     * @param element source leaf element
     * @param label   diagnostic label
     * @return non-blank exact content
     */
    private static String content(final Element element, final String label) {
        final NodeList children = element.getChildNodes();
        for (int index = 0; index < children.getLength(); index++) {
            if (children.item(index) instanceof Element) {
                throw new ValidateException(label + " must be text-only");
            }
        }
        return Assert.notBlank(element.getTextContent(), label + " must not be blank");
    }

    /**
     * Creates an already completed outcome stage.
     *
     * @param outcome completed outcome
     * @param <T>     success type
     * @return completed stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Creates a safe protocol rejection.
     *
     * @param description non-sensitive description
     * @param <T>         expected success type
     * @return rejected outcome
     */
    private static <T> Outcome<T> rejected(final String description) {
        return Outcome.rejected(new Outcome.Failure(ErrorCode._400, description, new JsonValue.ObjectValue(Map.of())));
    }

    /**
     * Creates a safe operational failure.
     *
     * @param description non-sensitive description
     * @param <T>         expected success type
     * @return failed outcome
     */
    private static <T> Outcome<T> failed(final String description) {
        return Outcome.failed(new Outcome.Failure(ErrorCode._500, description, new JsonValue.ObjectValue(Map.of())));
    }

    /**
     * Decrypts and signature-validates every encrypted assertion as one atomic response transformation.
     *
     * @param response signature-validated SAML Response
     * @param options  trusted service-provider Source options
     * @param context  immutable invocation context
     * @param timeout  shared end-to-end budget
     * @return stage containing an unchanged or completely decrypted Response, or a closed failure
     */
    public CompletionStage<Outcome<Response>> decrypt(
            final Response response,
            final SamlClientOptions options,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(response, "SAML Response must not be null");
        Assert.notNull(options, "SAML Source options must not be null");
        Assert.notNull(context, "SAML decryption context must not be null");
        Assert.notNull(timeout, "SAML decryption budget must not be null");
        if (response.assertions().stream().noneMatch(Response.EncryptedAssertion.class::isInstance)) {
            return completed(Outcome.succeeded(response));
        }
        CompletionStage<Outcome<List<Response.AssertionContent>>> stage = completed(
                Outcome.succeeded(new ArrayList<>()));
        for (Response.AssertionContent content : response.assertions()) {
            stage = stage.thenCompose(outcome -> switch (outcome) {
                case Outcome.Succeeded<List<Response.AssertionContent>> success -> content instanceof Response.PlainAssertion
                        ? completed(append(success.value(), content))
                        : decryptOne((Response.EncryptedAssertion) content, options, context, timeout)
                                .thenApply(decrypted -> switch (decrypted) {
                                    case Outcome.Succeeded<Assertion> assertion -> append(
                                            success.value(),
                                            new Response.PlainAssertion(assertion.value()));
                                    case Outcome.Rejected<Assertion> rejected -> Outcome.rejected(rejected.failure());
                                    case Outcome.Failed<Assertion> failed -> Outcome.failed(failed.failure());
                                });
                case Outcome.Rejected<List<Response.AssertionContent>> rejected -> completed(
                        Outcome.rejected(rejected.failure()));
                case Outcome.Failed<List<Response.AssertionContent>> failed -> completed(
                        Outcome.failed(failed.failure()));
            });
        }
        return stage.thenApply(outcome -> switch (outcome) {
            case Outcome.Succeeded<List<Response.AssertionContent>> success -> Outcome
                    .succeeded(copy(response, success.value()));
            case Outcome.Rejected<List<Response.AssertionContent>> rejected -> Outcome.rejected(rejected.failure());
            case Outcome.Failed<List<Response.AssertionContent>> failed -> Outcome.failed(failed.failure());
        });
    }

    /**
     * Decrypts one encrypted choice and validates its plaintext signature.
     *
     * @param encrypted encrypted assertion choice
     * @param options   trusted Source options
     * @param context   invocation context
     * @param timeout   shared budget
     * @return stage containing one trusted plaintext assertion
     */
    private CompletionStage<Outcome<Assertion>> decryptOne(
            final Response.EncryptedAssertion encrypted,
            final SamlClientOptions options,
            final Context context,
            final Timeout.Budget timeout) {
        if (timeout.expired())
            return completed(failed("SAML assertion decryption has no remaining time budget"));
        final EncryptedPayload payload;
        try {
            payload = payload(encrypted.xml());
            algorithms(payload);
        } catch (RuntimeException exception) {
            return completed(rejected("SAML EncryptedAssertion structure or algorithm is invalid"));
        }
        final KeyLoader.Request query = new KeyLoader.Request(options.entityId(), Optional.of(payload.keyName()),
                "decryption", payload.keyAlgorithm(), timeout.clock().now());
        final CompletionStage<Outcome<KeyMaterial>> resolution = Outcome.mapStage(
                () -> services.keyLoader().load(query, context, timeout),
                loaded -> services.keyParser().parse(query, loaded));
        if (resolution == null)
            return completed(failed("SAML decryption key loader returned no result stage"));
        return resolution
                .exceptionally(
                        cause -> SamlDecryptionService.<KeyMaterial>failed("SAML decryption key resolution failed"))
                .thenCompose(outcome -> switch (outcome) {
                    case Outcome.Succeeded<KeyMaterial> success -> decryptPlaintext(
                            payload,
                            success.value(),
                            options,
                            context,
                            timeout);
                    case Outcome.Rejected<KeyMaterial> rejected -> completed(Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<KeyMaterial> failed -> completed(Outcome.failed(failed.failure()));
                });
    }

    /**
     * Performs RSA-OAEP and AES-GCM decryption and securely re-decodes the assertion.
     *
     * @param payload  validated encrypted payload
     * @param resolved exact private key
     * @param options  trusted Source options
     * @param context  invocation context
     * @param timeout  shared budget
     * @return stage containing a signature-validated assertion
     */
    private CompletionStage<Outcome<Assertion>> decryptPlaintext(
            final EncryptedPayload payload,
            final KeyMaterial resolved,
            final SamlClientOptions options,
            final Context context,
            final Timeout.Budget timeout) {
        byte[] contentKey = null;
        byte[] plaintext = null;
        try {
            if (!payload.keyName().equals(resolved.keyId()) || !payload.keyAlgorithm().equals(resolved.algorithm())
                    || !(resolved.key() instanceof PrivateKey privateKey)) {
                return completed(rejected("Resolved SAML decryption key does not match the encrypted key"));
            }
            final Instant now = timeout.clock().now();
            if (now.isBefore(resolved.notBefore()) || !now.isBefore(resolved.notAfter())) {
                return completed(rejected("Resolved SAML decryption key is outside its validity interval"));
            }
            final Cipher unwrap = Cipher.getInstance("RSA/ECB/OAEPPadding");
            unwrap.init(
                    Cipher.DECRYPT_MODE,
                    privateKey,
                    new OAEPParameterSpec(Algorithm.SHA256.getValue(), "MGF1", MGF1ParameterSpec.SHA256,
                            PSource.PSpecified.DEFAULT));
            contentKey = unwrap.doFinal(payload.encryptedKey());
            final int keyBytes = keyBytes(payload.contentAlgorithm());
            if (contentKey.length != keyBytes)
                return completed(rejected("SAML encrypted content key length is invalid"));
            if (payload.ciphertext().length <= GCM_IV_BYTES + GCM_TAG_BITS / Byte.SIZE) {
                return completed(rejected("SAML AES-GCM ciphertext is too short"));
            }
            final byte[] iv = Arrays.copyOfRange(payload.ciphertext(), 0, GCM_IV_BYTES);
            final byte[] ciphertext = Arrays
                    .copyOfRange(payload.ciphertext(), GCM_IV_BYTES, payload.ciphertext().length);
            try {
                final Cipher decrypt = Cipher.getInstance("AES/GCM/NoPadding");
                decrypt.init(
                        Cipher.DECRYPT_MODE,
                        new SecretKeySpec(contentKey, "AES"),
                        new GCMParameterSpec(GCM_TAG_BITS, iv));
                plaintext = decrypt.doFinal(ciphertext);
            } finally {
                Arrays.fill(iv, (byte) 0);
                Arrays.fill(ciphertext, (byte) 0);
            }
            if (plaintext.length == 0
                    || plaintext.length > securityBaseline.require(Protocol.SAML).maximumMessageBytes()) {
                return completed(rejected("Decrypted SAML Assertion exceeds the configured message limit"));
            }
            final SamlMessageCodec.Document<Assertion> document = messageCodec.decodeAssertion(plaintext);
            return signatureValidator.validateAssertion(document, options, context, timeout)
                    .thenApply(validated -> switch (validated) {
                        case Outcome.Succeeded<SamlMessageCodec.Document<Assertion>> success -> Outcome
                                .succeeded(success.value().message());
                        case Outcome.Rejected<SamlMessageCodec.Document<Assertion>> rejected -> Outcome
                                .rejected(rejected.failure());
                        case Outcome.Failed<SamlMessageCodec.Document<Assertion>> failed -> Outcome
                                .failed(failed.failure());
                    });
        } catch (Exception exception) {
            return completed(rejected("SAML EncryptedAssertion decryption or authentication failed"));
        } finally {
            if (contentKey != null)
                Arrays.fill(contentKey, (byte) 0);
            if (plaintext != null)
                Arrays.fill(plaintext, (byte) 0);
        }
    }

    /**
     * Ensures both selected algorithms are explicitly allowed by the shared SAML baseline.
     *
     * @param payload parsed encrypted payload
     */
    private void algorithms(final EncryptedPayload payload) {
        if (!RSA_OAEP.equals(payload.keyAlgorithm()) || keyBytes(payload.contentAlgorithm()) == 0) {
            throw new ValidateException("SAML encrypted assertion algorithm is not implemented");
        }
        final Set<String> allowed = securityBaseline.require(Protocol.SAML).algorithms();
        if (!allowed.contains(payload.keyAlgorithm()) || !allowed.contains(payload.contentAlgorithm())) {
            throw new ValidateException("SAML encrypted assertion algorithm is outside the security baseline");
        }
    }

    /**
     * Carries the exact modern XML Encryption inputs extracted from one assertion.
     *
     * @param keyName          explicit private-key identifier
     * @param keyAlgorithm     RSA-OAEP algorithm URI
     * @param contentAlgorithm AES-GCM algorithm URI
     * @param encryptedKey     RSA-wrapped AES key bytes
     * @param ciphertext       XML Encryption AES-GCM IV, ciphertext, and tag bytes
     * @author Kimi Liu
     */
    private record EncryptedPayload(String keyName, String keyAlgorithm, String contentAlgorithm, byte[] encryptedKey,
            byte[] ciphertext) {

        /**
         * Validates required values and takes defensive ownership of secret byte sequences.
         *
         * @throws IllegalArgumentException if text is blank or a byte sequence is null
         * @throws ValidateException        if a byte sequence is empty
         */
        private EncryptedPayload {
            keyName = Assert.notBlank(keyName, "SAML encrypted key name must not be blank");
            keyAlgorithm = Assert.notBlank(keyAlgorithm, "SAML encrypted key algorithm must not be blank");
            contentAlgorithm = Assert.notBlank(contentAlgorithm, "SAML content encryption algorithm must not be blank");
            encryptedKey = bytes(encryptedKey, "SAML encrypted key");
            ciphertext = bytes(ciphertext, "SAML ciphertext");
        }

        /**
         * Validates and copies one non-empty secret byte sequence.
         *
         * @param value source bytes
         * @param label diagnostic label
         * @return copied bytes
         */
        private static byte[] bytes(final byte[] value, final String label) {
            final byte[] source = Assert.notNull(value, label + " must not be null");
            if (source.length == 0)
                throw new ValidateException(label + " must not be empty");
            return source.clone();
        }

        /**
         * Returns a defensive copy of the wrapped content key.
         *
         * @return copied wrapped key bytes
         */
        @Override
        public byte[] encryptedKey() {
            return encryptedKey.clone();
        }

        /**
         * Returns a defensive copy of the AES-GCM payload.
         *
         * @return copied ciphertext bytes
         */
        @Override
        public byte[] ciphertext() {
            return ciphertext.clone();
        }

    }

}
