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
package org.miaixz.bus.auth.protocol.saml.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.PrivateKey;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.xml.XMLConstants;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.protocol.saml.*;
import org.miaixz.bus.auth.protocol.saml.codec.MetadataCodec;
import org.miaixz.bus.auth.protocol.saml.codec.SamlMessageCodec;
import org.miaixz.bus.auth.protocol.saml.server.*;
import org.miaixz.bus.auth.provider.ProviderDriver;
import org.miaixz.bus.auth.resolver.KeyResolver;
import org.miaixz.bus.auth.shared.ExecutionServices;
import org.miaixz.bus.auth.shared.internal.RuntimeProvider;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.data.id.UUID;
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Compiles one registered SAML 2.0 identity Provider into its typed service runtime.
 * <p>
 * Compilation performs all settings and baseline validation before creating services. The runtime receives only
 * standard SAML models and exposes only endpoint-backed capabilities. Successful response and metadata models contain
 * the complete XML Signatures required by the compiled server-role Source policy before they cross the Registry
 * boundary; transport adapters only serialize those already secured models.
 * </p>
 *
 * @author Kimi Liu
 */
public final class SamlProviderDriver implements ProviderDriver<SamlProviderSettings> {

    /**
     * Secure maximum DOM nesting depth used by the SAML codecs.
     */
    private static final int MAXIMUM_XML_DEPTH = 64;

    /**
     * Creates a stateless SAML Provider driver.
     */
    public SamlProviderDriver() {
        // No initialization required.
    }

    /**
     * Builds the exact capability set represented by configured Provider endpoints.
     *
     * @param settings validated SAML Provider settings
     * @return immutable endpoint-accurate manifest
     */
    private static Capability.Manifest manifest(final SamlProviderSettings settings) {
        final List<Capability<?, ?>> capabilities = new ArrayList<>();
        capabilities.add(SamlProviderProfile.SINGLE_SIGN_ON);
        if (settings.singleLogoutServiceEndpoint().isPresent()) {
            capabilities.add(SamlProviderProfile.SINGLE_LOGOUT);
        }
        capabilities.add(SamlProviderProfile.METADATA);
        return new Capability.Manifest(capabilities);
    }

    /**
     * Returns the SAML identity-provider profile bound to this driver.
     *
     * @return immutable SAML Provider profile
     */
    @Override
    public SamlProviderProfile profile() {
        return new SamlProviderProfile();
    }

    /**
     * Consumes typed settings and assembles one endpoint-accurate identity-provider runtime.
     *
     * @param record   validated complete server-role Source registration
     * @param library  resolved Library owned by the Provider
     * @param services externally owned runtime dependencies
     * @return immutable executable SAML identity-provider Source runtime
     * @throws IllegalArgumentException if an argument is {@code null}
     * @throws ValidateException        if registration routing, settings, or algorithms are invalid
     */
    @Override
    public RuntimeProvider compile(
            final Registration.Record<Source> record,
            final Provider provider,
            final Library library,
            final ExecutionServices services) {
        Assert.notNull(record, "SAML Provider registration must not be null");
        Assert.notNull(library, "SAML Provider Library must not be null");
        Assert.notNull(services, "SAML Provider execution services must not be null");
        final Source source = record.resource();
        if (!profile().id().equals(source.getType()) || !supports(source.getProtocol())
                || !provider.getId().equals(source.getProvider_id())
                || !library.getId().equals(provider.getLibrary_id())) {
            throw new ValidateException("SAML server driver requires a matching Source registration");
        }
        final SamlProviderSettings settings = decode(source);
        if (!services.securityBaseline().require(Protocol.SAML).algorithms().contains(settings.signatureAlgorithm())) {
            throw new ValidateException("SAML Provider signature algorithm is outside the security baseline");
        }
        if (settings.clockSkew().compareTo(services.securityBaseline().require(Protocol.SAML).maximumClockSkew()) > 0) {
            throw new ValidateException("SAML Provider clock skew exceeds the security baseline");
        }
        final long maximumBytes = services.securityBaseline().require(Protocol.SAML).maximumMessageBytes();
        final SamlMessageCodec messageCodec = new SamlMessageCodec(maximumBytes, MAXIMUM_XML_DEPTH);
        final MetadataCodec metadataCodec = new MetadataCodec(maximumBytes, MAXIMUM_XML_DEPTH);
        final SamlErrorMapper errorMapper = new SamlErrorMapper(settings);
        final AssertionIssuer assertionIssuer = new AssertionIssuer(settings, services.attributeResolver(),
                messageCodec);
        final SingleSignOnService signOn = new SingleSignOnService(settings, services.clientResolver(), assertionIssuer,
                errorMapper);
        final SingleLogoutService logout = new SingleLogoutService(source.getId(), settings, services.clientResolver(),
                services, errorMapper);
        final MetadataService metadata = new MetadataService(settings, services.certificateResolver(), metadataCodec);
        final XmlSigner signer = new XmlSigner(settings, services.keyResolver(), messageCodec, metadataCodec);
        return new CompiledProvider(manifest(settings), new SamlIdentityProvider(signOn, logout, metadata), signer);
    }

