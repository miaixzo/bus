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
import org.miaixz.bus.auth.worker.WorkerSet;
import org.miaixz.bus.auth.worker.WorkerSlots;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.fabric.Context;

/**
 * Immutable root container for all protocol-execution dependencies.
 * <p>
 * This class owns the complete framework infrastructure, project data ports, pure parsers, authentication caches, and
 * non-relaxable security baseline assembled for one runtime. It deliberately does not implement {@link DriverServices}
 * and must never be passed directly to a Source driver. {@link #scope(WorkerSlots, SourceDriver.Dependencies)} creates
 * the only capability-limited view accepted by driver compilation.
 * </p>
 * <p>
 * Registration loading, Registry observation, identity completion, audit, parsing execution, and project business
 * services remain outside this class.
 * </p>
 *
 * @author Kimi Liu
 */
public final class RuntimeServices {

    private final Context fabricContext;
    private final JsonProvider jsonProvider;
    private final Executor executor;
    private final WorkerSet workers;
    private final BindingParser bindingParser;
    private final ConsumerParser consumerParser;
    private final SecretParser secretParser;
    private final KeyParser keyParser;
    private final CertificateParser certificateParser;
    private final AttributeParser attributeParser;
    private final ResourceParser resourceParser;
    private final StateCache stateCache;
    private final NonceCache nonceCache;
    private final AuthorizationCodeCache authorizationCodeCache;
    private final DeviceCodeCache deviceCodeCache;
    private final AuthorizationCache authorizationCache;
    private final AccessTokenCache accessTokenCache;
    private final RefreshTokenCache refreshTokenCache;
    private final SessionCache sessionCache;
    private final ReplayCache replayCache;
    private final SecurityBaseline securityBaseline;

    /**
     * Creates the immutable protocol dependency set and framework-owned stateless parsers and cache views.
     *
     * @param fabricContext    caller-owned Fabric execution context
     * @param jsonProvider     caller-owned JSON provider
     * @param executor         caller-owned executor
     * @param workers          selected project protocol data ports
     * @param cache            shared atomic authentication cache backend whose serializer can round-trip the immutable
     *                         AuthCache envelope graph and deterministically re-encode equal expected values
     * @param cacheNamespace   deployment-unique authentication cache namespace
     * @param securityBaseline non-relaxable framework security policy
     * @throws IllegalArgumentException if a dependency is {@code null}
     */
    public RuntimeServices(final Context fabricContext, final JsonProvider jsonProvider, final Executor executor,
                           final WorkerSet workers, final CacheX<String, Object> cache, final String cacheNamespace,
                           final SecurityBaseline securityBaseline) {
        this.fabricContext = Assert.notNull(fabricContext, "Fabric context must not be null");
        this.jsonProvider = Assert.notNull(jsonProvider, "JSON provider must not be null");
        this.executor = Assert.notNull(executor, "Runtime executor must not be null");
        this.workers = Assert.notNull(workers, "Worker set must not be null");
        this.bindingParser = new BindingParser();
        this.consumerParser = new ConsumerParser();
        this.secretParser = new SecretParser();
        this.keyParser = new KeyParser();
        this.certificateParser = new CertificateParser();
        this.attributeParser = new AttributeParser();
        this.resourceParser = new ResourceParser();
        final CacheX<String, Object> authenticationCache = Assert
                .notNull(cache, "Authentication cache must not be null");
        Assert.isTrue(
                authenticationCache.atomic(),
                "Authentication cache must support the complete atomic CacheX contract");
        final String deployment = Assert.notBlank(cacheNamespace, "Authentication cache namespace must not be blank");
        this.stateCache = new StateCache(authenticationCache, deployment, fabricContext.clock());
        this.nonceCache = new NonceCache(authenticationCache, deployment, fabricContext.clock());
        this.authorizationCodeCache = new AuthorizationCodeCache(authenticationCache, deployment,
                fabricContext.clock());
        this.deviceCodeCache = new DeviceCodeCache(authenticationCache, deployment, fabricContext.clock());
        this.authorizationCache = new AuthorizationCache(authenticationCache, deployment, fabricContext.clock());
        this.accessTokenCache = new AccessTokenCache(authenticationCache, deployment, fabricContext.clock());
        this.refreshTokenCache = new RefreshTokenCache(authenticationCache, deployment, fabricContext.clock());
        this.sessionCache = new SessionCache(authenticationCache, deployment, fabricContext.clock());
        this.replayCache = new ReplayCache(authenticationCache, deployment, fabricContext.clock());
        this.securityBaseline = Assert.notNull(securityBaseline, "Security baseline must not be null");
    }

    Context fabricContext() {
        return fabricContext;
    }

    JsonProvider jsonProvider() {
        return jsonProvider;
    }

    Executor executor() {
        return executor;
    }

    BindingLoader bindingLoader() {
        return workers.bindingLoader();
    }

    BindingParser bindingParser() {
        return bindingParser;
    }

    ConsumerLoader consumerLoader() {
        return workers.consumerLoader();
    }

    ConsumerParser consumerParser() {
        return consumerParser;
    }

    SecretLoader secretLoader() {
        return workers.secretLoader();
    }

    SecretParser secretParser() {
        return secretParser;
    }

    CredentialStore credentialStore() {
        return workers.credentialStore();
    }

    KeyLoader keyLoader() {
        return workers.keyLoader();
    }

    KeyParser keyParser() {
        return keyParser;
    }

    CertificateLoader certificateLoader() {
        return workers.certificateLoader();
    }

    CertificateParser certificateParser() {
        return certificateParser;
    }

    AttributeLoader attributeLoader() {
        return workers.attributeLoader();
    }

    AttributeParser attributeParser() {
        return attributeParser;
    }

    ResourceLoader resourceLoader() {
        return workers.resourceLoader();
    }

    ResourceParser resourceParser() {
        return resourceParser;
    }

    StateCache stateCache() {
        return stateCache;
    }

    NonceCache nonceCache() {
        return nonceCache;
    }

    AuthorizationCodeCache authorizationCodeCache() {
        return authorizationCodeCache;
    }

    DeviceCodeCache deviceCodeCache() {
        return deviceCodeCache;
    }

    AuthorizationCache authorizationCache() {
        return authorizationCache;
    }

    AccessTokenCache accessTokenCache() {
        return accessTokenCache;
    }

    RefreshTokenCache refreshTokenCache() {
        return refreshTokenCache;
    }

    SessionCache sessionCache() {
        return sessionCache;
    }

    ReplayCache replayCache() {
        return replayCache;
    }

    ConsentService consentService() {
        return workers.consentService();
    }

    SessionWorker sessionWorker() {
        return workers.sessionWorker();
    }

    SecurityBaseline securityBaseline() {
        return securityBaseline;
    }

    /**
     * Creates the only Driver-visible service view after validating every declared project data port.
     *
     * @param slots        exact project data ports required by one prepared Source
     * @param dependencies exact framework services required by the same prepared Source
     * @return capability-limited Driver service view
     * @throws IllegalArgumentException if an argument is {@code null} or a required project port is unavailable
     */
    DriverServices scope(final WorkerSlots slots, final SourceDriver.Dependencies dependencies) {
        final WorkerSlots selected = Assert.notNull(slots, "Source Worker slots must not be null");
        final SourceDriver.Dependencies required = Assert
                .notNull(dependencies, "Source framework dependencies must not be null");
        workers.require(selected);
        return new ScopedDriverServices(this, selected, required);
    }

}
