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

/**
 * Defines the ColorSubsampling values.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum ColorSubsampling {

    /**
     * The ybr xxx 422 value.
     */
    YBR_XXX_422 {

        @Override
        public int frameLength(int w, int h) {
            return w * h * 2;
        }

        @Override
        public int indexOfY(int x, int y, int w) {
            return (w * y + x) * 2 - (x % 2);
        }

        @Override
        public int indexOfBR(int x, int y, int w) {
            return (w * y * 2) + ((x >> 1) << 2) + 2;
        }
    },
    /**
     * The ybr xxx 420 value.
     */
    YBR_XXX_420 {

        @Override
        public int frameLength(int w, int h) {
            return w * h / 2 * 3;
        }

        @Override
        public int indexOfY(int x, int y, int w) {
            int withoutBR = y / 2;
            int withBR = y - withoutBR;
            return w * (withBR * 2 + withoutBR) + ((y % 2 == 0) ? (x * 2 - (x % 2)) : x);
        }

        @Override
        public int indexOfBR(int x, int y, int w) {
            return w * (y / 2) * 3 + ((x >> 1) << 2) + 2;
        }
    };

    /**
     * Executes the frame length operation.
     *
     * @param w the w.
     * @param h the h.
     * @return the operation result.
     */
    public abstract int frameLength(int w, int h);

    /**
     * Executes the index of y operation.
     *
     * @param x the x.
     * @param y the y.
     * @param w the w.
     * @return the operation result.
     */
    public abstract int indexOfY(int x, int y, int w);

    /**
     * Executes the index of br operation.
     *
     * @param x the x.
     * @param y the y.
     * @param w the w.
     * @return the operation result.
     */
    public abstract int indexOfBR(int x, int y, int w);

}
