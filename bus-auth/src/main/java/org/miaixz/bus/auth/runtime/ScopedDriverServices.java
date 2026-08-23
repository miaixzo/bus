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
package org.miaixz.bus.auth.runtime;

import java.util.concurrent.Executor;

import org.miaixz.bus.auth.Blueprint;
import org.miaixz.bus.auth.Policies;
import org.miaixz.bus.auth.cache.*;
import org.miaixz.bus.auth.resolver.*;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.source.SourceDriver;
import org.miaixz.bus.auth.worker.*;
import org.miaixz.bus.auth.worker.loader.*;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Enforces the complete capability boundary declared by one prepared Source driver.
 * <p>
 * Project-owned loaders and business bridges are exposed only when the matching {@link WorkerSlots.Slot} was declared.
 * Framework infrastructure, parsers, caches, and security policy are exposed only when the matching
 * {@link SourceDriver.Dependencies.Service} was declared. This proxy performs no loading, parsing, protocol work, or
 * shared-resource lifecycle management.
 * </p>
 *
 * @author Kimi Liu
 */
final class ScopedDriverServices implements DriverServices {

    /**
     * Complete runtime service set behind this capability boundary.
     */
    private final RuntimeServices services;

    /**
     * Exact immutable Source Blueprint entry owning this view.
     */
    private final Blueprint.SourceEntry entry;

    /**
     * Project data slots declared by the prepared Source.
     */
    private final WorkerSlots slots;

    /**
     * Framework dependencies declared by the prepared Source.
     */
    private final SourceDriver.Dependencies dependencies;

    /**
     * Callback correlation cache isolated to this Source configuration generation.
     */
    private final StateCache stateCache;

    /**
     * Nonce cache isolated to this Source configuration generation.
     */
    private final NonceCache nonceCache;

    /**
     * Authorization-code cache isolated to this Source configuration generation.
     */
    private final AuthorizationCodeCache authorizationCodeCache;

    /**
     * Device-code cache isolated to this Source configuration generation.
     */
    private final DeviceCodeCache deviceCodeCache;

    /**
     * Authorization lifecycle cache isolated to this Source configuration generation.
     */
    private final AuthorizationCache authorizationCache;

    /**
     * Access-token cache isolated to this Source configuration generation.
     */
    private final AccessTokenCache accessTokenCache;

    /**
     * Refresh-token cache isolated to this Source configuration generation.
     */
    private final RefreshTokenCache refreshTokenCache;

    /**
     * Session cache isolated to this Source configuration generation.
     */
    private final SessionCache sessionCache;

    /**
     * ID Token binding cache isolated to this Source configuration generation.
     */
    private final IdTokenCache idTokenCache;

    /**
     * Creates one capability-limited Source service view.
     *
     * @param services     complete runtime service set
     * @param entry        exact Source Blueprint entry
     * @param slots        declared project data slots
     * @param dependencies declared framework dependencies
     * @param generation   security-state generation assigned during Source compilation
     */
    ScopedDriverServices(final RuntimeServices services, final Blueprint.SourceEntry entry, final WorkerSlots slots,
            final SourceDriver.Dependencies dependencies, final long generation) {
        this.services = Assert.notNull(services, "Runtime services must not be null");
        this.entry = Assert.notNull(entry, "Scoped Source Blueprint entry must not be null");
        this.slots = Assert.notNull(slots, "Driver service slots must not be null");
        this.dependencies = Assert.notNull(dependencies, "Driver framework dependencies must not be null");
        Assert.isTrue(generation >= 0L, "Scoped Source generation must not be negative");
        final String sourceId = Assert
                .notBlank(this.entry.resource().getId(), "Scoped Source Blueprint entry id must not be blank");
        this.stateCache = services.stateCache(sourceId, generation);
        this.nonceCache = services.nonceCache(sourceId, generation);
        this.authorizationCodeCache = services.authorizationCodeCache(sourceId, generation);
        this.deviceCodeCache = services.deviceCodeCache(sourceId, generation);
        this.authorizationCache = services.authorizationCache(sourceId, generation);
        this.accessTokenCache = services.accessTokenCache(sourceId, generation);
        this.refreshTokenCache = services.refreshTokenCache(sourceId, generation);
        this.sessionCache = services.sessionCache(sourceId, generation);
        this.idTokenCache = services.idTokenCache(sourceId, generation);
    }

    /**
     * Returns the exact Source Blueprint entry bound to this scoped service view.
     *
     * @return immutable Source Blueprint entry
     */
    @Override
    public Blueprint.SourceEntry entry() {
        return entry;
    }

