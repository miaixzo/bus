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
package org.miaixz.bus.image.galaxy.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.spi.FileTypeDetector;

import org.miaixz.bus.core.xyz.StreamKit;

/**
 * A {@link FileTypeDetector} implementation that detects Genozip compressed files. It checks for the Genozip magic
 * number (0x27 0x05 0x20 0x12) at the beginning of the file.
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
public class GenozipFileDetector extends FileTypeDetector {

    /**
     * The MIME type for Genozip files.
     */
    public final static String APPLICATION_VND_GENOZIP = "application/vnd.genozip";

    @Override
    public String probeContentType(Path path) throws IOException {
        byte[] b = new byte[4];
        try (InputStream in = Files.newInputStream(path)) {
            return StreamKit.readAvailable(in, b, 0, 4) == 4 && b[0] == 0x27 && b[1] == 0x05 && b[2] == 0x20
                    && b[3] == 0x12 ? APPLICATION_VND_GENOZIP : null;
        }
    }

}
