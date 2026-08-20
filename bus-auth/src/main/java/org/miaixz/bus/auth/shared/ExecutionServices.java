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
package org.miaixz.bus.auth.shared;

import java.util.concurrent.Executor;

import org.miaixz.bus.auth.Registration;
import org.miaixz.bus.auth.cache.*;
import org.miaixz.bus.auth.resolver.*;
import org.miaixz.bus.auth.shared.audit.AuditSink;
import org.miaixz.bus.auth.shared.consent.ConsentService;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.fabric.Context;

/**
 * Carries the complete immutable set of externally implemented dependencies required by bus-auth runtime assembly.
 * <p>
 * Every dependency is explicit and strongly typed. This container creates, replaces, closes, or locates none of them.
 * Fabric Clock and Reactor are obtained through {@link #fabricContext()}, and all stores implement the authentication
 * atomic-store contracts rather than a generic cache fallback.
 * </p>
 *
 * @author Kimi Liu
 */
public final class ExecutionServices {

    /**
     * Shared Fabric transport, clock, and reactor context.
     */
    private final Context fabricContext;

    /**
     * Externally selected bus-extra JSON provider.
     */
    private final JsonProvider jsonProvider;

    /**
     * Caller-owned executor for framework asynchronous work.
     */
    private final Executor executor;

    /**
     * External strongly typed binding resolver for protocol-specific runtime ports.
     */
    private final BindingResolver bindingResolver;

    /**
     * External client registration resolver.
     */
    private final ClientResolver clientResolver;

    /**
     * External stable Subject lookup resolver.
     */
    private final SubjectResolver subjectResolver;

    /**
     * External credential descriptor resolver.
     */
    private final CredentialResolver credentialResolver;

    /**
     * External short-lived secret lease resolver.
     */
    private final SecretResolver secretResolver;

    /**
     * External store for protocol-generated dynamic credential material.
     */
    private final CredentialStore credentialStore;

    /**
     * External cryptographic key resolver.
     */
    private final KeyResolver keyResolver;

    /**
     * External certificate chain and trust-root resolver.
     */
    private final CertificateResolver certificateResolver;

    /**
     * External subject attribute resolver.
     */
    private final AttributeResolver attributeResolver;

    /**
     * External subject group resolver.
     */
    private final GroupResolver groupResolver;

    /**
     * External protected-resource resolver.
     */
    private final ResourceResolver resourceResolver;

    /**
     * Atomic OAuth state correlation store.
     */
    private final StateStore stateStore;

    /**
     * Atomic nonce store.
     */
    private final NonceStore nonceStore;

    /**
     * Atomic OAuth authorization-code store.
     */
    private final AuthorizationCodeStore authorizationCodeStore;

    /**
     * Atomic OAuth device-code store.
     */
    private final DeviceCodeStore deviceCodeStore;

    /**
     * Atomic access-token state store.
     */
    private final AccessTokenStore accessTokenStore;

    /**
     * Atomic refresh-token family store.
     */
    private final RefreshTokenStore refreshTokenStore;

    /**
     * Atomic framework Session store.
     */
    private final SessionStore sessionStore;

    /**
     * Atomic authentication replay-digest store.
     */
    private final ReplayStore replayStore;

    /**
     * External sanitized audit event sink.
     */
    private final AuditSink auditSink;

    /**
     * External user-consent decision service.
     */
    private final ConsentService consentService;

    /**
     * Frozen cross-protocol security baseline.
     */
    private final SecurityBaseline securityBaseline;

