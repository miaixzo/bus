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
package org.miaixz.bus.storage.magic;

import org.miaixz.bus.core.basic.normal.ErrorRegistry;
import org.miaixz.bus.core.basic.normal.Errors;

/**
 * Storage error codes, ranging from 113xxx.
 *
 * @author Kimi Liu
 */
public class ErrorCode extends org.miaixz.bus.core.basic.normal.ErrorCode {

    /**
     * Directory already exists.
     */
    public static final Errors _113000 = ErrorRegistry.register("113000", "Directory already exists");

    /**
     * Directory does not exist.
     */
    public static final Errors _113001 = ErrorRegistry.register("113001", "Directory does not exist");

    /**
     * File already exists.
     */
    public static final Errors _113002 = ErrorRegistry.register("113002", "File already exists");

    /**
     * Failed to get directory.
     */
    public static final Errors _113003 = ErrorRegistry.register("113003", "Failed to get directory");

    /**
     * Failed to calculate file MD5.
     */
    public static final Errors _113004 = ErrorRegistry.register("113004", "Failed to calculate file MD5");

    /**
     * Storage service configuration error, please check.
     */
    public static final Errors _113005 = ErrorRegistry
            .register("113005", "Storage service configuration error, please check");

    /**
     * Storage endpoint invalid.
     */
    public static final Errors _113006 = ErrorRegistry.register("113006", "Storage endpoint invalid");

    /**
     * Storage authentication failed.
     */
    public static final Errors _113007 = ErrorRegistry.register("113007", "Storage authentication failed");

    /**
     * Storage object does not exist.
     */
    public static final Errors _113008 = ErrorRegistry.register("113008", "Storage object does not exist");

    /**
     * Storage bucket does not exist.
     */
    public static final Errors _113009 = ErrorRegistry.register("113009", "Storage bucket does not exist");

    /**
     * Storage stream operation failed.
     */
    public static final Errors _113010 = ErrorRegistry.register("113010", "Storage stream operation failed");

    /**
     * This storage operation is not supported.
     */
    public static final Errors _113011 = ErrorRegistry.register("113011", "This storage operation is not supported");

    /**
     * Constructs a new ErrorCode with default settings.
     */
    public ErrorCode() {
        // No initialization required.
    }

}
