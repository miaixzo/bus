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

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.auth.worker.identity.ClaimLoader;
import org.miaixz.bus.auth.worker.identity.IdentityLoader;
import org.miaixz.bus.core.lang.Assert;

/**
 * Immutable aggregation of project-owned authentication data input and output ports.
 * <p>
 * This type only freezes explicitly supplied ports for runtime assembly. It performs no loading, parsing, registration,
 * security decision, consent decision, audit delivery, or service lookup by arbitrary type.
 * </p>
 *
 * @param registrationLoader complete registration-state input
 * @param bindingLoader      project runtime-binding input
 * @param consumerLoader     registered consumer input
 * @param subjectLoader      subject input
 * @param credentialLoader   credential-description input
 * @param secretLoader       protected secret input
 * @param credentialStore    generated dynamic-credential output and input
 * @param keyLoader          cryptographic key input
 * @param certificateLoader  certificate and trust-root input
 * @param attributeLoader    subject-attribute input
 * @param groupLoader        group-membership input
 * @param resourceLoader     protected-resource input
 * @param identityLoader     project identity input
 * @param claimLoader        project claim input
 * @param auditSink          sanitized audit-event output
 * @param consentService     project consent input and output
 * @param registryListeners  Registry publication observers
 * @author Kimi Liu
 */
public record WorkerSet(RegistrationLoader registrationLoader, BindingLoader bindingLoader,
        ConsumerLoader consumerLoader, SubjectLoader subjectLoader, CredentialLoader credentialLoader,
        SecretLoader secretLoader, CredentialStore credentialStore, KeyLoader keyLoader,
        CertificateLoader certificateLoader, AttributeLoader attributeLoader, GroupLoader groupLoader,
        ResourceLoader resourceLoader, IdentityLoader identityLoader, ClaimLoader claimLoader, AuditSink auditSink,
        ConsentService consentService, List<RegistryListener> registryListeners) {

    /**
     * Validates and freezes the complete project integration boundary.
     *
     * @throws IllegalArgumentException if a port, listener list, or listener is {@code null}
     */
    public WorkerSet {
        Assert.notNull(registrationLoader, "Registration loader must not be null");
        Assert.notNull(bindingLoader, "Binding loader must not be null");
        Assert.notNull(consumerLoader, "Consumer loader must not be null");
        Assert.notNull(subjectLoader, "Subject loader must not be null");
        Assert.notNull(credentialLoader, "Credential loader must not be null");
        Assert.notNull(secretLoader, "Secret loader must not be null");
        Assert.notNull(credentialStore, "Credential store must not be null");
        Assert.notNull(keyLoader, "Key loader must not be null");
        Assert.notNull(certificateLoader, "Certificate loader must not be null");
        Assert.notNull(attributeLoader, "Attribute loader must not be null");
        Assert.notNull(groupLoader, "Group loader must not be null");
        Assert.notNull(resourceLoader, "Resource loader must not be null");
        Assert.notNull(identityLoader, "Identity loader must not be null");
        Assert.notNull(claimLoader, "Claim loader must not be null");
        Assert.notNull(auditSink, "Audit sink must not be null");
        Assert.notNull(consentService, "Consent service must not be null");
        Assert.notNull(registryListeners, "Registry listener list must not be null");
        final List<RegistryListener> copy = new ArrayList<>(registryListeners.size());
        for (RegistryListener listener : registryListeners) {
            copy.add(Assert.notNull(listener, "Registry listener must not be null"));
        }
        registryListeners = List.copyOf(copy);
    }
}
