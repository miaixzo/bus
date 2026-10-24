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
package org.miaixz.bus.cortex.setting.event;

import org.miaixz.bus.cortex.setting.SettingEnforcer;

/**
 * Value-free audit and command receipts; formal success is committed by PublicationStore.
 *
 * @author Kimi Liu
 */
public interface EventStore {

    /**
     * Looks up tenant/actor/resource/resourceId/action/request identity; mismatched checksum is a conflict.
     *
     * @param context    authenticated tenant, actor and request identity
     * @param resource   target resource category
     * @param resourceId exact target resource identifier
     * @param action     operation name
     * @return matching receipt, or null if absent
     */
    Event findReceipt(SettingEnforcer.Context context, String resource, String resourceId, String action);

    /**
     * Appends an authenticated, value-free audit record; must reject externally forged publication success.
     *
     * @param context authenticated actor and request identity
     * @param event   value-free audit record to append
     * @return persisted audit record
     */
    Event append(SettingEnforcer.Context context, Event event);

}
