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
 * Defines implementation-neutral encoding contracts and reusable HTTP value primitives.
 * <p>
 * {@link org.miaixz.bus.auth.codec.DualCodec} supplies a typed encode/decode boundary.
 * {@link org.miaixz.bus.auth.codec.FormCodec} and {@link org.miaixz.bus.auth.codec.QueryCodec} preserve ordered
 * {@link org.miaixz.bus.auth.codec.NameValue parameters}, duplicate names, empty values, strict percent escapes, and
 * UTF-8 semantics. {@link org.miaixz.bus.auth.codec.HeaderCodec} and {@link org.miaixz.bus.auth.codec.HeaderValue}
 * handle validated field values without constructing an HTTP response.
 * </p>
 * <p>
 * Protocol-specific codecs compose these primitives and remain responsible for formal field vocabularies and wire
 * models. This package reuses bus-core URL, encoder, decoder, charset, and HTTP constants plus Fabric-neutral values;
 * it does not import OAuth, OpenID Connect, SAML, SCIM, LDAP, RADIUS, Vendor, Roster, or runtime types and does not
 * define a generic protocol envelope.
 * </p>
 * <p>
 * Decoders reject malformed UTF-8, invalid percent encoding, forbidden control characters, and multiplicity that the
 * calling standard does not allow. Callers own input-size limits and must erase encoded forms that contain credentials,
 * authorization codes, verifiers, or tokens immediately after transport completion; codecs do not log payloads.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.codec;
