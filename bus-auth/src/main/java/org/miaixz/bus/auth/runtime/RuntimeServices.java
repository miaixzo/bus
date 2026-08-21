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

import org.miaixz.bus.auth.Registration;
import org.miaixz.bus.auth.cache.*;
import org.miaixz.bus.auth.resolver.*;
import org.miaixz.bus.auth.shared.SecurityBaseline;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.source.SourceDriver;
import org.miaixz.bus.auth.worker.*;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.fabric.Context;

/**
 * Immutable root container for all protocol-execution dependencies.
 * <p>
 * This class owns the complete framework infrastructure, project data ports, pure parsers, authentication caches, and
 * non-relaxable security baseline assembled for one runtime. It deliberately does not implement {@link DriverServices}
 * and must never be passed directly to a Source driver.
 * {@link #scope(Registration.SourceEntry, WorkerSlots, SourceDriver.Dependencies)} creates the only capability-limited
 * view accepted by driver compilation.
 * </p>
 * <p>
 * Registration loading, Registry observation, identity completion, audit, parsing execution, and project business
 * services remain outside this class.
 * </p>
 *
 * @author Kimi Liu
 */
public final class RuntimeServices {

    /** Shared caller-owned fabric execution context. */
    private final Context fabricContext;
    /** Selected provider-neutral JSON implementation. */
    private final JsonProvider jsonProvider;
    /** Caller-owned executor for asynchronous Source work. */
    private final Executor executor;
    /** Selected project worker ports. */
    private final WorkerSet workers;
    /** Framework consumer parser. */
    private final ConsumerParser consumerParser;
    /** Framework secret parser. */
    private final SecretParser secretParser;
    /** Framework key parser. */
    private final KeyParser keyParser;
    /** Framework certificate parser. */
    private final CertificateParser certificateParser;
    /** Framework subject-attribute parser. */
    private final AttributeParser attributeParser;
    /** Framework protected-resource parser. */
    private final ResourceParser resourceParser;
    /** Callback correlation cache view. */
    private final StateCache stateCache;
    /** One-time nonce cache view. */
    private final NonceCache nonceCache;
    /** One-time authorization-code cache view. */
    private final AuthorizationCodeCache authorizationCodeCache;
    /** Device-authorization cache view. */
    private final DeviceCodeCache deviceCodeCache;
    /** Authoritative authorization lifecycle cache view. */
    private final AuthorizationCache authorizationCache;
    /** Access-token validation cache view. */
    private final AccessTokenCache accessTokenCache;
    /** Refresh-token family cache view. */
    private final RefreshTokenCache refreshTokenCache;
    /** Authentication Session cache view. */
    private final SessionCache sessionCache;
    /** Protocol replay-prevention cache view. */
    private final ReplayCache replayCache;
    /** Immutable non-relaxable runtime security policy. */
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
        this.consumerParser = new ConsumerParser();
        this.secretParser = new SecretParser();
        this.keyParser = new KeyParser();
        this.certificateParser = new CertificateParser();
        this.attributeParser = new AttributeParser();
        this.resourceParser = new ResourceParser();
        final CacheX<String, Object> authenticationCache = Assert
                .notNull(cache, "Authentication cache must not be null");
        Assert.isTrue(
                authenticationCache.supports(),
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

    /** {@return the shared fabric context} */
    Context fabricContext() {
        return fabricContext;
    }

    /** {@return the selected JSON provider} */
    JsonProvider jsonProvider() {
        return jsonProvider;
    }

    /** {@return the Source executor} */
    Executor executor() {
        return executor;
    }

    /** {@return the project binding loader} */
    BindingLoader bindingLoader() {
        return workers.bindingLoader();
    }

    /** {@return the project consumer loader} */
    ConsumerLoader consumerLoader() {
        return workers.consumerLoader();
    }

    /** {@return the framework consumer parser} */
    ConsumerParser consumerParser() {
        return consumerParser;
    }

    /** {@return the project secret loader} */
    SecretLoader secretLoader() {
        return workers.secretLoader();
    }

    /** {@return the framework secret parser} */
    SecretParser secretParser() {
        return secretParser;
    }

    /** {@return the project credential store} */
    CredentialStore credentialStore() {
        return workers.credentialStore();
    }

    /** {@return the project key loader} */
    KeyLoader keyLoader() {
        return workers.keyLoader();
    }

    /** {@return the framework key parser} */
    KeyParser keyParser() {
        return keyParser;
    }

    /** {@return the project certificate loader} */
    CertificateLoader certificateLoader() {
        return workers.certificateLoader();
    }

    /** {@return the framework certificate parser} */
    CertificateParser certificateParser() {
        return certificateParser;
    }

    /** {@return the project attribute loader} */
    AttributeLoader attributeLoader() {
        return workers.attributeLoader();
    }

    /** {@return the framework attribute parser} */
    AttributeParser attributeParser() {
        return attributeParser;
    }

    /** {@return the project resource loader} */
    ResourceLoader resourceLoader() {
        return workers.resourceLoader();
    }

    /** {@return the framework resource parser} */
    ResourceParser resourceParser() {
        return resourceParser;
    }

    /** {@return the callback-state cache} */
    StateCache stateCache() {
        return stateCache;
    }

    /** {@return the nonce cache} */
    NonceCache nonceCache() {
        return nonceCache;
    }

    /** {@return the authorization-code cache} */
    AuthorizationCodeCache authorizationCodeCache() {
        return authorizationCodeCache;
    }

    /** {@return the device-code cache} */
    DeviceCodeCache deviceCodeCache() {
        return deviceCodeCache;
    }

    /** {@return the authorization cache} */
    AuthorizationCache authorizationCache() {
        return authorizationCache;
    }

    /** {@return the access-token cache} */
    AccessTokenCache accessTokenCache() {
        return accessTokenCache;
    }

    /** {@return the refresh-token cache} */
    RefreshTokenCache refreshTokenCache() {
        return refreshTokenCache;
    }

    /** {@return the Session cache} */
    SessionCache sessionCache() {
        return sessionCache;
    }

    /** {@return the replay cache} */
    ReplayCache replayCache() {
        return replayCache;
    }

    /** {@return the project consent service} */
    ConsentService consentService() {
        return workers.consentService();
    }

    /** {@return the project Session worker} */
    SessionWorker sessionWorker() {
        return workers.sessionWorker();
    }

    /** {@return the runtime security baseline} */
    SecurityBaseline securityBaseline() {
        return securityBaseline;
    }

    /**
     * Creates the only Driver-visible service view after validating every declared project data port.
     *
     * @param registration exact Source registration owning the scoped view
     * @param slots        exact project data ports required by one prepared Source
     * @param dependencies exact framework services required by the same prepared Source
     * @return capability-limited Driver service view
     * @throws IllegalArgumentException if an argument is {@code null} or a required project port is unavailable
     */
    DriverServices scope(
            final Registration.SourceEntry registration,
            final WorkerSlots slots,
            final SourceDriver.Dependencies dependencies) {
        final Registration.SourceEntry source = Assert
                .notNull(registration, "Scoped Source registration must not be null");
        final WorkerSlots selected = Assert.notNull(slots, "Source Worker slots must not be null");
        final SourceDriver.Dependencies required = Assert
                .notNull(dependencies, "Source framework dependencies must not be null");
        workers.require(selected);
        return new ScopedDriverServices(this, source, selected, required);
    }

}
