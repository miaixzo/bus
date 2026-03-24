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
package org.miaixz.bus.limiter.magic;

/**
 * Enumeration representing different strategy modes for the limiter. These modes define how limiting rules are applied
 * and what actions are taken when limits are exceeded.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum StrategyMode {

    /**
     * Represents a fallback strategy, typically used for graceful degradation when a service is overloaded.
     */
    FALLBACK,
    /**
     * Represents a hotspot method strategy, focusing on limiting access to frequently accessed methods or resources.
     */
    HOT_METHOD,
    /**
     * Represents a request limiting strategy, controlling the rate of incoming requests.
     */
    REQUEST_LIMIT

}
