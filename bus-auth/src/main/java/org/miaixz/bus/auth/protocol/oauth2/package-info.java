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
 * Defines OAuth 2.x authorization, grant, token, metadata, introspection, revocation, and device-flow wire models.
 * <p>
 * Authorization requests and responses, registered response and grant types, scopes, token requests and responses,
 * errors, client authentication methods, authorization-server metadata, introspection, revocation, and device
 * authorization retain their formal RFC field names and types. RFC 8693 token exchange is represented only by
 * {@link org.miaixz.bus.auth.protocol.oauth2.TokenExchangeGrant} passed to the standard token operation.
 * {@link org.miaixz.bus.auth.protocol.oauth2.OAuth2} exposes explicit module assembly.
 * </p>
 * <p>
 * Client, server, codec, and internal packages consume these values. OpenID Connect extends the applicable OAuth models
 * in its own package, while Vendor adapters may compose standard operations only when their wire is conformant. Vendor
 * fields, Source authentication values, Registry references, Context, Timeout, Outcome, Bus errors, exceptions, and
 * custom envelopes never become OAuth wire content.
 * </p>
 * <p>
 * Constructors preserve required versus optional fields, exact token and error types, scope syntax, registered client
 * authentication, and extension ownership. Implementations must not invent missing {@code token_type}, expiry, scope,
 * grant, ID Token claims, or public methods such as {@code exchangeToken}, {@code tokenExchange}, or an independent
 * refresh operation.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.protocol.oauth2;
