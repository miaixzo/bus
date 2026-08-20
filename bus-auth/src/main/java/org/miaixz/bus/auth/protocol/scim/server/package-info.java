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
 * Implements the SCIM 2.0 Service Provider direction.
 * <p>
 * ScimResourceService owns common create, retrieve, replace, patch, delete, and search semantics; ScimUserService and
 * ScimGroupService apply resource-specific constraints; ScimBulkService executes bounded bulk operations;
 * ScimDiscoveryService publishes configuration, schemas, and resource types. ScimErrorMapper emits RFC 7644 errors, and
 * ScimServerScheme with ScimServerOptions declares only executable service capabilities.
 * </p>
 * <p>
 * Services consume formal SCIM models and codecs, authenticated request context, SecurityBaseline, and the externally
 * implemented ScimResourceStore port. The framework owns protocol validation and wire behavior; the external project
 * owns persistence, transaction implementation, record permissions, and deployment data. This package has no
 * Controller, Source/client role, identity login, Vendor integration, direct Registry lookup, or generic response DTO.
 * </p>
 * <p>
 * Operations enforce resource type, schema, mutability, uniqueness, If-Match/ETag, pagination, sorting, filtering,
 * patch-path cardinality, bulk dependency, failOnErrors, payload limits, authentication scheme, and one Budget. Create,
 * replace, patch, delete, and bulk changes are atomic according to the store contract. Returned attributes honor
 * {@code returned} and request selection rules; writeOnly secrets are closed after use and never returned. Errors
 * retain the exact HTTP status and registered {@code scimType} without exposing records, credentials, or framework
 * failures.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.protocol.scim.server;
