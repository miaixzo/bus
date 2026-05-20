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
package org.miaixz.bus.image.nimble.opencv.op;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import org.miaixz.bus.image.nimble.opencv.lut.ByteLut;

/**
 * Utility methods for BGR byte lookup tables.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class ByteLutCollection {

    /**
     * The BGR bands value.
     */
    private static final int BGR_BANDS = 3;

    /**
     * The standard LUT size value.
     */
    private static final int STANDARD_LUT_SIZE = 256;

    /**
     * The blue channel value.
     */
    private static final int BLUE_CHANNEL = 0;

    /**
     * The green channel value.
     */
    private static final int GREEN_CHANNEL = 1;

    /**
     * The red channel value.
     */
    private static final int RED_CHANNEL = 2;

    /**
     * The RGB components value.
     */
    private static final int RGB_COMPONENTS = 3;

    /**
     * The whitespace pattern value.
     */
    private static final String WHITESPACE_PATTERN = "\\s+";

    /**
     * Creates a new instance.
     */
    private ByteLutCollection() {
        // No initialization required.
    }

    /**
     * Executes the invert operation.
     *
     * @param lut the LUT.
     * @return the operation result.
     */
    public static byte[][] invert(byte[][] lut) {
        if (lut == null) {
            return null;
        }
        validateLutFormat(lut);
        byte[][] inverted = new byte[lut.length][lut[0].length];
        for (int band = 0; band < lut.length; band++) {
            for (int i = 0; i < lut[band].length; i++) {
                inverted[band][i] = lut[band][lut[band].length - 1 - i];
            }
        }
        return inverted;
    }

    /**
     * Reads the LUT files from resources dir.
     *
     * @param lutEntries the LUT entries.
     * @param lutFolder  the LUT folder.
     */
    public static void readLutFilesFromResourcesDir(List<ByteLut> lutEntries, Path lutFolder) {
        Objects.requireNonNull(lutEntries, "LUT list cannot be null");
        if (lutFolder == null || !Files.isDirectory(lutFolder)) {
            return;
        }
        try (var paths = Files.walk(lutFolder, 1)) {
            paths.filter(Files::isRegularFile).filter(Files::isReadable).forEach(path -> loadLutFile(lutEntries, path));
            lutEntries.sort(Comparator.comparing(ByteLut::name));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read LUT directory: " + lutFolder, e);
        }
    }

    /**
     * Reads the LUT file.
     *
     * @param scanner the scanner.
     * @return the operation result.
     */
    public static byte[][] readLutFile(Scanner scanner) {
        Objects.requireNonNull(scanner, "Scanner cannot be null");
        byte[][] lut = new byte[BGR_BANDS][STANDARD_LUT_SIZE];
        int lineIndex = 0;

        while (scanner.hasNext() && lineIndex < STANDARD_LUT_SIZE) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            if (parseLutLine(line, lut, lineIndex)) {
                lineIndex++;
            }
        }
        fillRemainingEntries(lut, lineIndex);
        return lut;
    }

    /**
     * Loads the LUT file.
     *
     * @param lutEntries the LUT entries.
     * @param filePath   the file path.
     */
    private static void loadLutFile(List<ByteLut> lutEntries, Path filePath) {
        try (var scanner = new Scanner(filePath, StandardCharsets.UTF_8)) {
            lutEntries.add(new ByteLut(nameWithoutExtension(filePath.getFileName().toString()), readLutFile(scanner)));
        } catch (Exception e) {
            throw new IllegalStateException("Cannot read LUT file: " + filePath, e);
        }
    }

    /**
     * Parses the LUT line.
     *
     * @param line      the line.
     * @param lut       the LUT.
     * @param lineIndex the line index.
     * @return true if the LUT line condition is true; otherwise false.
     */
    private static boolean parseLutLine(String line, byte[][] lut, int lineIndex) {
        String[] components = line.split(WHITESPACE_PATTERN);
        if (components.length != RGB_COMPONENTS) {
            return false;
        }
        try {
            lut[RED_CHANNEL][lineIndex] = parseColorComponent(components[0]);
            lut[GREEN_CHANNEL][lineIndex] = parseColorComponent(components[1]);
            lut[BLUE_CHANNEL][lineIndex] = parseColorComponent(components[2]);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Parses the color component.
     *
     * @param component the component.
     * @return the operation result.
     */
    private static byte parseColorComponent(String component) {
        int value = Integer.parseInt(component.trim());
        return (byte) Math.max(0, Math.min(255, value));
    }

    /**
     * Validates the LUT format.
     *
     * @param lut the LUT.
     */
    private static void validateLutFormat(byte[][] lut) {
        if (lut.length == 0) {
            throw new IllegalArgumentException("LUT must have at least one band");
        }
        int expectedLength = lut[0].length;
        for (byte[] band : lut) {
            if (band == null || band.length != expectedLength) {
                throw new IllegalArgumentException("All LUT bands must have the same length");
            }
        }
    }

    /**
     * Executes the remaining entries operation.
     *
     * @param lut        the LUT.
     * @param startIndex the start index.
     */
    private static void fillRemainingEntries(byte[][] lut, int startIndex) {
        for (int band = 0; band < BGR_BANDS; band++) {
            byte fillValue = startIndex > 0 ? lut[band][startIndex - 1] : 0;
            for (int i = startIndex; i < STANDARD_LUT_SIZE; i++) {
                lut[band][i] = startIndex > 0 ? fillValue : (byte) i;
            }
        }
    }

    /**
     * Executes the name without extension operation.
     *
     * @param name the name.
     * @return the operation result.
     */
    private static String nameWithoutExtension(String name) {
        int index = name.lastIndexOf('.');
        return index > 0 ? name.substring(0, index) : name;
    }

}
