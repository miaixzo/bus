/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.extra.image;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.*;

import org.miaixz.bus.core.io.file.FileType;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.ColorKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Image background recognition, replacement, and vectorization. This class calculates and replaces the background color
 * of an image based on a set of rules.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ImagePlace {

    /**
     * Array of currently supported image types. Results for other formats are not guaranteed.
     */
    public static String[] IMAGES_TYPE = { ImageKit.IMAGE_TYPE_JPG, ImageKit.IMAGE_TYPE_JPEG, ImageKit.IMAGE_TYPE_PNG };

    /**
     * Removes the background from an image with a solid color, making it transparent. It samples pixels from the image
     * edges to determine the background color, then adds a tolerance and sets the alpha of matching pixels to 0.
     *
     * @param inputPath  The path of the image to process.
     * @param outputPath The path for the output image.
     * @param tolerance  The tolerance value (0-255) for color matching.
     */
    public static void backgroundRemoval(final String inputPath, final String outputPath, final int tolerance) {
        backgroundRemoval(new File(inputPath), new File(outputPath), tolerance);
    }

    /**
     * Removes the background from an image with a solid color, making it transparent.
     *
     * @param input     The image file to process.
     * @param output    The final output file.
     * @param tolerance The tolerance value (0-255) for color matching.
     */
    public static void backgroundRemoval(final File input, final File output, final int tolerance) {
        backgroundRemoval(input, output, null, tolerance);
    }

    /**
     * Removes the background from an image, replacing it with a specified color or transparency. The output file must
     * be a .png.
     *
     * @param input     The image file to process.
     * @param output    The final output file (must be .png).
     * @param override  The color to replace the background with. If null, the background becomes transparent.
     * @param tolerance The tolerance value (0-255) for color matching.
     */
    public static void backgroundRemoval(
            final File input,
            final File output,
            final Color override,
            final int tolerance) {
        fileTypeValidation(input, IMAGES_TYPE);
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageKit.read(input);
            // The output image format is png
            ImageKit.write(backgroundRemoval(bufferedImage, override, tolerance), output);
        } finally {
            ImageKit.flush(bufferedImage);
        }
    }

    /**
     * Removes the background from a buffered image, replacing it with a specified color or transparency.
     *
     * @param bufferedImage The image stream to process.
     * @param override      The color to replace the background with. If null, the background becomes transparent.
     * @param tolerance     The tolerance value (0-255) for color matching.
     * @return The processed BufferedImage.
     */
    public static BufferedImage backgroundRemoval(
            final BufferedImage bufferedImage,
            final Color override,
            int tolerance) {
        // Tolerance value: max 255, min 0
        tolerance = Math.min(255, Math.max(tolerance, 0));
        // Draw icon
        final ImageIcon imageIcon = new ImageIcon(bufferedImage);
        final BufferedImage image = new BufferedImage(imageIcon.getIconWidth(), imageIcon.getIconHeight(),
                BufferedImage.TYPE_4BYTE_ABGR);
        // Graphics tool
        final Graphics graphics = image.getGraphics();
        graphics.drawImage(imageIcon.getImage(), 0, 0, imageIcon.getImageObserver());
        // RGB elements to be removed
        final String[] removeRgb = getRemoveRgb(bufferedImage);
        // Get the approximate main color of the image
        final String mainColor = getMainColor(bufferedImage);
        final int alpha = 0;
        for (int y = image.getMinY(); y < image.getHeight(); y++) {
            for (int x = image.getMinX(); x < image.getWidth(); x++) {
                // Get the hexadecimal value of the pixel
                int rgb = image.getRGB(x, y);
                final String hex = ColorKit.toHex((rgb & 0xff0000) >> 16, (rgb & 0xff00) >> 8, (rgb & 0xff));
                final boolean isTrue = ArrayKit.contains(removeRgb, hex) || areColorsWithinTolerance(
                        hexToRgb(mainColor),
                        new Color(Integer.parseInt(hex.substring(1), 16)),
                        tolerance);
                if (isTrue) {
                    rgb = override == null ? ((alpha + 1) << 24) | (rgb & 0x00ffffff) : override.getRGB();
                }
                image.setRGB(x, y, rgb);
            }
        }
        graphics.drawImage(image, 0, 0, imageIcon.getImageObserver());
        return image;
    }

    /**
     * Removes the background from an image provided as a byte array stream.
     *
     * @param outputStream The byte array output stream of the image to process.
     * @param override     The color to replace the background with. If null, the background becomes transparent.
     * @param tolerance    The tolerance value (0-255) for color matching.
     * @return The processed BufferedImage.
     */
    public static BufferedImage backgroundRemoval(
            final ByteArrayOutputStream outputStream,
            final Color override,
            final int tolerance) {
        try {
            return backgroundRemoval(
                    ImageIO.read(new ByteArrayInputStream(outputStream.toByteArray())),
                    override,
                    tolerance);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Gets the RGB elements to be removed by sampling 8 points from the image's border.
     *
     * @param image The image stream.
     * @return A String array containing the hexadecimal RGB values of the sampled points.
     */
    private static String[] getRemoveRgb(final BufferedImage image) {
        // Get the width and height of the image stream
        final int width = image.getWidth() - 1;
        final int height = image.getHeight() - 1;
        // Top-left
        final int leftUpPixel = image.getRGB(1, 1);
        final String leftUp = ColorKit
                .toHex((leftUpPixel & 0xff0000) >> 16, (leftUpPixel & 0xff00) >> 8, (leftUpPixel & 0xff));
        // Top-middle
        final int upMiddlePixel = image.getRGB(width / 2, 1);
        final String upMiddle = ColorKit
                .toHex((upMiddlePixel & 0xff0000) >> 16, (upMiddlePixel & 0xff00) >> 8, (upMiddlePixel & 0xff));
        // Top-right
        final int rightUpPixel = image.getRGB(width, 1);
        final String rightUp = ColorKit
                .toHex((rightUpPixel & 0xff0000) >> 16, (rightUpPixel & 0xff00) >> 8, (rightUpPixel & 0xff));
        // Middle-right
        final int rightMiddlePixel = image.getRGB(width, height / 2);
        final String rightMiddle = ColorKit.toHex(
                (rightMiddlePixel & 0xff0000) >> 16,
                (rightMiddlePixel & 0xff00) >> 8,
                (rightMiddlePixel & 0xff));
        // Bottom-right
        final int lowerRightPixel = image.getRGB(width, height);
        final String lowerRight = ColorKit
                .toHex((lowerRightPixel & 0xff0000) >> 16, (lowerRightPixel & 0xff00) >> 8, (lowerRightPixel & 0xff));
        // Bottom-middle
        final int lowerMiddlePixel = image.getRGB(width / 2, height);
        final String lowerMiddle = ColorKit.toHex(
                (lowerMiddlePixel & 0xff0000) >> 16,
                (lowerMiddlePixel & 0xff00) >> 8,
                (lowerMiddlePixel & 0xff));
        // Bottom-left
        final int leftLowerPixel = image.getRGB(1, height);
        final String leftLower = ColorKit
                .toHex((leftLowerPixel & 0xff0000) >> 16, (leftLowerPixel & 0xff00) >> 8, (leftLowerPixel & 0xff));
        // Middle-left
        final int leftMiddlePixel = image.getRGB(1, height / 2);
        final String leftMiddle = ColorKit
                .toHex((leftMiddlePixel & 0xff0000) >> 16, (leftMiddlePixel & 0xff00) >> 8, (leftMiddlePixel & 0xff));
        // RGB elements to be removed
        return new String[] { leftUp, upMiddle, rightUp, rightMiddle, lowerRight, lowerMiddle, leftLower, leftMiddle };
    }

    /**
     * Converts a hexadecimal color code to an RGB Color object.
     *
     * @param hex The hexadecimal color code (e.g., "#FF0000").
     * @return The corresponding {@link Color} object.
     */
    public static Color hexToRgb(final String hex) {
        return new Color(Integer.parseInt(hex.substring(1), 16));
    }

    /**
     * Determines if two colors are within a given tolerance.
     *
     * @param color1    The first color.
     * @param color2    The second color.
     * @param tolerance The tolerance value.
     * @return {@code true} if the colors are within tolerance, {@code false} otherwise.
     */
    public static boolean areColorsWithinTolerance(final Color color1, final Color color2, final int tolerance) {
        return areColorsWithinTolerance(color1, color2, new Color(tolerance, tolerance, tolerance));
    }

    /**
     * Determines if two colors are within a given tolerance for each RGB component.
     *
     * @param color1    The first color.
     * @param color2    The second color.
     * @param tolerance The tolerance for each RGB component.
     * @return {@code true} if the colors are within tolerance, {@code false} otherwise.
     */
    public static boolean areColorsWithinTolerance(final Color color1, final Color color2, final Color tolerance) {
        return (color1.getRed() - color2.getRed() < tolerance.getRed()
                && color1.getRed() - color2.getRed() > -tolerance.getRed())
                && (color1.getBlue() - color2.getBlue() < tolerance.getBlue()
                        && color1.getBlue() - color2.getBlue() > -tolerance.getBlue())
                && (color1.getGreen() - color2.getGreen() < tolerance.getGreen()
                        && color1.getGreen() - color2.getGreen() > -tolerance.getGreen());
    }

    /**
     * Gets the approximate dominant color of an image by finding the most frequent RGB value.
     *
     * @param input The path to the image file.
     * @return A hexadecimal color code representing the dominant color.
     */
    public static String getMainColor(final String input) {
        return getMainColor(new File(input));
    }

    /**
     * Gets the approximate dominant color of an image by finding the most frequent RGB value.
     *
     * @param input The image file.
     * @return A hexadecimal color code representing the dominant color.
     */
    public static String getMainColor(final File input) {
        try {
            return getMainColor(ImageIO.read(input));
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Gets the approximate dominant color of an image by finding the most frequent RGB value.
     *
     * @param bufferedImage The image stream.
     * @return A hexadecimal color code representing the dominant color.
     */
    public static String getMainColor(final BufferedImage bufferedImage) {
        if (bufferedImage == null) {
            throw new IllegalArgumentException("The image stream is empty");
        }

        // Stores all RGB elements of the image
        final List<String> list = new ArrayList<>();
        for (int y = bufferedImage.getMinY(); y < bufferedImage.getHeight(); y++) {
            for (int x = bufferedImage.getMinX(); x < bufferedImage.getWidth(); x++) {
                final int pixel = bufferedImage.getRGB(x, y);
                list.add(
                        ((pixel & 0xff0000) >> 16) + Symbol.MINUS + ((pixel & 0xff00) >> 8) + Symbol.MINUS
                                + (pixel & 0xff));
            }
        }

        final Map<String, Integer> map = new HashMap<>(list.size(), 1);
        for (final String string : list) {
            Integer integer = map.get(string);
            if (integer == null) {
                integer = 1;
            } else {
                integer++;
            }
            map.put(string, integer);
        }
        String max = Normal.EMPTY;
        long num = 0;
        for (final Map.Entry<String, Integer> entry : map.entrySet()) {
            final String key = entry.getKey();
            final Integer temp = entry.getValue();
            if (StringKit.isBlank(max) || temp > num) {
                max = key;
                num = temp;
            }
        }
        final String[] strings = max.split(Symbol.MINUS);
        // The number of RGB components is 3
        final int rgbLength = 3;
        if (strings.length == rgbLength) {
            return ColorKit
                    .toHex(Integer.parseInt(strings[0]), Integer.parseInt(strings[1]), Integer.parseInt(strings[2]));
        }
        return Normal.EMPTY;
    }

    /**
     * Validates the file type against a list of allowed types.
     *
     * @param input      The file to validate.
     * @param imagesType An array of allowed file types.
     */
    private static void fileTypeValidation(final File input, final String[] imagesType) {
        Assert.isTrue(input.exists(), "File {} not exist!", input);
        // Get the image type
        final String type = FileType.getType(input);
        // Type comparison
        if (!ArrayKit.contains(imagesType, type)) {
            throw new IllegalArgumentException(StringKit.format("Format {} of File not supported!", type));
        }
    }

}
