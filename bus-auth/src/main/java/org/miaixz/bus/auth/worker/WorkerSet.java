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
package org.miaixz.bus.auth.worker;

import org.miaixz.bus.auth.worker.loader.*;
import org.miaixz.bus.core.lang.Assert;

/**
 * Freezes the project data ports selected for one authentication runtime.
 * <p>
 * A project supplies only ports required by its selected Source drivers. Accessing an absent port fails immediately
 * during compilation of the Source that requires it; this class never installs permissive or silent no-op behavior.
 * Registration loading, Registry listeners, identity completion, parsing, security, and auditing are outside this
 * aggregation.
 * </p>
 *
 * @author Kimi Liu
 */
public class WorkerSet {

    /**
     * Optional project binding resolver.
     */
    private final BindingResolver bindingResolver;
    /**
     * Optional project consumer loader.
     */
    private final ConsumerLoader consumerLoader;
    /**
     * Optional project consumer evidence verifier.
     */
    private final ConsumerVerifier consumerVerifier;
    /**
     * Optional project federation relation loader.
     */
    private final FederationLoader federationLoader;
    /**
     * Optional project secret loader.
     */
    private final SecretLoader secretLoader;
    /**
     * Optional project credential store.
     */
    private final CredentialStore credentialStore;
    /**
     * Optional project key loader.
     */
    private final KeyLoader keyLoader;
    /**
     * Optional project certificate loader.
     */
    private final CertificateLoader certificateLoader;
    /**
     * Optional project attribute loader.
     */
    private final AttributeLoader attributeLoader;
    /**
     * Optional project resource loader.
     */
    private final ResourceLoader resourceLoader;
    /**
     * Optional project consent service.
     */
    private final ConsentService consentService;
    /**
     * Optional project Session worker.
     */
    private final SessionWorker sessionWorker;

    /**
     * Creates one immutable worker-port selection.
     *
     * @param builder populated builder
     */
    public WorkerSet(final Builder builder) {
        this.bindingResolver = builder.bindingResolver;
        this.consumerLoader = builder.consumerLoader;
        this.consumerVerifier = builder.consumerVerifier;
        this.federationLoader = builder.federationLoader;
        this.secretLoader = builder.secretLoader;
        this.credentialStore = builder.credentialStore;
        this.keyLoader = builder.keyLoader;
        this.certificateLoader = builder.certificateLoader;
        this.attributeLoader = builder.attributeLoader;
        this.resourceLoader = builder.resourceLoader;
        this.consentService = builder.consentService;
        this.sessionWorker = builder.sessionWorker;
    }

    /**
     * {@return a new empty worker-set builder}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Requires one project port selected by a Source.
     *
     * @param <T>     port type
     * @param value   selected port
     * @param message missing-port message
     * @return required port
     */
    private static <T> T required(final T value, final String message) {
        return Assert.notNull(value, message);
    }

    /**
     * {@return the required project binding resolver}
     */
    public BindingResolver bindingResolver() {
        return required(bindingResolver, "Binding resolver is required by the selected Source driver");
    }

    /**
     * {@return the required project consumer loader}
     */
    public ConsumerLoader consumerLoader() {
        return required(consumerLoader, "Consumer loader is required by the selected Source driver");
    }

    /**
     * {@return the required project consumer evidence verifier}
     */
    public ConsumerVerifier consumerVerifier() {
        return required(consumerVerifier, "Consumer verifier is required by the selected Source driver");
    }

    /**
     * {@return the required project federation relation loader}
     */
    public FederationLoader federationLoader() {
        return required(federationLoader, "Federation loader is required by the selected Source driver");
    }

    /**
     * {@return the required project secret loader}
     */
    public SecretLoader secretLoader() {
        return required(secretLoader, "Secret loader is required by the selected Source driver");
    }

    /**
     * {@return the required project credential store}
     */
    public CredentialStore credentialStore() {
        return required(credentialStore, "Credential store is required by the selected Source driver");
    }

    /**
     * {@return the required project key loader}
     */
    public KeyLoader keyLoader() {
        return required(keyLoader, "Key loader is required by the selected Source driver");
    }

    /**
     * {@return the required project certificate loader}
     */
    public CertificateLoader certificateLoader() {
        return required(certificateLoader, "Certificate loader is required by the selected Source driver");
    }

    /**
     * {@return the required project attribute loader}
     */
    public AttributeLoader attributeLoader() {
        return required(attributeLoader, "Attribute loader is required by the selected Source driver");
    }

    /**
     * {@return the required project resource loader}
     */
    public ResourceLoader resourceLoader() {
        return required(resourceLoader, "Resource loader is required by the selected Source driver");
    }

    /**
     * {@return the required project consent service}
     */
    public ConsentService consentService() {
        return required(consentService, "Consent service is required by the selected Source driver");
    }

    /**
     * {@return the required project Session worker}
     */
    public SessionWorker sessionWorker() {
        return required(sessionWorker, "Session worker is required by the selected Source driver");
    }

