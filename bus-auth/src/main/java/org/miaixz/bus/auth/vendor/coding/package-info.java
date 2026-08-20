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
 * Declares the CODING team OAuth 2.0 Vendor manifest and tenant-bound options.
 * <p>
 * CodingManifest exposes the single {@code coding/default} OAUTH2 variant with authorization, token, and OpenAPI
 * endpoint templates restricted to {@code {instance}.coding.net}. It declares client_secret_post, CLIENT_SECRET,
 * prohibited PKCE, {@code user:profile:ro} default scope, Source authentication only, and deviations for
 * comma-delimited authorization scope, callback team and scope, string {@code expires_in}, and the OpenAPI request and
 * response envelope.
 * </p>
 * <p>
 * CodingOptions adds one non-blank DNS-label {@code team}, which is the only legal endpoint template instance. Users
 * cannot submit a complete host, URL, arbitrary tenant domain, endpoint, issuer, or refresh mode. This package exports
 * no public OAuth authorization or token capability, private token or OpenAPI DTO, custom expires-in type, independent
 * refresh operation, or platform error envelope.
 * </p>
 * <p>
 * Routing, team, CLIENT_SECRET reference, exact callback, and unique registered scopes containing
 * {@code user:profile:ro} are mandatory. The manifest remains OAUTH2 because it is an OAuth authorization-code client,
 * while the non-standard scope delimiter, token field type, and OpenAPI envelope remain isolated in Source
 * authentication. Only a positive integral OpenAPI {@code Response.User.Id} converted to decimal text may become the
 * external subject.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.coding;
