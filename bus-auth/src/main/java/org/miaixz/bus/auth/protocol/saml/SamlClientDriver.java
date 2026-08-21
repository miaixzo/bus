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
package org.miaixz.bus.auth.protocol.saml;

import java.io.ByteArrayInputStream;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.cache.ExpiringValue;
import org.miaixz.bus.auth.cache.StateCache;
import org.miaixz.bus.auth.guard.ReplayGuard;
import org.miaixz.bus.auth.protocol.saml.client.*;
import org.miaixz.bus.auth.protocol.saml.codec.MetadataCodec;
import org.miaixz.bus.auth.protocol.saml.codec.PostBindingCodec;
import org.miaixz.bus.auth.protocol.saml.codec.RedirectBindingCodec;
import org.miaixz.bus.auth.protocol.saml.codec.SamlMessageCodec;
import org.miaixz.bus.auth.protocol.saml.security.SamlAssertionValidator;
import org.miaixz.bus.auth.protocol.saml.security.SamlDecryptionService;
import org.miaixz.bus.auth.protocol.saml.security.SamlReplayValidator;
import org.miaixz.bus.auth.protocol.saml.security.SamlSignatureValidator;
import org.miaixz.bus.auth.resolver.KeyMaterial;
import org.miaixz.bus.auth.source.*;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.worker.KeyLoader;
import org.miaixz.bus.auth.worker.SourceWorker;
import org.miaixz.bus.auth.worker.WorkerSlots;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.data.id.UUID;
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.crypto.center.Sign;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.UnoUrl;

/**
 * Compiles one registered SAML 2.0 service-provider Source into its typed protocol runtime.
 * <p>
 * The driver injects all trust, replay, decryption, transport, and signing dependencies from DriverServices. It exposes
 * both standard SAML operations and the two protocol-neutral Source-authentication capabilities. Its internal adapter
 * performs browser correlation, POST decoding, original-document signature verification, assertion consumption, and
 * verified identity mapping without exposing those application concerns as SAML wire operations.
 * </p>
 *
 * @author Kimi Liu
 */
public final class SamlClientDriver implements SourceDriver<SamlClientOptions> {

    /**
     * Secure maximum DOM nesting depth shared by Source codecs.
     */
    private static final int MAXIMUM_XML_DEPTH = Normal._64;

    /**
     * Standard signing-key use supplied to the external key inventory.
     */
    private static final String SIGNING_USE = org.miaixz.bus.auth.Builder.SIGNING;

    /**
     * Fixed lifetime of a one-time SAML browser interaction.
     */
    private static final Duration BROWSER_INTERACTION_LIFETIME = Duration.ofMinutes(10);

    /**
     * Isolated StateCache purpose used only by SAML browser interactions.
     */
    private static final String BROWSER_STATE_PURPOSE = "saml-browser";

    /**
     * Entity NameID format used by the service-provider Issuer.
     */
    private static final String ENTITY_NAME_ID = Saml.NameIdFormats.ENTITY;

    /**
     * Persistent NameID format requested for stable external identity mapping.
     */
    private static final String PERSISTENT_NAME_ID = Saml.NameIdFormats.PERSISTENT;

    /**
     * Creates a stateless SAML Source driver.
     */
    public SamlClientDriver() {
        // No initialization required.
    }

