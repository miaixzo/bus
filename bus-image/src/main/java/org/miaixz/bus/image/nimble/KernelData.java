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
package org.miaixz.bus.image.nimble;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Immutable convolution kernel data for image filtering operations.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class KernelData {

    /**
     * The min sigma value.
     */
    private static final float MIN_SIGMA = 1.0e-5f;

    /**
     * The none value.
     */
    public static final KernelData NONE = new KernelData("None", KernelType.IDENTITY, 1, 1, new float[] { 1.0f });

    /**
     * The mean value.
     */
    public static final KernelData MEAN = new KernelData("Mean", KernelType.SMOOTHING, 3, 3,
            new float[] { 1, 1, 1, 1, 1, 1, 1, 1, 1 }, 9);

    /**
     * The blur value.
     */
    public static final KernelData BLUR = new KernelData("Blur", KernelType.SMOOTHING, 3, 3,
            new float[] { 0, 1, 0, 1, 4, 1, 0, 1, 0 }, 8);

    /**
     * The sharpen value.
     */
    public static final KernelData SHARPEN = new KernelData("Sharpen", KernelType.SHARPENING, 3, 3,
            new float[] { 0, -1, 0, -1, 8, -1, 0, -1, 0 }, 4);

    /**
     * The edge detect value.
     */
    public static final KernelData EDGE_DETECT = new KernelData("Edge Detect", KernelType.EDGE_DETECTION, 3, 3,
            new float[] { 0, -1, 0, -1, 4, -1, 0, -1, 0 });

    /**
     * The emboss value.
     */
    public static final KernelData EMBOSS = new KernelData("Emboss", KernelType.SPECIAL_EFFECT, 3, 3,
            new float[] { -5, 0, 0, 0, 1, 0, 0, 0, 5 });

    /**
     * The gaussian 3 value.
     */
    public static final KernelData GAUSSIAN_3 = createGaussianKernel("Gaussian 3x3", 3);

    /**
     * The gaussian 5 value.
     */
    public static final KernelData GAUSSIAN_5 = createGaussianKernel("Gaussian 5x5", 5);

    /**
     * The all filters value.
     */
    private static final List<KernelData> ALL_FILTERS = List
            .of(NONE, MEAN, BLUR, SHARPEN, EDGE_DETECT, EMBOSS, GAUSSIAN_3, GAUSSIAN_5);

    /**
     * The name value.
     */
    private final String name;

    /**
     * The type value.
     */
    private final KernelType type;

    /**
     * The width value.
     */
    private final int width;

    /**
     * The height value.
     */
    private final int height;

    /**
     * The x origin value.
     */
    private final int xOrigin;

    /**
     * The y origin value.
     */
    private final int yOrigin;

    /**
     * The data value.
     */
    private final float[] data;

    /**
     * Creates a new instance.
     *
     * @param name   the name.
     * @param type   the type.
     * @param width  the width.
     * @param height the height.
     * @param data   the data.
     */
    public KernelData(String name, KernelType type, int width, int height, float[] data) {
        this(name, type, width, height, width / 2, height / 2, data, 1);
    }

    /**
     * Creates a new instance.
     *
     * @param name    the name.
     * @param type    the type.
     * @param width   the width.
     * @param height  the height.
     * @param data    the data.
     * @param divisor the divisor.
     */
    public KernelData(String name, KernelType type, int width, int height, float[] data, int divisor) {
        this(name, type, width, height, width / 2, height / 2, data, divisor);
    }

    /**
     * Creates a new instance.
     *
     * @param name    the name.
     * @param type    the type.
     * @param width   the width.
     * @param height  the height.
     * @param xOrigin the x origin.
     * @param yOrigin the y origin.
     * @param data    the data.
     * @param divisor the divisor.
     */
    public KernelData(String name, KernelType type, int width, int height, int xOrigin, int yOrigin, float[] data,
            int divisor) {
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.type = Objects.requireNonNull(type, "Type cannot be null");
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Kernel dimensions must be positive");
        }
        if (data == null || data.length != width * height) {
            throw new IllegalArgumentException("Kernel data length must equal width * height");
        }
        this.width = width;
        this.height = height;
        this.xOrigin = xOrigin < 0 || xOrigin >= width ? width / 2 : xOrigin;
        this.yOrigin = yOrigin < 0 || yOrigin >= height ? height / 2 : yOrigin;
        this.data = normalize(data, divisor);
    }

    /**
     * Normalizes the normalize.
     *
     * @param source  the source.
     * @param divisor the divisor.
     * @return the operation result.
     */
    private static float[] normalize(float[] source, int divisor) {
        float[] copy = Arrays.copyOf(source, source.length);
        if (divisor != 0 && divisor != 1) {
            for (int i = 0; i < copy.length; i++) {
                copy[i] /= divisor;
            }
        }
        return copy;
    }

    /**
     * Creates the gaussian kernel.
     *
     * @param name the name.
     * @param size the size.
     * @return the operation result.
     */
    public static KernelData createGaussianKernel(String name, int size) {
        if (size <= 0 || size % 2 == 0) {
            throw new IllegalArgumentException("Gaussian kernel size must be a positive odd number");
        }
        float sigma = Math.max(MIN_SIGMA, size / 3.0f);
        float[] data = new float[size * size];
        int center = size / 2;
        float sum = 0.0f;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int dx = x - center;
                int dy = y - center;
                float value = (float) Math.exp(-(dx * dx + dy * dy) / (2.0f * sigma * sigma));
                data[y * size + x] = value;
                sum += value;
            }
        }
        if (sum != 0.0f) {
            for (int i = 0; i < data.length; i++) {
                data[i] /= sum;
            }
        }
        return new KernelData(name, KernelType.SMOOTHING, size, size, data);
    }

    /**
     * Returns the all filters.
     *
     * @return the all filters.
     */
    public static List<KernelData> getAllFilters() {
        return ALL_FILTERS;
    }

    /**
     * Returns the name.
     *
     * @return the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the type.
     *
     * @return the type.
     */
    public KernelType getType() {
        return type;
    }

    /**
     * Returns the width.
     *
     * @return the width.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height.
     *
     * @return the height.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns the x origin.
     *
     * @return the x origin.
     */
    public int getXOrigin() {
        return xOrigin;
    }

    /**
     * Returns the y origin.
     *
     * @return the y origin.
     */
    public int getYOrigin() {
        return yOrigin;
    }

    /**
     * Returns the data.
     *
     * @return the data.
     */
    public float[] getData() {
        return Arrays.copyOf(data, data.length);
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Defines the KernelType values.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum KernelType {
        /**
         * Constant for the identity value.
         */
        IDENTITY,
        /**
         * Constant for the smoothing value.
         */
        SMOOTHING,
        /**
         * Constant for the sharpening value.
         */
        SHARPENING,
        /**
         * Constant for the edge detection value.
         */
        EDGE_DETECTION,
        /**
         * Constant for the special effect value.
         */
        SPECIAL_EFFECT

    }

}
