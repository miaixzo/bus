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
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.fabric.Context;

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

    Context fabricContext();

    JsonProvider jsonProvider();

    Executor executor();

    BindingLoader bindingLoader();

    BindingParser bindingParser();

    ConsumerLoader consumerLoader();

    ConsumerParser consumerParser();

    SecretLoader secretLoader();

    SecretParser secretParser();

    CredentialStore credentialStore();

    KeyLoader keyLoader();

    KeyParser keyParser();

    CertificateLoader certificateLoader();

    CertificateParser certificateParser();

    AttributeLoader attributeLoader();

    AttributeParser attributeParser();

    ResourceLoader resourceLoader();

    ResourceParser resourceParser();

    StateCache stateCache();

    NonceCache nonceCache();

    AuthorizationCodeCache authorizationCodeCache();

    DeviceCodeCache deviceCodeCache();

    AuthorizationCache authorizationCache();

    AccessTokenCache accessTokenCache();

    RefreshTokenCache refreshTokenCache();

    SessionCache sessionCache();

    ReplayCache replayCache();

    ConsentService consentService();

    SessionWorker sessionWorker();

    SecurityBaseline securityBaseline();

}
