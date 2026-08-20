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

import java.util.List;
import java.util.concurrent.Executor;

import org.miaixz.bus.auth.cache.AccessTokenCache;
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
import org.miaixz.bus.auth.resolver.CredentialParser;
import org.miaixz.bus.auth.resolver.GroupParser;
import org.miaixz.bus.auth.resolver.KeyParser;
import org.miaixz.bus.auth.resolver.ResourceParser;
import org.miaixz.bus.auth.resolver.SecretParser;
import org.miaixz.bus.auth.resolver.SubjectParser;
import org.miaixz.bus.auth.shared.SecurityBaseline;
import org.miaixz.bus.auth.worker.AttributeLoader;
import org.miaixz.bus.auth.worker.AuditSink;
import org.miaixz.bus.auth.worker.BindingLoader;
import org.miaixz.bus.auth.worker.CertificateLoader;
import org.miaixz.bus.auth.worker.ConsentService;
import org.miaixz.bus.auth.worker.ConsumerLoader;
import org.miaixz.bus.auth.worker.CredentialLoader;
import org.miaixz.bus.auth.worker.CredentialStore;
import org.miaixz.bus.auth.worker.GroupLoader;
import org.miaixz.bus.auth.worker.KeyLoader;
import org.miaixz.bus.auth.worker.RegistrationLoader;
import org.miaixz.bus.auth.worker.RegistryListener;
import org.miaixz.bus.auth.worker.ResourceLoader;
import org.miaixz.bus.auth.worker.SecretLoader;
import org.miaixz.bus.auth.worker.SubjectLoader;
import org.miaixz.bus.auth.worker.WorkerSet;
import org.miaixz.bus.auth.worker.identity.ClaimLoader;
import org.miaixz.bus.auth.worker.identity.IdentityLoader;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.fabric.Context;

/**
 * Immutable runtime dependency container.
 * <p>
 * External I/O ports and pure parsers are exposed as separate collaborators. This class performs no loading, parsing,
 * registration, security decision, or audit delivery.
 * </p>
 *
 * @author Kimi Liu
 */
public final class ExecutionServices {

    private final Context fabricContext;
    private final JsonProvider jsonProvider;
    private final Executor executor;
    private final WorkerSet workers;
    private final BindingLoader bindingLoader;
    private final BindingParser bindingParser;
    private final ConsumerLoader consumerLoader;
    private final ConsumerParser consumerParser;
    private final SubjectLoader subjectLoader;
    private final SubjectParser subjectParser;
    private final CredentialLoader credentialLoader;
    private final CredentialParser credentialParser;
    private final SecretLoader secretLoader;
    private final SecretParser secretParser;
    private final CredentialStore credentialStore;
    private final KeyLoader keyLoader;
    private final KeyParser keyParser;
    private final CertificateLoader certificateLoader;
    private final CertificateParser certificateParser;
    private final AttributeLoader attributeLoader;
    private final AttributeParser attributeParser;
    private final GroupLoader groupLoader;
    private final GroupParser groupParser;
    private final ResourceLoader resourceLoader;
    private final ResourceParser resourceParser;
    private final StateCache stateCache;
    private final NonceCache nonceCache;
    private final AuthorizationCodeCache authorizationCodeCache;
    private final DeviceCodeCache deviceCodeCache;
    private final AccessTokenCache accessTokenCache;
    private final RefreshTokenCache refreshTokenCache;
    private final SessionCache sessionCache;
    private final ReplayCache replayCache;
    private final AuditSink auditSink;
    private final ConsentService consentService;
    private final SecurityBaseline securityBaseline;

