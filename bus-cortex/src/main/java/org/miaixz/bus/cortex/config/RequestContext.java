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
package org.miaixz.bus.cortex.config;

import java.util.Collections;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * Request context used by gray router.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
public class RequestContext {

    /**
     * Client IP address used for gray-routing evaluation.
     */
    private String clientIp;
    /**
     * Request headers available to gray-routing rules.
     */
    private Map<String, String> headers;

    /**
     * Returns an immutable view of request headers.
     *
     * @return immutable header map, or an empty map if headers are absent
     */
    public Map<String, String> headerView() {
        return headers == null ? Collections.emptyMap() : Collections.unmodifiableMap(headers);
    }

}
