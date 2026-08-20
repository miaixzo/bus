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
 * Implements the non-exported CODING team authorization, token, and OpenAPI identity flow.
 * <p>
 * CodingSourceAdapter resolves each endpoint from the validated team label, uses RedirectManager for state, emits the
 * fixed authorization query with comma-delimited scope, and binds callback {@code team} and returned scope to the
 * registration and initiated request. It exchanges the code through the ordered client-secret form and calls the fixed
 * OpenAPI action {@code DescribeCodingCurrentUser} with one Bearer header and JSON body.
 * </p>
 * <p>
 * The adapter may compose endpoint templates, shared query and form codecs, SecretResolver, JsonProvider,
 * SecurityBaseline, and Fabric. Its token object and OpenAPI envelope remain private. It does not expose O2A or O2T,
 * infer a refresh grant from contradictory documentation, accept arbitrary tenant hosts, send PKCE, use the obsolete
 * account endpoint, publish phone data, or turn CODING errors into standard OAuth responses.
 * </p>
 * <p>
 * Callback method, target, unique parameters, state, team, and scope subset are exact. Token success requires Bearer,
 * matching team, required scope, non-blank access and refresh tokens, and a positive decimal-string {@code expires_in};
 * the alternate code/msg/data error envelope is mutually exclusive. OpenAPI success requires one Response object,
 * non-blank RequestId, and positive integral User.Id. Only its decimal text is ExternalIdentity.subject. Secret, code,
 * tokens, callback data, request body, response body, phone, and platform errors never enter diagnostics.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.coding.internal;
