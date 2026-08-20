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
 * Implements the RFC 5849 consumer and Source-client role.
 * <p>
 * {@link org.miaixz.bus.auth.protocol.oauth1.client.OAuth1Client} aggregates temporary credentials, resource-owner
 * authorization, token credentials, and protected-resource operations configured by
 * {@link org.miaixz.bus.auth.protocol.oauth1.client.OAuth1ClientSettings}.
 * {@link org.miaixz.bus.auth.protocol.oauth1.client.OAuth1SourceProfile} exposes the generic Source catalog entry;
 * operation-specific clients preserve their matching request and response models.
 * </p>
 * <p>
 * This package composes the OAuth 1.0 model, codecs, signer, resolver and credential-store ports, and Fabric HTTP
 * transport. It implements only the consumer/client direction: it does not act as an authorization server, create a
 * local identity, choose a Vendor, load registrations, or infer an identity endpoint from an OAuth credential.
 * </p>
 * <p>
 * Each call binds consumer key, callback, endpoint, signature method, nonce, timestamp, temporary credential, verifier,
 * and token credential to one Context and Budget. Dynamic secrets are stored under isolated expiring keys and consumed
 * atomically; redirect and network targets remain profile-bound, and no credential or signed request is logged.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.protocol.oauth1.client;
