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
 * Implements the non-exported Twitter OAuth 1.0 Source identity composition.
 * <p>
 * TwitterSourceAdapter delegates TemporaryCredentials, ResourceOwnerAuthorization, TokenCredentials, and
 * ProtectedResource unchanged to the shared OAuth 1.0 client. Source initiation requests an official callback,
 * correlates the temporary credential identifier through RedirectManager, and relies on the shared credential store to
 * retain its secret for at most ten minutes. Completion accepts exactly a successful
 * {@code oauth_token}/{@code oauth_verifier} branch or a denial branch, then atomically consumes both correlation and
 * temporary secret through the standard token-credential operation.
 * </p>
 * <p>
 * The adapter signs the fixed protected-resource request with the standard client and only appends registered
 * {@code include_entities=true} and {@code include_email=true}. Bounded duplicate-rejecting JSON parsing requires a
 * positive decimal {@code id_str}; only that value becomes ExternalIdentity subject and only the frozen string or null
 * fields become attributes.
 * </p>
 * <p>
 * Private composition may depend on standard OAuth 1.0 models, client services, vendor flow, Fabric, JSON, credential
 * storage, and Bus validation, but protocol servers, Registry loaders, and external projects must not depend on it.
 * Consumer, temporary, and token secrets, verifier, tokens, signature data, response bodies, and profile values must
 * not escape through Context, tracing, logs, or public failures.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.twitter.internal;