    /**
     * Requires one declared project data slot.
     *
     * @param slot requested slot
     */
    private void require(final WorkerSlots.Slot slot) {
        if (!slots.contains(slot)) {
            throw new ValidateException("Source driver accessed undeclared project slot " + slot.name());
        }
    }

    /**
     * Requires one declared framework service.
     *
     * @param service requested service
     */
    private void require(final SourceDriver.Dependencies.Service service) {
        if (!dependencies.contains(service)) {
            throw new ValidateException("Source driver accessed undeclared framework service " + service.name());
        }
    }

    /**
     * Returns the Source executor only when the driver declared that framework dependency.
     *
     * @return caller-owned Source executor
     */
    @Override
    public Executor executor() {
        require(SourceDriver.Dependencies.Service.EXECUTOR);
        return services.executor();
    }

    /**
     * Returns the project binding resolver only for a declared binding slot.
     *
     * @return project binding resolver
     */
    @Override
    public BindingResolver bindingResolver() {
        require(WorkerSlots.Slot.BINDING);
        return services.bindingResolver();
    }

    /**
     * Returns the project consumer loader only for a declared consumer slot.
     *
     * @return project consumer loader
     */
    @Override
    public ConsumerLoader consumerLoader() {
        require(WorkerSlots.Slot.CONSUMER);
        return services.consumerLoader();
    }

    /**
     * Returns the framework consumer parser only for a declared consumer slot.
     *
     * @return framework consumer parser
     */
    @Override
    public ConsumerParser consumerParser() {
        require(WorkerSlots.Slot.CONSUMER);
        return services.consumerParser();
    }

    /**
     * Returns the project consumer verifier only for its explicit verifier slot.
     *
     * @return project consumer verifier
     */
    @Override
    public ConsumerVerifier consumerVerifier() {
        require(WorkerSlots.Slot.CONSUMER_VERIFIER);
        return services.consumerVerifier();
    }

    /**
     * Returns the project federation loader only for a declared federation slot.
     *
     * @return project federation loader
     */
    @Override
    public FederationLoader federationLoader() {
        require(WorkerSlots.Slot.FEDERATION);
        return services.federationLoader();
    }

    /**
     * Returns the framework federation parser only for a declared federation slot.
     *
     * @return framework federation parser
     */
    @Override
    public FederationParser federationParser() {
        require(WorkerSlots.Slot.FEDERATION);
        return services.federationParser();
    }

    /**
     * Returns the project secret loader only for a declared secret slot.
     *
     * @return project secret loader
     */
    @Override
    public SecretLoader secretLoader() {
        require(WorkerSlots.Slot.SECRET);
        return services.secretLoader();
    }

    /**
     * Returns the framework secret parser only for a declared secret slot.
     *
     * @return framework secret parser
     */
    @Override
    public SecretParser secretParser() {
        require(WorkerSlots.Slot.SECRET);
        return services.secretParser();
    }

    /**
     * Returns the project credential store only for a declared credential slot.
     *
     * @return project credential store
     */
    @Override
    public CredentialStore credentialStore() {
        require(WorkerSlots.Slot.CREDENTIAL);
        return services.credentialStore();
    }

    /**
     * Returns the project key loader only for a declared key slot.
     *
     * @return project key loader
     */
    @Override
    public KeyLoader keyLoader() {
        require(WorkerSlots.Slot.KEY);
        return services.keyLoader();
    }

    /**
     * Returns the framework key parser only for a declared key slot.
     *
     * @return framework key parser
     */
    @Override
    public KeyParser keyParser() {
        require(WorkerSlots.Slot.KEY);
        return services.keyParser();
    }

    /**
     * Returns the project certificate loader only for a declared certificate slot.
     *
     * @return project certificate loader
     */
    @Override
    public CertificateLoader certificateLoader() {
        require(WorkerSlots.Slot.CERTIFICATE);
        return services.certificateLoader();
    }

    /**
     * Returns the framework certificate parser only for a declared certificate slot.
     *
     * @return framework certificate parser
     */
    @Override
    public CertificateParser certificateParser() {
        require(WorkerSlots.Slot.CERTIFICATE);
        return services.certificateParser();
    }

    /**
     * Returns the project attribute loader only for a declared attribute slot.
     *
     * @return project attribute loader
     */
    @Override
    public AttributeLoader attributeLoader() {
        require(WorkerSlots.Slot.ATTRIBUTE);
        return services.attributeLoader();
    }

