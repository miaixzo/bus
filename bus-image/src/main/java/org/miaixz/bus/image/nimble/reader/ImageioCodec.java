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
package org.miaixz.bus.image.nimble.reader;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

import javax.imageio.ImageIO;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * JDK ImageIO codec facade for non-DICOM still image formats.
 *
 * @author Kimi Liu
 */
public class ImageioCodec {

    /**
     * The name value.
     */
    public static final String NAME = "JDK ImageIO";

    /**
     * Creates a new instance.
     */
    public ImageioCodec() {
        // No initialization required.
    }

    /**
     * Gets the reader mime types.
     *
     * @return the reader mime types.
     */
    public static String[] getReaderMIMETypes() {
        return ImageIO.getReaderMIMETypes();
    }

    /**
     * Gets the reader extensions.
     *
     * @return the reader extensions.
     */
    public static String[] getReaderExtensions() {
        return ImageIO.getReaderFileSuffixes();
    }

    /**
     * Gets the writer mime types.
     *
     * @return the writer mime types.
     */
    public static String[] getWriterMIMETypes() {
        return ImageIO.getWriterMIMETypes();
    }

    /**
     * Gets the writer extensions.
     *
     * @return the writer extensions.
     */
    public static String[] getWriterExtensions() {
        return ImageIO.getWriterFileSuffixes();
    }

    /**
     * Determines whether mime type supported.
     *
     * @param mimeType the mime type.
     * @return true if the condition is met; otherwise false.
     */
    public static boolean isMimeTypeSupported(String mimeType) {
        return containsIgnoreCase(getReaderMIMETypes(), mimeType);
    }

    /**
     * Determines whether reader extension supported.
     *
     * @param extension the extension.
     * @return true if the condition is met; otherwise false.
     */
    public static boolean isReaderExtensionSupported(String extension) {
        return containsIgnoreCase(getReaderExtensions(), normalizeExtension(extension));
    }

    /**
     * Determines whether writer extension supported.
     *
     * @param extension the extension.
     * @return true if the condition is met; otherwise false.
     */
    public static boolean isWriterExtensionSupported(String extension) {
        return containsIgnoreCase(getWriterExtensions(), normalizeExtension(extension));
    }

    /**
     * Executes the read operation.
     *
     * @param path the path.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    public static BufferedImage read(Path path) throws IOException {
        return ImageIO.read(Objects.requireNonNull(path, "path").toFile());
    }

    /**
     * Sets the use cache.
     *
     * @param useCache the use cache.
     */
    public static void setUseCache(boolean useCache) {
        ImageIO.setUseCache(useCache);
    }

    /**
     * Executes the disable disk cache operation.
     */
    public static void disableDiskCache() {
        setUseCache(false);
    }

    /**
     * Gets the codec name.
     *
     * @return the codec name.
     */
    public static String getCodecName() {
        return NAME;
    }

    /**
     * Tests whether codec metadata contains the requested value, ignoring case.
     *
     * @param values supported codec values; examined elements must be non-null
     * @param value  the requested value; null or blank returns false without examining metadata
     * @return whether a matching value is found
     * @throws NullPointerException if a non-blank value is supplied and the array or an examined element is null
     */
    private static boolean containsIgnoreCase(String[] values, String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String normalized = StringKit.toLowerCase(value.trim());
        for (String item : values) {
            Objects.requireNonNull(item, "Codec format name must not be null");
            if (normalized.equals(StringKit.toLowerCase(item))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Executes the normalize extension operation.
     *
     * @param extension the extension.
     * @return the operation result.
     */
    private static String normalizeExtension(String extension) {
        if (extension == null) {
            return null;
        }
        String value = extension.trim();
        return value.startsWith(Symbol.DOT) ? value.substring(1) : value;
    }

}
