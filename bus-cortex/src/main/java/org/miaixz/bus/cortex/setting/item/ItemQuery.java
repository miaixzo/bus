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

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Lookup criteria for a specific setting entry or a filtered setting list.
 *
 * <p>
 * Setting-only resolution semantics should accumulate here rather than backflowing into
 * {@link org.miaixz.bus.cortex.Vector}. Application scope is inherited from {@code app_id}, and profile remains the
 * environment dimension. The logical entry coordinate is {@code data_id}.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@SuperBuilder
public class ItemQuery extends ItemScope {

    /**
     * Creates an empty setting query.
     */
    public ItemQuery() {

    }

    /**
     * Setting data identifier.
     */
    private String data_id;
    /**
     * Optional fallback content returned when the target entry cannot be resolved.
     */
    private String fallbackValue;
    /**
     * Whether overlay content should be preferred during resolution.
     */
    private boolean preferOverlay = true;
    /**
     * Request context used for gray-release evaluation after namespace, profile, and application matching.
     */
    private GrayRequestContext requestContext;

}
