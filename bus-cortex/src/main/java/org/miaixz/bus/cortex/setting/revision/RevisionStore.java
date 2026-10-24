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
package org.miaixz.bus.cortex.setting.revision;

import java.util.List;
import java.util.Set;

/**
 * Read-only access to immutable successfully published definitions.
 *
 * @author Kimi Liu
 */
public interface RevisionStore {

    /**
     * Reads one exact published snapshot; absence is not latest fallback.
     *
     * @param tenant   exact tenant identifier
     * @param revision exact published snapshot identifier
     * @return requested published snapshot, or null if absent
     */
    Revision find(String tenant, String revision);

    /**
     * Reads an exact published snapshot batch.
     *
     * @param tenant    exact tenant identifier
     * @param revisions exact published snapshot identifiers
     * @return available snapshots from the requested batch
     */
    List<Revision> findAll(String tenant, Set<String> revisions);

    /**
     * Lists only successful publication history, never saved edits.
     *
     * @param tenant exact tenant identifier
     * @param item   source item identifier
     * @param offset zero-based page offset
     * @param limit  maximum number of snapshots to return
     * @return page of snapshots created by successful publications
     */
    List<Revision> byItem(String tenant, String item, int offset, int limit);

    /**
     * Reads snapshots belonging to one formal release.
     *
     * @param tenant  exact tenant identifier
     * @param release formal release identifier
     * @return snapshots first persisted by the requested release
     */
    List<Revision> byRelease(String tenant, String release);

}
