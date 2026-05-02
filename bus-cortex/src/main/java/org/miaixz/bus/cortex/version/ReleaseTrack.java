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
package org.miaixz.bus.cortex.version;

/**
 * Built-in release tracks. Custom tracks are accepted as normalized string values by {@link #normalize(String)}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum ReleaseTrack {

    /**
     * Stable release track intended for normal production traffic.
     */
    STABLE("stable"),
    /**
     * Beta release track intended for preview validation.
     */
    BETA("beta"),
    /**
     * Canary release track intended for limited rollout validation.
     */
    CANARY("canary");

    /**
     * Normalized storage key for the release track.
     */
    private final String key;

    /**
     * Creates a release track with its normalized storage key.
     *
     * @param key normalized track key
     */
    ReleaseTrack(String key) {
        this.key = key;
    }

    /**
     * Returns the normalized storage key.
     *
     * @return normalized track key
     */
    public String key() {
        return key;
    }

    /**
     * Normalizes a raw release track, using the stable track when no value is supplied.
     *
     * @param track raw release track
     * @return normalized release track key
     */
    public static String normalize(String track) {
        return track == null || track.isBlank() ? STABLE.key : track.trim().toLowerCase();
    }

}
