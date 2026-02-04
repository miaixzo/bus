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

import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.xyz.StreamKit;

/**
 * A {@link FileTypeDetector} implementation that detects Bzip2 compressed files. It checks for the Bzip2 magic number
 * (0x42 0x5A 0x68) at the beginning of the file. It also handles specific file extensions for VCF Bzip2 files.
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
public class Bzip2FileDetector extends FileTypeDetector {

    /**
     * Checks if the given filename ends with any of the specified extensions (case-insensitive).
     * 
     * @param filename The name of the file.
     * @param exts     An array of extensions to check against.
     * @return {@code true} if the filename ends with one of the extensions, {@code false} otherwise.
     */
    private static boolean endsWith(String filename, String... exts) {
        for (String ext : exts) {
            if (filename.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String probeContentType(Path path) throws IOException {
        byte[] b = new byte[3];
        try (InputStream in = Files.newInputStream(path)) {
            return StreamKit.readAvailable(in, b, 0, 3) == 3 && b[0] == 0x42 && b[1] == 0x5A && b[2] == 0x68
                    ? endsWith(path.getFileName().toString().toLowerCase(), ".vcf.bz2", ".vcfbzip2", ".vcfbz2")
                            ? MediaType.APPLICATION_PRS_VCFBZIP
                            : MediaType.APPLICATION_X_BZIP2
                    : null;
        }
    }

}
