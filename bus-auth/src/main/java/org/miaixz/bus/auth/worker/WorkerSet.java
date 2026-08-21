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
public final class WorkerSet {

    private final BindingLoader bindingLoader;
    private final ConsumerLoader consumerLoader;
    private final SecretLoader secretLoader;
    private final CredentialStore credentialStore;
    private final KeyLoader keyLoader;
    private final CertificateLoader certificateLoader;
    private final AttributeLoader attributeLoader;
    private final ResourceLoader resourceLoader;
    private final ConsentService consentService;
    private final SessionWorker sessionWorker;

    private WorkerSet(final Builder builder) {
        this.bindingLoader = builder.bindingLoader;
        this.consumerLoader = builder.consumerLoader;
        this.secretLoader = builder.secretLoader;
        this.credentialStore = builder.credentialStore;
        this.keyLoader = builder.keyLoader;
        this.certificateLoader = builder.certificateLoader;
        this.attributeLoader = builder.attributeLoader;
        this.resourceLoader = builder.resourceLoader;
        this.consentService = builder.consentService;
        this.sessionWorker = builder.sessionWorker;
    }

    public static Builder builder() {
        return new Builder();
    }

    private static <T> T required(final T value, final String message) {
        return Assert.notNull(value, message);
    }

    public BindingLoader bindingLoader() {
        return required(bindingLoader, "Binding loader is required by the selected Source driver");
    }

    public ConsumerLoader consumerLoader() {
        return required(consumerLoader, "Consumer loader is required by the selected Source driver");
    }

    public SecretLoader secretLoader() {
        return required(secretLoader, "Secret loader is required by the selected Source driver");
    }

    public CredentialStore credentialStore() {
        return required(credentialStore, "Credential store is required by the selected Source driver");
    }

    public KeyLoader keyLoader() {
        return required(keyLoader, "Key loader is required by the selected Source driver");
    }

    public CertificateLoader certificateLoader() {
        return required(certificateLoader, "Certificate loader is required by the selected Source driver");
    }

    public AttributeLoader attributeLoader() {
        return required(attributeLoader, "Attribute loader is required by the selected Source driver");
    }

    public ResourceLoader resourceLoader() {
        return required(resourceLoader, "Resource loader is required by the selected Source driver");
    }

    public ConsentService consentService() {
        return required(consentService, "Consent service is required by the selected Source driver");
    }

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
                case BINDING -> bindingLoader();
                case CONSUMER -> consumerLoader();
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
     */
    public static final class Builder {

        private BindingLoader bindingLoader;
        private ConsumerLoader consumerLoader;
        private SecretLoader secretLoader;
        private CredentialStore credentialStore;
        private KeyLoader keyLoader;
        private CertificateLoader certificateLoader;
        private AttributeLoader attributeLoader;
        private ResourceLoader resourceLoader;
        private ConsentService consentService;
        private SessionWorker sessionWorker;

        private Builder() {
            // Created through WorkerSet.builder().
        }

        public Builder bindingLoader(final BindingLoader value) {
            this.bindingLoader = Assert.notNull(value, "Binding loader must not be null");
            return this;
        }

        public Builder consumerLoader(final ConsumerLoader value) {
            this.consumerLoader = Assert.notNull(value, "Consumer loader must not be null");
            return this;
        }

        public Builder secretLoader(final SecretLoader value) {
            this.secretLoader = Assert.notNull(value, "Secret loader must not be null");
            return this;
        }

        public Builder credentialStore(final CredentialStore value) {
            this.credentialStore = Assert.notNull(value, "Credential store must not be null");
            return this;
        }

        public Builder keyLoader(final KeyLoader value) {
            this.keyLoader = Assert.notNull(value, "Key loader must not be null");
            return this;
        }

        public Builder certificateLoader(final CertificateLoader value) {
            this.certificateLoader = Assert.notNull(value, "Certificate loader must not be null");
            return this;
        }

        public Builder attributeLoader(final AttributeLoader value) {
            this.attributeLoader = Assert.notNull(value, "Attribute loader must not be null");
            return this;
        }

        public Builder resourceLoader(final ResourceLoader value) {
            this.resourceLoader = Assert.notNull(value, "Resource loader must not be null");
            return this;
        }

        public Builder consentService(final ConsentService value) {
            this.consentService = Assert.notNull(value, "Consent service must not be null");
            return this;
        }

        public Builder sessionWorker(final SessionWorker value) {
            this.sessionWorker = Assert.notNull(value, "Session worker must not be null");
            return this;
        }

        public WorkerSet build() {
            return new WorkerSet(this);
        }

    }

}
