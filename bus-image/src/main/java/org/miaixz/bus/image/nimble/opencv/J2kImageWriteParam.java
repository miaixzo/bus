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
package org.miaixz.bus.image.nimble.opencv;

import java.util.Locale;

import javax.imageio.ImageWriteParam;

/**
 * Represents the J2kImageWriteParam type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class J2kImageWriteParam extends ImageWriteParam {

    /**
     * The compression types value.
     */
    private static final String[] COMPRESSION_TYPES = { "LOSSY", "LOSSLESS" };

    /**
     * The compression ratiofactor value.
     */
    private int compressionRatiofactor;

    /**
     * Creates a new instance.
     *
     * @param locale the locale.
     */
    public J2kImageWriteParam(Locale locale) {
        super(locale);
        super.canWriteCompressed = true;
        super.compressionMode = MODE_EXPLICIT;
        super.compressionType = "LOSSY";
        super.compressionTypes = COMPRESSION_TYPES;
        this.compressionRatiofactor = 10;
    }

    /**
     * Sets the compression type.
     *
     * @param compressionType the compression type.
     */
    @Override
    public void setCompressionType(String compressionType) {
        super.setCompressionType(compressionType);
        if (isCompressionLossless()) {
            this.compressionRatiofactor = 0;
        }
    }

    /**
     * Determines whether compression lossless.
     *
     * @return true if the condition is met; otherwise false.
     */
    @Override
    public boolean isCompressionLossless() {
        return compressionType.equals("LOSSLESS");
    }

    /**
     * Gets the compression ratiofactor.
     *
     * @return the compression ratiofactor.
     */
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