    /**
     * Routes exact declared SAML Provider capabilities to the compiled facade.
     *
     * @author Kimi Liu
     */
    private static final class CompiledProvider implements RuntimeProvider {

        /**
         * Endpoint-accurate immutable capability manifest.
         */
        private final Capability.Manifest manifest;

        /**
         * Compiled typed SAML identity-provider facade.
         */
        private final SamlIdentityProvider provider;

        /**
         * Provider-policy XML signer applied before successful models leave the runtime.
         */
        private final XmlSigner signer;

        /**
         * Creates one immutable compiled SAML identity-provider Source runtime.
         *
         * @param manifest exact runtime manifest
         * @param provider typed SAML facade
         * @param signer   XML signer bound to the same Provider settings and key inventory
         */
        private CompiledProvider(final Capability.Manifest manifest, final SamlIdentityProvider provider,
                final XmlSigner signer) {
            this.manifest = Assert.notNull(manifest, "SAML Provider manifest must not be null");
            this.provider = Assert.notNull(provider, "SAML identity-provider facade must not be null");
            this.signer = Assert.notNull(signer, "SAML Provider XML signer must not be null");
        }

        /**
         * Narrows a delegated result through the declared response class.
         *
         * @param stage        delegated outcome stage
         * @param responseType exact declared response class
         * @param <S>          expected success type
         * @return type-safe delegated outcome
         */
        private static <S> CompletionStage<Outcome<S>> narrow(
                final CompletionStage<? extends Outcome<?>> stage,
                final Class<S> responseType) {
            return stage.thenApply(outcome -> switch (outcome) {
                case Outcome.Succeeded<?> success -> Outcome.succeeded(responseType.cast(success.value()));
                case Outcome.Rejected<?> rejected -> Outcome.rejected(rejected.failure());
                case Outcome.Failed<?> failed -> Outcome.failed(failed.failure());
            });
        }

        /**
         * Returns a safe rejection for an undeclared capability.
         *
         * @param <S> expected success type
         * @return completed not-found outcome
         */
        private static <S> CompletionStage<Outcome<S>> missing() {
            return completed(
                    Outcome.rejected(
                            new Outcome.Failure(ErrorCode._404, "SAML Provider capability is not available",
                                    new JsonValue.ObjectValue(Map.of()))));
        }

        /**
         * Returns a safe rejection for a request or response class mismatch.
         *
         * @param <S> expected success type
         * @return completed bad-request outcome
         */
        private static <S> CompletionStage<Outcome<S>> mismatch() {
            return completed(
                    Outcome.rejected(
                            new Outcome.Failure(ErrorCode._400,
                                    "SAML Provider capability type does not match the request",
                                    new JsonValue.ObjectValue(Map.of()))));
        }

        /**
         * Creates a type-inferred completed outcome stage.
         *
         * @param outcome completed outcome
         * @param <T>     success type
         * @return completed stage
         */
        private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
            return CompletableFuture.completedFuture(outcome);
        }

        /**
         * Returns the exact endpoint-backed capability manifest.
         *
         * @return immutable SAML Provider manifest
         */
        @Override
        public Capability.Manifest manifest() {
            return manifest;
        }

