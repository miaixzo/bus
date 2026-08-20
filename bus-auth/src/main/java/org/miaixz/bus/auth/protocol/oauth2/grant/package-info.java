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
 * Implements reusable OAuth 2.x grant-domain state transitions and credential issuance.
 * <p>
 * {@link org.miaixz.bus.auth.protocol.oauth2.grant.AuthorizationCodeIssuer} owns authorization-code creation and
 * consumption state, {@link org.miaixz.bus.auth.protocol.oauth2.grant.AccessTokenIssuer} materializes tokens from an
 * already authorized grant, and {@link org.miaixz.bus.auth.protocol.oauth2.grant.RefreshTokenRotator} performs atomic
 * refresh-token rotation and reuse handling. OAuth 2.x and OpenID Connect server orchestration may reuse these classes.
 * {@link org.miaixz.bus.auth.protocol.oauth2.grant.GrantPolicy} prevents grant processing from depending on endpoint or
 * server configuration packages.
 * </p>
 * <p>
 * This package does not decode HTTP requests, expose endpoints, authenticate project users, choose a Source, load
 * registrations, create business sessions, or implement project authorization and persistence.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.protocol.oauth2.grant;
