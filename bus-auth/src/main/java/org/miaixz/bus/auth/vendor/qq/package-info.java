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
 * Declares QQ Open Platform and QQ Mini Program Vendor manifests and externally loaded options.
 * <p>
 * QqManifest separates {@code qq/open} from {@code qq/mini-program}. The Open Platform variant is an OAuth client with
 * fixed authorization, token, refresh, OpenID, and profile endpoints, but publishes only redirect Source authentication
 * and standard OAuth authorization because its historical text token response omits mandatory {@code token_type}. Comma
 * scope, query Client Secret, empty form, text token, JSONP OpenID, and query profile behavior remain private
 * deviations. The Mini Program variant uses its real HTTPS transport and publishes only direct Source authentication
 * through its fixed {@code jscode2session} endpoint.
 * </p>
 * <p>
 * QqOptions carries shared routing, App ID, Client Secret reference, and variant-specific callback and scope values.
 * Open requires an exact registered HTTP or HTTPS callback and a unique ordered scope list containing
 * {@code get_user_info} when explicit; it may select verified UnionID as subject. Mini Program prohibits callback,
 * scopes, and UnionID preference. No endpoint, token parser, JSONP wrapper, replay control, session-key model, or
 * platform response record is externally configurable.
 * </p>
 * <p>
 * This exported package is registration metadata only; runtime use must enter a Registry-obtained Provider. Open
 * identity uses bound OpenID or the requested available UnionID, while Mini Program identity always uses OpenID.
 * Credentials, state, codes, access and refresh tokens, session keys, callback bodies, JSONP, and profile documents
 * must not enter diagnostics, Context, tracing, logs, or public failure details.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.qq;
