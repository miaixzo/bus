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
 * Defines the RFC 5849 OAuth 1.0 protocol models and registered values used by Source clients.
 * <p>
 * The package models temporary-credential requests and responses, resource-owner authorization, token-credential
 * requests and responses, and protected-resource access as distinct operations.
 * {@link org.miaixz.bus.auth.protocol.oauth1.OAuth1Parameter} preserves protocol parameter names and values, while
 * {@link org.miaixz.bus.auth.protocol.oauth1.SignatureMethod} retains registered signing method identifiers.
 * {@link org.miaixz.bus.auth.protocol.oauth1.OAuth1} exposes explicit module assembly.
 * </p>
 * <p>
 * Client, codec, and security child packages consume these standard values. Vendor adapters may compose the standard
 * client and then call their own identity resource, but no Vendor field, ExternalIdentity, Registry reference, Context,
 * Outcome, or custom token DTO becomes part of an OAuth 1.0 request or response.
 * </p>
 * <p>
 * Temporary and token credentials remain different credential classes with separate secrets and lifetimes. Parameters
 * preserve RFC encoding and multiplicity, callback ownership and verifier correlation are mandatory, and credentials,
 * signatures, base strings, and protected-resource bodies must not enter logs or framework failure details.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.protocol.oauth1;
