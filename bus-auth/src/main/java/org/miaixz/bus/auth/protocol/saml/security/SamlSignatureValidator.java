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
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.xml.XMLConstants;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.protocol.saml.*;
import org.miaixz.bus.auth.protocol.saml.client.SamlClientOptions;
import org.miaixz.bus.auth.protocol.saml.codec.RedirectBindingCodec;
import org.miaixz.bus.auth.protocol.saml.codec.SamlMessageCodec;
import org.miaixz.bus.auth.protocol.saml.server.SamlServerOptions;
import org.miaixz.bus.auth.resolver.CertificateMaterial;
import org.miaixz.bus.auth.shared.SecurityBaseline;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.worker.CertificateLoader;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xml.XXE;
import org.miaixz.bus.crypto.builtin.CertificateChain;
import org.miaixz.bus.crypto.builtin.CertificateChainCleaner;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Validates SAML XML and HTTP-Redirect signatures against externally trusted certificate material.
 * <p>
 * XML signatures are always checked on the retained original DOM. Reference URIs must identify the exact signed root or
 * Assertion by a unique document ID, and JSR-105 secure validation is mandatory. Embedded {@code KeyInfo} never
 * establishes trust; certificate chains and trust roots are supplied by the external loader and parsed before cleaning
 * Bus certificate-chain implementation.
 * </p>
 *
 * @author Kimi Liu
 */
public final class SamlSignatureValidator {

    /**
     * SAML certificate use passed to the external certificate loader.
     */
    private static final String SIGNING_USE = "signing";

    /**
     * Allowed enveloped-signature transform URI.
     */
    private static final String ENVELOPED = Transform.ENVELOPED;

    /**
     * External certificate loader and framework-owned certificate parser.
     */
    private final DriverServices services;

    /**
     * Shared non-relaxable algorithm and resource policy.
     */
    private final SecurityBaseline securityBaseline;

    /**
     * Creates a SAML signature validator.
     *
     * @param services         external loaders and pure parsers
     * @param securityBaseline shared SAML security baseline
     * @throws IllegalArgumentException if a collaborator is {@code null}
     */
    public SamlSignatureValidator(final DriverServices services,
            final SecurityBaseline securityBaseline) {
        this.services = Assert.notNull(services, "SAML execution services must not be null");
        this.securityBaseline = Assert.notNull(securityBaseline, "SAML security baseline must not be null");
    }

    /**
     * Locates one already typed plain assertion by its unique identifier.
     *
     * @param response typed response
     * @param id       assertion identifier
     * @return matching assertion
     */
    private static Assertion assertion(final Response response, final String id) {
        return response.assertions().stream().filter(Response.PlainAssertion.class::isInstance)
                .map(Response.PlainAssertion.class::cast).map(Response.PlainAssertion::assertion)
                .filter(value -> id.equals(value.id())).findFirst()
                .orElseThrow(() -> new ValidateException("SAML Assertion is missing from its typed Response"));
    }

    /**
     * Verifies one HTTP-Redirect signature over the exact retained octets.
     *
     * @param value     retained binding signature
     * @param publicKey trusted public key
     * @return whether the cryptographic signature is valid
     */
    private static boolean verifyRedirect(final RedirectBindingCodec.Signature value, final PublicKey publicKey) {
        try {
            final Signature verifier = Signature.getInstance(jca(value.algorithm()));
            verifier.initVerify(publicKey);
            verifier.update(value.signedInput());
            return verifier.verify(value.value());
        } catch (Exception exception) {
            return false;
        }
    }

    /**
     * Maps supported XML Signature URIs to their JCA verification names.
     *
     * @param algorithm XML Signature method URI
     * @return exact JCA signature name
     */
    private static String jca(final String algorithm) {
        return switch (algorithm) {
            case "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256" -> Algorithm.SHA256WITHRSA.getValue();
            case "http://www.w3.org/2001/04/xmldsig-more#rsa-sha384" -> Algorithm.SHA384WITHRSA.getValue();
            case "http://www.w3.org/2001/04/xmldsig-more#rsa-sha512" -> Algorithm.SHA512WITHRSA.getValue();
            case "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256" -> Algorithm.SHA256WITHECDSA.getValue();
            case "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha384" -> Algorithm.SHA384WITHECDSA.getValue();
            case "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha512" -> Algorithm.SHA512WITHECDSA.getValue();
            default -> throw new ValidateException("Unsupported SAML signature algorithm");
        };
    }

