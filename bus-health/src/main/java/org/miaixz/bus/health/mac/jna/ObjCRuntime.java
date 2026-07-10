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
package org.miaixz.bus.health.mac.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

/**
 * Objective-C runtime bindings for dispatching messages to AppKit classes through {@code objc_msgSend}. On ARM64, each
 * {@code objc_msgSend} binding must match the selector's parameter shape, so only the overloads used by bus-health are
 * declared here.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface ObjCRuntime extends Library {

    /**
     * Singleton instance of the Objective-C runtime library.
     */
    ObjCRuntime INSTANCE = Native.load("objc", ObjCRuntime.class);

    /**
     * Gets an Objective-C class by name.
     *
     * @param className The class name.
     * @return The class pointer, or {@code null} if unavailable.
     */
    Pointer objc_getClass(String className);

    /**
     * Registers or looks up an Objective-C selector.
     *
     * @param selectorName The selector name.
     * @return The selector pointer.
     */
    Pointer sel_registerName(String selectorName);

    /**
     * Sends a message with no explicit arguments.
     *
     * @param receiver The message receiver.
     * @param selector The selector.
     * @return The returned pointer.
     */
    Pointer objc_msgSend(Pointer receiver, Pointer selector);

    /**
     * Sends a message with a single long argument.
     *
     * @param receiver The message receiver.
     * @param selector The selector.
     * @param index    The long argument.
     * @return The returned pointer.
     */
    Pointer objc_msgSend(Pointer receiver, Pointer selector, long index);

    /**
     * Pushes an Objective-C autorelease pool for the current thread.
     *
     * @return The autorelease pool pointer.
     */
    Pointer objc_autoreleasePoolPush();

    /**
     * Pops an Objective-C autorelease pool.
     *
     * @param pool The autorelease pool pointer.
     */
    void objc_autoreleasePoolPop(Pointer pool);

}
