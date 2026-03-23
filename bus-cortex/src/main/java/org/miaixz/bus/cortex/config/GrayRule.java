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

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Gray release rule definition.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
public class GrayRule {

    /**
     * Gray-routing strategy type.
     */
    private GrayType type;
    /**
     * Explicit client IP addresses matched by the rule.
     */
    private List<String> ipList;
    /**
     * Inclusive client IP range in string form.
     */
    private String ipRange;
    /**
     * Traffic percentage selected by the rule.
     */
    private int percentage;
    /**
     * Request header name used for header-based matching.
     */
    private String headerKey;
    /**
     * Required request header value for header-based matching.
     */
    private String headerValue;
    /**
     * Configuration content returned when the rule matches.
     */
    private String grayContent;

}