    /**
     * Creates the Redirect Binding signing operation backed by the exact external key loader and pure parser.
     *
     * @param options  trusted Source options
     * @param services runtime key and clock dependencies
     * @return asynchronous exact-byte signing operation
     */
    private static RedirectBindingCodec.SigningOperation signingOperation(
            final SamlClientOptions options,
            final DriverServices services) {
        return (keyId, algorithm, input, context, timeout) -> {
            if (!options.signingKeyId().equals(keyId) || !options.signatureAlgorithm().equals(algorithm)
                    || !services.securityBaseline().require(Protocol.SAML).algorithms().contains(algorithm)) {
                return completed(rejected("SAML Redirect signing selection does not match Source options"));
            }
            final Instant now = timeout.clock().now();
            final KeyLoader.Request query = new KeyLoader.Request(options.entityId(), Optional.of(keyId), SIGNING_USE,
                    algorithm, now);
            final CompletionStage<Outcome<KeyMaterial>> resolution;
            try {
                resolution = Outcome.mapStage(
                        () -> services.keyLoader().load(services.registration(), query, context, timeout),
                        loaded -> services.keyParser().parse(services.registration(), query, loaded));
            } catch (RuntimeException exception) {
                return completed(failed("SAML Redirect signing key resolution failed"));
            }
            if (resolution == null)
                return completed(failed("SAML Redirect signing key loader returned no stage"));
            return resolution.exceptionally(
                    cause -> SamlClientDriver.<KeyMaterial>failed("SAML Redirect signing key resolution failed"))
                    .thenApply(outcome -> switch (outcome) {
                        case Outcome.Succeeded<KeyMaterial> success -> sign(
                                success.value(),
                                keyId,
                                algorithm,
                                input,
                                now);
                        case Outcome.Rejected<KeyMaterial> rejected -> Outcome.rejected(rejected.failure());
                        case Outcome.Failed<KeyMaterial> failed -> Outcome.failed(failed.failure());
                    });
        };
    }

    /**
     * Signs exact Redirect Binding bytes with a validated resolved private key.
     *
     * @param resolved  resolved key material
     * @param keyId     expected key identifier
     * @param algorithm expected XML Signature algorithm URI
     * @param input     exact signed input bytes
     * @param now       shared-clock validity instant
     * @return signature bytes or a closed failure
     */
    private static Outcome<byte[]> sign(
            final KeyMaterial resolved,
            final String keyId,
            final String algorithm,
            final byte[] input,
            final Instant now) {
        try {
            if (!keyId.equals(resolved.keyId()) || !algorithm.equals(resolved.algorithm())
                    || !(resolved.key() instanceof PrivateKey privateKey) || now.isBefore(resolved.notBefore())
                    || !now.isBefore(resolved.notAfter())) {
                return rejected("Resolved SAML Redirect signing key does not match Source options");
            }
            final Sign signer = new Sign(coreAlgorithm(algorithm), new KeyPair(null, privateKey));
            return Outcome.succeeded(signer.sign(input));
        } catch (Exception exception) {
            return failed("SAML Redirect signing failed");
        }
    }

    /**
     * Maps the implemented SAML signature-method URIs to exact JCA names.
     *
     * @param algorithm standard XML Signature method URI
     * @return exact JCA signature algorithm
     */
    private static Algorithm coreAlgorithm(final String algorithm) {
        return switch (algorithm) {
            case "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256" -> Algorithm.SHA256WITHRSA;
            case "http://www.w3.org/2001/04/xmldsig-more#rsa-sha384" -> Algorithm.SHA384WITHRSA;
            case "http://www.w3.org/2001/04/xmldsig-more#rsa-sha512" -> Algorithm.SHA512WITHRSA;
            case "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256" -> Algorithm.SHA256WITHECDSA;
            case "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha384" -> Algorithm.SHA384WITHECDSA;
            case "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha512" -> Algorithm.SHA512WITHECDSA;
            default -> throw new ValidateException("Unsupported SAML Redirect signature algorithm");
        };
    }

