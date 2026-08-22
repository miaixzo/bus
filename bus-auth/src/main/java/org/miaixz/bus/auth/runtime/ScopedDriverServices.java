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
import org.miaixz.bus.auth.FabricX;
import org.miaixz.bus.auth.cache.*;
import org.miaixz.bus.auth.resolver.*;
import org.miaixz.bus.auth.shared.SecurityBaseline;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.source.SourceDriver;
import org.miaixz.bus.auth.worker.*;
import org.miaixz.bus.auth.worker.loader.*;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonProvider;

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
     * Complete runtime service inventory behind this capability boundary.
     */
    private final RuntimeServices services;

    /**
     * Exact immutable Source registration owning this view.
     */
    private final Blueprint.SourceEntry registration;

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
     * @param services     complete runtime service inventory
     * @param registration exact Source registration
     * @param slots        declared project data slots
     * @param dependencies declared framework dependencies
     * @param generation   security-state generation assigned during Source compilation
     */
    ScopedDriverServices(final RuntimeServices services, final Blueprint.SourceEntry registration,
            final WorkerSlots slots, final SourceDriver.Dependencies dependencies, final long generation) {
        this.services = Assert.notNull(services, "Runtime services must not be null");
        this.registration = Assert.notNull(registration, "Scoped Source registration must not be null");
        this.slots = Assert.notNull(slots, "Driver service slots must not be null");
        this.dependencies = Assert.notNull(dependencies, "Driver framework dependencies must not be null");
        Assert.isTrue(generation >= 0L, "Scoped Source generation must not be negative");
        final String sourceId = Assert
                .notBlank(this.registration.resource().getId(), "Scoped Source registration id must not be blank");
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

    @Override
    public Blueprint.SourceEntry registration() {
        return registration;
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

    @Override
    public FabricX fabric() {
        require(SourceDriver.Dependencies.Service.FABRIC);
        return services.fabric();
    }

    @Override
    public JsonProvider jsonProvider() {
        require(SourceDriver.Dependencies.Service.JSON_PROVIDER);
        return services.jsonProvider();
    }

    @Override
    public Executor executor() {
        require(SourceDriver.Dependencies.Service.EXECUTOR);
        return services.executor();
    }

    @Override
    public BindingResolver bindingResolver() {
        require(WorkerSlots.Slot.BINDING);
        return services.bindingResolver();
    }

    @Override
    public ConsumerLoader consumerLoader() {
        require(WorkerSlots.Slot.CONSUMER);
        return services.consumerLoader();
    }

    @Override
    public ConsumerParser consumerParser() {
        require(WorkerSlots.Slot.CONSUMER);
        return services.consumerParser();
    }

    @Override
    public ConsumerVerifier consumerVerifier() {
        require(WorkerSlots.Slot.CONSUMER_VERIFIER);
        return services.consumerVerifier();
    }

    @Override
    public FederationLoader federationLoader() {
        require(WorkerSlots.Slot.FEDERATION);
        return services.federationLoader();
    }

    @Override
    public FederationParser federationParser() {
        require(WorkerSlots.Slot.FEDERATION);
        return services.federationParser();
    }

    @Override
    public SecretLoader secretLoader() {
        require(WorkerSlots.Slot.SECRET);
        return services.secretLoader();
    }

    @Override
    public SecretParser secretParser() {
        require(WorkerSlots.Slot.SECRET);
        return services.secretParser();
    }

    @Override
    public CredentialStore credentialStore() {
        require(WorkerSlots.Slot.CREDENTIAL);
        return services.credentialStore();
    }

    @Override
    public KeyLoader keyLoader() {
        require(WorkerSlots.Slot.KEY);
        return services.keyLoader();
    }

    @Override
    public KeyParser keyParser() {
        require(WorkerSlots.Slot.KEY);
        return services.keyParser();
    }

    @Override
    public CertificateLoader certificateLoader() {
        require(WorkerSlots.Slot.CERTIFICATE);
        return services.certificateLoader();
    }

    @Override
    public CertificateParser certificateParser() {
        require(WorkerSlots.Slot.CERTIFICATE);
        return services.certificateParser();
    }

    @Override
    public AttributeLoader attributeLoader() {
        require(WorkerSlots.Slot.ATTRIBUTE);
        return services.attributeLoader();
    }

    @Override
    public AttributeParser attributeParser() {
        require(WorkerSlots.Slot.ATTRIBUTE);
        return services.attributeParser();
    }

    @Override
    public ResourceLoader resourceLoader() {
        require(WorkerSlots.Slot.RESOURCE);
        return services.resourceLoader();
    }

    @Override
    public ResourceParser resourceParser() {
        require(WorkerSlots.Slot.RESOURCE);
        return services.resourceParser();
    }

    @Override
    public StateCache stateCache() {
        require(SourceDriver.Dependencies.Service.STATE_CACHE);
        return stateCache;
    }

    @Override
    public NonceCache nonceCache() {
        require(SourceDriver.Dependencies.Service.NONCE_CACHE);
        return nonceCache;
    }

    @Override
    public AuthorizationCodeCache authorizationCodeCache() {
        require(SourceDriver.Dependencies.Service.AUTHORIZATION_CODE_CACHE);
        return authorizationCodeCache;
    }

    @Override
    public DeviceCodeCache deviceCodeCache() {
        require(SourceDriver.Dependencies.Service.DEVICE_CODE_CACHE);
        return deviceCodeCache;
    }

    @Override
    public AuthorizationCache authorizationCache() {
        require(SourceDriver.Dependencies.Service.AUTHORIZATION_CACHE);
        return authorizationCache;
    }

    @Override
    public AccessTokenCache accessTokenCache() {
        require(SourceDriver.Dependencies.Service.ACCESS_TOKEN_CACHE);
        return accessTokenCache;
    }

    @Override
    public RefreshTokenCache refreshTokenCache() {
        require(SourceDriver.Dependencies.Service.REFRESH_TOKEN_CACHE);
        return refreshTokenCache;
    }

    @Override
    public SessionCache sessionCache() {
        require(SourceDriver.Dependencies.Service.SESSION_CACHE);
        return sessionCache;
    }

    @Override
    public IdTokenCache idTokenCache() {
        require(SourceDriver.Dependencies.Service.ID_TOKEN_CACHE);
        return idTokenCache;
    }

    @Override
    public ReplayCache replayCache() {
        require(SourceDriver.Dependencies.Service.REPLAY_CACHE);
        return services.replayCache();
    }

    @Override
    public ConsentService consentService() {
        require(WorkerSlots.Slot.CONSENT);
        return services.consentService();
    }

    @Override
    public SessionWorker sessionWorker() {
        require(WorkerSlots.Slot.SESSION);
        return services.sessionWorker();
    }

    @Override
    public SecurityBaseline securityBaseline() {
        require(SourceDriver.Dependencies.Service.SECURITY_BASELINE);
        return services.securityBaseline();
    }

}
