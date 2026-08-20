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
 * Maps RFC 5849 OAuth 1.0 parameters to and from their formal wire representations.
 * <p>
 * {@link org.miaixz.bus.auth.protocol.oauth1.codec.OAuth1AuthorizationHeaderCodec} handles the OAuth HTTP Authorization
 * scheme, {@link org.miaixz.bus.auth.protocol.oauth1.codec.OAuth1FormCodec} handles ordered form-encoded parameters,
 * and {@link org.miaixz.bus.auth.protocol.oauth1.codec.OAuth1ResponseDecoder} decodes the temporary- and
 * token-credential response vocabulary without inventing a JSON envelope.
 * </p>
 * <p>
 * OAuth 1.0 clients and security code compose these codecs over the formal protocol models and common form/header
 * primitives. This package does not perform HTTP calls, resolve secrets, generate signatures, select a Vendor, map an
 * identity, or encode Registry, Context, Outcome, and exception values.
 * </p>
 * <p>
 * Codecs preserve duplicates where RFC normalization requires them, reject invalid percent encoding, malformed UTF-8,
 * forbidden header characters, ambiguous credential fields, and unsupported multiplicity. Realm remains outside the
 * signature parameter set, and credential secrets, verifier values, signatures, and raw bodies are never logged.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.protocol.oauth1.codec;