    /**
     * Builds the exact capability set represented by configured Source endpoints.
     *
     * @param options validated Source options
     * @return immutable endpoint-accurate capability manifest
     */
    private static Capability.Manifest manifest(final SamlClientOptions options) {
        final List<Capability<?, ?>> capabilities = new ArrayList<>();
        capabilities.add(SamlClientScheme.SINGLE_SIGN_ON);
        if (options.singleLogoutServiceEndpoint().isPresent())
            capabilities.add(SamlClientScheme.SINGLE_LOGOUT);
        capabilities.add(SamlClientScheme.METADATA);
        capabilities.add(SourceWorkflow.INITIATE);
        capabilities.add(SourceWorkflow.COMPLETE);
        return new Capability.Manifest(capabilities);
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
     * Creates a safe expected signing rejection.
     *
     * @param description non-sensitive description
     * @param <T>         expected success type
     * @return rejected outcome
     */
    private static <T> Outcome<T> rejected(final String description) {
        return Outcome.rejected(new Outcome.Failure(ErrorCode._400, description, new JsonValue.ObjectValue(Map.of())));
    }

    /**
     * Creates a safe operational signing failure.
     *
     * @param description non-sensitive description
     * @param <T>         expected success type
     * @return failed outcome
     */
    private static <T> Outcome<T> failed(final String description) {
        return Outcome.failed(new Outcome.Failure(ErrorCode._500, description, new JsonValue.ObjectValue(Map.of())));
    }

    /**
     * Returns the SAML service-provider scheme bound to this driver.
     *
     * @return immutable SAML Source scheme
     */
    @Override
    public SamlClientScheme scheme() {
        return new SamlClientScheme();
    }

    @Override
    public SamlClientOptions require(final Options<?> options) {
        if (options instanceof SamlClientOptions value) {
            return value;
        }
        throw new ValidateException("SAML client driver requires SamlClientOptions");
    }

    @Override
    public WorkerSlots slots(final Source source, final SamlClientOptions options) {
        return WorkerSlots.of(WorkerSlots.Slot.KEY, WorkerSlots.Slot.CERTIFICATE);
    }

    @Override
    public Dependencies dependencies(final Source source, final SamlClientOptions options) {
        return Dependencies.of(
                Dependencies.Service.FABRIC_CONTEXT,
                Dependencies.Service.EXECUTOR,
                Dependencies.Service.STATE_CACHE,
                Dependencies.Service.REPLAY_CACHE,
                Dependencies.Service.SECURITY_BASELINE);
    }

    /**
     * Consumes typed options and assembles one endpoint-accurate service-provider runtime.
     *
     * @param prepared one-time validated Source graph, Options and dependency declaration
     * @param services dependency-scoped runtime services
     * @return immutable executable SAML Source
     * @throws IllegalArgumentException if an argument is {@code null}
     * @throws ValidateException        if routing, options, or security policy is invalid
     */
    @Override
    public SourceWorker compile(final Prepared<SamlClientOptions> prepared, final DriverServices services) {
        Assert.notNull(prepared, "SAML Source preparation must not be null");
        Assert.notNull(services, "SAML Source execution services must not be null");
        final Registration.SourceEntry record = prepared.registration();
        final Provider provider = prepared.provider();
        final Library library = prepared.library();
        final Source source = record.resource();
        final String namespace = library.getNamespace_id();
        if (!scheme().id().equals(source.getType()) || !supports(source.getProtocol()) || namespace == null
                || namespace.isBlank() || !provider.getId().equals(source.getProvider_id())
                || !library.getId().equals(provider.getLibrary_id())) {
            throw new ValidateException("SAML Source driver requires a matching Source registration");
        }
        final SamlClientOptions options = prepared.options();
        final var policy = services.securityBaseline().require(Protocol.SAML);
        if (!policy.algorithms().contains(options.signatureAlgorithm())) {
            throw new ValidateException("SAML Source signature algorithm is outside the security baseline");
        }
        if (options.clockSkew().compareTo(policy.maximumClockSkew()) > 0) {
            throw new ValidateException("SAML Source clock skew exceeds the security baseline");
        }
        final SamlMessageCodec messageCodec = new SamlMessageCodec(policy.maximumMessageBytes(), MAXIMUM_XML_DEPTH);
        final MetadataCodec metadataCodec = new MetadataCodec(policy.maximumMessageBytes(), MAXIMUM_XML_DEPTH);
        final SamlSignatureValidator signatureValidator = new SamlSignatureValidator(services,
                services.securityBaseline());
        final MetadataClient metadataClient = new MetadataClient(options, services, metadataCodec, signatureValidator);
        final SamlAssertionValidator assertionValidator = new SamlAssertionValidator(services.securityBaseline());
        final SamlReplayValidator replayValidator = new SamlReplayValidator(new ReplayGuard(services.replayCache()),
                namespace);
        final SamlDecryptionService decryptionService = new SamlDecryptionService(services, messageCodec,
                signatureValidator, services.securityBaseline());
        final AssertionConsumerService consumer = new AssertionConsumerService(options, assertionValidator,
                replayValidator, decryptionService);
        final PostBindingCodec post = new PostBindingCodec(messageCodec);
        final RedirectBindingCodec redirect = new RedirectBindingCodec(messageCodec,
                signingOperation(options, services));
        final SamlServiceProvider serviceProvider = new SamlServiceProvider(metadataClient, consumer, redirect,
                options);
        final SourceAdapter adapter = new SourceAdapter(source.getId(), namespace, options, serviceProvider, post,
                signatureValidator, services.stateCache());
        return new CompiledClient(manifest(options), serviceProvider, adapter);
    }

    /**
     * Routes exact declared SAML Source capabilities to the compiled service-provider facade.
     *
     * @author Kimi Liu
     */
    private static final class CompiledClient implements SourceWorker {

        /**
         * Endpoint-accurate immutable capability manifest.
         */
        private final Capability.Manifest manifest;

        /**
         * Compiled typed SAML service-provider facade.
         */
        private final SamlServiceProvider serviceProvider;

        /**
         * Application-level browser authentication adapter.
         */
        private final SourceAdapter adapter;

        /**
         * Creates one immutable compiled Source.
         *
         * @param manifest        exact runtime manifest
         * @param serviceProvider typed SAML facade
         * @param adapter         application-level browser adapter
         */
        private CompiledClient(final Capability.Manifest manifest, final SamlServiceProvider serviceProvider,
                final SourceAdapter adapter) {
            this.manifest = Assert.notNull(manifest, "SAML Source manifest must not be null");
            this.serviceProvider = Assert.notNull(serviceProvider, "SAML service-provider facade must not be null");
            this.adapter = Assert.notNull(adapter, "SAML Source browser adapter must not be null");
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
                            new Outcome.Failure(ErrorCode._404, "SAML Source capability is not available",
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
                                    "SAML Source capability type does not match the request",
                                    new JsonValue.ObjectValue(Map.of()))));
        }

