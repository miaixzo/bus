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
 * Defines the SCIM 2.0 resource, protocol-operation, discovery, and error models.
 * <p>
 * User, Group, Resource, Schema, ResourceType, and ServiceProviderConfig preserve RFC 7643 schema identities and typed
 * attributes. ResourceTarget keeps route identity outside bodies; SearchParameters is shared by GET SearchQuery and
 * POST SearchRequest; PatchRequest associates target and If-Match with a PatchOp whose operations contain only typed
 * attribute paths and values. ListResponse, BulkRequest, BulkResponse, and ErrorResponse preserve RFC 7644 semantics.
 * schema extensions remain isolated by their schema URI in typed JsonValue content.
 * </p>
 * <p>
 * SCIM server and codec packages consume these values. The external project implements resource storage and access
 * control through declared ports. This package does not contain persistence, Controller, Source/client behavior,
 * Registry execution, identity sign-in, Vendor data, generic framework envelopes, or duplicate entity/page/error
 * abstractions from bus-core.
 * </p>
 * <p>
 * Models retain exact SCIM field names, JSON types, schema URNs, caseExact behavior, mutability, returned, uniqueness,
 * ETag, location, and HTTP status semantics. Password and other writeOnly values remain in closeable SecretLease-backed
 * fields and never enter responses, equality, textual diagnostics, or logs. Input collections, nesting, filters, patch
 * paths, bulk references, payload size, and extension values are bounded, and malformed cross-schema references fail
 * without partial mutation.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.protocol.scim;
