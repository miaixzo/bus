/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.           ~
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

import org.miaixz.bus.health.Builder;

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

/**
 * Wrapper classes for JNA clases which extend {@link com.sun.jna.ptr.ByReference} intended for use in
 * try-with-resources blocks.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface ByRef {

    class CloseableIntByReference extends IntByReference implements AutoCloseable {

        public CloseableIntByReference() {
            super();
        }

        public CloseableIntByReference(int value) {
            super(value);
        }

        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

    class CloseableLongByReference extends LongByReference implements AutoCloseable {

        public CloseableLongByReference() {
            super();
        }

        public CloseableLongByReference(long value) {
            super(value);
        }

        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

    class CloseableNativeLongByReference extends NativeLongByReference implements AutoCloseable {

        public CloseableNativeLongByReference() {
            super();
        }

        public CloseableNativeLongByReference(NativeLong nativeLong) {
            super(nativeLong);
        }

        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

    class CloseablePointerByReference extends PointerByReference implements AutoCloseable {

        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

    class CloseableLONGLONGByReference extends LONGLONGByReference implements AutoCloseable {

        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

    class CloseableULONGptrByReference extends ULONG_PTRByReference implements AutoCloseable {

        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

    class CloseableHANDLEByReference extends HANDLEByReference implements AutoCloseable {

        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

    class CloseableSizeTByReference extends size_t.ByReference implements AutoCloseable {

        public CloseableSizeTByReference() {
            super();
        }

        public CloseableSizeTByReference(long value) {
            super(value);
        }

        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

    class CloseablePROCESSENTRY32ByReference extends PROCESSENTRY32.ByReference implements AutoCloseable {

        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

}