        /**
         * Returns the exact endpoint-backed capability manifest.
         *
         * @return immutable SAML Source manifest
         */
        @Override
        public Capability.Manifest manifest() {
            return manifest;
        }

        /**
         * Invokes one exact declared SAML Source capability.
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
            Assert.notNull(capability, "SAML Source capability must not be null");
            Assert.notNull(context, "SAML Source context must not be null");
            Assert.notNull(timeout, "SAML Source budget must not be null");
            if (!manifest.capabilities().contains(capability))
                return missing();
            if (capability == SamlClientScheme.SINGLE_SIGN_ON) {
                if (request == null || request.getClass() != AuthnRequest.class
                        || capability.requestType() != AuthnRequest.class || capability.responseType() != UnoUrl.class)
                    return mismatch();
                return narrow(
                        serviceProvider.singleSignOn((AuthnRequest) request, Optional.empty(), context, timeout),
                        capability.responseType());
            }
            if (capability == SamlClientScheme.SINGLE_LOGOUT) {
                if (request == null || request.getClass() != LogoutRequest.class
                        || capability.requestType() != LogoutRequest.class || capability.responseType() != UnoUrl.class)
                    return mismatch();
                return narrow(
                        serviceProvider.singleLogout((LogoutRequest) request, Optional.empty(), context, timeout),
                        capability.responseType());
            }
            if (capability == SamlClientScheme.METADATA) {
                if (request != null || capability.requestType() != Void.class
                        || capability.responseType() != EntityDescriptor.class)
                    return mismatch();
                return narrow(serviceProvider.metadata(context, timeout), capability.responseType());
            }
            if (capability == SourceWorkflow.INITIATE) {
                if (!(request instanceof SourceWorkflow.Request.BrowserStart start)
                        || capability.requestType() != SourceWorkflow.Request.Start.class
                        || capability.responseType() != SourceWorkflow.Stage.class)
                    return mismatch();
                return narrow(adapter.initiate(start, context, timeout), capability.responseType());
            }
            if (capability == SourceWorkflow.COMPLETE) {
                if (!(request instanceof SourceWorkflow.Request.BrowserCallback callback)
                        || capability.requestType() != SourceWorkflow.Request.Completion.class
                        || capability.responseType() != ExternalIdentity.class)
                    return mismatch();
                return narrow(adapter.complete(callback, context, timeout), capability.responseType());
            }
            return missing();
        }

    }

    /**
     * Adapts SAML Web Browser SSO to the protocol-neutral Source authentication entry points.
     * <p>
     * Each instance belongs to one compiled Source. The request ID is also the opaque RelayState and the one-time
     * correlation value, allowing the callback to recover the exact expected {@code InResponseTo} without storing a
     * protocol message or extending the shared callback contract.
     * </p>
     *
     * @author Kimi Liu
     */
    private static final class SourceAdapter {

