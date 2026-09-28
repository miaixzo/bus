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
 * Durable directed-resource relationship contract.
 *
 * @author Kimi Liu
 */
public interface ReferenceStore {

    /**
     * Persists one directed relationship.
     *
     * @param reference relationship to persist
     * @return persisted relationship
     */
    Reference save(Reference reference);

    /**
     * Removes one relationship selected by its business coordinates.
     *
     * @param tenant_id tenant identifier, when available
     * @param source_id source resource identifier
     * @param target_id target resource identifier
     * @param type      relationship type
     */
    void remove(String tenant_id, String source_id, String target_id, String type);

    /**
     * Lists relationships that originate from one resource.
     *
     * @param tenant_id tenant identifier, when available
     * @param source_id source resource identifier
     * @param type      optional relationship type
     * @return outgoing relationships
     */
    List<Reference> outgoing(String tenant_id, String source_id, String type);

    /**
     * Lists relationships that point to one resource.
     *
     * @param tenant_id tenant identifier, when available
     * @param target_id target resource identifier
     * @param type      optional relationship type
     * @return incoming relationships
     */
    List<Reference> incoming(String tenant_id, String target_id, String type);
}
