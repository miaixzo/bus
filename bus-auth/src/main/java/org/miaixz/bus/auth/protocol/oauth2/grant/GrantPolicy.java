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
package org.miaixz.bus.auth.protocol.oauth2.grant;

import java.time.Duration;
import java.util.Set;

import org.miaixz.bus.auth.protocol.oauth2.GrantType;

/**
 * Exposes only the immutable OAuth 2.x policy required by grant processing.
 * <p>
 * Provider options implement this contract so grant mechanisms do not depend on endpoint or server orchestration
 * packages. It contains no endpoint addresses, client-authentication policy, persistence, or project configuration.
 * </p>
 *
 * @author Kimi Liu
 */
public interface GrantPolicy {

    String issuer();

    Set<String> scopesSupported();

    Set<GrantType> grantTypesSupported();

    Duration authorizationCodeLifetime();

    Duration accessTokenLifetime();

    Duration refreshTokenLifetime();

    boolean pkceRequired();

    boolean refreshTokenRotationRequired();

}