        /**
         * Registered Source identifier represented by this runtime.
         */
        private final String sourceId;

        /**
         * Namespace used to isolate one-time state keys.
         */
        private final String namespace;

        /**
         * Validated service-provider options.
         */
        private final SamlClientOptions options;

        /**
         * Standard SAML service-provider operations.
         */
        private final SamlServiceProvider serviceProvider;

        /**
         * Strict HTTP-POST Binding codec.
         */
        private final PostBindingCodec postBindingCodec;

        /**
         * Original-document XML Signature validator.
         */
        private final SamlSignatureValidator signatureValidator;

        /**
         * Framework callback-state cache backed by bus-cache.
         */
        private final StateCache stateCache;

        /**
         * Creates one Source-bound SAML browser adapter.
         *
         * @param sourceId           registered Source identifier
         * @param namespace          namespace resolved from the owning Library
         * @param options            validated service-provider options
         * @param serviceProvider    standard SAML service-provider facade
         * @param postBindingCodec   strict HTTP-POST Binding codec
         * @param signatureValidator original-document signature validator
         * @param stateCache         atomic one-time callback state cache
         * @throws IllegalArgumentException if text is blank or a collaborator is {@code null}
         */
        private SourceAdapter(final String sourceId, final String namespace, final SamlClientOptions options,
                final SamlServiceProvider serviceProvider, final PostBindingCodec postBindingCodec,
                final SamlSignatureValidator signatureValidator, final StateCache stateCache) {
            this.sourceId = Assert.notBlank(sourceId, "SAML Source id must not be blank");
            this.namespace = Assert.notBlank(namespace, "SAML owning Library namespace must not be blank");
            this.options = Assert.notNull(options, "SAML Source adapter options must not be null");
            this.serviceProvider = Assert
                    .notNull(serviceProvider, "SAML Source adapter service provider must not be null");
            this.postBindingCodec = Assert.notNull(postBindingCodec, "SAML Source POST Binding codec must not be null");
            this.signatureValidator = Assert
                    .notNull(signatureValidator, "SAML Source signature validator must not be null");
            this.stateCache = Assert.notNull(stateCache, "SAML Source state cache must not be null");
        }

        /**
         * Converts only text-only SAML AttributeValue elements into provider-neutral JSON attributes.
         *
         * @param assertion trusted assertion
         * @return immutable safe attribute object
         */
        private static JsonValue.ObjectValue attributes(final Assertion assertion) {
            final Map<String, List<JsonValue>> collected = new LinkedHashMap<>();
            for (Assertion.StatementContent statement : assertion.statements()) {
                if (!(statement instanceof Assertion.AttributesStatement values))
                    continue;
                for (AttributeStatement.AttributeContent content : values.statement().attributes()) {
                    if (!(content instanceof AttributeStatement.PlainAttribute plain))
                        continue;
                    final Attribute attribute = plain.attribute();
                    final List<JsonValue> target = collected
                            .computeIfAbsent(attribute.name(), ignored -> new ArrayList<>());
                    for (byte[] xml : attribute.values()) {
                        final String text = textAttributeValue(xml);
                        if (text != null)
                            target.add(new JsonValue.StringValue(text));
                    }
                }
            }
            final Map<String, JsonValue> result = new LinkedHashMap<>();
            collected.forEach((name, values) -> {
                if (values.size() == 1)
                    result.put(name, values.getFirst());
                else if (!values.isEmpty())
                    result.put(name, new JsonValue.ArrayValue(values));
            });
            return new JsonValue.ObjectValue(result);
        }

