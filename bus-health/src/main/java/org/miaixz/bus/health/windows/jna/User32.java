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
package org.miaixz.bus.health.windows.jna;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;

/**
 * Binding to the {@code user32.dll} Connecting and Configuring Displays functions.
 *
 * @author Kimi Liu
 */
public interface User32 extends StdCallLibrary {

    /**
     * The shared User32 instance.
     */
    User32 INSTANCE = Native.load("user32", User32.class);

    /**
     * Retrieves the size of the buffers required to call {@code QueryDisplayConfig}.
     *
     * @param flags                    a combination of QDC flags
     * @param numPathArrayElements     receives the number of path elements
     * @param numModeInfoArrayElements receives the number of mode info elements
     * @return {@code ERROR_SUCCESS} on success, otherwise a Win32 error code
     */
    int GetDisplayConfigBufferSizes(
            int flags,
            IntByReference numPathArrayElements,
            IntByReference numModeInfoArrayElements);

    /**
     * Retrieves active display paths and modes.
     *
     * @param flags                    a combination of QDC flags
     * @param numPathArrayElements     in/out count of path array elements
     * @param pathArray                buffer receiving path elements
     * @param numModeInfoArrayElements in/out count of mode info elements
     * @param modeInfoArray            buffer receiving mode info elements
     * @param currentTopologyId        optional topology identifier output
     * @return {@code ERROR_SUCCESS} on success, otherwise a Win32 error code
     */
    int QueryDisplayConfig(
            int flags,
            IntByReference numPathArrayElements,
            Pointer pathArray,
            IntByReference numModeInfoArrayElements,
            Pointer modeInfoArray,
            Pointer currentTopologyId);

    /**
     * Retrieves display configuration information about a target device.
     *
     * @param requestPacket a target device name request packet
     * @return {@code ERROR_SUCCESS} on success, otherwise a Win32 error code
     */
    int DisplayConfigGetDeviceInfo(Pointer requestPacket);

}