    /**
     * Detects SHA-1 algorithm identifiers without case ambiguity.
     *
     * @param algorithm standard algorithm identifier
     * @return whether the identifier selects SHA-1
     */
    private static boolean sha1(final String algorithm) {
        return algorithm == null || algorithm.toLowerCase(java.util.Locale.ROOT).contains("sha1");
    }

    /**
     * Restricts transforms to enveloped signature and canonicalization operations.
     *
     * @param algorithm transform URI
     * @return whether the transform is safe for the SAML enveloped profile
     */
    private static boolean safeTransform(final String algorithm) {
        return ENVELOPED.equals(algorithm) || algorithm.startsWith("http://www.w3.org/2001/10/xml-exc-c14n#")
                || algorithm.startsWith("http://www.w3.org/TR/2001/REC-xml-c14n-20010315");
    }

    /**
     * Parses original signed XML with all external entity mechanisms disabled.
     *
     * @param xml original document bytes
     * @return secure namespace-aware DOM
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
            throw new ValidateException("Signed SAML XML cannot be parsed securely", exception);
        }
    }

    /**
     * Registers only IDs already proven unique by the owning secure codec.
     *
     * @param element current element
     * @param ids     codec-proven unique ID inventory
     */
    private static void registerIds(final Element element, final Set<String> ids) {
        for (int index = 0; index < element.getAttributes().getLength(); index++) {
            if (element.getAttributes().item(index) instanceof Attr attribute && ids.contains(attribute.getValue())
                    && Set.of("ID", "Id", "id").contains(attribute.getLocalName())) {
                element.setIdAttributeNode(attribute, true);
            }
        }
        final NodeList children = element.getChildNodes();
        for (int index = 0; index < children.getLength(); index++) {
            if (children.item(index) instanceof Element child)
                registerIds(child, ids);
        }
    }

    /**
     * Locates the unique element carrying one registered ID.
     *
     * @param root       document root
     * @param expectedId exact ID value
     * @return matching element
     */
    private static Element id(final Element root, final String expectedId) {
        if (expectedId.equals(root.getAttribute("ID")))
            return root;
        final NodeList children = root.getChildNodes();
        for (int index = 0; index < children.getLength(); index++) {
            if (children.item(index) instanceof Element child) {
                final Element result = id(child, expectedId);
                if (result != null)
                    return result;
            }
        }
        return null;
    }

    /**
     * Finds one direct XML Signature child and rejects wrapping duplicates.
     *
     * @param target expected signed element
     * @return direct Signature child, or {@code null}
     */
    private static Element directSignature(final Element target) {
        if (target == null)
            throw new ValidateException("Signed SAML XML target ID is missing");
        Element result = null;
        final NodeList children = target.getChildNodes();
        for (int index = 0; index < children.getLength(); index++) {
            if (children.item(index) instanceof Element child && XMLSignature.XMLNS.equals(child.getNamespaceURI())
                    && "Signature".equals(child.getLocalName())) {
                if (result != null)
                    throw new ValidateException("Signed SAML element has duplicate Signatures");
                result = child;
            }
        }
        return result;
    }

    /**
     * Returns the entityID lexical value from a required entity-format Issuer.
     *
     * @param issuer required issuer model
     * @param label  diagnostic message label
     * @return issuer lexical value
     */
    private static String issuer(final Issuer issuer, final String label) {
        if (issuer == null)
            throw new ValidateException(label + " requires Issuer");
        return Assert.notBlank(issuer.nameId().value(), label + " Issuer must not be blank");
    }

    /**
     * Validates common asynchronous invocation arguments.
     *
     * @param context immutable invocation context
     * @param timeout shared operation budget
     */
    private static void invocation(final Context context, final Timeout.Budget timeout) {
        Assert.notNull(context, "SAML signature context must not be null");
        Assert.notNull(timeout, "SAML signature budget must not be null");
    }