        /**
         * Reads one safe text-only AttributeValue and ignores complex XML values.
         *
         * @param xml complete trusted AttributeValue element
         * @return decoded text, or {@code null} for a complex element
         */
        private static String textAttributeValue(final byte[] xml) {
            try {
                final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
                factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                factory.setXIncludeAware(false);
                factory.setExpandEntityReferences(false);
                factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, Normal.EMPTY);
                factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, Normal.EMPTY);
                final Element root = factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml))
                        .getDocumentElement();
                if (!Saml.Namespaces.ASSERTION.equals(root.getNamespaceURI())
                        || !"AttributeValue".equals(root.getLocalName()))
                    return null;
                for (Node child = root.getFirstChild(); child != null; child = child.getNextSibling()) {
                    if (child.getNodeType() == Node.ELEMENT_NODE)
                        return null;
                }
                return root.getTextContent();
            } catch (Exception exception) {
                return null;
            }
        }

        /**
         * Validates common invocation containers.
         *
         * @param context immutable invocation context
         * @param timeout shared end-to-end budget
         * @throws IllegalArgumentException if a component is {@code null}
         */
        private static void invocation(final Context context, final Timeout.Budget timeout) {
            Assert.notNull(context, "SAML Source invocation context must not be null");
            Assert.notNull(timeout, "SAML Source invocation budget must not be null");
        }

        /**
         * Starts one service-provider initiated Web Browser SSO interaction.
         *
         * @param request Source-bound browser start request
         * @param context immutable invocation context
         * @param timeout shared end-to-end budget
         * @return stage containing a redirect and durable one-time correlation
         */
        private CompletionStage<Outcome<SourceWorkflow.Stage>> initiate(
                final SourceWorkflow.Request.BrowserStart request,
                final Context context,
                final Timeout.Budget timeout) {
            Assert.notNull(request, "SAML browser start request must not be null");
            invocation(context, timeout);
            if (!sourceId.equals(request.sourceId())
                    || !options.assertionConsumerServiceUrl().equals(request.callbackTarget().redirectUri())) {
                return completed(rejected("SAML browser start does not match the registered Source callback"));
            }
            if (timeout.expired())
                return completed(failed("SAML browser initiation has no remaining time budget"));
            final String requestId = Symbol.C_UNDERLINE + UUID.randomUUID().toString(true);
            final Instant now = timeout.clock().now();
            final Instant expiresAt = now.plus(BROWSER_INTERACTION_LIFETIME);
            final Callback.Correlation correlation = new Callback.Correlation(sourceId, requestId, Optional.empty(),
                    expiresAt);
            final AuthnRequest authenticationRequest = authenticationRequest(requestId, now);
            return serviceProvider.singleSignOn(authenticationRequest, Optional.of(requestId), context, timeout)
                    .thenCompose(outcome -> switch (outcome) {
                        case Outcome.Succeeded<UnoUrl> success -> store(
                                correlation,
                                success.value().toString(),
                                context,
                                timeout);
                        case Outcome.Rejected<UnoUrl> rejected -> completed(Outcome.rejected(rejected.failure()));
                        case Outcome.Failed<UnoUrl> failed -> completed(Outcome.failed(failed.failure()));
                    });
        }

        /**
         * Completes one correlated HTTP-POST SAML Response interaction.
         *
         * @param request Source-bound browser callback
         * @param context immutable invocation context
         * @param timeout shared end-to-end budget
         * @return stage containing a verified external identity
         */
        private CompletionStage<Outcome<ExternalIdentity>> complete(
                final SourceWorkflow.Request.BrowserCallback request,
                final Context context,
                final Timeout.Budget timeout) {
            Assert.notNull(request, "SAML browser callback request must not be null");
            invocation(context, timeout);
            if (!sourceId.equals(request.sourceId())
                    || !options.assertionConsumerServiceUrl().equals(request.callback().requestUri())
                    || timeout.expired()) {
                return completed(rejected("SAML browser callback does not match an active Source interaction"));
            }
            final PostBindingCodec.Decoded<Response> decoded;
            final String relayState;
            try {
                decoded = postBindingCodec.decode(request.callback(), Response.class);
                relayState = Assert
                        .notBlank(decoded.relayState().getOrNull(), "SAML browser callback requires RelayState");
            } catch (RuntimeException exception) {
                return completed(rejected("SAML browser callback does not contain a valid HTTP-POST Response"));
            }
            final CompletionStage<ExpiringValue<Callback.Correlation>> taken;
            try {
                taken = stateCache.consume(stateKey(relayState));
            } catch (RuntimeException exception) {
                return completed(failed("SAML browser correlation cache failed"));
            }
            if (taken == null)
                return completed(failed("SAML browser correlation cache returned no stage"));
            return taken
                    .handle(
                            (value, cause) -> cause == null ? Outcome.succeeded(value)
                                    : SamlClientDriver.<ExpiringValue<Callback.Correlation>>failed(
                                            "SAML browser correlation cache failed"))
                    .thenCompose(outcome -> switch (outcome) {
                        case Outcome.Succeeded<ExpiringValue<Callback.Correlation>> success -> validateCorrelation(
                                success.value(),
                                relayState,
                                decoded,
                                context,
                                timeout);
                        case Outcome.Rejected<ExpiringValue<Callback.Correlation>> rejected -> completed(
                                Outcome.rejected(rejected.failure()));
                        case Outcome.Failed<ExpiringValue<Callback.Correlation>> failed -> completed(
                                Outcome.failed(failed.failure()));
                    });
        }

        /**
         * Stores one-time correlation only after Redirect URL generation succeeds.
         *
         * @param correlation Source-bound correlation
         * @param location    generated SAML Redirect URL
         * @param context     immutable invocation context retained for signature parity
         * @param timeout     shared end-to-end budget retained for deadline validation
         * @return stage containing the redirect only when atomic state creation succeeds
         */
        private CompletionStage<Outcome<SourceWorkflow.Stage>> store(
                final Callback.Correlation correlation,
                final String location,
                final Context context,
                final Timeout.Budget timeout) {
            invocation(context, timeout);
            if (timeout.expired()) {
                return completed(failed("SAML browser initiation expired before correlation storage"));
            }
            final CompletionStage<Boolean> created;
            try {
                created = stateCache.issue(
                        stateKey(correlation.state()),
                        new ExpiringValue<>(correlation, correlation.expiresAt()));
            } catch (RuntimeException exception) {
                return completed(failed("SAML browser correlation cache failed"));
            }
            if (created == null)
                return completed(failed("SAML browser correlation cache returned no stage"));
            return created.handle((stored, cause) -> {
                if (cause != null || stored == null)
                    return failed("SAML browser correlation cache failed");
                if (!stored)
                    return failed("SAML browser correlation identifier collided");
                return Outcome.succeeded(new SourceWorkflow.Stage.Redirect(location, correlation));
            });
        }

        /**
         * Validates consumed correlation before starting cryptographic response processing.
         *
         * @param stored     atomically consumed correlation value
         * @param relayState exact callback RelayState
         * @param decoded    original decoded SAML Response document
         * @param context    immutable invocation context
         * @param timeout    shared end-to-end budget
         * @return stage containing the verified identity or a closed failure
         */
        private CompletionStage<Outcome<ExternalIdentity>> validateCorrelation(
                final ExpiringValue<Callback.Correlation> stored,
                final String relayState,
                final PostBindingCodec.Decoded<Response> decoded,
                final Context context,
                final Timeout.Budget timeout) {
            if (stored == null) {
                return completed(rejected("SAML browser correlation is missing or already consumed"));
            }
            final Callback.Correlation correlation = stored.value();
            final Instant now = timeout.clock().now();
            if (!sourceId.equals(correlation.sourceId()) || !relayState.equals(correlation.state())
                    || !stored.expiresAt().equals(correlation.expiresAt()) || !now.isBefore(correlation.expiresAt())) {
                return completed(rejected("SAML browser correlation is invalid or expired"));
            }
            return signatureValidator.validateResponse(decoded.document(), options, context, timeout)
                    .thenCompose(outcome -> switch (outcome) {
                        case Outcome.Succeeded<SamlMessageCodec.Document<Response>> success -> serviceProvider
                                .consume(success.value().message(), correlation.state(), context, timeout);
                        case Outcome.Rejected<SamlMessageCodec.Document<Response>> rejected -> completed(
                                Outcome.rejected(rejected.failure()));
                        case Outcome.Failed<SamlMessageCodec.Document<Response>> failed -> completed(
                                Outcome.failed(failed.failure()));
                    }).thenApply(outcome -> switch (outcome) {
                        case Outcome.Succeeded<Response> success -> identity(success.value(), timeout.clock().now());
                        case Outcome.Rejected<Response> rejected -> Outcome.rejected(rejected.failure());
                        case Outcome.Failed<Response> failed -> Outcome.failed(failed.failure());
                    });
        }

        /**
         * Builds the exact standard Authentication Request used by browser initiation.
         *
         * @param requestId unpredictable XML ID and RelayState
         * @param now       shared-clock issue instant
         * @return standard service-provider Authentication Request
         */
        private AuthnRequest authenticationRequest(final String requestId, final Instant now) {
            final Issuer issuer = new Issuer(new NameID(options.entityId(), Optional.empty(), Optional.empty(),
                    Optional.of(ENTITY_NAME_ID), Optional.empty()));
            final NameIDPolicy nameIdPolicy = new NameIDPolicy(Optional.of(PERSISTENT_NAME_ID), Optional.empty(),
                    Optional.of(Boolean.TRUE));
            return new AuthnRequest(requestId, "2.0", now,
                    Optional.of(options.singleSignOnServiceEndpoint().url().toString()), Optional.empty(),
                    Optional.of(issuer), Optional.empty(), List.of(), Optional.empty(), Optional.of(nameIdPolicy),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.of(SamlBinding.HTTP_POST), Optional.empty(),
                    Optional.of(options.assertionConsumerServiceUrl()), Optional.empty(), Optional.empty());
        }

        /**
         * Maps exactly one authenticated SAML assertion to a verified external identity.
         *
         * @param response   fully validated successful SAML Response
         * @param verifiedAt shared-clock verification instant
         * @return successful application-level result or a deterministic rejection
         */
        private Outcome<ExternalIdentity> identity(final Response response, final Instant verifiedAt) {
            Assertion selected = null;
            NameID identifier = null;
            for (Response.AssertionContent content : response.assertions()) {
                if (!(content instanceof Response.PlainAssertion plain))
                    continue;
                final Assertion assertion = plain.assertion();
                final Subject subject = assertion.subject().getOrNull();
                final Subject.Identifier choice = subject == null ? null : subject.identifier().getOrNull();
                final boolean authenticated = assertion.statements().stream()
                        .anyMatch(Assertion.AuthenticationStatement.class::isInstance);
                if (choice instanceof Subject.NamedIdentifier named && authenticated) {
                    if (selected != null)
                        return rejected("SAML Response contains ambiguous authenticated identities");
                    selected = assertion;
                    identifier = named.value();
                }
            }
            if (selected == null || identifier == null || identifier.value().isBlank()) {
                return rejected("SAML Response does not contain one authenticated NameID subject");
            }
            final JsonValue.ObjectValue attributes = attributes(selected);
            final Evidence evidence = new Evidence(Evidence.Type.FEDERATED, Evidence.Strength.SINGLE_FACTOR,
                    new Evidence.Claim("saml_subject", new JsonValue.StringValue(identifier.value()),
                            options.identityProviderEntityId(), verifiedAt));
            final ExternalIdentity identity = new ExternalIdentity(sourceId, identifier.value(), attributes,
                    List.of(evidence));
            return Outcome.succeeded(identity);
        }

        /**
         * Produces the irreversible isolated StateCache key for one RelayState.
         *
         * @param state opaque RelayState value
         * @return SHA-256 hexadecimal StateCache key
         */
        private String stateKey(final String state) {
            return Builder.sha256Hex(
                    namespace + Symbol.C_NUL + sourceId + Symbol.C_NUL + BROWSER_STATE_PURPOSE + Symbol.C_NUL + state);
        }

    }

}
