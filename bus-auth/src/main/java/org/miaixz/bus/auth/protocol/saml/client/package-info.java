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
 * Implements the SAML 2.0 Service Provider and generic Source direction.
 * <p>
 * {@link org.miaixz.bus.auth.protocol.saml.client.SamlServiceProvider} initiates Web Browser SSO and Single Logout with
 * HTTP-Redirect or HTTP-POST bindings. AssertionConsumerService decodes and validates the returned SAML Response before
 * producing a stable external identity, while MetadataClient obtains and validates Identity Provider metadata.
 * SamlSourceProfile and SamlSourceSettings declare the exact entity, endpoints, bindings, trust material, and
 * capabilities of a generic SAML Source.
 * </p>
 * <p>
 * This package consumes SAML models, binding and metadata codecs, SAML security validators, resolvers, replay storage,
 * SecurityBaseline, and Fabric transport. It does not host an Identity Provider, issue Assertions, persist project
 * data, select a Vendor, establish trust from unvalidated metadata, invoke Registry directly, or expose XML, keys, or
 * framework failures as an application identity.
 * </p>
 * <p>
 * Each flow binds the exact SP and IdP entity IDs, destination, assertion consumer URL, binding, RelayState,
 * InResponseTo, audience, recipient, time window, requested authentication context, and one Budget. Signatures are
 * validated over the consumed object, encrypted content is revalidated after decryption, assertion IDs are consumed
 * once, and only a verified NameID or explicitly configured stable attribute may become the external subject. XML,
 * assertions, attributes, private keys, session indexes, and RelayState never enter logs or failure details.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.protocol.saml.client;
