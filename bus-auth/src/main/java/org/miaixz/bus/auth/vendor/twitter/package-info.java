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
 * Declares the public Twitter OAuth 1.0 Vendor definition and externally loaded settings.
 * <p>
 * TwitterDefinition fixes {@code twitter/oauth1}, the temporary-credential, resource-owner authorization,
 * token-credential, and protected-resource endpoints, HMAC-SHA1, a ten-minute temporary credential lifetime, and an
 * empty realm and scope. It exposes redirect Source authentication together with the four RFC 5849 client capabilities.
 * Only the Twitter profile query flags and bounded user projection are registered Source-completion deviations; OAuth
 * protocol requests and credentials retain their standard models.
 * </p>
 * <p>
 * TwitterSourceSettings contains routing, official consumer key, external Client Secret or Shared Secret reference, and
 * one exact registered HTTP or HTTPS OAuth callback. Scope must be empty. Fixed endpoints, signature method, temporary
 * lifetime, profile query, credential-store behavior, and identity projection cannot be externally supplied.
 * </p>
 * <p>
 * This exported package provides registration metadata; execution enters a Registry-obtained Provider. Identity is
 * created only from a positive decimal Twitter {@code id_str}; profile strings remain attributes. Consumer secrets,
 * temporary and token secrets, callback verifier, access tokens, response bodies, and profile data must not enter
 * diagnostics, Context, tracing, logs, or public failures.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.twitter;
