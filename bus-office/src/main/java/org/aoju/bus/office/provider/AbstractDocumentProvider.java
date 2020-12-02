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
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.office.provider;

import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.office.magic.family.DocumentFormat;

import java.io.File;

/**
 * 所有文档规范实现的基类.
 *
 * @author Kimi Liu
 * @version 6.1.3
 * @since JDK 1.8+
 */
public abstract class AbstractDocumentProvider implements DocumentProvider {

    private final File file;
    private DocumentFormat documentFormat;

    protected AbstractDocumentProvider(final File file) {
        super();

        Assert.notNull(file, "The file is null");

        this.file = file;
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public DocumentFormat getFormat() {
        return documentFormat;
    }

    /**
     * @param documentFormat 要设置的文档格式.
     */
    public void setDocumentFormat(final DocumentFormat documentFormat) {
        Assert.notNull(documentFormat, "The document format is null or unsupported");
        this.documentFormat = documentFormat;
    }

}
