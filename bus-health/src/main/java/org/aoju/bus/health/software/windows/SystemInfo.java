/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.health.software.windows;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase.SYSTEM_INFO;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.IntByReference;
import org.aoju.bus.logger.Logger;

/**
 * Windows OS native system information.
 *
 * @author Kimi Liu
 * @version 5.5.9
 * @since JDK 1.8+
 */
public class SystemInfo {

    // Populated during call to init
    private SYSTEM_INFO systemInfo = null;

    /**
     * Constructor for WindowsOSSystemInfo.
     */
    public SystemInfo() {
        init();
    }

    /**
     * Constructor for WindowsOSSystemInfo.
     *
     * @param si a {@link com.sun.jna.platform.win32.WinBase.SYSTEM_INFO} object.
     */
    public SystemInfo(SYSTEM_INFO si) {
        this.systemInfo = si;
    }

    private void init() {
        SYSTEM_INFO si = new SYSTEM_INFO();
        Kernel32.INSTANCE.GetSystemInfo(si);

        try {
            IntByReference isWow64 = new IntByReference();
            // This returns a pseudo handle, currently (HANDLE)-1, that is
            // interpreted as the current process handle. The pseudo handle need
            // not be closed when it is no longer needed. Calling the
            // CloseHandle function with a pseudo handle has no effect.
            HANDLE hProcess = Kernel32.INSTANCE.GetCurrentProcess();
            if (Kernel32.INSTANCE.IsWow64Process(hProcess, isWow64) && isWow64.getValue() > 0) {
                // Populates the class variable with information
                Kernel32.INSTANCE.GetNativeSystemInfo(si);
            }
        } catch (UnsatisfiedLinkError e) {
            // no WOW64 support
            Logger.trace("No WOW64 support: {}", e);
        }

        this.systemInfo = si;
        Logger.debug("Initialized OSNativeSystemInfo");
    }

    /**
     * Number of processors.
     *
     * @return Integer.
     */
    public int getNumberOfProcessors() {
        return this.systemInfo.dwNumberOfProcessors.intValue();
    }

}
