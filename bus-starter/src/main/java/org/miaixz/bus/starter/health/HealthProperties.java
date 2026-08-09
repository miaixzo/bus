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
package org.miaixz.bus.starter.health;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import lombok.Getter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.health.builtin.TID;
import org.miaixz.bus.starter.GeniusBuilder;

/**
 * Immutable health endpoint properties.
 *
 * @author Kimi Liu
 */
@Getter
@Validated
@ConfigurationProperties(GeniusBuilder.HEALTH)
public class HealthProperties {

    /**
     * Whether the health integration is enabled.
     */
    private final boolean enabled;
    /**
     * Health detail identifiers exposed by the Bus health indicator.
     */
    private final List<String> details;

    /**
     * Creates validated health properties.
     *
     * @param enabled whether health integration is enabled
     * @param details ordered health detail identifiers
     * @throws ValidateException when a configured detail is blank or unknown
     */
    public HealthProperties(@DefaultValue("false") boolean enabled, @DefaultValue List<String> details) {
        Set<String> normalized = new LinkedHashSet<>();
        if (details != null) {
            for (String detail : details) {
                String value = detail == null ? Normal.EMPTY : detail.trim();
                if (value.isEmpty() || (!TID.ALL.equals(value) && !TID.ALL_TID.contains(value))) {
                    throw new ValidateException(ErrorCode._400, "Unknown bus.health.details TID: " + detail);
                }
                normalized.add(value);
            }
        }
        this.enabled = enabled;
        this.details = List.copyOf(normalized);
    }

}