    /**
     * Creates an immutable complete runtime dependency set in the frozen assembly order.
     *
     * @param fabricContext          shared Fabric context
     * @param jsonProvider           external JSON provider
     * @param executor               caller-owned executor
     * @param bindingResolver        strongly typed resolver for external protocol runtime ports
     * @param clientResolver         client registration resolver
     * @param subjectResolver        stable Subject resolver
     * @param credentialResolver     credential descriptor resolver
     * @param secretResolver         short-lived secret resolver
     * @param credentialStore        protocol-generated dynamic credential store
     * @param keyResolver            cryptographic key resolver
     * @param certificateResolver    certificate and trust-root resolver
     * @param attributeResolver      subject attribute resolver
     * @param groupResolver          subject group resolver
     * @param resourceResolver       protected-resource resolver
     * @param stateStore             atomic state store
     * @param nonceStore             atomic nonce store
     * @param authorizationCodeStore atomic authorization-code store
     * @param deviceCodeStore        atomic device-code store
     * @param accessTokenStore       atomic access-token store
     * @param refreshTokenStore      atomic refresh-token store
     * @param sessionStore           atomic Session store
     * @param replayStore            atomic replay store
     * @param auditSink              external audit sink
     * @param consentService         external consent service
     * @param securityBaseline       frozen cross-protocol security baseline
     * @throws IllegalArgumentException if any dependency is {@code null}
     */
    public ExecutionServices(final Context fabricContext, final JsonProvider jsonProvider, final Executor executor,
            final BindingResolver bindingResolver, final ClientResolver clientResolver,
            final SubjectResolver subjectResolver, final CredentialResolver credentialResolver,
            final SecretResolver secretResolver, final CredentialStore credentialStore, final KeyResolver keyResolver,
            final CertificateResolver certificateResolver, final AttributeResolver attributeResolver,
            final GroupResolver groupResolver, final ResourceResolver resourceResolver, final StateStore stateStore,
            final NonceStore nonceStore, final AuthorizationCodeStore authorizationCodeStore,
            final DeviceCodeStore deviceCodeStore, final AccessTokenStore accessTokenStore,
            final RefreshTokenStore refreshTokenStore, final SessionStore sessionStore, final ReplayStore replayStore,
            final AuditSink auditSink, final ConsentService consentService, final SecurityBaseline securityBaseline) {
        this.fabricContext = Assert.notNull(fabricContext, "Fabric context must not be null");
        this.jsonProvider = Assert.notNull(jsonProvider, "JSON provider must not be null");
        this.executor = Assert.notNull(executor, "Runtime executor must not be null");
        this.bindingResolver = Assert.notNull(bindingResolver, "Runtime binding resolver must not be null");
        this.clientResolver = Assert.notNull(clientResolver, "Client resolver must not be null");
        this.subjectResolver = Assert.notNull(subjectResolver, "Subject resolver must not be null");
        this.credentialResolver = Assert.notNull(credentialResolver, "Credential resolver must not be null");
        this.secretResolver = Assert.notNull(secretResolver, "Secret resolver must not be null");
        this.credentialStore = Assert.notNull(credentialStore, "Credential store must not be null");
        this.keyResolver = Assert.notNull(keyResolver, "Key resolver must not be null");
        this.certificateResolver = Assert.notNull(certificateResolver, "Certificate resolver must not be null");
        this.attributeResolver = Assert.notNull(attributeResolver, "Attribute resolver must not be null");
        this.groupResolver = Assert.notNull(groupResolver, "Group resolver must not be null");
        this.resourceResolver = Assert.notNull(resourceResolver, "Resource resolver must not be null");
        this.stateStore = Assert.notNull(stateStore, "State store must not be null");
        this.nonceStore = Assert.notNull(nonceStore, "Nonce store must not be null");
        this.authorizationCodeStore = Assert
                .notNull(authorizationCodeStore, "Authorization code store must not be null");
        this.deviceCodeStore = Assert.notNull(deviceCodeStore, "Device code store must not be null");
        this.accessTokenStore = Assert.notNull(accessTokenStore, "Access token store must not be null");
        this.refreshTokenStore = Assert.notNull(refreshTokenStore, "Refresh token store must not be null");
        this.sessionStore = Assert.notNull(sessionStore, "Session store must not be null");
        this.replayStore = Assert.notNull(replayStore, "Replay store must not be null");
        this.auditSink = Assert.notNull(auditSink, "Audit sink must not be null");
        this.consentService = Assert.notNull(consentService, "Consent service must not be null");
        this.securityBaseline = Assert.notNull(securityBaseline, "Security baseline must not be null");
    }

    /**
     * Returns the shared Fabric context used by every compiled runtime transport.
     *
     * @return shared Fabric context
     */
    public Context fabricContext() {
        return fabricContext;
    }

    /**
     * Returns the externally selected JSON provider used for protocol wire documents.
     *
     * @return externally selected JSON provider
     */
    public JsonProvider jsonProvider() {
        return jsonProvider;
    }

    /**
     * Returns the caller-owned executor used for asynchronous framework work.
     *
     * @return caller-owned asynchronous executor
     */
    public Executor executor() {
        return executor;
    }

    /**
     * Returns the resolver for strongly typed external protocol runtime ports.
     *
     * @return external strongly typed protocol runtime-port resolver
     */
    public BindingResolver bindingResolver() {
        return bindingResolver;
    }

    /**
     * Returns the resolver for registered OAuth and OpenID client metadata.
     *
     * @return client registration resolver
     */
    public ClientResolver clientResolver() {
        return clientResolver;
    }

    /**
     * Returns the resolver that maps stable subject references to subject data.
     *
     * @return stable Subject resolver
     */
    public SubjectResolver subjectResolver() {
        return subjectResolver;
    }

