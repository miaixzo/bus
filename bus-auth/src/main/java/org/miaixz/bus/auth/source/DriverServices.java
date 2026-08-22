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
package org.miaixz.bus.auth.source;

import java.util.concurrent.Executor;

import org.miaixz.bus.auth.Blueprint;
import org.miaixz.bus.auth.FabricX;
import org.miaixz.bus.auth.cache.*;
import org.miaixz.bus.auth.resolver.*;
import org.miaixz.bus.auth.shared.SecurityBaseline;
import org.miaixz.bus.auth.worker.*;
import org.miaixz.bus.auth.worker.loader.*;
import org.miaixz.bus.extra.json.JsonProvider;

/**
 * Defines the protocol execution services visible to a compiled Source driver.
 * <p>
 * Runtime assembly owns the implementation. Protocol and Vendor packages depend only on this lower-level contract and
 * therefore never import the runtime assembly package.
 * </p>
 *
 * @author Kimi Liu
 */
public interface DriverServices {

    /**
     * Returns the exact immutable registration bound to this Source service view.
     *
     * @return current Source registration
     */
    Blueprint.SourceEntry registration();

    /**
     * Returns the Runtime-scoped facade for every Fabric protocol used by bus-auth.
     *
     * @return guarded Fabric protocol facade
     */
    FabricX fabric();

    /**
     * Returns the selected provider-neutral JSON implementation.
     *
     * @return selected JSON implementation
     */
    JsonProvider jsonProvider();

    /**
     * Returns the executor assigned to asynchronous Source work.
     *
     * @return Source executor
     */
    Executor executor();

    /**
     * Returns the project binding loading port.
     *
     * @return binding resolver
     */
    BindingResolver bindingResolver();

    /**
     * Returns the project consumer metadata loading port.
     *
     * @return consumer loader
     */
    ConsumerLoader consumerLoader();

    /**
     * Returns the framework consumer metadata parser.
     *
     * @return consumer parser
     */
    ConsumerParser consumerParser();

    /**
     * Returns the project server-side consumer evidence verifier.
     *
     * @return consumer evidence verifier
     */
    ConsumerVerifier consumerVerifier();

    /**
     * Returns the project federation relation loader.
     *
     * @return federation relation loader
     */
    FederationLoader federationLoader();

    /**
     * Returns the framework federation relation parser.
     *
     * @return federation relation parser
     */
    FederationParser federationParser();

    /**
     * Returns the project secret loading port.
     *
     * @return secret loader
     */
    SecretLoader secretLoader();

    /**
     * Returns the framework secret parser.
     *
     * @return secret parser
     */
    SecretParser secretParser();

    /**
     * Returns the project credential storage port.
     *
     * @return credential store
     */
    CredentialStore credentialStore();

    /**
     * Returns the project cryptographic key loading port.
     *
     * @return key loader
     */
    KeyLoader keyLoader();

    /**
     * Returns the framework cryptographic key parser.
     *
     * @return key parser
     */
    KeyParser keyParser();

    /**
     * Returns the project certificate loading port.
     *
     * @return certificate loader
     */
    CertificateLoader certificateLoader();

    /**
     * Returns the framework certificate parser.
     *
     * @return certificate parser
     */
    CertificateParser certificateParser();

    /**
     * Returns the project subject-attribute loading port.
     *
     * @return attribute loader
     */
    AttributeLoader attributeLoader();

    /**
     * Returns the framework subject-attribute parser.
     *
     * @return attribute parser
     */
    AttributeParser attributeParser();

    /**
     * Returns the project protected-resource loading port.
     *
     * @return resource loader
     */
    ResourceLoader resourceLoader();

    /**
     * Returns the framework protected-resource parser.
     *
     * @return resource parser
     */
    ResourceParser resourceParser();

    /**
     * Returns the callback correlation cache view.
     *
     * @return state cache
     */
    StateCache stateCache();

    /**
     * Returns the one-time nonce cache view.
     *
     * @return nonce cache
     */
    NonceCache nonceCache();

    /**
     * Returns the one-time authorization-code cache view.
     *
     * @return authorization-code cache
     */
    AuthorizationCodeCache authorizationCodeCache();

    /**
     * Returns the device-authorization cache view.
     *
     * @return device-code cache
     */
    DeviceCodeCache deviceCodeCache();

    /**
     * Returns the authoritative authorization lifecycle cache view.
     *
     * @return authorization cache
     */
    AuthorizationCache authorizationCache();

    /**
     * Returns the access-token validation cache view.
     *
     * @return access-token cache
     */
    AccessTokenCache accessTokenCache();

    /**
     * Returns the refresh-token family cache view.
     *
     * @return refresh-token cache
     */
    RefreshTokenCache refreshTokenCache();

    /**
     * Returns the authentication Session cache view.
     *
     * @return Session cache
     */
    SessionCache sessionCache();

    /**
     * Returns the issued ID Token logout-binding cache.
     *
     * @return issued ID Token binding cache
     */
    IdTokenCache idTokenCache();

    /**
     * Returns the protocol replay-prevention cache view.
     *
     * @return replay cache
     */
    ReplayCache replayCache();

    /**
     * Returns the project consent lookup and persistence port.
     *
     * @return consent service
     */
    ConsentService consentService();

    /**
     * Returns the framework authentication Session coordinator.
     *
     * @return Session worker
     */
    SessionWorker sessionWorker();

    /**
     * Returns immutable security limits applied to Source execution.
     *
     * @return security baseline
     */
    SecurityBaseline securityBaseline();

}
