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
 * Implements the OAuth 2.x authorization-server and Provider direction.
 * <p>
 * Endpoint types translate an already authenticated Fabric HTTP exchange into the formal authorization, token,
 * introspection, revocation, device authorization, or authorization-server metadata operation. Their corresponding
 * service contracts own protocol decisions, while {@link org.miaixz.bus.auth.protocol.oauth2.server.OAuth2ErrorMapper}
 * maps failures to the error response defined for the specific OAuth endpoint.
 * {@link org.miaixz.bus.auth.protocol.oauth2.server.OAuth2ServerScheme} and
 * {@link org.miaixz.bus.auth.protocol.oauth2.server.OAuth2ServerOptions} declare only capabilities and options
 * implemented by the compiled server-role Source runtime.
 * </p>
 * <p>
 * This package consumes the standard OAuth model and codec packages, verified client identity carried by Context,
 * consent and claim ports, the sibling grant package, atomic stores, SecurityBaseline, and Fabric transport. It does
 * not load registrations, persist clients or grants, authenticate an end user, implement project permissions, select a
 * Vendor, or expose Registry, Outcome, exceptions, or Bus errors as OAuth wire content.
 * </p>
 * <p>
 * Every endpoint enforces its registered HTTP method, media type, client-authentication policy, redirect URI, grant,
 * scope, issuer, and one shared Budget. Authorization codes and device codes are single-use, refresh-token rotation is
 * atomic, PKCE and redirect matching fail closed, and token material is never placed in diagnostics. Revocation success
 * remains an empty response, inactive introspection remains {@code active=false}, and an endpoint emits only the
 * success or error representation defined by its governing RFC.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.protocol.oauth2.server;
