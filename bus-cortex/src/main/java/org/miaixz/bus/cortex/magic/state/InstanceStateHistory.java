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
package org.miaixz.bus.cortex.magic.state;

import lombok.Getter;
import lombok.Setter;

/**
 * Historical record of an instance state transition.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
public class InstanceStateHistory {

    /** State captured at this point in time. */
    private InstanceState state;
    /** Unix epoch milliseconds when the state was recorded. */
    private long timestamp;
    /** Human-readable reason for the state transition. */
    private String reason;
    /** Namespace of the affected instance. */
    private String namespace;
    /** Service method of the affected instance. */
    private String method;
    /** Service version of the affected instance. */
    private String version;
    /** Stable fingerprint of the affected instance. */
    private String fingerprint;

}
