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
 * JPEG XL image write parameters for the native OpenCV writer.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class JXLImageWriteParam extends ImageWriteParam {

    private static final String[] COMPRESSION_TYPES = { "LOSSLESS", "LOSSY" };

    private int effort = 7;
    private int decodingSpeed;

    public JXLImageWriteParam(Locale locale) {
        super(locale);
        super.canWriteCompressed = true;
        super.compressionMode = MODE_EXPLICIT;
        super.compressionType = "LOSSLESS";
        super.compressionTypes = COMPRESSION_TYPES;
        super.compressionQuality = 0.90F;
    }

    public int getEffort() {
        return effort;
    }

    public void setEffort(int effort) {
        if (effort < 1 || effort > 9)
            throw new IllegalArgumentException("Effort must be between 1 and 9, got: " + effort);
        this.effort = effort;
    }

    @Override
    public boolean isCompressionLossless() {
        return "LOSSLESS".equals(compressionType);
    }

    public float getEffectiveQuality() {
        return isCompressionLossless() ? 1.0F : compressionQuality;
    }

    public void setLossless() {
        compressionType = "LOSSLESS";
    }

    @Override
    public void setCompressionQuality(float quality) {
        super.setCompressionQuality(Math.max(0.0F, Math.min(1.0F, quality)));
    }

    public int getDecodingSpeed() {
        return decodingSpeed;
    }

    public void setDecodingSpeed(int decodingSpeed) {
        if (decodingSpeed < 0 || decodingSpeed > 4)
            throw new IllegalArgumentException("Decoding speed must be between 0 and 4, got: " + decodingSpeed);
        this.decodingSpeed = decodingSpeed;
    }

}
