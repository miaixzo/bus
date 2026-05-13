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

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Represents a wrapper for a file containing raw image data (not compressed with a specific header), providing methods
 * to read and write image data to and from the file.
 * <p>
 * This record provides a type-safe way to handle raw image files with validation and error handling capabilities using
 * modern Java NIO Path API.
 *
 * @param path the path.
 * @author Kimi Liu
 * @since Java 21+
 */
public record FileRawImage(Path path) {

    /**
     * The header length value.
     */
    public static final int HEADER_LENGTH = 46;

    /**
     * Creates a new instance.
     *
     * @param path the path.
     */
    public FileRawImage {
        Objects.requireNonNull(path, "Path cannot be null");
    }

    /**
     * Executes the of operation.
     *
     * @param file the file.
     * @return the operation result.
     */
    public static FileRawImage of(File file) {
        Objects.requireNonNull(file, "File cannot be null");
        return new FileRawImage(file.toPath());
    }

    /**
     * Executes the of operation.
     *
     * @param filePath the file path.
     * @return the operation result.
     */
    public static FileRawImage of(String filePath) {
        if (!StringKit.hasText(filePath)) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }
        return new FileRawImage(Path.of(filePath));
    }

    /**
     * Executes the file operation.
     *
     * @return the operation result.
     */
    public File file() {
        return path.toFile();
    }

    /**
     * Reads the read.
     *
     * @return the operation result.
     */
    public ImageCV read() {
        return ImageIOHandler.readImageWithCvException(path, null);
    }

    /**
     * Reads the safely.
     *
     * @return the operation result.
     */
    public Optional<ImageCV> readSafely() {
        try {
            return Optional.ofNullable(ImageIOHandler.readImageWithCvException(path, null));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Writes the write.
     *
     * @param image the image.
     * @return true if the write condition is true; otherwise false.
     */
    public boolean write(PlanarImage image) {
        Objects.requireNonNull(image, "Image cannot be null");
        return ImageIOHandler.writeImage(image.toMat(), path);
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        return "FileRawImage[path=" + path + "]";
    }

}
