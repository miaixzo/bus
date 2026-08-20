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
package org.miaixz.bus.auth.registry;

import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.core.basic.entity.Result;
import org.miaixz.bus.core.basic.entity.Tracer;
import org.miaixz.bus.core.lang.Optional;

/**
 * Defines the external persistence boundary for authentication management resources.
 * <p>
 * Implementations belong to an integrating project and may use a database, remote service, or other data source. The
 * framework supplies no persistence implementation and performs no authorization decisions. A successful operation
 * changes managed data only; it does not implicitly reload the authentication Registry.
 * </p>
 *
 * @param <R> Bus entity used for creation, complete replacement, lookup, and paged query criteria
 * @author Kimi Liu
 */
public interface ResourceService<R extends Tracer> {

    /**
     * Persists a new complete authentication resource.
     *
     * @param resource complete resource to create
     * @param context  current non-secret authentication call context
     * @param timeout  shared end-to-end time budget
     * @return asynchronous stage containing the persisted resource
     */
    CompletionStage<R> create(R resource, Context context, Timeout.Budget timeout);

    /**
     * Replaces the writable state of an existing authentication resource.
     * <p>
     * The resource is a complete replacement, not a patch. An external API that supports PATCH must merge and validate
     * its patch before calling this method.
     * </p>
     *
     * @param resource complete resource state to persist
     * @param context  current non-secret authentication call context
     * @param timeout  shared end-to-end time budget
     * @return asynchronous stage containing the persisted resource
     */
    CompletionStage<R> update(R resource, Context context, Timeout.Budget timeout);

    /**
     * Deletes one authentication resource by its inherited Bus entity identifier.
     *
     * @param id      resource identifier
     * @param context current non-secret authentication call context
     * @param timeout shared end-to-end time budget
     * @return asynchronous stage completed after deletion
     */
    CompletionStage<Void> delete(String id, Context context, Timeout.Budget timeout);

    /**
     * Looks up one authentication resource by its inherited Bus entity identifier.
     *
     * @param id      resource identifier
     * @param context current non-secret authentication call context
     * @param timeout shared end-to-end time budget
     * @return asynchronous stage containing the resource when it exists
     */
    CompletionStage<Optional<R>> get(String id, Context context, Timeout.Budget timeout);

    /**
     * Queries a page using the inherited Bus entity query fields and resource-specific filters.
     *
     * @param query   entity carrying page, ordering, keyword, and resource-specific query criteria
     * @param context current non-secret authentication call context
     * @param timeout shared end-to-end time budget
     * @return asynchronous stage containing the Bus page result
     */
    CompletionStage<Result<R>> page(R query, Context context, Timeout.Budget timeout);

}