    /**
     * Releases a successful prerequisite outcome as the original typed value.
     *
     * @param outcome prerequisite result
     * @param value   original value
     * @param <T>     original value type
     * @return mapped outcome
     */
    private static <T> Outcome<T> release(final Outcome<Void> outcome, final T value) {
        return switch (outcome) {
            case Outcome.Succeeded<Void> ignored -> Outcome.succeeded(value);
            case Outcome.Rejected<Void> rejected -> Outcome.rejected(rejected.failure());
            case Outcome.Failed<Void> failed -> Outcome.failed(failed.failure());
        };
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
     * Creates a safe protocol rejection without exposing XML or key material.
     *
     * @param description non-sensitive description
     * @param <T>         expected success type
     * @return rejected outcome
     */
    private static <T> Outcome<T> rejected(final String description) {
        return Outcome.rejected(failure(ErrorCode._400, description));
    }

    /**
     * Creates a safe operational failure without exposing key material.
     *
     * @param description non-sensitive description
     * @param <T>         expected success type
     * @return failed outcome
     */
    private static <T> Outcome<T> failed(final String description) {
        return Outcome.failed(failure(ErrorCode._500, description));
    }

    /**
     * Creates immutable safe failure detail using Bus error definitions.
     *
     * @param error       shared Bus error definition
     * @param description non-sensitive description
     * @return immutable failure detail
     */
    private static Outcome.Failure failure(final Errors error, final String description) {
        return new Outcome.Failure(error, description, new JsonValue.ObjectValue(Map.of()));
    }

    /**
     * Validates signed SAML Metadata for one explicitly expected entity.
     *
     * @param document         original metadata document
     * @param expectedEntityId exact configured entityID
     * @param context          immutable invocation context
     * @param timeout          shared end-to-end budget
     * @return stage containing the unchanged trusted document or closed failure
     */
    public CompletionStage<Outcome<SamlMessageCodec.Document<EntityDescriptor>>> validateMetadata(
            final SamlMessageCodec.Document<EntityDescriptor> document,
            final String expectedEntityId,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(document, "SAML Metadata document must not be null");
        Assert.notBlank(expectedEntityId, "Expected SAML Metadata entityID must not be blank");
        invocation(context, timeout);
        if (!expectedEntityId.equals(document.message().entityId())) {
            return completed(rejected("SAML Metadata entityID does not match the configured entity"));
        }
        return validateXml(
                document.xml(),
                document.ids(),
                expectedEntityId,
                document.message().id().getOrNull(),
                null,
                true,
                context,
                timeout).thenApply(outcome -> release(outcome, document));
    }

    /**
     * Validates required and present signatures on a SAML Response and its plain assertions.
     *
     * @param document original Response document
     * @param options  trusted service-provider Source options
     * @param context  immutable invocation context
     * @param timeout  shared end-to-end budget
     * @return stage containing the unchanged trusted document or closed failure
     */
    public CompletionStage<Outcome<SamlMessageCodec.Document<Response>>> validateResponse(
            final SamlMessageCodec.Document<Response> document,
            final SamlClientOptions options,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(document, "SAML Response document must not be null");
        Assert.notNull(options, "SAML Source options must not be null");
        invocation(context, timeout);
        final Response response = document.message();
        final String issuer = issuer(response.issuer().getOrNull(), "SAML Response");
        if (!options.identityProviderEntityId().equals(issuer)) {
            return completed(rejected("SAML Response issuer does not match the trusted identity provider"));
        }
        final boolean rootSigned = response.signature().isPresent();
        if (options.wantResponsesSigned() && !rootSigned) {
            return completed(rejected("SAML Response requires a response signature"));
        }
        final List<String> assertions = new ArrayList<>();
        boolean everyPlainSigned = true;
        for (Response.AssertionContent content : response.assertions()) {
            if (content instanceof Response.PlainAssertion plain) {
                assertions.add(plain.assertion().id());
                everyPlainSigned &= plain.assertion().signature().isPresent();
                if (options.wantAssertionsSigned() && !plain.assertion().signature().isPresent()) {
                    return completed(rejected("SAML Response contains an unsigned assertion"));
                }
            }
        }
        if (!rootSigned && !everyPlainSigned
                && response.assertions().stream().noneMatch(Response.EncryptedAssertion.class::isInstance)) {
            return completed(rejected("SAML Response must have a trusted response or assertion signature"));
        }
        return validateResponseSignatures(document, options, issuer, rootSigned, assertions, context, timeout);
    }

    /**
     * Validates an Authentication Request HTTP-Redirect signature when required or present.
     *
     * @param decoded decoded request retaining exact signed input
     * @param options identity-provider signature policy
     * @param context immutable invocation context
     * @param timeout shared end-to-end budget
     * @return stage containing the unchanged decoded request or closed failure
     */
    public CompletionStage<Outcome<RedirectBindingCodec.Decoded<AuthnRequest>>> validateAuthnRequest(
            final RedirectBindingCodec.Decoded<AuthnRequest> decoded,
            final SamlServerOptions options,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(decoded, "SAML Authentication Request must not be null");
        Assert.notNull(options, "SAML Provider options must not be null");
        return validateRedirect(
                decoded,
                options.wantAuthnRequestsSigned(),
                options.signatureAlgorithm(),
                issuer(decoded.document().message().issuer().getOrNull(), "SAML Authentication Request"),
                context,
                timeout);
    }

    /**
     * Validates a Logout Request HTTP-Redirect signature when required or present.
     *
     * @param decoded decoded request retaining exact signed input
     * @param options identity-provider signature policy
     * @param context immutable invocation context
     * @param timeout shared end-to-end budget
     * @return stage containing the unchanged decoded request or closed failure
     */
    public CompletionStage<Outcome<RedirectBindingCodec.Decoded<LogoutRequest>>> validateLogoutRequest(
            final RedirectBindingCodec.Decoded<LogoutRequest> decoded,
            final SamlServerOptions options,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(decoded, "SAML Logout Request must not be null");
        Assert.notNull(options, "SAML Provider options must not be null");
        return validateRedirect(
                decoded,
                options.wantLogoutRequestsSigned(),
                options.signatureAlgorithm(),
                issuer(decoded.document().message().issuer().getOrNull(), "SAML Logout Request"),
                context,
                timeout);
    }

    /**
     * Validates one decrypted Assertion signature on its original plaintext document.
     *
     * @param document decrypted assertion document
     * @param options  trusted service-provider Source options
     * @param context  immutable invocation context
     * @param timeout  shared end-to-end budget
     * @return stage containing the unchanged trusted assertion document or closed failure
     */
    CompletionStage<Outcome<SamlMessageCodec.Document<Assertion>>> validateAssertion(
            final SamlMessageCodec.Document<Assertion> document,
            final SamlClientOptions options,
            final Context context,
            final Timeout.Budget timeout) {
        final Assertion assertion = Assert.notNull(document, "SAML Assertion document must not be null").message();
        final String issuer = issuer(assertion.issuer(), "SAML Assertion");
        if (!options.identityProviderEntityId().equals(issuer)) {
            return completed(rejected("SAML Assertion issuer does not match the trusted identity provider"));
        }
        if (!assertion.signature().isPresent()) {
            return completed(rejected("Encrypted SAML Assertion must contain a signature"));
        }
        return validateXml(
                document.xml(),
                document.ids(),
                issuer,
                assertion.id(),
                options.signatureAlgorithm(),
                true,
                context,
                timeout).thenApply(outcome -> release(outcome, document));
    }

    /**
     * Validates response and assertion XML signatures sequentially against the same trusted issuer.
     *
     * @param document     original Response document
     * @param options      Source algorithm policy
     * @param issuer       trusted identity-provider entityID
     * @param rootSigned   whether a root Signature is present
     * @param assertionIds signed or potentially signed plain assertion IDs
     * @param context      invocation context
     * @param timeout      shared budget
     * @return stage containing the original trusted document or failure
     */
    private CompletionStage<Outcome<SamlMessageCodec.Document<Response>>> validateResponseSignatures(
            final SamlMessageCodec.Document<Response> document,
            final SamlClientOptions options,
            final String issuer,
            final boolean rootSigned,
            final List<String> assertionIds,
            final Context context,
            final Timeout.Budget timeout) {
        CompletionStage<Outcome<Void>> stage = rootSigned
                ? validateXml(
                        document.xml(),
                        document.ids(),
                        issuer,
                        document.message().id(),
                        options.signatureAlgorithm(),
                        true,
                        context,
                        timeout)
                : completed(Outcome.succeeded(null));
        for (String assertionId : assertionIds) {
            final boolean signed = assertion(document.message(), assertionId).signature().isPresent();
            if (signed) {
                stage = stage.thenCompose(outcome -> switch (outcome) {
                    case Outcome.Succeeded<Void> ignored -> validateXml(
                            document.xml(),
                            document.ids(),
                            issuer,
                            assertionId,
                            options.signatureAlgorithm(),
                            true,
                            context,
                            timeout);
                    case Outcome.Rejected<Void> rejected -> completed(Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<Void> failed -> completed(Outcome.failed(failed.failure()));
                });
            }
        }
        return stage.thenApply(outcome -> release(outcome, document));
    }

    /**
     * Validates one retained HTTP-Redirect signature.
     *
     * @param decoded             decoded binding result
     * @param required            whether the profile requires a signature
     * @param configuredAlgorithm exact configured signature algorithm
     * @param issuer              certificate authority coordinate
     * @param context             invocation context
     * @param timeout             shared budget
     * @param <T>                 request message type
     * @return unchanged decoded value or closed failure
     */
    private <T> CompletionStage<Outcome<RedirectBindingCodec.Decoded<T>>> validateRedirect(
            final RedirectBindingCodec.Decoded<T> decoded,
            final boolean required,
            final String configuredAlgorithm,
            final String issuer,
            final Context context,
            final Timeout.Budget timeout) {
        invocation(context, timeout);
        if (!decoded.signature().isPresent()) {
            return required ? completed(rejected("SAML Redirect request requires a signature"))
                    : completed(Outcome.succeeded(decoded));
        }
        final RedirectBindingCodec.Signature signature = decoded.signature().getOrNull();
        if (!configuredAlgorithm.equals(signature.algorithm()) || !allowed(signature.algorithm())) {
            return completed(rejected("SAML Redirect signature algorithm is not allowed"));
        }
        return certificate(issuer, context, timeout).thenApply(outcome -> switch (outcome) {
            case Outcome.Succeeded<PublicKey> success -> verifyRedirect(signature, success.value())
                    ? Outcome.succeeded(decoded)
                    : rejected("SAML Redirect signature is invalid");
            case Outcome.Rejected<PublicKey> rejected -> Outcome.rejected(rejected.failure());
            case Outcome.Failed<PublicKey> failed -> Outcome.failed(failed.failure());
        });
    }

    /**
     * Validates exactly one XML Signature attached to the element with the expected ID.
     *
     * @param xml                 original complete XML document
     * @param ids                 unique document ID inventory
     * @param issuer              external certificate coordinate
     * @param expectedId          exact signed element ID
     * @param configuredAlgorithm optional exact profile algorithm
     * @param required            whether the signature is mandatory
     * @param context             invocation context
     * @param timeout             shared budget
     * @return success or closed validation/resolution failure
     */
    private CompletionStage<Outcome<Void>> validateXml(
            final byte[] xml,
            final Set<String> ids,
            final String issuer,
            final String expectedId,
            final String configuredAlgorithm,
            final boolean required,
            final Context context,
            final Timeout.Budget timeout) {
        if (expectedId == null || !ids.contains(expectedId)) {
            return completed(rejected("Signed SAML element requires a unique registered XML ID"));
        }
        final org.w3c.dom.Document dom;
        final Element target;
        final Element signature;
        try {
            dom = parse(xml);
            registerIds(dom.getDocumentElement(), ids);
            target = id(dom.getDocumentElement(), expectedId);
            signature = directSignature(target);
        } catch (RuntimeException exception) {
            return completed(rejected("SAML signed XML structure is invalid"));
        }
        if (signature == null) {
            return required ? completed(rejected("Required SAML XML Signature is missing"))
                    : completed(Outcome.succeeded(null));
        }
        return certificate(issuer, context, timeout).thenApply(outcome -> switch (outcome) {
            case Outcome.Succeeded<PublicKey> success -> verifyXml(
                    signature,
                    expectedId,
                    configuredAlgorithm,
                    success.value()) ? Outcome.succeeded(null) : rejected("SAML XML Signature is invalid");
            case Outcome.Rejected<PublicKey> rejected -> Outcome.rejected(rejected.failure());
            case Outcome.Failed<PublicKey> failed -> Outcome.failed(failed.failure());
        });
    }

    /**
     * Resolves and cleans one trusted signing certificate chain.
     *
     * @param issuer  trusted SAML issuer
     * @param context invocation context
     * @param timeout shared budget
     * @return stage containing the cleaned leaf public key
     */
    private CompletionStage<Outcome<PublicKey>> certificate(
            final String issuer,
            final Context context,
            final Timeout.Budget timeout) {
        if (timeout.expired())
            return completed(failed("SAML signature validation has no remaining time budget"));
        final CertificateLoader.Request request = new CertificateLoader.Request(issuer, SIGNING_USE,
                timeout.clock().now());
        final CompletionStage<Outcome<CertificateMaterial>> resolution = Outcome.mapStage(
                () -> services.certificateLoader().load(request, context, timeout),
                loaded -> services.certificateParser().parse(request, loaded));
        if (resolution == null)
            return completed(failed("SAML certificate loader returned no result stage"));
        return resolution.handle((outcome, cause) -> {
            if (cause != null || outcome == null)
                return failed("SAML certificate resolution failed");
            return switch (outcome) {
                case Outcome.Succeeded<CertificateMaterial> success -> {
                    try {
                        final CertificateMaterial resolved = success.value();
                        final CertificateChain clean = CertificateChainCleaner.of(resolved.trustRoots())
                                .clean(resolved.chain().certificates(), issuer);
                        if (!(clean.leaf() instanceof X509Certificate certificate)) {
                            yield failed("SAML signing certificate is not X.509");
                        }
                        certificate.checkValidity(java.util.Date.from(timeout.clock().now()));
                        yield Outcome.succeeded(certificate.getPublicKey());
                    } catch (Exception exception) {
                        yield rejected("SAML signing certificate chain is not trusted or time-valid");
                    }
                }
                case Outcome.Rejected<CertificateMaterial> rejected -> Outcome.rejected(rejected.failure());
                case Outcome.Failed<CertificateMaterial> failed -> Outcome.failed(failed.failure());
            };
        });
    }

    /**
     * Performs strict JSR-105 validation of one enveloped XML Signature.
     *
     * @param signatureElement    exact direct Signature child
     * @param expectedId          exact signed element ID
     * @param configuredAlgorithm optional exact configured signature algorithm
     * @param publicKey           trusted certificate public key
     * @return whether signature structure, algorithms, reference, and cryptographic value are valid
     */
    private boolean verifyXml(
            final Element signatureElement,
            final String expectedId,
            final String configuredAlgorithm,
            final PublicKey publicKey) {
        try {
            final DOMValidateContext validation = new DOMValidateContext(publicKey, signatureElement);
            validation.setProperty("org.jcp.xml.dsig.secureValidation", Boolean.TRUE);
            final XMLSignature signature = XMLSignatureFactory.getInstance("DOM").unmarshalXMLSignature(validation);
            final String algorithm = signature.getSignedInfo().getSignatureMethod().getAlgorithm();
            if (!allowed(algorithm) || (configuredAlgorithm != null && !configuredAlgorithm.equals(algorithm))) {
                return false;
            }
            final List<?> references = signature.getSignedInfo().getReferences();
            if (references.size() != 1 || !(references.getFirst() instanceof Reference reference)
                    || !(Symbol.C_HASH + expectedId).equals(reference.getURI())
                    || sha1(reference.getDigestMethod().getAlgorithm())) {
                return false;
            }
            for (Object value : reference.getTransforms()) {
                if (!(value instanceof Transform transform) || !safeTransform(transform.getAlgorithm()))
                    return false;
            }
            return signature.validate(validation);
        } catch (Exception exception) {
            return false;
        }
    }

    /**
     * Checks an algorithm against the shared SAML baseline and the SHA-1 prohibition.
     *
     * @param algorithm exact standard algorithm URI
     * @return whether the baseline permits the algorithm
     */
    private boolean allowed(final String algorithm) {
        return !sha1(algorithm) && securityBaseline.require(Protocol.SAML).algorithms().contains(algorithm);
    }

}
