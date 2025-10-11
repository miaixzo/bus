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
package org.miaixz.bus.spring.banner;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.logger.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Generates an ASCII art banner from an image file.
 * <p>
 * This class converts a given image into an ASCII art representation, optionally applying color and inversion. It uses
 * luminance and color distance calculations to map image pixels to ASCII characters and ANSI colors.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ImageBanner {

    private static final double RED_WEIGHT = 0.2126d;
    private static final double GREEN_WEIGHT = 0.7152d;
    private static final double BLUE_WEIGHT = 0.0722d;

    private final File image;
    private final Map<String, Color> colors = new HashMap<>();

    /**
     * Constructs an {@code ImageBanner} with the specified image file.
     *
     * @param image The image file to convert to ASCII art.
     * @throws RuntimeException if the image file is not found or is null.
     */
    public ImageBanner(File image) {
        if (null == image || !image.exists()) {
            throw new RuntimeException("Image not found !");
        }
        this.image = image;
        colorsInit();
    }

    /**
     * Calculates the luminance of a color.
     *
     * @param color   The color to calculate luminance for.
     * @param inverse If {@code true}, calculates inverse luminance.
     * @return The luminance value (0-100).
     */
    private static int getLuminance(Color color, boolean inverse) {
        double red = color.getRed();
        double green = color.getGreen();
        double blue = color.getBlue();

        double luminance;

        if (inverse) {
            luminance = (RED_WEIGHT * (255.0d - red)) + (GREEN_WEIGHT * (255.0d - green))
                    + (BLUE_WEIGHT * (255.0d - blue));
        } else {
            luminance = (RED_WEIGHT * red) + (GREEN_WEIGHT * green) + (BLUE_WEIGHT * blue);
        }

        return (int) Math.ceil((luminance / 255.0d) * 100);
    }

    /**
     * Gets an ASCII character representation based on the color's luminance.
     *
     * @param color The color to convert.
     * @param dark  If {@code true}, uses a dark background character set.
     * @return An ASCII character representing the color.
     */
    private static char getAsciiCharacter(Color color, boolean dark) {
        double luminance = getLuminance(color, dark);

        if (luminance >= 90) {
            return Symbol.C_SPACE;
        } else if (luminance >= 80) {
            return Symbol.C_DOT;
        } else if (luminance >= 70) {
            return Symbol.C_STAR;
        } else if (luminance >= 60) {
            return Symbol.C_COLON;
        } else if (luminance >= 50) {
            return 'o';
        } else if (luminance >= 40) {
            return Symbol.C_AND;
        } else if (luminance >= 30) {
            return Symbol.C_EIGHT;
        } else if (luminance >= 20) {
            return Symbol.C_HASH;
        } else {
            return Symbol.C_AT;
        }
    }

    /**
     * Calculates the Euclidean color distance between two colors.
     *
     * @param color1 The first color.
     * @param color2 The second color.
     * @return The Euclidean distance.
     */
    private static double getColorDistance(Color color1, Color color2) {
        double redDelta = (color1.getRed() - color2.getRed()) * RED_WEIGHT;
        double greenDelta = (color1.getGreen() - color2.getGreen()) * GREEN_WEIGHT;
        double blueDelta = (color1.getBlue() - color2.getBlue()) * BLUE_WEIGHT;

        return Math.pow(redDelta, 2.0d) + Math.pow(greenDelta, 2.0d) + Math.pow(blueDelta, 2.0d);
    }

    /**
     * Resizes a {@link BufferedImage} to fit within a maximum width while maintaining aspect ratio.
     *
     * @param sourceImage The image to resize.
     * @param maxWidth    The maximum width for the resized image.
     * @param aspectRatio The aspect ratio to apply during resizing.
     * @return The resized {@link BufferedImage}.
     */
    private static BufferedImage resizeImage(BufferedImage sourceImage, int maxWidth, double aspectRatio) {
        int width;
        double resizeRatio;
        if (sourceImage.getWidth() > maxWidth) {
            resizeRatio = (double) maxWidth / (double) sourceImage.getWidth();
            width = maxWidth;
        } else {
            resizeRatio = 1.0d;
            width = sourceImage.getWidth();
        }

        int height = (int) (Math.ceil(resizeRatio * aspectRatio * (double) sourceImage.getHeight()));
        Image image = sourceImage.getScaledInstance(width, height, Image.SCALE_DEFAULT);

        BufferedImage resizedImage = new BufferedImage(image.getWidth(null), image.getHeight(null),
                BufferedImage.TYPE_INT_RGB);

        resizedImage.getGraphics().drawImage(image, 0, 0, null);
        return resizedImage;
    }

    /**
     * Calculates the CIE94 color distance between two colors.
     *
     * @param color1 The first color.
     * @param color2 The second color.
     * @return The CIE94 color distance.
     */
    private static double getColorDistanceCIE94(final Color color1, final Color color2) {
        // Convert to L*a*b* color space
        float[] lab1 = toLab(color1);
        float[] lab2 = toLab(color2);

        // Make it more readable
        double L1 = lab1[0];
        double a1 = lab1[1];
        double b1 = lab1[2];
        double L2 = lab2[0];
        double a2 = lab2[1];
        double b2 = lab2[2];

        // CIE94 coefficients for graphic arts
        double kL = 1;
        double K1 = 0.045;
        double K2 = 0.015;
        // Weighting factors
        double sl = 1.0;
        double kc = 1.0;
        double kh = 1.0;

        double c1 = Math.sqrt(a1 * a1 + b1 * b1);
        double deltaC = c1 - Math.sqrt(a2 * a2 + b2 * b2);
        double deltaA = a1 - a2;
        double deltaB = b1 - b2;
        double deltaH = Math.sqrt(Math.max(0.0, deltaA * deltaA + deltaB * deltaB - deltaC * deltaC));

        return Math.sqrt(
                Math.max(
                        0.0,
                        Math.pow((L1 - L2) / (kL * sl), 2) + Math.pow(deltaC / (kc * (1 + K1 * c1)), 2)
                                + Math.pow(deltaH / (kh * (1 + K2 * c1)), 2.0)));
    }

    /**
     * Converts a {@link Color} to its CIE L*a*b* values.
     *
     * @param color The color to convert.
     * @return An array of float representing L*a*b* values.
     */
    static float[] toLab(Color color) {
        float[] xyz = color.getColorComponents(ColorSpace.getInstance(ColorSpace.CS_CIEXYZ), null);

        return xyzToLab(xyz);
    }

    /**
     * Converts CIE XYZ values to CIE L*a*b* values.
     *
     * @param colorvalue An array of float representing XYZ values.
     * @return An array of float representing L*a*b* values.
     */
    static float[] xyzToLab(float[] colorvalue) {
        double l = f(colorvalue[1]);
        double L = 116.0 * l - 16.0;
        double a = 500.0 * (f(colorvalue[0]) - l);
        double b = 200.0 * (l - f(colorvalue[2]));
        return new float[] { (float) L, (float) a, (float) b };
    }

    /**
     * Helper function for CIE L*a*b* conversion.
     *
     * @param t The input value.
     * @return The calculated value.
     */
    private static double f(double t) {
        if (t > 216.0 / 24389.0) {
            return Math.cbrt(t);
        } else {
            return (1.0 / 3.0) * Math.pow(29.0 / 6.0, 2) * t + (4.0 / 29.0);
        }
    }

    /**
     * Initializes the map of ANSI color names to their RGB color values.
     */
    private void colorsInit() {
        this.colors.put("BLACK", new Color(0, 0, 0));
        this.colors.put("RED", new Color(170, 0, 0));
        this.colors.put("GREEN", new Color(0, 170, 0));
        this.colors.put("YELLOW", new Color(170, 85, 0));
        this.colors.put("BLUE", new Color(0, 0, 170));
        this.colors.put("MAGENTA", new Color(170, 0, 170));
        this.colors.put("CYAN", new Color(0, 170, 170));
        this.colors.put("WHITE", new Color(170, 170, 170));

        this.colors.put("BRIGHT_BLACK", new Color(85, 85, 85));
        this.colors.put("BRIGHT_RED", new Color(255, 85, 85));
        this.colors.put("BRIGHT_GREEN", new Color(85, 255, 85));
        this.colors.put("BRIGHT_YELLOW", new Color(255, 255, 85));
        this.colors.put("BRIGHT_BLUE", new Color(85, 85, 255));
        this.colors.put("BRIGHT_MAGENTA", new Color(255, 85, 255));
        this.colors.put("BRIGHT_CYAN", new Color(85, 255, 255));
        this.colors.put("BRIGHT_WHITE", new Color(255, 255, 255));
    }

    /**
     * Generates an ASCII art banner from the image.
     *
     * @param maxWidth    The maximum width of the generated banner.
     * @param aspectRatio The aspect ratio to use for resizing the image.
     * @param invert      If {@code true}, inverts the colors for the ASCII conversion.
     * @param cie94       If {@code true}, uses CIE94 color distance for color matching; otherwise, uses Euclidean
     *                    distance.
     * @return The ASCII art representation of the image, or an empty string if an error occurs.
     */
    public String printBanner(Integer maxWidth, Double aspectRatio, boolean invert, boolean cie94) {
        String headlessProperty = System.getProperty("java.awt.headless");
        String banner = Normal.EMPTY;
        try {
            System.setProperty("java.awt.headless", "true");
            BufferedImage sourceImage = ImageIO.read(new FileInputStream(this.image));
            BufferedImage resizedImage = resizeImage(sourceImage, maxWidth, aspectRatio);
            banner = imageToBanner(resizedImage, invert, cie94);
        } catch (Exception ex) {
            Logger.warn(
                    "WARNING ! Image banner not printable: " + this.image + " (" + ex.getClass() + ": '"
                            + ex.getMessage() + "')");
        } finally {
            if (null != headlessProperty) {
                System.setProperty("java.awt.headless", headlessProperty);
            }
        }
        return banner;
    }

    /**
     * Converts a {@link BufferedImage} to its ASCII art representation.
     *
     * @param image The image to convert.
     * @param dark  If {@code true}, uses a dark background for the banner.
     * @param cie94 If {@code true}, uses CIE94 color distance for color matching.
     * @return The ASCII art string.
     */
    private String imageToBanner(BufferedImage image, boolean dark, boolean cie94) {
        StringBuilder banner = new StringBuilder();

        for (int y = 0; y < image.getHeight(); y++) {
            if (dark) {
                banner.append("${Ansi.Color.BG_BLACK}");
            } else {
                banner.append("${Ansi.Color.BG_DEFAULT}");
            }
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = new Color(image.getRGB(x, y), false);
                banner.append(getFormatString(color, dark, cie94));
            }
            if (dark) {
                banner.append("${Ansi.Color.DEFAULT}");
            }
            banner.append("${Ansi.Color.DEFAULT}\n");
        }

        return banner.toString();
    }

    /**
     * Gets the formatted ANSI string for a given color.
     *
     * @param color The color to format.
     * @param dark  If {@code true}, uses a dark background.
     * @param cie94 If {@code true}, uses CIE94 color distance for matching.
     * @return The ANSI formatted string with the closest color and ASCII character.
     */
    protected String getFormatString(Color color, boolean dark, boolean cie94) {
        String matchedColorName = null;
        Double minColorDistance = null;

        for (Entry<String, Color> colorOption : this.colors.entrySet()) {
            double distance;
            if (cie94) {
                distance = getColorDistanceCIE94(color, colorOption.getValue());
            } else {
                distance = getColorDistance(color, colorOption.getValue());
            }

            if (null == minColorDistance || distance < minColorDistance) {
                minColorDistance = distance;
                matchedColorName = colorOption.getKey();
            }
        }

        return "${Ansi.Color." + matchedColorName + Symbol.BRACE_RIGHT + getAsciiCharacter(color, dark);
    }

}