    /**
     * Returns the resolver for non-secret credential descriptors.
     *
     * @return credential descriptor resolver
     */
    public CredentialResolver credentialResolver() {
        return credentialResolver;
    }

    /**
     * Returns the resolver that leases secret material for the shortest required lifetime.
     *
     * @return short-lived secret lease resolver
     */
    public SecretResolver secretResolver() {
        return secretResolver;
    }

    /**
     * Returns the external store for protocol-generated dynamic credentials.
     *
     * @return protocol-generated dynamic credential store
     */
    public CredentialStore credentialStore() {
        return credentialStore;
    }

    /**
     * Returns the resolver for cryptographic signing, verification, and encryption keys.
     *
     * @return cryptographic key resolver
     */
    public KeyResolver keyResolver() {
        return keyResolver;
    }

    /**
     * Returns the resolver for certificate chains and trust anchors.
     *
     * @return certificate chain and trust-root resolver
     */
    public CertificateResolver certificateResolver() {
        return certificateResolver;
    }

    /**
     * Returns the resolver for externally maintained subject attributes.
     *
     * @return subject attribute resolver
     */
    public AttributeResolver attributeResolver() {
        return attributeResolver;
    }

    /**
     * Returns the resolver for externally maintained subject group membership.
     *
     * @return subject group resolver
     */
    public GroupResolver groupResolver() {
        return groupResolver;
    }

    /**
     * Returns the resolver for protected-resource metadata used by authorization decisions.
     *
     * @return protected-resource resolver
     */
    public ResourceResolver resourceResolver() {
        return resourceResolver;
    }

    /**
     * Returns the atomic store for browser state correlation values.
     *
     * @return atomic state correlation store
     */
    public StateStore stateStore() {
        return stateStore;
    }

    /**
     * Returns the atomic store for one-time nonce values.
     *
     * @return atomic nonce store
     */
    public NonceStore nonceStore() {
        return nonceStore;
    }

    /**
     * Returns the atomic store for OAuth authorization codes and their bindings.
     *
     * @return atomic authorization-code store
     */
    public AuthorizationCodeStore authorizationCodeStore() {
        return authorizationCodeStore;
    }

    /**
     * Returns the atomic store for OAuth device authorization state.
     *
     * @return atomic device-code store
     */
    public DeviceCodeStore deviceCodeStore() {
        return deviceCodeStore;
    }

    /**
     * Returns the atomic store for issued access-token state.
     *
     * @return atomic access-token store
     */
    public AccessTokenStore accessTokenStore() {
        return accessTokenStore;
    }

    /**
     * Returns the atomic store for refresh-token rotation state.
     *
     * @return atomic refresh-token store
     */
    public RefreshTokenStore refreshTokenStore() {
        return refreshTokenStore;
    }

    /**
     * Returns the atomic store for framework authentication sessions.
     *
     * @return atomic framework Session store
     */
    public SessionStore sessionStore() {
        return sessionStore;
    }

    /**
     * Returns the atomic store for replay-detection digests.
     *
     * @return atomic replay-digest store
     */
    public ReplayStore replayStore() {
        return replayStore;
    }

    /**
     * Returns the external sink that receives only sanitized audit events.
     *
     * @return external sanitized audit sink
     */
    public AuditSink auditSink() {
        return auditSink;
    }

    /**
     * Returns the external service that decides and records user consent.
     *
     * @return external user-consent service
     */
    public ConsentService consentService() {
        return consentService;
    }

    /**
     * Returns the immutable cross-protocol algorithm and transport security baseline.
     *
     * @return frozen cross-protocol security baseline
     */
    public SecurityBaseline securityBaseline() {
        return securityBaseline;
    }

    /**
     * Resolves an externally implemented protocol runtime port for one exact registration.
     * <p>
     * This compile-time port is not a mutable service registry: bus-auth supplies the validated registration and an
     * exact interface class, and the external project must either return one matching implementation or fail the
     * complete Registry snapshot assembly. It enables SCIM, LDAP, and RADIUS drivers to obtain their externally owned
     * data-plane implementations without adding protocol packages to this runtime container.
     * </p>
     *
     * @author Kimi Liu
     */
    public interface BindingResolver {

        /**
         * Resolves one exact external runtime-port implementation.
         *
         * @param registration validated registration being compiled
         * @param contract     exact protocol port interface required by its driver
         * @param <T>          protocol runtime-port type
         * @return non-null implementation assignable to {@code contract}
         * @throws IllegalArgumentException                             if an argument is {@code null}
         * @throws org.miaixz.bus.core.lang.exception.ValidateException if the binding is absent, ambiguous, disabled,
         *                                                              or not assignable to the requested contract
         */
        <T> T resolve(Registration.Record<?> registration, Class<T> contract);

    }

}
