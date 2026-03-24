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
package org.miaixz.bus.notify.metric.unisms;

import org.miaixz.bus.notify.Context;
import org.miaixz.bus.notify.magic.Notice;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Notice for Uni SMS service.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UniNotice extends Notice {

    /**
     * Indicates whether to use simple mode. Defaults to true.
     */
    @Builder.Default
    private boolean simple = true;

    /**
     * The name of the template variable.
     */
    private String templateName;
    /**
     * The retry interval in milliseconds.
     */
    private int retryInterval;
    /**
     * The maximum number of retries.
     */
    private int maxRetries;

    /**
     * Retrieves the default API request address. This address is used when the {@link Context} endpoint is empty.
     *
     * @return The default API request address for Uni SMS.
     */
    @Override
    public String getUrl() {
        return this.url = "https://uni.apistd.com/";
    }

}
