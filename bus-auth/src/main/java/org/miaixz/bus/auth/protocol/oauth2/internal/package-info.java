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
/**
 * Compiles and assembles non-exported OAuth 2.x runtime components.
 * <p>
 * OAuth2ProviderDriver and OAuth2SourceDriver bind the direction-specific profiles to exact registration validation and
 * runtime capability construction. {@link org.miaixz.bus.auth.protocol.oauth2.internal.AuthorizationCodeIssuer},
 * {@link org.miaixz.bus.auth.protocol.oauth2.internal.AccessTokenIssuer}, and
 * {@link org.miaixz.bus.auth.protocol.oauth2.internal.RefreshTokenRotator} are narrow issuance ports consumed by server
 * services; their storage and project-specific implementations remain outside this module.
 * </p>
 * <p>
 * RuntimeBuilder receives these drivers through the public OAuth2 facade. This package may depend on formal OAuth
 * models, public Provider and Source profiles, Registry SPI, resolvers, stores, SecurityBaseline, and shared
 * cryptographic capabilities. It does not expose a public protocol operation, load project data, discover classes by
 * reflection or ServiceLoader, retain a mutable global registry, call Vendor adapters, or implement persistence.
 * </p>
 * <p>
 * Compilation fails closed when the registration direction, protocol, namespace, settings type, endpoint set,
 * client-authentication method, grant set, conformance, or declared manifest is inconsistent. A failed candidate never
 * publishes a partial runtime. Issuance and rotation receive one bounded Context and Budget, use atomic state changes,
 * and prevent authorization codes, refresh tokens, access tokens, client secrets, and signing material from escaping
 * their operation lifetime or entering diagnostics.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.protocol.oauth2.internal;
