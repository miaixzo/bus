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
package org.miaixz.bus.image.nimble.opencv.lut.colormap;

import java.util.Objects;
import java.util.function.DoubleUnaryOperator;

import org.miaixz.bus.image.nimble.opencv.LookupTableCV;
import org.miaixz.bus.image.nimble.opencv.lut.ByteLut;

/**
 * Turns a {@link ColorMap} into the concrete tables the rendering code consumes: the 256-entry {@link ByteLut}, BGR /
 * ABGR byte planes, an interleaved RGBA texture, or a {@link LookupTableCV} indexed by stored pixel values.
 *
 * @author Kimi Liu
 */
public final class ColorMapCompiler {

    public static final int BYTE_LUT_ENTRIES = 256;
    public static final int MAX_LOOKUP_ENTRIES = 1 << ColorMap.MAX_BITS;

    private ColorMapCompiler() {
    }

    /**
     * Samples evenly over the map's default range, both ends included.
     */
    public static Rgba[] sample(ColorMap map, int entries) {
        requireEntries(entries);
        ColorMapDomain domain = map.domain();
        ColorMapSampler sampler = map.sampler();
        var samples = new Rgba[entries];
        for (int i = 0; i < entries; i++) {
            samples[i] = sampler.sample(domain.denormalize((double) i / (entries - 1)));
        }
        return samples;
    }

    /**
     * One sample per input value in {@code [minInput, maxInput]}, mapped to the domain first.
     */
    public static Rgba[] sample(ColorMap map, int minInput, int maxInput, DoubleUnaryOperator inputToDomain) {
        Objects.requireNonNull(inputToDomain, "Input mapping cannot be null");
        int entries = lookupEntries(minInput, maxInput);
        ColorMapSampler sampler = map.sampler();
        var samples = new Rgba[entries];
        for (int i = 0; i < entries; i++) {
            samples[i] = sampler.sample(inputToDomain.applyAsDouble((double) minInput + i));
        }
        return samples;
    }

    /**
     * Linear mapping of a display window onto the map's default range.
     */
    public static DoubleUnaryOperator windowToDomain(ColorMap map, double windowMin, double windowMax) {
        if (!(windowMax > windowMin)) {
            throw new IllegalArgumentException("Window must have max > min");
        }
        ColorMapDomain domain = map.domain();
        double scale = domain.span() / (windowMax - windowMin);
        return v -> domain.min() + (v - windowMin) * scale;
    }

    /**
     * 256-entry table that remembers {@code map} as its {@link ByteLut#source()}.
     */
    public static ByteLut toByteLut(ColorMap map) {
        return new ByteLut(map.name(), toBgr(map, BYTE_LUT_ENTRIES), map);
    }

    /**
     * {@code [3][entries]} planes in B, G, R order, the {@link ByteLut} layout.
     */
    public static byte[][] toBgr(ColorMap map, int entries) {
        return pack(sample(map, entries), false);
    }

    /**
     * {@code [4][entries]} planes in A, B, G, R order, the layout of 4-byte ABGR images.
     */
    public static byte[][] toAbgr(ColorMap map, int entries) {
        return pack(sample(map, entries), true);
    }

    /**
     * Interleaved RGBA8 bytes, the layout of a 1D GPU texture.
     */
    public static byte[] toRgba(ColorMap map, int entries) {
        return interleave(sample(map, entries));
    }

    /**
     * Interleaved RGBA8 bytes with one texel per input value.
     */
    public static byte[] toRgba(ColorMap map, int minInput, int maxInput, DoubleUnaryOperator inputToDomain) {
        return interleave(sample(map, minInput, maxInput, inputToDomain));
    }

    /**
     * Byte lookup table indexed by stored pixel value, with B, G, R and optionally A bands, so a 16-bit single-channel
     * image becomes an 8-bit color image in one {@link LookupTableCV#lookup(org.opencv.core.Mat)} pass.
     */
    public static LookupTableCV toLookupTable(
            ColorMap map,
            int minInput,
            int maxInput,
            DoubleUnaryOperator inputToDomain,
            boolean withAlpha) {
        Rgba[] samples = sample(map, minInput, maxInput, inputToDomain);
        byte[][] abgr = pack(samples, true);
        byte[][] bands = withAlpha ? new byte[][] { abgr[1], abgr[2], abgr[3], abgr[0] }
                : new byte[][] { abgr[1], abgr[2], abgr[3] };
        return new LookupTableCV(bands, minInput);
    }

    private static int lookupEntries(int minInput, int maxInput) {
        long entries = (long) maxInput - minInput + 1;
        if (entries < 1 || entries > MAX_LOOKUP_ENTRIES) {
            throw new IllegalArgumentException(
                    "Input range must hold 1 to %d values: [%d, %d]".formatted(MAX_LOOKUP_ENTRIES, minInput, maxInput));
        }
        return (int) entries;
    }

    private static void requireEntries(int entries) {
        if (entries < 2) {
            throw new IllegalArgumentException("At least 2 entries are needed: " + entries);
        }
    }

    // Planes in A, B, G, R order when withAlpha, else B, G, R.
    private static byte[][] pack(Rgba[] samples, boolean withAlpha) {
        int n = samples.length;
        var planes = new byte[withAlpha ? 4 : 3][n];
        int shift = withAlpha ? 1 : 0;
        for (int i = 0; i < n; i++) {
            Rgba s = samples[i];
            if (withAlpha) {
                planes[0][i] = (byte) s.alpha8();
            }
            planes[shift][i] = (byte) s.blue8();
            planes[shift + 1][i] = (byte) s.green8();
            planes[shift + 2][i] = (byte) s.red8();
        }
        return planes;
    }

    private static byte[] interleave(Rgba[] samples) {
        var data = new byte[samples.length * 4];
        for (int i = 0; i < samples.length; i++) {
            Rgba s = samples[i];
            int o = i << 2;
            data[o] = (byte) s.red8();
            data[o + 1] = (byte) s.green8();
            data[o + 2] = (byte) s.blue8();
            data[o + 3] = (byte) s.alpha8();
        }
        return data;
    }
}