        /**
         * Invokes one exact declared SAML server-role Source capability.
         *
         * @param capability exact declared capability object
         * @param request    exact standard request or {@code null} for Metadata
         * @param context    immutable invocation context
         * @param timeout    shared end-to-end budget
         * @param <Q>        request type
         * @param <S>        success type
         * @return delegated typed outcome or a closed mismatch rejection
         */
        @Override
        public <Q, S> CompletionStage<Outcome<S>> invoke(
                final Capability<Q, S> capability,
                final Q request,
                final Context context,
                final Timeout.Budget timeout) {
            Assert.notNull(capability, "SAML Provider capability must not be null");
            Assert.notNull(context, "SAML Provider context must not be null");
            Assert.notNull(timeout, "SAML Provider budget must not be null");
            if (!manifest.capabilities().contains(capability))
                return missing();
            if (capability == SamlProviderProfile.SINGLE_SIGN_ON) {
                if (request == null || request.getClass() != AuthnRequest.class
                        || capability.requestType() != AuthnRequest.class
                        || capability.responseType() != Response.class)
                    return mismatch();
                return narrow(
                        provider.singleSignOn((AuthnRequest) request, context, timeout)
                                .thenCompose(outcome -> switch (outcome) {
                                    case Outcome.Succeeded<Response> success -> signer
                                            .sign(success.value(), context, timeout);
                                    case Outcome.Rejected<Response> rejected -> completed(
                                            Outcome.rejected(rejected.failure()));
                                    case Outcome.Failed<Response> failed -> completed(Outcome.failed(failed.failure()));
                                }),
                        capability.responseType());
            }
            if (capability == SamlProviderProfile.SINGLE_LOGOUT) {
                if (request == null || request.getClass() != LogoutRequest.class
                        || capability.requestType() != LogoutRequest.class
                        || capability.responseType() != LogoutResponse.class)
                    return mismatch();
                return narrow(
                        provider.singleLogout((LogoutRequest) request, context, timeout)
                                .thenCompose(outcome -> switch (outcome) {
                                    case Outcome.Succeeded<LogoutResponse> success -> signer
                                            .sign(success.value(), context, timeout);
                                    case Outcome.Rejected<LogoutResponse> rejected -> completed(
                                            Outcome.rejected(rejected.failure()));
                                    case Outcome.Failed<LogoutResponse> failed -> completed(
                                            Outcome.failed(failed.failure()));
                                }),
                        capability.responseType());
            }
            if (capability == SamlProviderProfile.METADATA) {
                if (request != null || capability.requestType() != Void.class
                        || capability.responseType() != EntityDescriptor.class)
                    return mismatch();
                return narrow(provider.metadata(context, timeout).thenCompose(outcome -> switch (outcome) {
                    case Outcome.Succeeded<EntityDescriptor> success -> signer.sign(success.value(), context, timeout);
                    case Outcome.Rejected<EntityDescriptor> rejected -> completed(Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<EntityDescriptor> failed -> completed(Outcome.failed(failed.failure()));
                }), capability.responseType());
            }
            return missing();
        }

    }

    /**
     * Applies the compiled server-role Source's enveloped XML Signature policy to standard SAML models.
     * <p>
     * The signer is immutable and resolves the current external private key for each operation. JSR-105 performs
     * canonicalization, digesting, reference construction, and signature generation against a secure DOM. Only the
     * complete generated {@code ds:Signature} element is retained in the returned standard model.
     * </p>
     *
     * @author Kimi Liu
     */
    private static final class XmlSigner {

        /**
         * XML Signature namespace used to locate the generated element.
         */
        private static final String XML_SIGNATURE_NAMESPACE = XMLSignature.XMLNS;

        /**
         * Exact external key-inventory use for SAML signing material.
         */
        private static final String SIGNING_USE = "signing";

        /**
         * Immutable Provider signing policy.
         */
        private final SamlProviderSettings settings;

        /**
         * External exact-key resolver.
         */
        private final KeyResolver keyResolver;

        /**
         * Deterministic SAML protocol serializer.
         */
        private final SamlMessageCodec messageCodec;

