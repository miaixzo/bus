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
import java.util.Map;
import java.util.Set;

/**
 * Current configuration-to-application relationships only.
 *
 * @author Kimi Liu
 */
public interface ReferenceStore {

    /**
     * Reads current relationships for one source.
     *
     * @param tenant exact tenant identifier
     * @param item   source item identifier
     * @return current relationships for the source item
     */
    List<Reference> byItem(String tenant, String item);

    /**
     * Reads current relationships for one application.
     *
     * @param tenant exact tenant identifier
     * @param app    target application identifier
     * @return current relationships for the target application
     */
    List<Reference> byApp(String tenant, String app);

    /**
     * Loads relationships in one consistent batch.
     *
     * @param tenant exact tenant identifier
     * @param apps   target application identifiers
     * @return current relationships for the requested applications
     */
    List<Reference> byApps(String tenant, Set<String> apps);

    /**
     * Counts distinct current target applications per source.
     *
     * @param tenant exact tenant identifier
     * @param items  source item identifiers
     * @return distinct target application counts keyed by source item identifier
     */
    Map<String, Long> countByItem(String tenant, Set<String> items);

    /**
     * Counts distinct current source configurations per application.
     *
     * @param tenant exact tenant identifier
     * @param apps   target application identifiers
     * @return distinct source item counts keyed by target application identifier
     */
    Map<String, Long> countByApp(String tenant, Set<String> apps);

    /**
     * Replaces the complete relationship set atomically, checking membership, editions, ownership, alias uniqueness,
     * targets, tenant and published pins.
     *
     * @param tenant   exact tenant identifier
     * @param item     source item identifier
     * @param next     complete desired relationship set
     * @param expected complete expected relationship identifier-to-edition map
     * @return committed replacement relationship set
     */
    List<Reference> compareAndSet(String tenant, String item, List<Reference> next, Map<String, Long> expected);

}
