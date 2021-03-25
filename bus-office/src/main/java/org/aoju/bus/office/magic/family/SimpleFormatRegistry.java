/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.office.magic.family;

import org.aoju.bus.core.toolkit.StringKit;

import java.util.*;
import java.util.stream.Collectors;

/**
 * SimpleDocumentFormatRegistry包含office支持的文档格式集合.
 *
 * @author Kimi Liu
 * @version 6.2.2
 * @since JDK 1.8+
 */
public class SimpleFormatRegistry implements FormatRegistry {

    private final Map<String, DocumentFormat> fmtsByExtension = new HashMap<>();
    private final Map<String, DocumentFormat> fmtsByMediaType = new HashMap<>();

    /**
     * 向注册表添加新格式.
     *
     * @param documentFormat 要添加的格式.
     */
    public void addFormat(final DocumentFormat documentFormat) {
        documentFormat
                .getExtensions()
                .stream()
                .map(StringKit::lowerCase)
                .forEach(ext -> fmtsByExtension.put(ext, documentFormat));
        fmtsByMediaType.put(StringKit.lowerCase(documentFormat.getMediaType()), documentFormat);
    }

    @Override
    public DocumentFormat getFormatByExtension(final String extension) {
        return null == extension ? null : fmtsByExtension.get(StringKit.lowerCase(extension));
    }

    @Override
    public DocumentFormat getFormatByMediaType(final String mediaType) {
        return null == mediaType ? null : fmtsByMediaType.get(StringKit.lowerCase(mediaType));
    }

    @Override
    public Set<DocumentFormat> getOutputFormats(final FamilyType family) {
        return Optional.ofNullable(family).map(docFam -> fmtsByMediaType
                .values()
                .stream()
                .filter(format -> null != format.getStoreProperties(docFam))
                .collect(Collectors.toSet()))
                .orElse(new HashSet<>());
    }

}
