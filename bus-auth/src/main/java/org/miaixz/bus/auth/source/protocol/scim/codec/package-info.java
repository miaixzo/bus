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
 * Encodes and decodes SCIM 2.0 JSON resources, operations, discovery documents, errors, and filters.
 * <p>
 * ScimResourceCodec preserves User, Group, and extension schemas; ScimSearchRequestCodec separates GET SearchQuery from
 * POST SearchRequest and ScimListResponseCodec maps bus-core Result pagination; ScimPatchCodec keeps route/header state
 * outside PatchOp and ScimBulkCodec preserves typed ResourceTarget values; ScimDiscoveryCodec owns service
 * configuration, schema, and resource-type documents; ScimErrorCodec owns RFC 7644 errors. ScimFilterParser and
 * ScimFilterEncoder implement the formal filter grammar rather than a project query language.
 * </p>
 * <p>
 * SCIM services call this package at the HTTP boundary. Codecs consume the application-wide strict {@link JsonKit}
 * facade, typed {@link JsonValue} values, standard HTTP values, and SCIM models. They do not access persistence,
 * authorize callers, apply mutations, resolve credentials, invoke Roster, create identities, interpret Vendor data, or
 * introduce a framework request, response, page, or error envelope.
 * </p>
 * <p>
 * Decoding enforces schema URNs, required and singleton fields, exact JSON types, duplicate rejection, attribute
 * mutability, filter grammar and precedence, patch path/value cardinality, bulkId references, HTTP method constraints,
 * status syntax, and configured size, depth, collection, string, and operation limits. Encoding preserves SCIM case,
 * omission, schema isolation, pagination, ETag, location, and extension values. Password and other writeOnly
 * SecretLease values are accepted only by mutation decoders, are closed deterministically, and are never encoded or
 * logged.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.source.protocol.scim.codec;

import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.extra.json.JsonValue;