    /**
     * Verifies the complete project data contract of one Source during snapshot compilation.
     *
     * @param slots required Worker slots
     */
    public void require(final WorkerSlots slots) {
        Assert.notNull(slots, "Source Worker slots must not be null");
        for (WorkerSlots.Slot slot : slots.slots()) {
            switch (slot) {
                case BINDING -> bindingResolver();
                case CONSUMER -> consumerLoader();
                case CONSUMER_VERIFIER -> consumerVerifier();
                case FEDERATION -> federationLoader();
                case SECRET -> secretLoader();
                case CREDENTIAL -> credentialStore();
                case KEY -> keyLoader();
                case CERTIFICATE -> certificateLoader();
                case ATTRIBUTE -> attributeLoader();
                case RESOURCE -> resourceLoader();
                case CONSENT -> consentService();
                case SESSION -> sessionWorker();
            }
        }
    }

    /**
     * Collects explicit project ports without deciding which protocols are enabled.
     *
     * @author Kimi Liu
     */
    public static class Builder {

        /**
         * Selected binding resolver.
         */
        private BindingResolver bindingResolver;
        /**
         * Selected consumer loader.
         */
        private ConsumerLoader consumerLoader;
        /**
         * Selected consumer evidence verifier.
         */
        private ConsumerVerifier consumerVerifier;
        /**
         * Selected federation relation loader.
         */
        private FederationLoader federationLoader;
        /**
         * Selected secret loader.
         */
        private SecretLoader secretLoader;
        /**
         * Selected credential store.
         */
        private CredentialStore credentialStore;
        /**
         * Selected key loader.
         */
        private KeyLoader keyLoader;
        /**
         * Selected certificate loader.
         */
        private CertificateLoader certificateLoader;
        /**
         * Selected attribute loader.
         */
        private AttributeLoader attributeLoader;
        /**
         * Selected resource loader.
         */
        private ResourceLoader resourceLoader;
        /**
         * Selected consent service.
         */
        private ConsentService consentService;
        /**
         * Selected Session worker.
         */
        private SessionWorker sessionWorker;

        /**
         * Creates an empty worker-port builder.
         */
        public Builder() {
            // No initialization required.
            // Created through WorkerSet.builder().
        }

        /**
         * Sets the project binding resolver.
         *
         * @param value binding resolver
         * @return this builder
         */
        public Builder bindingResolver(final BindingResolver value) {
            this.bindingResolver = Assert.notNull(value, "Binding resolver must not be null");
            return this;
        }

        /**
         * Sets the project consumer loader.
         *
         * @param value consumer loader
         * @return this builder
         */
        public Builder consumerLoader(final ConsumerLoader value) {
            this.consumerLoader = Assert.notNull(value, "Consumer loader must not be null");
            return this;
        }

        /**
         * Sets the project consumer evidence verifier.
         *
         * @param value consumer evidence verifier
         * @return this builder
         */
        public Builder consumerVerifier(final ConsumerVerifier value) {
            this.consumerVerifier = Assert.notNull(value, "Consumer verifier must not be null");
            return this;
        }

        /**
         * Sets the project federation relation loader.
         *
         * @param value federation relation loader
         * @return this builder
         */
        public Builder federationLoader(final FederationLoader value) {
            this.federationLoader = Assert.notNull(value, "Federation loader must not be null");
            return this;
        }

        /**
         * Sets the project secret loader.
         *
         * @param value secret loader
         * @return this builder
         */
        public Builder secretLoader(final SecretLoader value) {
            this.secretLoader = Assert.notNull(value, "Secret loader must not be null");
            return this;
        }

        /**
         * Sets the project credential store.
         *
         * @param value credential store
         * @return this builder
         */
        public Builder credentialStore(final CredentialStore value) {
            this.credentialStore = Assert.notNull(value, "Credential store must not be null");
            return this;
        }

        /**
         * Sets the project key loader.
         *
         * @param value key loader
         * @return this builder
         */
        public Builder keyLoader(final KeyLoader value) {
            this.keyLoader = Assert.notNull(value, "Key loader must not be null");
            return this;
        }

        /**
         * Sets the project certificate loader.
         *
         * @param value certificate loader
         * @return this builder
         */
        public Builder certificateLoader(final CertificateLoader value) {
            this.certificateLoader = Assert.notNull(value, "Certificate loader must not be null");
            return this;
        }

        /**
         * Sets the project attribute loader.
         *
         * @param value attribute loader
         * @return this builder
         */
        public Builder attributeLoader(final AttributeLoader value) {
            this.attributeLoader = Assert.notNull(value, "Attribute loader must not be null");
            return this;
        }

        /**
         * Sets the project resource loader.
         *
         * @param value resource loader
         * @return this builder
         */
        public Builder resourceLoader(final ResourceLoader value) {
            this.resourceLoader = Assert.notNull(value, "Resource loader must not be null");
            return this;
        }

        /**
         * Sets the project consent service.
         *
         * @param value consent service
         * @return this builder
         */
        public Builder consentService(final ConsentService value) {
            this.consentService = Assert.notNull(value, "Consent service must not be null");
            return this;
        }

        /**
         * Sets the project Session worker.
         *
         * @param value Session worker
         * @return this builder
         */
        public Builder sessionWorker(final SessionWorker value) {
            this.sessionWorker = Assert.notNull(value, "Session worker must not be null");
            return this;
        }

        /**
         * {@return an immutable worker-port selection}
         */
        public WorkerSet build() {
            return new WorkerSet(this);
        }

    }

}
