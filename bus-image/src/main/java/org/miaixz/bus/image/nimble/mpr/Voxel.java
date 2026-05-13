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
package org.miaixz.bus.image.nimble.mpr;

import java.util.Arrays;

/**
 * Channel values of one voxel.
 *
 * @param <T> numeric sample type
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Voxel<T extends Number> {

    /**
     * The values value.
     */
    private final Number[] values;

    /**
     * Creates a new instance.
     *
     * @param channels the channels.
     */
    public Voxel(int channels) {
        if (channels <= 0) {
            throw new IllegalArgumentException("channels must be > 0: " + channels);
        }
        this.values = new Number[channels];
    }

    /**
     * Gets the channels.
     *
     * @return the channels.
     */
    public int getChannels() {
        return values.length;
    }

    /**
     * Sets the value.
     *
     * @param channel the channel.
     * @param value   the value.
     */
    public void setValue(int channel, T value) {
        if (channel >= 0 && channel < values.length) {
            values[channel] = value;
        }
    }

    /**
     * Gets the value.
     *
     * @param channel the channel.
     * @return the value.
     */
    @SuppressWarnings("unchecked")
    public T getValue(int channel) {
        return channel >= 0 && channel < values.length ? (T) values[channel] : null;
    }

    /**
     * Gets the value.
     *
     * @return the value.
     */
    public T getValue() {
        return getValue(0);
    }

    /**
     * Gets the values.
     *
     * @return the values.
     */
    public Number[] getValues() {
        return values.clone();
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        return "Voxel" + Arrays.toString(values);
    }

}
