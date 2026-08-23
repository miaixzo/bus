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
import org.miaixz.bus.auth.Policies;
import org.miaixz.bus.auth.cache.*;
import org.miaixz.bus.auth.resolver.*;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.source.SourceDriver;
import org.miaixz.bus.auth.worker.*;
import org.miaixz.bus.auth.worker.loader.*;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.lang.Assert;

/**
 * Immutable root container for all protocol-execution dependencies.
 * <p>
 * This class owns the complete framework infrastructure, project data ports, pure parsers, authentication caches, and
 * non-relaxable security policies assembled for one runtime. It deliberately does not implement {@link DriverServices}
 * and must never be passed directly to a Source driver.
 * {@link #scope(Blueprint.SourceEntry, WorkerSlots, SourceDriver.Dependencies, long)} creates the only
 * capability-limited view accepted by driver compilation.
 * </p>
 * <p>
 * Blueprint loading, Roster observation, identity completion, audit, parsing execution, and project business services
 * remain outside this class. Application-wide JSON selection also remains outside this container; protocol code uses
 * {@link org.miaixz.bus.extra.json.JsonKit} directly.
 * </p>
 *
 * @author Kimi Liu
 */
public class RuntimeServices {

    /**
     * Caller-owned executor for asynchronous Source work.
     */
    private final Executor executor;
    /**
     * Selected project worker ports.
     */
    private final WorkerSet workers;
    /**
     * Framework consumer parser.
     */
    private final ConsumerParser consumerParser;
    /**
     * Framework federation relation parser.
     */
    private final FederationParser federationParser;
    /**
     * Framework secret parser.
     */
    private final SecretParser secretParser;
    /**
     * Framework key parser.
     */
    private final KeyParser keyParser;
    /**
     * Framework certificate parser.
     */
    private final CertificateParser certificateParser;
    /**
     * Framework subject-attribute parser.
     */
    private final AttributeParser attributeParser;
    /**
     * Framework protected-resource parser.
     */
    private final ResourceParser resourceParser;
    /**
     * Shared bus-cache backend used to create immutable Source-generation-scoped views.
     */
    private final CacheX<String, Object> authenticationCache;

    /**
     * Deployment identifier used to isolate authentication cache keys.
     */
    private final String cacheDeployment;
    /**
     * Protocol replay-prevention cache view.
     */
    private final ReplayCache replayCache;
    /**
     * Immutable non-relaxable runtime protocol security policies.
     */
    private final Policies policies;

    /**
     * Creates the immutable protocol dependency set and framework-owned stateless parsers and cache views.
     *
     * @param policies        immutable non-relaxable authentication transport and protocol policies
     * @param executor        caller-owned executor
     * @param workers         selected project protocol data ports
     * @param cache           shared atomic authentication cache backend whose serializer can round-trip the immutable
     *                        AuthCache envelope graph and deterministically re-encode equal expected values
     * @param cacheDeployment deployment identifier used to isolate authentication cache keys
     * @throws IllegalArgumentException if a dependency is {@code null}
     */
    public RuntimeServices(final Policies policies, final Executor executor, final WorkerSet workers,
            final CacheX<String, Object> cache, final String cacheDeployment) {
        this.policies = Assert.notNull(policies, "Authentication security policies must not be null");
        this.executor = Assert.notNull(executor, "Runtime executor must not be null");
        this.workers = Assert.notNull(workers, "Worker set must not be null");
        this.consumerParser = new ConsumerParser();
        this.federationParser = new FederationParser();
        this.secretParser = new SecretParser();
        this.keyParser = new KeyParser();
        this.certificateParser = new CertificateParser();
        this.attributeParser = new AttributeParser();
        this.resourceParser = new ResourceParser();
        this.authenticationCache = Assert.notNull(cache, "Authentication cache must not be null");
        Assert.isTrue(
                authenticationCache.supports(),
                "Authentication cache must support the complete atomic CacheX contract");
        this.cacheDeployment = Assert
                .notBlank(cacheDeployment, "Authentication cache deployment identifier must not be blank");
        this.replayCache = new ReplayCache(authenticationCache, this.cacheDeployment, FabricX.clock());
    }

