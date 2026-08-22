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
 * Defines the protocol-neutral identity-completion worker boundary.
 * <p>
 * This package may coordinate a verified Source authentication result through framework-owned external-identity
 * validation, project-supplied identity and claim loaders, pure parsing, Principal construction, and an immutable
 * authentication result. Every worker must preserve the invocation context, shared timeout, and typed Outcome without
 * exposing protocol tokens or Vendor-private payloads.
 * </p>
 * <p>
 * The package does not implement account persistence, automatic registration, account merging, user CRUD, roles,
 * permissions, business sessions, cookies, project tokens, HTTP redirects, auditing, Controllers, Repositories, or
 * Mappers. Those behaviors remain in the external project. A project extension interface belongs here only when a
 * framework worker actually invokes it; an unused integration contract must not be added as a placeholder.
 * </p>
 * <p>
 * The {@code worker} parent package remains available for other independent authentication workers. Classes in this
 * child package must participate only in identity completion and must not become a general application-service layer.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.worker.identity;
