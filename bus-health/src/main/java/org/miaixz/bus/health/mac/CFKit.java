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
package org.miaixz.bus.health.mac;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;

import com.sun.jna.Pointer;
import com.sun.jna.platform.mac.CoreFoundation.CFStringRef;

/**
 * CF String retrieving
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class CFKit {

    /**
     * /** Convert a pointer to a CFString into a String.
     *
     * @param result Pointer to the CFString
     * @return a CFString or "unknown" if it has no value
     */
    public static String cfPointerToString(Pointer result) {
        return cfPointerToString(result, true);
    }

    /**
     * Convert a pointer to a CFString into a String.
     *
     * @param result        Pointer to the CFString
     * @param returnUnknown Whether to return the "unknown" string
     * @return a CFString including a possible empty one if {@code returnUnknown} is false, or "unknown" if it is true
     */
    public static String cfPointerToString(Pointer result, boolean returnUnknown) {
        String s = Normal.EMPTY;
        if (result != null) {
            CFStringRef cfs = new CFStringRef(result);
            s = cfs.stringValue();
        }
        if (returnUnknown && s.isEmpty()) {
            return Normal.UNKNOWN;
        }
        return s;
    }

}
