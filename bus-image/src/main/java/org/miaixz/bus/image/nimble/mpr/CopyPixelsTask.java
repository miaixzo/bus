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

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.function.IntBinaryOperator;

/**
 * Fork/join pixel copy helper used by MPR slice extraction.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class CopyPixelsTask extends RecursiveAction {

    /**
     * The threshold value.
     */
    private static final int THRESHOLD = 4096;

    /**
     * The start value.
     */
    private final int start;

    /**
     * The end value.
     */
    private final int end;

    /**
     * The width value.
     */
    private final int width;

    /**
     * The set pixel value.
     */
    private final IntBinaryOperator setPixel;

    /**
     * Creates a new instance.
     *
     * @param start    the start.
     * @param end      the end.
     * @param width    the width.
     * @param setPixel the set pixel.
     */
    public CopyPixelsTask(int start, int end, int width, IntBinaryOperator setPixel) {
        this.start = start;
        this.end = end;
        this.width = width;
        this.setPixel = setPixel;
    }

    /**
     * Executes the copy operation.
     *
     * @param width    the width.
     * @param height   the height.
     * @param setPixel the set pixel.
     */
    public static void copy(int width, int height, IntBinaryOperator setPixel) {
        if (width <= 0 || height <= 0) {
            return;
        }
        ForkJoinPool.commonPool().invoke(new CopyPixelsTask(0, Math.multiplyExact(width, height), width, setPixel));
    }

    /**
     * Executes the compute operation.
     */
    @Override
    protected void compute() {
        if (end - start <= THRESHOLD) {
            if (start >= end || width <= 0) {
                return;
            }
            int x = start % width;
            int y = start / width;
            for (int i = start; i < end; i++) {
                setPixel.applyAsInt(x, y);
                if (++x >= width) {
                    x = 0;
                    y++;
                }
            }
        } else {
            int mid = (start + end) / 2;
            invokeAll(new CopyPixelsTask(start, mid, width, setPixel), new CopyPixelsTask(mid, end, width, setPixel));
        }
    }

}