    /**
     * {@return the Source executor}
     */
    Executor executor() {
        return executor;
    }

    /**
     * {@return the project binding resolver}
     */
    BindingResolver bindingResolver() {
        return workers.bindingResolver();
    }

    /**
     * {@return the project consumer loader}
     */
    ConsumerLoader consumerLoader() {
        return workers.consumerLoader();
    }

    /**
     * {@return the framework consumer parser}
     */
    ConsumerParser consumerParser() {
        return consumerParser;
    }

    /**
     * {@return the project consumer evidence verifier}
     */
    ConsumerVerifier consumerVerifier() {
        return workers.consumerVerifier();
    }

    /**
     * {@return the project federation relation loader}
     */
    FederationLoader federationLoader() {
        return workers.federationLoader();
    }

    /**
     * {@return the framework federation relation parser}
     */
    FederationParser federationParser() {
        return federationParser;
    }

    /**
     * {@return the project secret loader}
     */
    SecretLoader secretLoader() {
        return workers.secretLoader();
    }

    /**
     * {@return the framework secret parser}
     */
    SecretParser secretParser() {
        return secretParser;
    }

    /**
     * {@return the project credential store}
     */
    CredentialStore credentialStore() {
        return workers.credentialStore();
    }

    /**
     * {@return the project key loader}
     */
    KeyLoader keyLoader() {
        return workers.keyLoader();
    }

    /**
     * {@return the framework key parser}
     */
    KeyParser keyParser() {
        return keyParser;
    }

    /**
     * {@return the project certificate loader}
     */
    CertificateLoader certificateLoader() {
        return workers.certificateLoader();
    }

    /**
     * {@return the framework certificate parser}
     */
    CertificateParser certificateParser() {
        return certificateParser;
    }

    /**
     * {@return the project attribute loader}
     */
    AttributeLoader attributeLoader() {
        return workers.attributeLoader();
    }

    /**
     * {@return the framework attribute parser}
     */
    AttributeParser attributeParser() {
        return attributeParser;
    }

    /**
     * {@return the project resource loader}
     */
    ResourceLoader resourceLoader() {
        return workers.resourceLoader();
    }

    /**
     * {@return the framework resource parser}
     */
    ResourceParser resourceParser() {
        return resourceParser;
    }

    /**
     * Creates the callback-state cache bound to one compiled Source generation.
     *
     * @param sourceId   exact Source identifier
     * @param generation non-negative Source configuration generation
     * @return Source-generation-scoped callback-state cache
     */
    StateCache stateCache(final String sourceId, final long generation) {
        return new StateCache(authenticationCache, cacheDeployment, sourceId, generation, FabricX.clock());
    }

    /**
     * Creates the nonce cache bound to one compiled Source generation.
     *
     * @param sourceId   exact Source identifier
     * @param generation non-negative Source configuration generation
     * @return Source-generation-scoped nonce cache
     */
    NonceCache nonceCache(final String sourceId, final long generation) {
        return new NonceCache(authenticationCache, cacheDeployment, sourceId, generation, FabricX.clock());
    }

    /**
     * Creates the authorization-code cache bound to one compiled Source generation.
     *
     * @param sourceId   exact Source identifier
     * @param generation non-negative Source configuration generation
     * @return Source-generation-scoped authorization-code cache
     */
    AuthorizationCodeCache authorizationCodeCache(final String sourceId, final long generation) {
        return new AuthorizationCodeCache(authenticationCache, cacheDeployment, sourceId, generation, FabricX.clock());
    }