    /**
     * Creates the complete immutable runtime dependency set and the framework-owned stateless parsers and cache views.
     *
     * @param fabricContext    caller-owned Fabric execution context
     * @param jsonProvider     caller-owned JSON provider
     * @param executor         caller-owned executor
     * @param workers          complete project data input and output boundary
     * @param cache            shared authentication cache backend
     * @param securityBaseline non-relaxable framework security policy
     * @throws IllegalArgumentException if a dependency is {@code null}
     */
    public ExecutionServices(final Context fabricContext, final JsonProvider jsonProvider, final Executor executor,
            final WorkerSet workers, final CacheX<String, Object> cache, final SecurityBaseline securityBaseline) {
        this.fabricContext = Assert.notNull(fabricContext, "Fabric context must not be null");
        this.jsonProvider = Assert.notNull(jsonProvider, "JSON provider must not be null");
        this.executor = Assert.notNull(executor, "Runtime executor must not be null");
        this.workers = Assert.notNull(workers, "Worker set must not be null");
        this.bindingLoader = workers.bindingLoader();
        this.bindingParser = new BindingParser();
        this.consumerLoader = workers.consumerLoader();
        this.consumerParser = new ConsumerParser();
        this.subjectLoader = workers.subjectLoader();
        this.subjectParser = new SubjectParser();
        this.credentialLoader = workers.credentialLoader();
        this.credentialParser = new CredentialParser();
        this.secretLoader = workers.secretLoader();
        this.secretParser = new SecretParser();
        this.credentialStore = workers.credentialStore();
        this.keyLoader = workers.keyLoader();
        this.keyParser = new KeyParser();
        this.certificateLoader = workers.certificateLoader();
        this.certificateParser = new CertificateParser();
        this.attributeLoader = workers.attributeLoader();
        this.attributeParser = new AttributeParser();
        this.groupLoader = workers.groupLoader();
        this.groupParser = new GroupParser();
        this.resourceLoader = workers.resourceLoader();
        this.resourceParser = new ResourceParser();
        final CacheX<String, Object> authenticationCache = Assert
                .notNull(cache, "Authentication cache must not be null");
        this.stateCache = new StateCache(authenticationCache);
        this.nonceCache = new NonceCache(authenticationCache);
        this.authorizationCodeCache = new AuthorizationCodeCache(authenticationCache);
        this.deviceCodeCache = new DeviceCodeCache(authenticationCache);
        this.accessTokenCache = new AccessTokenCache(authenticationCache);
        this.refreshTokenCache = new RefreshTokenCache(authenticationCache);
        this.sessionCache = new SessionCache(authenticationCache);
        this.replayCache = new ReplayCache(authenticationCache);
        this.auditSink = workers.auditSink();
        this.consentService = workers.consentService();
        this.securityBaseline = Assert.notNull(securityBaseline, "Security baseline must not be null");
    }

    public Context fabricContext() {
        return fabricContext;
    }

    public JsonProvider jsonProvider() {
        return jsonProvider;
    }

    public Executor executor() {
        return executor;
    }

    public RegistrationLoader registrationLoader() {
        return workers.registrationLoader();
    }

    public List<RegistryListener> registryListeners() {
        return workers.registryListeners();
    }

    public IdentityLoader identityLoader() {
        return workers.identityLoader();
    }

    public ClaimLoader claimLoader() {
        return workers.claimLoader();
    }

    public BindingLoader bindingLoader() {
        return bindingLoader;
    }

    public BindingParser bindingParser() {
        return bindingParser;
    }

    public ConsumerLoader consumerLoader() {
        return consumerLoader;
    }

    public ConsumerParser consumerParser() {
        return consumerParser;
    }

    public SubjectLoader subjectLoader() {
        return subjectLoader;
    }

    public SubjectParser subjectParser() {
        return subjectParser;
    }

    public CredentialLoader credentialLoader() {
        return credentialLoader;
    }

    public CredentialParser credentialParser() {
        return credentialParser;
    }

    public SecretLoader secretLoader() {
        return secretLoader;
    }

    public SecretParser secretParser() {
        return secretParser;
    }

    public CredentialStore credentialStore() {
        return credentialStore;
    }

    public KeyLoader keyLoader() {
        return keyLoader;
    }

    public KeyParser keyParser() {
        return keyParser;
    }

    public CertificateLoader certificateLoader() {
        return certificateLoader;
    }

    public CertificateParser certificateParser() {
        return certificateParser;
    }

    public AttributeLoader attributeLoader() {
        return attributeLoader;
    }

    public AttributeParser attributeParser() {
        return attributeParser;
    }

    public GroupLoader groupLoader() {
        return groupLoader;
    }

    public GroupParser groupParser() {
        return groupParser;
    }

    public ResourceLoader resourceLoader() {
        return resourceLoader;
    }

    public ResourceParser resourceParser() {
        return resourceParser;
    }

    public StateCache stateCache() {
        return stateCache;
    }

    public NonceCache nonceCache() {
        return nonceCache;
    }

    public AuthorizationCodeCache authorizationCodeCache() {
        return authorizationCodeCache;
    }

    public DeviceCodeCache deviceCodeCache() {
        return deviceCodeCache;
    }

    public AccessTokenCache accessTokenCache() {
        return accessTokenCache;
    }

    public RefreshTokenCache refreshTokenCache() {
        return refreshTokenCache;
    }

    public SessionCache sessionCache() {
        return sessionCache;
    }

    public ReplayCache replayCache() {
        return replayCache;
    }

    public AuditSink auditSink() {
        return auditSink;
    }

    public ConsentService consentService() {
        return consentService;
    }

    public SecurityBaseline securityBaseline() {
        return securityBaseline;
    }
}
