/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
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

import java.util.Set;

/**
 * 实现此接口的类应该保留office支持的文档格式集合.
 *
 * @author Kimi Liu
 * @version 5.8.8
 * @since JDK 1.8+
 */
public interface FormatRegistry {

    /**
     * 获取指定扩展名的文档格式.
     *
     * @param extension 将返回其文档格式的扩展名.
     * @return 如果指定的扩展不存在文档格式，则使用找到的文档格式，或者使用{@code null}.
     */
    DocumentFormat getFormatByExtension(String extension);

    /**
     * 获取指定媒体类型的文档格式.
     *
     * @param mediaType 将返回其文档格式的媒体类型.
     * @return 如果指定的媒体类型不存在文档格式，则使用找到的文档格式，或者使用{@code null}.
     */
    DocumentFormat getFormatByMediaType(String mediaType);

    /**
     * @param family 将返回其文档格式的集合.
     * @return 包含指定系列的所有文档格式的集合.
     */
    Set<DocumentFormat> getOutputFormats(FamilyType family);

}