    /**
     * Creates the device-code cache bound to one compiled Source generation.
     *
     * @param sourceId   exact Source identifier
     * @param generation non-negative Source configuration generation
     * @return Source-generation-scoped device-code cache
     */
    DeviceCodeCache deviceCodeCache(final String sourceId, final long generation) {
        return new DeviceCodeCache(authenticationCache, cacheDeployment, sourceId, generation, FabricX.clock());
    }

    /**
     * Creates the authorization lifecycle cache bound to one compiled Source generation.
     *
     * @param sourceId   exact Source identifier
     * @param generation non-negative Source configuration generation
     * @return Source-generation-scoped authorization lifecycle cache
     */
    AuthorizationCache authorizationCache(final String sourceId, final long generation) {
        return new AuthorizationCache(authenticationCache, cacheDeployment, sourceId, generation, FabricX.clock());
    }

    /**
     * Creates the access-token cache bound to one compiled Source generation.
     *
     * @param sourceId   exact Source identifier
     * @param generation non-negative Source configuration generation
     * @return Source-generation-scoped access-token cache
     */
    AccessTokenCache accessTokenCache(final String sourceId, final long generation) {
        return new AccessTokenCache(authenticationCache, cacheDeployment, sourceId, generation, FabricX.clock());
    }

    /**
     * Creates the refresh-token cache bound to one compiled Source generation.
     *
     * @param sourceId   exact Source identifier
     * @param generation non-negative Source configuration generation
     * @return Source-generation-scoped refresh-token cache
     */
    RefreshTokenCache refreshTokenCache(final String sourceId, final long generation) {
        return new RefreshTokenCache(authenticationCache, cacheDeployment, sourceId, generation, FabricX.clock());
    }

    /**
     * Creates the Session cache bound to one compiled Source generation.
     *
     * @param sourceId   exact Source identifier
     * @param generation non-negative Source configuration generation
     * @return Source-generation-scoped Session cache
     */
    SessionCache sessionCache(final String sourceId, final long generation) {
        return new SessionCache(authenticationCache, cacheDeployment, sourceId, generation, FabricX.clock());
    }

    /**
     * Creates the issued ID Token binding cache bound to one compiled Source generation.
     *
     * @param sourceId   exact Source identifier
     * @param generation non-negative Source configuration generation
     * @return Source-generation-scoped ID Token binding cache
     */
    IdTokenCache idTokenCache(final String sourceId, final long generation) {
        return new IdTokenCache(authenticationCache, cacheDeployment, sourceId, generation, FabricX.clock());
    }

    /**
     * {@return the replay cache}
     */
    ReplayCache replayCache() {
        return replayCache;
    }

    /**
     * {@return the project consent service}
     */
    ConsentService consentService() {
        return workers.consentService();
    }

    /**
     * {@return the project Session worker}
     */
    SessionWorker sessionWorker() {
        return workers.sessionWorker();
    }

    /**
     * {@return the runtime security policies}
     */
    Policies policies() {
        return policies;
    }

    /**
     * Creates the only Driver-visible service view after validating every declared project data port.
     *
     * @param entry        exact Source Blueprint entry owning the scoped view
     * @param slots        exact project data ports required by one prepared Source
     * @param dependencies exact framework services required by the same prepared Source
     * @param generation   security-state generation assigned by snapshot compilation
     * @return capability-limited Driver service view
     * @throws IllegalArgumentException if an argument is {@code null} or a required project port is unavailable
     */
    DriverServices scope(
            final Blueprint.SourceEntry entry,
            final WorkerSlots slots,
            final SourceDriver.Dependencies dependencies,
            final long generation) {
        final Blueprint.SourceEntry source = Assert.notNull(entry, "Scoped Source Blueprint entry must not be null");
        final WorkerSlots selected = Assert.notNull(slots, "Source Worker slots must not be null");
        final SourceDriver.Dependencies required = Assert
                .notNull(dependencies, "Source framework dependencies must not be null");
        workers.require(selected);
        Assert.isTrue(generation >= 0L, "Source security-state generation must not be negative");
        return new ScopedDriverServices(this, source, selected, required, generation);
    }

}
