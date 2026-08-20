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
 * Encodes and decodes SAML 2.0 XML, Metadata, HTTP-Redirect, and HTTP-POST representations.
 * <p>
 * SamlMessageCodec owns the namespace-qualified Core XML document mapping and preserves the exact bytes and unique IDs
 * consumed by signature validation. MetadataCodec maps EntityDescriptor and role descriptors. RedirectBindingCodec
 * applies DEFLATE, Base64, query encoding, RelayState, SigAlg, and Signature ordering; PostBindingCodec maps the base64
 * message and RelayState form fields. HTTP-Artifact is intentionally absent because the codec package does not
 * implement its SOAP resolution profile.
 * </p>
 * <p>
 * SAML client and server operations call these codecs around the security package. Codecs may use shared Base64,
 * compression, XML, query, and Bus cryptographic primitives, but they do not establish trust, resolve credentials,
 * choose policy, perform network calls, persist replay state, invoke Registry, create identity, interpret Vendor data,
 * or translate SAML into a generic JSON or framework envelope.
 * </p>
 * <p>
 * XML parsing disables DTDs, external entities, external schemas, and uncontrolled references and enforces bounded
 * bytes, depth, text, attributes, children, and unique ID values. The reader rejects unknown critical structure,
 * duplicate singleton elements, namespace confusion, duplicate binding parameters, invalid Base64 or DEFLATE, and
 * ambiguous signatures. Signing input preserves the exact standard parameter order. Raw XML, assertions, attributes,
 * RelayState, signatures, encrypted content, and keys are never included in diagnostics.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.protocol.saml.codec;