        /**
         * Deterministic SAML metadata serializer.
         */
        private final MetadataCodec metadataCodec;

        /**
         * Creates an XML signer bound to one compiled server-role Source runtime.
         *
         * @param settings      validated Provider settings
         * @param keyResolver   external signing-key resolver
         * @param messageCodec  deterministic SAML message codec
         * @param metadataCodec deterministic SAML metadata codec
         * @throws IllegalArgumentException if a collaborator is {@code null}
         */
        private XmlSigner(final SamlProviderSettings settings, final KeyResolver keyResolver,
                final SamlMessageCodec messageCodec, final MetadataCodec metadataCodec) {
            this.settings = Assert.notNull(settings, "SAML signing settings must not be null");
            this.keyResolver = Assert.notNull(keyResolver, "SAML signing key resolver must not be null");
            this.messageCodec = Assert.notNull(messageCodec, "SAML signing message codec must not be null");
            this.metadataCodec = Assert.notNull(metadataCodec, "SAML signing metadata codec must not be null");
        }

        /**
         * Selects the schema position before which an enveloped Signature is inserted.
         *
         * @param root identified SAML root element
         * @return first child following the optional Issuer, or {@code null} when Signature is the final child
         */
        private static Node signatureInsertionPoint(final Element root) {
            Node child = root.getFirstChild();
            while (child != null) {
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    final Element element = (Element) child;
                    if (!("urn:oasis:names:tc:SAML:2.0:assertion".equals(element.getNamespaceURI())
                            && "Issuer".equals(element.getLocalName())))
                        return child;
                }
                child = child.getNextSibling();
            }
            return null;
        }

