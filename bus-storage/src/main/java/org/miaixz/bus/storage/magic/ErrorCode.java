/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
     * Constructs a new AbstractProvider with default settings.
     */
    public ErrorCode() {

    }

    /**
     * Common: File upload failed.
     */
    public static final Errors _113000 = ErrorRegistry.builder().key("113000").value("文件上传失败").build();

    /**
     * Common: Directory already exists.
     */
    public static final Errors _113001 = ErrorRegistry.builder().key("113001").value("目录已存在").build();

    /**
     * Common: Directory does not exist.
     */
    public static final Errors _113002 = ErrorRegistry.builder().key("113002").value("目录不存在").build();

    /**
     * Common: File does not exist.
     */
    public static final Errors _113003 = ErrorRegistry.builder().key("113003").value("文件不存在").build();

    /**
     * Common: File already exists.
     */
    public static final Errors _113004 = ErrorRegistry.builder().key("113004").value("文件已存在").build();

    /**
     * Common: Failed to get directory.
     */
    public static final Errors _113005 = ErrorRegistry.builder().key("113005").value("目录获取失败").build();

    /**
     * Common: Failed to calculate file MD5.
     */
    public static final Errors _113006 = ErrorRegistry.builder().key("113006").value("文件MD5计算失败").build();

}
