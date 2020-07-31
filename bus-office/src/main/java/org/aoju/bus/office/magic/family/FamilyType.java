/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.office.magic.family;

/**
 * 表示office支持的文档类型.
 *
 * @author Kimi Liu
 * @version 6.0.2
 * @since JDK 1.8+
 */
public enum FamilyType {

    /**
     * 文本文档
     * {"odt", "doc", "docx", "rtf"}
     */
    TEXT,

    /**
     * 电子表格文件
     * {"ods", "xls", "xlsx", "csv"}
     */
    SPREADSHEET,

    /**
     * 电子表格文件
     * {"odp", "ppt", "pptx"}
     */
    PRESENTATION,

    /**
     * 图像文件
     * {"odg", "png", "svg","jpg", "jpeg", "gif", "bmp", "ico", "raw"}
     */
    DRAWING,

    /**
     * 压缩文件
     * {"rar", "zip", "jar", "7-zip", "tar", "gzip", "7z"}
     */
    ARCHIVE,

    /**
     * 媒体文件
     * {"mp3", "mp4", "wav", "flv"}
     */
    MEDIA,

    /**
     * 媒体文件
     * {"txt", "html", "htm", "asp", "jsp", "xml", "json", "c",
     * "cpp","json", "md", "gitignore", "java", "py", "prg",
     * "cmd","sql", "sh", "bat", "m", "bas", "properties"}
     */
    SIMTEXT

}
