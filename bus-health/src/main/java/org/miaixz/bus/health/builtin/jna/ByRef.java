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
package org.miaixz.bus.health.builtin.jna;

import com.sun.jna.NativeLong;
import com.sun.jna.platform.unix.LibCAPI.size_t;
import com.sun.jna.platform.win32.BaseTSD.ULONG_PTRByReference;
import com.sun.jna.platform.win32.Tlhelp32.PROCESSENTRY32;
import com.sun.jna.platform.win32.WinDef.LONGLONGByReference;
import com.sun.jna.platform.win32.WinNT.HANDLEByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.ptr.PointerByReference;

import org.miaixz.bus.health.Builder;

/**
 * Wrapper classes for JNA clases which extend {@link com.sun.jna.ptr.ByReference} intended for use in
 * try-with-resources blocks.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface ByRef {

    /**
     * The CloseableIntByReference class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    class CloseableIntByReference extends IntByReference implements AutoCloseable {

        /**
         * Creates a new CloseableIntByReference instance.
         */
        public CloseableIntByReference() {
            super();
        }

        /**
         * Creates a new CloseableIntByReference instance.
         *
         * @param value the value
         */
        public CloseableIntByReference(int value) {
            super(value);
        }

        /**
         * Closes this resource.
         */
        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }

    }

    /**
     * The CloseableLongByReference class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    class CloseableLongByReference extends LongByReference implements AutoCloseable {

        /**
         * Creates a new CloseableLongByReference instance.
         */
        public CloseableLongByReference() {
            super();
        }

        /**
         * Creates a new CloseableLongByReference instance.
         *
         * @param value the value
         */
        public CloseableLongByReference(long value) {
            super(value);
        }

        /**
         * Closes this resource.
         */
        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }

    }

    /**
     * The CloseableNativeLongByReference class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    class CloseableNativeLongByReference extends NativeLongByReference implements AutoCloseable {

        /**
         * Creates a new CloseableNativeLongByReference instance.
         */
        public CloseableNativeLongByReference() {
            super();
        }

        /**
         * Creates a new CloseableNativeLongByReference instance.
         *
         * @param nativeLong the native long
         */
        public CloseableNativeLongByReference(NativeLong nativeLong) {
            super(nativeLong);
        }

        /**
         * Closes this resource.
         */
        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }

    }

    /**
     * The CloseablePointerByReference class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    class CloseablePointerByReference extends PointerByReference implements AutoCloseable {

        /**
         * Closes this resource.
         */
        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }

    }

    /**
     * The CloseableLONGLONGByReference class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    class CloseableLONGLONGByReference extends LONGLONGByReference implements AutoCloseable {

        /**
         * Closes this resource.
         */
        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }

    }

    /**
     * The CloseableULONGptrByReference class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    class CloseableULONGptrByReference extends ULONG_PTRByReference implements AutoCloseable {

        /**
         * Closes this resource.
         */
        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }

    }

    /**
     * The CloseableHANDLEByReference class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    class CloseableHANDLEByReference extends HANDLEByReference implements AutoCloseable {

        /**
         * Closes this resource.
         */
        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }

    }

    /**
     * The CloseableSizeTByReference class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    class CloseableSizeTByReference extends size_t.ByReference implements AutoCloseable {

        /**
         * Creates a new CloseableSizeTByReference instance.
         */
        public CloseableSizeTByReference() {
            super();
        }

        /**
         * Creates a new CloseableSizeTByReference instance.
         *
         * @param value the value
         */
        public CloseableSizeTByReference(long value) {
            super(value);
        }

        /**
         * Closes this resource.
         */
        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }

    }

    /**
     * The CloseablePROCESSENTRY32ByReference class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    class CloseablePROCESSENTRY32ByReference extends PROCESSENTRY32.ByReference implements AutoCloseable {

        /**
         * Closes this resource.
         */
        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }

    }

}
