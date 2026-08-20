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
 * Encodes and decodes RADIUS packets, attributes, and EAP-Message fragments.
 * <p>
 * RadiusPacketDecoder is constructed with the version selected by the trusted transport and maps the Code, Length,
 * version-specific header, and ordered attributes to one of the six packet models. RadiusPacketEncoder performs the
 * inverse mapping. RadiusAttributeCodec preserves registered and unknown attributes, including Type 26 vendor-specific
 * content. EapMessageCodec joins or splits one complete EAP packet across consecutive Type 79 attributes.
 * </p>
 * <p>
 * RADIUS server orchestration and transport adapters call this package. Codecs consume only typed packet values and
 * bounded byte primitives. They do not guess protocol version, open sockets, negotiate TLS or ALPN, allocate Token
 * counters, resolve clients or secrets, calculate authenticators, persist accounting data, invoke Registry, interpret
 * Vendor login data, or create an HTTP, JSON, or framework envelope.
 * </p>
 * <p>
 * Decoding rejects unknown packet Codes, invalid Length, truncated headers or attributes, attribute lengths below two,
 * invalid Vendor-Id width, malformed VSA or EAP length, nonconsecutive EAP fragments, and configured packet limits.
 * Bytes after the declared Length are transport padding and are ignored. Legacy accounting remains at most 4095 bytes;
 * Access and RADIUS/1.1 remain at most 4096 bytes. RADIUS/1.1 reserved octets are ignored on input and zero on output,
 * and unknown attributes retain exact order and octets without entering diagnostics.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.protocol.radius.codec;
