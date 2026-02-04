/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
