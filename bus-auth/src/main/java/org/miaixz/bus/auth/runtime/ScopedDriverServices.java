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

import org.miaixz.bus.auth.cache.AccessTokenCache;
import org.miaixz.bus.auth.cache.AuthorizationCache;
import org.miaixz.bus.auth.cache.AuthorizationCodeCache;
import org.miaixz.bus.auth.cache.DeviceCodeCache;
import org.miaixz.bus.auth.cache.NonceCache;
import org.miaixz.bus.auth.cache.RefreshTokenCache;
import org.miaixz.bus.auth.cache.ReplayCache;
import org.miaixz.bus.auth.cache.SessionCache;
import org.miaixz.bus.auth.cache.StateCache;
import org.miaixz.bus.auth.resolver.AttributeParser;
import org.miaixz.bus.auth.resolver.BindingParser;
import org.miaixz.bus.auth.resolver.CertificateParser;
import org.miaixz.bus.auth.resolver.ConsumerParser;
import org.miaixz.bus.auth.resolver.KeyParser;
import org.miaixz.bus.auth.resolver.ResourceParser;
import org.miaixz.bus.auth.resolver.SecretParser;
import org.miaixz.bus.auth.shared.SecurityBaseline;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.source.SourceDriver;
import org.miaixz.bus.auth.worker.AttributeLoader;
import org.miaixz.bus.auth.worker.BindingLoader;
import org.miaixz.bus.auth.worker.CertificateLoader;
import org.miaixz.bus.auth.worker.ConsentService;
import org.miaixz.bus.auth.worker.ConsumerLoader;
import org.miaixz.bus.auth.worker.CredentialStore;
import org.miaixz.bus.auth.worker.KeyLoader;
import org.miaixz.bus.auth.worker.ResourceLoader;
import org.miaixz.bus.auth.worker.SecretLoader;
import org.miaixz.bus.auth.worker.SessionWorker;
import org.miaixz.bus.auth.worker.WorkerSlots;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.fabric.Context;

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

    private final RuntimeServices services;

    private final WorkerSlots slots;

    private final SourceDriver.Dependencies dependencies;

    ScopedDriverServices(final RuntimeServices services, final WorkerSlots slots,
            final SourceDriver.Dependencies dependencies) {
        this.services = Assert.notNull(services, "Runtime services must not be null");
        this.slots = Assert.notNull(slots, "Driver service slots must not be null");
        this.dependencies = Assert.notNull(dependencies, "Driver framework dependencies must not be null");
    }

    private void require(final WorkerSlots.Slot slot) {
        if (!slots.contains(slot)) {
            throw new ValidateException("Source driver accessed undeclared project slot " + slot.name());
        }
    }

    private void require(final SourceDriver.Dependencies.Service service) {
        if (!dependencies.contains(service)) {
            throw new ValidateException("Source driver accessed undeclared framework service " + service.name());
        }
    }

    @Override
    public Context fabricContext() {
        require(SourceDriver.Dependencies.Service.FABRIC_CONTEXT);
        return services.fabricContext();
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
    public BindingLoader bindingLoader() {
        require(WorkerSlots.Slot.BINDING);
        return services.bindingLoader();
    }

    @Override
    public BindingParser bindingParser() {
        require(WorkerSlots.Slot.BINDING);
        return services.bindingParser();
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
        return services.stateCache();
    }

    @Override
    public NonceCache nonceCache() {
        require(SourceDriver.Dependencies.Service.NONCE_CACHE);
        return services.nonceCache();
    }

    @Override
    public AuthorizationCodeCache authorizationCodeCache() {
        require(SourceDriver.Dependencies.Service.AUTHORIZATION_CODE_CACHE);
        return services.authorizationCodeCache();
    }

    @Override
    public DeviceCodeCache deviceCodeCache() {
        require(SourceDriver.Dependencies.Service.DEVICE_CODE_CACHE);
        return services.deviceCodeCache();
    }

    @Override
    public AuthorizationCache authorizationCache() {
        require(SourceDriver.Dependencies.Service.AUTHORIZATION_CACHE);
        return services.authorizationCache();
    }

    @Override
    public AccessTokenCache accessTokenCache() {
        require(SourceDriver.Dependencies.Service.ACCESS_TOKEN_CACHE);
        return services.accessTokenCache();
    }

    @Override
    public RefreshTokenCache refreshTokenCache() {
        require(SourceDriver.Dependencies.Service.REFRESH_TOKEN_CACHE);
        return services.refreshTokenCache();
    }

    @Override
    public SessionCache sessionCache() {
        require(SourceDriver.Dependencies.Service.SESSION_CACHE);
        return services.sessionCache();
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
