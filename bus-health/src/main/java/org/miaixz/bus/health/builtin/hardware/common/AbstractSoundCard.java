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
package org.miaixz.bus.health.builtin.hardware.common;

import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.health.builtin.hardware.SoundCard;

/**
 * An abstract Sound Card
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Immutable
public abstract class AbstractSoundCard implements SoundCard {

    /**
     * The kernelVersion value.
     */
    private final String kernelVersion;

    /**
     * The name value.
     */
    private final String name;

    /**
     * The codec value.
     */
    private final String codec;

    /**
     * Abstract Sound Card Constructor
     *
     * @param kernelVersion The version
     * @param name          The name
     * @param codec         The codec
     */
    protected AbstractSoundCard(String kernelVersion, String name, String codec) {
        this.kernelVersion = kernelVersion;
        this.name = name;
        this.codec = codec;
    }

    /**
     * Returns the driver version.
     *
     * @return the get driver version result
     */
    @Override
    public String getDriverVersion() {
        return this.kernelVersion;
    }

    /**
     * Returns the name.
     *
     * @return the get name result
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Returns the codec.
     *
     * @return the get codec result
     */
    @Override
    public String getCodec() {
        return this.codec;
    }

    /**
     * Returns the to string result.
     *
     * @return the to string result
     */
    @Override
    public String toString() {
        String builder = "SoundCard@" + Integer.toHexString(hashCode()) + " [name=" + this.name + ", kernelVersion="
                + this.kernelVersion + ", codec=" + this.codec + ']';
        return builder;
    }

}
