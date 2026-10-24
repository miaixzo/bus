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
package org.miaixz.bus.cortex.setting.reference;

import java.util.List;

/**
 * Durable relationship store scoped by the mandatory tenant identifier on each reference.
 * <p>
 * Implementations must reject missing tenant identifiers and access outside the authenticated tenant. Writes must
 * verify that both endpoints belong to that tenant and match the relationship type. An ITEM_APP source must be a shared
 * item; application-owned items must not create an ITEM_APP relationship. ITEM_APP writes validate alias, LATEST/FIXED
 * strategy, and pinned revision ownership using Reference.validate. Application-local aliases are unique within each
 * tenant/application, including collisions with private items. Updating or removing a reference creates pending
 * application changes; it never changes the active release. Policy changes use edition compare-and-set and cannot
 * silently overwrite a different duplicate tuple. ROLLOUT_APP identifies explicitly selected applications, while
 * ROLLOUT_ITEM identifies included configuration items; application selection must not be inferred from item ownership
 * or current shared references.
 * <p>
 * The tuple {@code (tenant_id, source_id, target_id, type)} is unique. Implementations must enforce this constraint
 * atomically in durable storage, including concurrent saves, and return the existing active relationship on duplicate
 * saves. Recreating an inactive relationship must reactivate that relationship instead of inserting a duplicate.
 *
 * @author Kimi Liu
 */
public interface ReferenceStore {

    /**
     * Idempotently persists one complete, tenant-qualified relationship.
     *
     * @param reference relationship with nonblank tenant, source, target, and supported type
     * @return persisted or already existing active relationship
     * @throws IllegalArgumentException if coordinates are missing, unsupported, or violate tenant or endpoint ownership
     */
    Reference save(Reference reference);

    /**
     * Stages reference-policy changes using an atomic tenant/tuple/edition comparison. The host generates a pending
     * effective revision; active application snapshots remain unchanged. Null values may deliberately clear a fixed pin
     * when switching to LATEST.
     *
     * @param reference replacement relationship policy
     * @param expected previous relationship used for the atomic tuple and edition comparison
     * @return updated relationship
     * @throws UnsupportedOperationException by default; the host must implement an atomic relationship-policy update
     */
    default Reference update(Reference reference, Reference expected) {
        throw new UnsupportedOperationException("Atomic reference-policy update is not implemented");
    }

    /**
     * Removes only the relationship matching all four business coordinates. An absent relationship is a no-op.
     *
     * @param reference relationship with nonblank tenant, source, target, and supported type
     * @throws IllegalArgumentException if required coordinates are missing or tenant access is denied
     */
    void remove(Reference reference);

    /**
     * Lists active relationships originating from the supplied source within the required tenant. Optional target and
     * type fields narrow the result; they never broaden its tenant scope.
     *
     * @param reference nonblank tenant and source identifiers, with optional target and supported type
     * @return matching active outgoing relationships, or an empty list
     * @throws IllegalArgumentException if tenant or source is missing, type is unsupported, or tenant access is denied
     */
    List<Reference> outgoing(Reference reference);

    /**
     * Lists active relationships pointing to the supplied target within the required tenant. Optional source and type
     * fields narrow the result; they never broaden its tenant scope.
     *
     * @param reference nonblank tenant and target identifiers, with optional source and supported type
     * @return matching active incoming relationships, or an empty list
     * @throws IllegalArgumentException if tenant or target is missing, type is unsupported, or tenant access is denied
     */
    List<Reference> incoming(Reference reference);

}
