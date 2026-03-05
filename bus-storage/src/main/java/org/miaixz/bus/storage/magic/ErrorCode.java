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
 * @since Java 17+
 */
public class ErrorCode extends org.miaixz.bus.core.basic.normal.ErrorCode {

    /**
     * Constructs a new ErrorCode with default settings.
     */
    public ErrorCode() {

    }

    /**
     * File upload failed.
     */
    public static final Errors _113000 = ErrorRegistry.builder().key("113000").value("File upload failed").build();

    /**
     * Directory already exists.
     */
    public static final Errors _113001 = ErrorRegistry.builder().key("113001").value("Directory already exists")
            .build();

    /**
     * Directory does not exist.
     */
    public static final Errors _113002 = ErrorRegistry.builder().key("113002").value("Directory does not exist")
            .build();

    /**
     * File does not exist.
     */
    public static final Errors _113003 = ErrorRegistry.builder().key("113003").value("File does not exist").build();

    /**
     * File already exists.
     */
    public static final Errors _113004 = ErrorRegistry.builder().key("113004").value("File already exists").build();

    /**
     * Failed to get directory.
     */
    public static final Errors _113005 = ErrorRegistry.builder().key("113005").value("Failed to get directory").build();

    /**
     * Failed to calculate file MD5.
     */
    public static final Errors _113006 = ErrorRegistry.builder().key("113006").value("Failed to calculate file MD5")
            .build();

    /**
     * Storage service configuration error, please check.
     */
    public static final Errors _113007 = ErrorRegistry.builder().key("113007")
            .value("Storage service configuration error, please check").build();

}
