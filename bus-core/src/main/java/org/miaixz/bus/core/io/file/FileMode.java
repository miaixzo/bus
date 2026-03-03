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
package org.miaixz.bus.core.io.file;

/**
 * File read and write modes, commonly used with {@link java.io.RandomAccessFile}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum FileMode {

    /**
     * Opens for reading only. Invoking any of the write methods of the resulting object will cause an
     * {@link java.io.IOException} to be thrown.
     */
    r,
    /**
     * Opens for reading and writing.
     */
    rw,
    /**
     * Opens for reading and writing. Every update to the file's content or metadata is to be written synchronously to
     * the underlying storage device.
     */
    rws,
    /**
     * Opens for reading and writing. Every update to the file's content is to be written synchronously to the
     * underlying storage device.
     */
    rwd

}
