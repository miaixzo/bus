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
 * Declares Microsoft global and China cloud login and enterprise Graph Source variants.
 * <p>
 * MicrosoftManifest exposes {@code microsoft/global} and {@code microsoft/china}. Each variant owns constrained tenant
 * templates for standard authorization, token, and refresh operations plus its fixed Microsoft Graph current-user
 * endpoint. Both require CLIENT_SECRET form authentication, prohibit PKCE, default to compatibility identity scopes
 * plus {@code User.Read}, and publish Source authentication with standard OAuth authorization and token operations.
 * </p>
 * <p>
 * MicrosoftOptions contains routing, canonical Application ID, secret reference, exact registered callback, unique
 * delegated scopes, and a validated tenant alias, UUID, or verified domain. It cannot accept arbitrary hosts, complete
 * URLs, endpoint overrides, cloud mixing, PKCE, Graph models, or a separate refresh operation. China rejects the
 * unsupported consumer audience.
 * </p>
 * <p>
 * Source identity accepts only the non-blank Microsoft Graph {@code id} as its subject. User principal name, mail,
 * display name, office, and other Graph fields remain attributes. The two cloud variants never share endpoints or infer
 * a cloud from Context, and all standard token results retain OAuth model and field semantics.
 * </p>
 * <p>
 * {@code enterprise-global} and {@code enterprise-china} are separate application-permission variants with concrete
 * tenant selection, CLIENT_SECRET credentials, cloud-specific token authority, Graph origin, and application scope.
 * They expose describe, snapshot, changes, and retrieve for the implemented USER, ORGANIZATION, GROUP, ROLE, and
 * SERVICE_ACCOUNT projections and selected membership, manager, role-member, and application-assignment relations.
 * Other Graph directoryObject types are outside this contract.
 * </p>
 * <p>
 * Graph delta is available only for user, group, and service-principal kinds. The opaque Cursor binds the selected
 * cloud, operation, kind, official nextLink or deltaLink, and replay offset; a URL from the other national cloud or an
 * expired delta token is rejected and requires a new baseline. Dispatcher provides invocation only—external projects
 * own scheduling, durable checkpoints, reconciliation, and persistence.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.microsoft;