    /**
     * Returns the framework attribute parser only for a declared attribute slot.
     *
     * @return framework attribute parser
     */
    @Override
    public AttributeParser attributeParser() {
        require(WorkerSlots.Slot.ATTRIBUTE);
        return services.attributeParser();
    }

    /**
     * Returns the project resource loader only for a declared resource slot.
     *
     * @return project resource loader
     */
    @Override
    public ResourceLoader resourceLoader() {
        require(WorkerSlots.Slot.RESOURCE);
        return services.resourceLoader();
    }

    /**
     * Returns the framework resource parser only for a declared resource slot.
     *
     * @return framework resource parser
     */
    @Override
    public ResourceParser resourceParser() {
        require(WorkerSlots.Slot.RESOURCE);
        return services.resourceParser();
    }

    /**
     * Returns the generation-scoped state cache only when explicitly declared.
     *
     * @return callback state cache
     */
    @Override
    public StateCache stateCache() {
        require(SourceDriver.Dependencies.Service.STATE_CACHE);
        return stateCache;
    }

    /**
     * Returns the generation-scoped nonce cache only when explicitly declared.
     *
     * @return nonce cache
     */
    @Override
    public NonceCache nonceCache() {
        require(SourceDriver.Dependencies.Service.NONCE_CACHE);
        return nonceCache;
    }

    /**
     * Returns the generation-scoped authorization-code cache only when explicitly declared.
     *
     * @return authorization-code cache
     */
    @Override
    public AuthorizationCodeCache authorizationCodeCache() {
        require(SourceDriver.Dependencies.Service.AUTHORIZATION_CODE_CACHE);
        return authorizationCodeCache;
    }

    /**
     * Returns the generation-scoped device-code cache only when explicitly declared.
     *
     * @return device-code cache
     */
    @Override
    public DeviceCodeCache deviceCodeCache() {
        require(SourceDriver.Dependencies.Service.DEVICE_CODE_CACHE);
        return deviceCodeCache;
    }

    /**
     * Returns the generation-scoped authorization cache only when explicitly declared.
     *
     * @return authorization cache
     */
    @Override
    public AuthorizationCache authorizationCache() {
        require(SourceDriver.Dependencies.Service.AUTHORIZATION_CACHE);
        return authorizationCache;
    }

    /**
     * Returns the generation-scoped access-token cache only when explicitly declared.
     *
     * @return access-token cache
     */
    @Override
    public AccessTokenCache accessTokenCache() {
        require(SourceDriver.Dependencies.Service.ACCESS_TOKEN_CACHE);
        return accessTokenCache;
    }

    /**
     * Returns the generation-scoped refresh-token cache only when explicitly declared.
     *
     * @return refresh-token cache
     */
    @Override
    public RefreshTokenCache refreshTokenCache() {
        require(SourceDriver.Dependencies.Service.REFRESH_TOKEN_CACHE);
        return refreshTokenCache;
    }

    /**
     * Returns the generation-scoped session cache only when explicitly declared.
     *
     * @return session cache
     */
    @Override
    public SessionCache sessionCache() {
        require(SourceDriver.Dependencies.Service.SESSION_CACHE);
        return sessionCache;
    }

    /**
     * Returns the generation-scoped ID Token cache only when explicitly declared.
     *
     * @return ID Token cache
     */
    @Override
    public IdTokenCache idTokenCache() {
        require(SourceDriver.Dependencies.Service.ID_TOKEN_CACHE);
        return idTokenCache;
    }

    /**
     * Returns the shared replay cache only when the driver declared that dependency.
     *
     * @return protocol replay cache
     */
    @Override
    public ReplayCache replayCache() {
        require(SourceDriver.Dependencies.Service.REPLAY_CACHE);
        return services.replayCache();
    }

    /**
     * Returns the project consent service only for a declared consent slot.
     *
     * @return project consent service
     */
    @Override
    public ConsentService consentService() {
        require(WorkerSlots.Slot.CONSENT);
        return services.consentService();
    }

    /**
     * Returns the project session worker only for a declared session slot.
     *
     * @return project session worker
     */
    @Override
    public SessionWorker sessionWorker() {
        require(WorkerSlots.Slot.SESSION);
        return services.sessionWorker();
    }

    /**
     * Returns the immutable security policies only when the driver declared that dependency.
     *
     * @return runtime security policies
     */
    @Override
    public Policies policies() {
        require(SourceDriver.Dependencies.Service.POLICIES);
        return services.policies();
    }

}
