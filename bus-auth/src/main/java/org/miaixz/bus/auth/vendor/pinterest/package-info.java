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
 * Declares the public Pinterest OAuth Vendor manifest and externally loaded options.
 * <p>
 * PinterestManifest fixes {@code pinterest/default}, its authorization, token, and profile endpoints, default
 * {@code read_public} scope, and redirect Source-authentication plus standard OAuth authorization/token capabilities.
 * Comma-delimited authorization scope, query token fields and Client Secret, the empty form POST, status/message token
 * envelope, query profile token and field selector, and status/message/data profile envelope are registered vendor
 * deviations confined to the non-exported adapter.
 * </p>
 * <p>
 * PinterestOptions contains only routing, Client ID, Client Secret reference, one exact registered HTTP or HTTPS
 * callback, and a unique ordered subset of the frozen Pinterest scope vocabulary. The fixed endpoints, delimiter, query
 * names, form shape, profile fields, envelope records, avatar selector, PKCE behavior, and transport policy are
 * manifest-owned and cannot be supplied by an external project.
 * </p>
 * <p>
 * This exported package provides registration and management metadata. Runtime execution must enter a Provider obtained
 * from Registry and delegate to the private adapter. Only a valid Pinterest profile {@code id} becomes the
 * ExternalIdentity subject; username, names, biography, and the {@code 60x60} image remain attributes. Client secrets,
 * state, callback codes, access tokens, complete response envelopes, profile fields, and platform error messages must
 * not enter options diagnostics, Context, tracing, logs, or public failure details.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.pinterest;
