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

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;

import org.miaixz.bus.core.Version;

/**
 * ImageIO writer SPI for native JPEG XL encoding.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class NativeJXLImageWriterSpi extends ImageWriterSpi {

    public NativeJXLImageWriterSpi() {
        this(NativeJXLImageWriter.class);
    }

    public NativeJXLImageWriterSpi(Class<? extends NativeJXLImageWriter> writer) {
        super("Miaixz Team", Version._VERSION, NativeJXLImageReaderSpi.NAMES, NativeJXLImageReaderSpi.SUFFIXES,
                NativeJXLImageReaderSpi.MIMES, writer.getName(), new Class[] { ImageOutputStream.class },
                new String[] { NativeJXLImageReaderSpi.class.getName() }, false, null, null, null, null, false, null,
                null, null, null);
    }

    @Override
    public boolean canEncodeImage(ImageTypeSpecifier type) {
        return NativeJPEGImageWriterSpi.checkCommonJpgRequirement(type);
    }

    @Override
    public String getDescription(Locale locale) {
        return "Natively-accelerated JPEG XL Image Writer";
    }

    @Override
    public ImageWriter createWriterInstance(Object extension) {
        return new NativeJXLImageWriter(this);
    }

}
