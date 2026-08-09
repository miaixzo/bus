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
package org.miaixz.bus.vortex;

/**
 * Defines how a route prepares and delivers its response body.
 * <p>
 * Route assets expose only buffered, realtime-streaming and download modes. {@link #TRANSFORMING} is an internal mode
 * reserved for bounded format conversion and is therefore not returned by {@link #of(Integer)}.
 *
 * @author Kimi Liu
 */
public enum Delivery {

    /**
     * A bounded response that must be fully materialized before it is written.
     */
    BUFFERED,

    /**
     * A bounded response whose representation is transformed before it is written.
     */
    TRANSFORMING,

    /**
     * A backpressure-aware, latency-sensitive response such as SSE or LLM output.
     */
    STREAMING,

    /**
     * A backpressure-aware file response governed by download-specific admission and progress limits.
     */
    DOWNLOAD;

    /**
     * Resolves the mandatory route-asset value.
     *
     * @param value configured asset value: {@code 1}=buffered, {@code 2}=realtime stream, {@code 3}=download
     * @return resolved response delivery
     * @throws IllegalStateException when the value is null or outside the supported range
     */
    public static Delivery of(Integer value) {
        if (Integer.valueOf(1).equals(value)) {
            return BUFFERED;
        }
        if (Integer.valueOf(2).equals(value)) {
            return STREAMING;
        }
        if (Integer.valueOf(3).equals(value)) {
            return DOWNLOAD;
        }
        throw new IllegalStateException("Route asset stream must be explicitly set to 1, 2 or 3");
    }

}
