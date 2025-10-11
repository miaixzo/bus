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
package org.miaixz.bus.core.xyz;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.ansi.*;
import org.miaixz.bus.core.text.CharsBacker;

/**
 * Color utility class.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ColorKit {

    private static final Map<String, Color> COLOR_MAPPING;
    /**
     * Upper bound for RGB color values.
     */
    private static final int RGB_COLOR_BOUND = 256;

    static {
        final Map<String, Color> colorMap = MapKit.builder("BLACK", Color.BLACK).put("WHITE", Color.WHITE)
                .put("LIGHTGRAY", Color.LIGHT_GRAY).put("LIGHT_GRAY", Color.LIGHT_GRAY).put("GRAY", Color.GRAY)
                .put("DARKGRAY", Color.DARK_GRAY).put("DARK_GRAY", Color.DARK_GRAY).put("RED", Color.RED)
                .put("PINK", Color.PINK).put("ORANGE", Color.ORANGE).put("YELLOW", Color.YELLOW)
                .put("GREEN", Color.GREEN).put("MAGENTA", Color.MAGENTA).put("CYAN", Color.CYAN).put("BLUE", Color.BLUE)
                // Dark Gold
                .put("DARKGOLD", hexToColor("#9e7e67")).put("DARK_GOLD", hexToColor("#9e7e67"))
                // Light Gold
                .put("LIGHTGOLD", hexToColor("#ac9c85")).put("LIGHT_GOLD", hexToColor("#ac9c85")).build();
        COLOR_MAPPING = MapKit.view(colorMap);
    }

    /**
     * Converts a Color to its CSS `rgb()` representation, e.g., `rgb(255, 0, 0)`.
     *
     * @param color The AWT color.
     * @return The `rgb(red, green, blue)` string.
     */
    public static String toCssRgb(final Color color) {
        return StringKit.builder().append("rgb(").append(color.getRed()).append(Symbol.COMMA).append(color.getGreen())
                .append(Symbol.COMMA).append(color.getBlue()).append(")").toString();
    }

    /**
     * Converts a Color to its CSS `rgba()` representation, e.g., `rgba(255, 0, 0, 0.5)`.
     *
     * @param color The AWT color.
     * @return The `rgba(red, green, blue, alpha)` string.
     */
    public static String toCssRgba(final Color color) {
        return StringKit.builder().append("rgba(").append(color.getRed()).append(Symbol.COMMA).append(color.getGreen())
                .append(Symbol.COMMA).append(color.getBlue()).append(Symbol.COMMA).append(color.getAlpha() / 255D)
                .append(")").toString();
    }

    /**
     * Converts a {@link Color} object to its hex representation, e.g., `#fcf6d6`.
     *
     * @param color The {@link Color}.
     * @return The hexadecimal color string.
     */
    public static String toHex(final Color color) {
        return toHex(color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * Converts RGB integer values to a hexadecimal color code.
     *
     * @param r Red component (0-255).
     * @param g Green component (0-255).
     * @param b Blue component (0-255).
     * @return The hexadecimal color string (e.g., #FF0000).
     */
    public static String toHex(final int r, final int g, final int b) {
        if (r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255) {
            throw new IllegalArgumentException("RGB values must be between 0 and 255!");
        }
        return String.format("#%02X%02X%02X", r, g, b);
    }

    /**
     * Converts a color string into a {@link Color} object. Supports the following formats:
     * 
     * <pre>
     * 1. English color names (case-insensitive)
     * 2. Hexadecimal format, e.g., `#fcf6d6` or `$fcf6d6`
     * 3. RGB format, e.g., `13,148,252`
     * 4. RGBA format, e.g., `13,148,252,1` or `13,148,252,0.5`
     * </pre>
     *
     * @param colorName The color name, hex string, or RGB(A) string.
     * @return A {@link Color} object.
     */
    public static Color getColor(String colorName) {
        if (StringKit.isBlank(colorName)) {
            return null;
        }
        colorName = colorName.toUpperCase();

        final Color color = COLOR_MAPPING.get(colorName);
        if (null != color) {
            return color;
        }

        if (StringKit.startWith(colorName, Symbol.C_HASH)) {
            return hexToColor(colorName);
        } else if (StringKit.startWith(colorName, Symbol.C_DOLLAR)) {
            // '$' can be used as a substitute for '#' in URLs
            return hexToColor(Symbol.HASH + colorName.substring(1));
        }

        final List<String> rgb = CharsBacker.split(colorName, Symbol.COMMA);
        final int size = rgb.size();

        if (3 == size) {
            final Integer[] rgbIntegers = Convert.toIntArray(rgb);
            return new Color(rgbIntegers[0], rgbIntegers[1], rgbIntegers[2]);
        }
        if (4 == size) {
            final Float[] rgbFloats = Convert.toFloatArray(rgb);
            Float a = rgbFloats[3];
            if (a <= 1) {
                // Handle CSS alpha format (0.0 to 1.0)
                a *= 255;
            }
            return new Color(rgbFloats[0], rgbFloats[1], rgbFloats[2], a);
        }

        return null;
    }

    /**
     * Gets a {@link Color} from a single integer RGB value.
     *
     * @param rgb The RGB value.
     * @return A {@link Color} object.
     */
    public static Color getColor(final int rgb) {
        return new Color(rgb);
    }

    /**
     * Gets a {@link Color} from an RGBA array or a grayscale value.
     *
     * @param gray A single grayscale value (0x0000 black to 0xFFFF white).
     * @param rgba An optional array of RGBA values.
     * @return A {@link Color} object.
     */
    public static Color getColor(int gray, int[] rgba) {
        int r, g, b, a = 255;
        if (rgba != null && rgba.length >= 3) {
            r = Math.min(rgba[0], 255);
            g = Math.min(rgba[1], 255);
            b = Math.min(rgba[2], 255);
            if (rgba.length > 3) {
                a = Math.min(rgba[3], 255);
            }
        } else {
            r = g = b = gray >> 8;
        }
        return new Color(r, g, b, a);
    }

    /**
     * Converts a hexadecimal color string (e.g., `#fcf6d6`) to a `Color` object.
     *
     * @param hex The hexadecimal color string.
     * @return A {@link Color} object.
     */
    public static Color hexToColor(final String hex) {
        return getColor(Integer.parseInt(StringKit.removePrefix(hex, Symbol.HASH), 16));
    }

    /**
     * Blends two colors together based on their alpha values.
     *
     * @param color1 The first color.
     * @param color2 The second color.
     * @return The blended color.
     */
    public static Color add(final Color color1, final Color color2) {
        final double r1 = color1.getRed();
        final double g1 = color1.getGreen();
        final double b1 = color1.getBlue();
        final double a1 = color1.getAlpha();

        final double r2 = color2.getRed();
        final double g2 = color2.getGreen();
        final double b2 = color2.getBlue();
        final double a2 = color2.getAlpha();

        final int r = (int) ((r1 * a1 / 255 + r2 * a2 / 255) / (a1 / 255 + a2 / 255));
        final int g = (int) ((g1 * a1 / 255 + g2 * a2 / 255) / (a1 / 255 + a2 / 255));
        final int b = (int) ((b1 * a1 / 255 + b2 * a2 / 255) / (a1 / 255 + a2 / 255));
        return new Color(r, g, b);
    }

    /**
     * Generates a random color.
     *
     * @return A random color.
     */
    public static Color randomColor() {
        return randomColor(null);
    }

    /**
     * Generates a random color using a specific `Random` instance.
     *
     * @param random The {@link Random} instance.
     * @return A random color.
     */
    public static Color randomColor(Random random) {
        if (null == random) {
            random = RandomKit.getRandom();
        }
        return new Color(random.nextInt(RGB_COLOR_BOUND), random.nextInt(RGB_COLOR_BOUND),
                random.nextInt(RGB_COLOR_BOUND));
    }

    /**
     * Generates a random color that has a minimum distance from a given comparison color.
     *
     * @param compareColor The color to compare against.
     * @param minDistance  The minimum color distance.
     * @return A random color.
     */
    public static Color randomColor(final Color compareColor, final int minDistance) {
        Color color = randomColor();
        if (null != compareColor && minDistance > 0) {
            Assert.isTrue(
                    minDistance < maxDistance(compareColor) * 2 / 3,
                    "minDistance is too large, there are too few remaining colors!");
            while (computeColorDistance(compareColor, color) < minDistance) {
                color = randomColor();
            }
        }
        return color;
    }

    /**
     * Calculates the color distance between two colors using Euclidean distance in the RGB space.
     *
     * @param color1 The first color.
     * @param color2 The second color.
     * @return The color distance value.
     */
    public static int computeColorDistance(final Color color1, final Color color2) {
        if (null == color1 || null == color2) {
            return 0;
        }
        return (int) Math.sqrt(
                Math.pow(color1.getRed() - color2.getRed(), 2) + Math.pow(color1.getGreen() - color2.getGreen(), 2)
                        + Math.pow(color1.getBlue() - color2.getBlue(), 2));
    }

    /**
     * Converts an AWT {@link Color} to the nearest matching ANSI color.
     *
     * @param rgb          The RGB color value.
     * @param is8Bit       If true, uses 8-bit ANSI colors; otherwise, uses 4-bit.
     * @param isBackground If true, returns the background color version.
     * @return The ANSI color element.
     */
    public static AnsiElement toAnsiColor(final int rgb, final boolean is8Bit, final boolean isBackground) {
        return toAnsiColor(getColor(rgb), is8Bit, isBackground);
    }

    /**
     * Converts an AWT {@link Color} to the nearest matching ANSI color.
     *
     * @param color        The {@link Color}.
     * @param is8Bit       If true, uses 8-bit ANSI colors; otherwise, uses 4-bit.
     * @param isBackground If true, returns the background color version.
     * @return The ANSI color element.
     */
    public static AnsiElement toAnsiColor(final Color color, final boolean is8Bit, final boolean isBackground) {
        if (is8Bit) {
            final Ansi8BitColor ansiElement = (Ansi8BitColor) Ansi8bitMapping.INSTANCE.lookupClosest(color);
            if (isBackground) {
                return ansiElement.asBackground();
            }
            return ansiElement;
        } else {
            final Ansi4BitColor ansiElement = (Ansi4BitColor) Ansi4bitMapping.INSTANCE.lookupClosest(color);
            if (isBackground) {
                return ansiElement.asBackground();
            }
            return ansiElement;
        }
    }

    /**
     * Gets the dominant color from a given image.
     *
     * @param image      The {@link BufferedImage}.
     * @param rgbFilters Optional RGB colors to filter out.
     * @return The dominant color as a hex string (e.g., "#ffffff").
     */
    public static String getMainColor(final BufferedImage image, final int[]... rgbFilters) {
        int r, g, b;
        final Map<String, Long> countMap = new HashMap<>();
        final int width = image.getWidth();
        final int height = image.getHeight();
        final int minx = image.getMinX();
        final int miny = image.getMinY();
        for (int i = minx; i < width; i++) {
            for (int j = miny; j < height; j++) {
                final int pixel = image.getRGB(i, j);
                r = (pixel & 0xff0000) >> 16;
                g = (pixel & 0xff00) >> 8;
                b = (pixel & 0xff);
                if (matchFilters(r, g, b, rgbFilters)) {
                    continue;
                }
                countMap.merge(r + Symbol.MINUS + g + Symbol.MINUS + b, 1L, Long::sum);
            }
        }
        String maxColor = null;
        long maxCount = 0;
        for (final Map.Entry<String, Long> entry : countMap.entrySet()) {
            final String key = entry.getKey();
            final Long count = entry.getValue();
            if (count > maxCount) {
                maxColor = key;
                maxCount = count;
            }
        }
        final String[] splitRgbStr = CharsBacker.splitToArray(maxColor, Symbol.MINUS);
        String rHex = Integer.toHexString(Integer.parseInt(splitRgbStr[0]));
        String gHex = Integer.toHexString(Integer.parseInt(splitRgbStr[1]));
        String bHex = Integer.toHexString(Integer.parseInt(splitRgbStr[2]));
        rHex = rHex.length() == 1 ? "0" + rHex : rHex;
        gHex = gHex.length() == 1 ? "0" + gHex : gHex;
        bHex = bHex.length() == 1 ? "0" + bHex : bHex;
        return Symbol.HASH + rHex + gHex + bHex;
    }

    /**
     * Calculates the maximum possible color distance from a given color.
     *
     * @param color The color.
     * @return The maximum possible distance.
     */
    public static int maxDistance(final Color color) {
        final int maxX = RGB_COLOR_BOUND - 2 * color.getRed();
        final int maxY = RGB_COLOR_BOUND - 2 * color.getGreen();
        final int maxZ = RGB_COLOR_BOUND - 2 * color.getBlue();
        return (int) Math.sqrt(maxX * maxX + maxY * maxY + maxZ * maxZ);
    }

    /**
     * Checks if a given RGB value matches any of the RGB filters.
     *
     * @param r          The red component.
     * @param g          The green component.
     * @param b          The blue component.
     * @param rgbFilters The color filters.
     * @return {@code true} if there is a match.
     */
    private static boolean matchFilters(final int r, final int g, final int b, final int[]... rgbFilters) {
        if (ArrayKit.isNotEmpty(rgbFilters)) {
            for (final int[] rgbFilter : rgbFilters) {
                if (r == rgbFilter[0] && g == rgbFilter[1] && b == rgbFilter[2]) {
                    return true;
                }
            }
        }
        return false;
    }

}