        /**
         * Serializes one generated XML element without an XML declaration.
         *
         * @param node generated Signature element
         * @return standalone UTF-8 element bytes
         */
        private static byte[] serialize(final Node node) {
            try {
                final TransformerFactory factory = TransformerFactory.newInstance();
                factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, Normal.EMPTY);
                factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, Normal.EMPTY);
                final var transformer = factory.newTransformer();
                transformer.setOutputProperty(OutputKeys.ENCODING, Charset.DEFAULT_UTF_8);
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                transformer.setOutputProperty(OutputKeys.INDENT, "no");
                final ByteArrayOutputStream output = new ByteArrayOutputStream();
                transformer.transform(new DOMSource(node), new StreamResult(output));
                return output.toByteArray();
            } catch (Exception exception) {
                throw new ValidateException("SAML Signature serialization failed", exception);
            }
        }

        /**
         * Returns an assertion copy containing one complete XML Signature.
         *
         * @param assertion source assertion
         * @param signature generated Signature element
         * @return signed assertion copy
         */
        private static Assertion withSignature(final Assertion assertion, final byte[] signature) {
            return new Assertion(assertion.version(), assertion.id(), assertion.issueInstant(), assertion.issuer(),
                    Optional.of(signature), assertion.subject(), assertion.conditions(), assertion.advice(),
                    assertion.statements());
        }

        /**
         * Returns a response copy containing one complete XML Signature.
         *
         * @param response  source response
         * @param signature generated Signature element
         * @return signed response copy
         */
        private static Response withSignature(final Response response, final byte[] signature) {
            return new Response(response.id(), response.inResponseTo(), response.version(), response.issueInstant(),
                    response.destination(), response.consent(), response.issuer(), Optional.of(signature),
                    response.extensions(), response.status(), response.assertions());
        }

        /**
         * Returns a logout response copy containing one complete XML Signature.
         *
         * @param response  source response
         * @param signature generated Signature element
         * @return signed logout response copy
         */
        private static LogoutResponse withSignature(final LogoutResponse response, final byte[] signature) {
            return new LogoutResponse(response.id(), response.inResponseTo(), response.version(),
                    response.issueInstant(), response.destination(), response.consent(), response.issuer(),
                    Optional.of(signature), response.extensions(), response.status());
        }

        /**
         * Returns a metadata copy with the generated signing ID.
         *
         * @param descriptor source descriptor
         * @param id         generated XML ID
         * @return identified descriptor copy
         */
        private static EntityDescriptor identified(final EntityDescriptor descriptor, final String id) {
            return new EntityDescriptor(descriptor.entityId(), descriptor.validUntil(), descriptor.cacheDuration(),
                    Optional.of(id), descriptor.signature(), descriptor.extensions(), descriptor.identityProviders(),
                    descriptor.serviceProviders(), descriptor.organization(), descriptor.contacts(),
                    descriptor.additionalMetadataLocations(), descriptor.attributes());
        }

        /**
         * Returns a metadata copy containing one complete XML Signature.
         *
         * @param descriptor identified source descriptor
         * @param signature  generated Signature element
         * @return signed descriptor copy
         */
        private static EntityDescriptor withSignature(final EntityDescriptor descriptor, final byte[] signature) {
            return new EntityDescriptor(descriptor.entityId(), descriptor.validUntil(), descriptor.cacheDuration(),
                    descriptor.id(), Optional.of(signature), descriptor.extensions(), descriptor.identityProviders(),
                    descriptor.serviceProviders(), descriptor.organization(), descriptor.contacts(),
                    descriptor.additionalMetadataLocations(), descriptor.attributes());
        }

        /**
         * Creates a non-sensitive operational failure.
         *
         * @param description safe diagnostic description
         * @param <T>         expected success type
         * @return failed outcome
         */
        private static <T> Outcome<T> failed(final String description) {
            return Outcome
                    .failed(new Outcome.Failure(ErrorCode._500, description, new JsonValue.ObjectValue(Map.of())));
        }

        /**
         * Creates a completed outcome stage.
         *
         * @param outcome completed outcome
         * @param <T>     success type
         * @return completed stage
         */
        private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
            return CompletableFuture.completedFuture(outcome);
        }

        /**
         * Signs the required assertions and/or response according to Provider policy.
         *
         * @param response unsigned successful standard Response
         * @param context  immutable invocation context
         * @param timeout  shared end-to-end budget
         * @return stage containing a policy-complete signed Response
         */
        private CompletionStage<Outcome<Response>> sign(
                final Response response,
                final Context context,
                final Timeout.Budget timeout) {
            Assert.notNull(response, "SAML Response to sign must not be null");
            return resolve(context, timeout).thenApply(outcome -> switch (outcome) {
                case Outcome.Succeeded<KeyResolver.ResolvedKey> success -> {
                    try {
                        Response signed = response;
                        if (settings.signAssertions() && !signed.assertions().isEmpty()) {
                            signed = signAssertions(signed, privateKey(success.value(), timeout));
                        }
                        if (settings.signResponses() || signed.assertions().isEmpty())
                            signed = withSignature(
                                    signed,
                                    signature(
                                            messageCodec.encode(signed),
                                            signed.id(),
                                            privateKey(success.value(), timeout)));
                        yield Outcome.succeeded(signed);
                    } catch (RuntimeException exception) {
                        yield failed("SAML Response XML signing failed");
                    }
                }
                case Outcome.Rejected<KeyResolver.ResolvedKey> rejected -> Outcome.rejected(rejected.failure());
                case Outcome.Failed<KeyResolver.ResolvedKey> failed -> Outcome.failed(failed.failure());
            });
        }

        /**
         * Signs every successful LogoutResponse before it leaves the identity-provider Source runtime.
         *
         * @param response unsigned standard LogoutResponse
         * @param context  immutable invocation context
         * @param timeout  shared end-to-end budget
         * @return stage containing a signed LogoutResponse
         */
        private CompletionStage<Outcome<LogoutResponse>> sign(
                final LogoutResponse response,
                final Context context,
                final Timeout.Budget timeout) {
            Assert.notNull(response, "SAML LogoutResponse to sign must not be null");
            return resolve(context, timeout).thenApply(outcome -> switch (outcome) {
                case Outcome.Succeeded<KeyResolver.ResolvedKey> success -> {
                    try {
                        yield Outcome.succeeded(
                                withSignature(
                                        response,
                                        signature(
                                                messageCodec.encode(response),
                                                response.id(),
                                                privateKey(success.value(), timeout))));
                    } catch (RuntimeException exception) {
                        yield failed("SAML LogoutResponse XML signing failed");
                    }
                }
                case Outcome.Rejected<KeyResolver.ResolvedKey> rejected -> Outcome.rejected(rejected.failure());
                case Outcome.Failed<KeyResolver.ResolvedKey> failed -> Outcome.failed(failed.failure());
            });
        }

        /**
         * Assigns an ID when required and signs published identity-provider metadata.
         *
         * @param descriptor unsigned standard EntityDescriptor
         * @param context    immutable invocation context
         * @param timeout    shared end-to-end budget
         * @return stage containing a signed EntityDescriptor
         */
        private CompletionStage<Outcome<EntityDescriptor>> sign(
                final EntityDescriptor descriptor,
                final Context context,
                final Timeout.Budget timeout) {
            Assert.notNull(descriptor, "SAML EntityDescriptor to sign must not be null");
            final EntityDescriptor identified = descriptor.id().isPresent() ? descriptor
                    : identified(descriptor, Symbol.C_UNDERLINE + UUID.randomUUID().toString(true));
            return resolve(context, timeout).thenApply(outcome -> switch (outcome) {
                case Outcome.Succeeded<KeyResolver.ResolvedKey> success -> {
                    try {
                        yield Outcome.succeeded(
                                withSignature(
                                        identified,
                                        signature(
                                                metadataCodec.encode(identified),
                                                identified.id().getOrNull(),
                                                privateKey(success.value(), timeout))));
                    } catch (RuntimeException exception) {
                        yield failed("SAML Metadata XML signing failed");
                    }
                }
                case Outcome.Rejected<KeyResolver.ResolvedKey> rejected -> Outcome.rejected(rejected.failure());
                case Outcome.Failed<KeyResolver.ResolvedKey> failed -> Outcome.failed(failed.failure());
            });
        }

        /**
         * Resolves the exact current Provider signing key.
         *
         * @param context immutable invocation context
         * @param timeout shared end-to-end budget
         * @return stage containing current key material or a closed failure
         */
        private CompletionStage<Outcome<KeyResolver.ResolvedKey>> resolve(
                final Context context,
                final Timeout.Budget timeout) {
            Assert.notNull(context, "SAML signing context must not be null");
            Assert.notNull(timeout, "SAML signing budget must not be null");
            if (timeout.expired())
                return completed(failed("SAML XML signing has no remaining time budget"));
            final KeyResolver.Query query = new KeyResolver.Query(settings.entityId(),
                    Optional.of(settings.signingKeyId()), SIGNING_USE, settings.signatureAlgorithm(),
                    timeout.clock().now());
            try {
                final CompletionStage<Outcome<KeyResolver.ResolvedKey>> stage = keyResolver
                        .resolve(query, context, timeout);
                if (stage == null)
                    return completed(failed("SAML signing key resolver returned no stage"));
                return stage.handle(
                        (outcome, cause) -> cause == null && outcome != null ? outcome
                                : XmlSigner.<KeyResolver.ResolvedKey>failed("SAML signing key resolution failed"));
            } catch (RuntimeException exception) {
                return completed(failed("SAML signing key resolution failed"));
            }
        }

        /**
         * Validates resolved key selection and returns its private key.
         *
         * @param resolved resolved external key
         * @param timeout  shared clock source
         * @return validated private key
         * @throws ValidateException if key identity, algorithm, type, or validity does not match settings
         */
        private PrivateKey privateKey(final KeyResolver.ResolvedKey resolved, final Timeout.Budget timeout) {
            Assert.notNull(resolved, "Resolved SAML signing key must not be null");
            final Instant now = timeout.clock().now();
            if (!settings.signingKeyId().equals(resolved.keyId())
                    || !settings.signatureAlgorithm().equals(resolved.algorithm())
                    || !(resolved.key() instanceof PrivateKey privateKey) || now.isBefore(resolved.notBefore())
                    || !now.isBefore(resolved.notAfter())) {
                throw new ValidateException("Resolved SAML signing key does not match Provider policy");
            }
            return privateKey;
        }

        /**
         * Signs every plain assertion while rejecting encrypted output from the local issuer.
         *
         * @param response response containing locally issued assertions
         * @param key      validated signing private key
         * @return response containing signed assertions
         */
        private Response signAssertions(final Response response, final PrivateKey key) {
            final List<Response.AssertionContent> assertions = new ArrayList<>(response.assertions().size());
            for (Response.AssertionContent content : response.assertions()) {
                if (!(content instanceof Response.PlainAssertion plain)) {
                    throw new ValidateException("SAML Provider cannot emit an encrypted unsigned assertion");
                }
                final Assertion assertion = plain.assertion();
                final byte[] signature = signature(messageCodec.encode(assertion), assertion.id(), key);
                assertions.add(new Response.PlainAssertion(withSignature(assertion, signature)));
            }
            return new Response(response.id(), response.inResponseTo(), response.version(), response.issueInstant(),
                    response.destination(), response.consent(), response.issuer(), response.signature(),
                    response.extensions(), response.status(), assertions);
        }

        /**
         * Generates one enveloped XML Signature over the exact identified root.
         *
         * @param xml deterministic unsigned XML document
         * @param id  root XML ID referenced by the signature
         * @param key validated private key
         * @return complete generated {@code ds:Signature} element bytes
         * @throws ValidateException if secure parsing, root identity, signing, or serialization fails
         */
        private byte[] signature(final byte[] xml, final String id, final PrivateKey key) {
            try {
                final DocumentBuilderFactory builder = DocumentBuilderFactory.newInstance();
                builder.setNamespaceAware(true);
                builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                builder.setFeature("http://xml.org/sax/features/external-general-entities", false);
                builder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                builder.setXIncludeAware(false);
                builder.setExpandEntityReferences(false);
                builder.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, Normal.EMPTY);
                builder.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, Normal.EMPTY);
                final org.w3c.dom.Document document = builder.newDocumentBuilder().parse(new ByteArrayInputStream(xml));
                final Element root = Assert.notNull(document.getDocumentElement(), "SAML signing root must exist");
                if (!id.equals(root.getAttribute("ID"))) {
                    throw new ValidateException("SAML signing root ID does not match the requested reference");
                }
                root.setIdAttribute("ID", true);
                final XMLSignatureFactory factory = XMLSignatureFactory.getInstance("DOM");
                final Transform enveloped = factory
                        .newTransform(Transform.ENVELOPED, (javax.xml.crypto.dsig.spec.TransformParameterSpec) null);
                final Transform canonical = factory.newTransform(
                        CanonicalizationMethod.EXCLUSIVE,
                        (javax.xml.crypto.dsig.spec.TransformParameterSpec) null);
                final Reference reference = factory.newReference(
                        Symbol.C_HASH + id,
                        factory.newDigestMethod(DigestMethod.SHA256, null),
                        List.of(enveloped, canonical),
                        null,
                        null);
                final SignedInfo signedInfo = factory.newSignedInfo(
                        factory.newCanonicalizationMethod(
                                CanonicalizationMethod.EXCLUSIVE,
                                (javax.xml.crypto.dsig.spec.C14NMethodParameterSpec) null),
                        factory.newSignatureMethod(settings.signatureAlgorithm(), null),
                        List.of(reference));
                final Node insertionPoint = signatureInsertionPoint(root);
                final DOMSignContext signingContext = insertionPoint == null ? new DOMSignContext(key, root)
                        : new DOMSignContext(key, root, insertionPoint);
                signingContext.setDefaultNamespacePrefix("ds");
                signingContext.setProperty("org.jcp.xml.dsig.secureValidation", Boolean.TRUE);
                factory.newXMLSignature(signedInfo, null).sign(signingContext);
                final NodeList signatures = root.getElementsByTagNameNS(XML_SIGNATURE_NAMESPACE, "Signature");
                if (signatures.getLength() != 1) {
                    throw new ValidateException("SAML signing produced an ambiguous Signature element");
                }
                return serialize(signatures.item(0));
            } catch (ValidateException exception) {
                throw exception;
            } catch (Exception exception) {
                throw new ValidateException("SAML XML Signature generation failed", exception);
            }
        }

    }

}
