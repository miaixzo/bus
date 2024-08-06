/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.nimble.opencv;

import java.util.Locale;

import javax.imageio.ImageWriteParam;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class J2kImageWriteParam extends ImageWriteParam {

    private static final String[] COMPRESSION_TYPES = { "LOSSY", "LOSSLESS" };

    private int compressionRatiofactor;

    public J2kImageWriteParam(Locale locale) {
        super(locale);
        super.canWriteCompressed = true;
        super.compressionMode = MODE_EXPLICIT;
        super.compressionType = "LOSSY";
        super.compressionTypes = COMPRESSION_TYPES;
        this.compressionRatiofactor = 10;
    }

    @Override
    public void setCompressionType(String compressionType) {
        super.setCompressionType(compressionType);
        if (isCompressionLossless()) {
            this.compressionRatiofactor = 0;
        }
    }

    @Override
    public boolean isCompressionLossless() {
        return compressionType.equals("LOSSLESS");
    }

    public int getCompressionRatiofactor() {
        return compressionRatiofactor;
    }

    /**
     * Set the lossy compression factor Near-lossless compression ratios of 5:1 to 20:1 (e.g. compressionRatiofactor =
     * 10) Lossy compression with acceptable degradation can have ratios of 30:1 to 100:1 (e.g. compressionRatiofactor =
     * 50)
     *
     * @param compressionRatiofactor the compression ratio
     */
    public void setCompressionRatiofactor(int compressionRatiofactor) {
        this.compressionRatiofactor = compressionRatiofactor;
    }

}
