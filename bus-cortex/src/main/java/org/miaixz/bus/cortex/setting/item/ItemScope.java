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
package org.miaixz.bus.cortex.setting.item;

import lombok.Builder.Default;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Request scope only; not a persistent configuration entity.
 *
 * @author Kimi Liu
 */
@Getter
@Setter
@SuperBuilder
public class ItemScope {

    /**
     * Optional exact source Item ID; this request is not a persisted entity.
     */
    private String id;
    /**
     * Required trusted tenant coordinate for the request.
     */
    private String tenant_id;
    /**
     * Optional application context; required for runtime delivery and subscriptions.
     */
    private String app_id;
    /**
     * Tracing identifier; never an authorization credential.
     */
    private String requestId;
    /**
     * Management-only inclusion of logically deleted source definitions.
     */
    private boolean includeDeleted;
    /**
     * Positive bounded page size, defaulting to 100.
     */
    @Default
    private int limit = 100;
    /**
     * Nonnegative management page offset.
     */
    private int offset;

    /**
     * Creates an empty ItemScope for construction or deserialization.
     */
    public ItemScope() {
        this.limit = 100;
    }

}
