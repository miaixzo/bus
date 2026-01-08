/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.plugin;

import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;
import org.miaixz.bus.image.nimble.ICCProfile;
import org.miaixz.bus.image.nimble.reader.ImageioReadParam;

import javax.imageio.*;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

/**
 * The {@code Dcm2Jpg} class provides functionality to convert DICOM images to other image formats like JPEG, PNG, etc.,
 * using the ImageIO API. It allows specifying various options for the conversion, such as frame selection, windowing,
 * and output format settings.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Dcm2Jpg {

    /**
     * The ImageReader for DICOM format.
     */
    private final ImageReader imageReader = ImageIO.getImageReadersByFormatName("DICOM").next();
    /**
     * Functional interface for the image reading process.
     */
    private ReadImage readImage;
    /**
     * The file suffix for the output image.
     */
    private String suffix;
    /**
     * The frame number to extract (1-based).
     */
    private int frame = 1;
    /**
     * The index of the VOI window to apply.
     */
    private int windowIndex;
    /**
     * The index of the VOI LUT to apply.
     */
    private int voiLUTIndex;
    /**
     * A flag indicating whether to prefer windowing over VOI LUTs.
     */
    private boolean preferWindow = true;
    /**
     * The window center for grayscale rendering.
     */
    private float windowCenter;
    /**
     * The window width for grayscale rendering.
     */
    private float windowWidth;
    /**
     * A flag to enable or disable auto-windowing.
     */
    private boolean autoWindowing = true;
    /**
     * A flag to ignore the Presentation LUT Shape attribute.
     */
    private boolean ignorePresentationLUTShape;
    /**
     * The DICOM Presentation State object.
     */
    private Attributes prState;
    /**
     * The ImageWriter for the target format.
     */
    private ImageWriter imageWriter;
    /**
     * The write parameters for the ImageWriter.
     */
    private ImageWriteParam imageWriteParam;
    /**
     * A bitmask to control the activation of overlays.
     */
    private int overlayActivationMask = 0xffff;
    /**
     * The grayscale value to use for rendering overlays.
     */
    private int overlayGrayscaleValue = 0xffff;
    /**
     * The RGB value to use for rendering overlays.
     */
    private int overlayRGBValue = 0xffffff;
    /**
     * The ICC Profile option to apply.
     */
    private ICCProfile.Option iccProfile = ICCProfile.Option.none;

    /**
     * Creates a predicate to match a writer based on its class name.
     *
     * @param clazz The class name to match. Can be a fully qualified name or end with '*' for prefix matching.
     * @return A predicate that tests if an object's class name matches the given name.
     */
    private static Predicate<Object> matchClassName(String clazz) {
        Predicate<String> predicate = clazz.endsWith("*") ? startsWith(clazz.substring(0, clazz.length() - 1))
                : clazz::equals;
        return w -> predicate.test(w.getClass().getName());
    }

    /**
     * Creates a predicate that checks if a string starts with a given prefix.
     *
     * @param prefix The prefix to check for.
     * @return A predicate for the startsWith check.
     */
    private static Predicate<String> startsWith(String prefix) {
        return s -> s.startsWith(prefix);
    }

    /**
     * Loads a DICOM dataset from a file.
     *
     * @param f The file to load from.
     * @return The loaded DICOM attributes.
     * @throws IOException if an I/O error occurs.
     */
    private static Attributes loadDicomObject(File f) throws IOException {
        if (f == null)
            return null;
        ImageInputStream dis = new ImageInputStream(f);
        try {
            return dis.readDataset();
        } finally {
            IoKit.close(dis);
        }
    }

    /**
     * Initializes the ImageWriter for the specified output format.
     *
     * @param formatName      The name of the target format (e.g., "jpeg").
     * @param suffix          The file suffix for the output file.
     * @param clazz           The class name of the specific ImageWriter to use.
     * @param compressionType The compression type for the writer.
     * @param quality         The compression quality (0.0-1.0).
     * @throws IllegalArgumentException if no writer is found for the given format or class.
     */
    public void initImageWriter(
            String formatName,
            String suffix,
            String clazz,
            String compressionType,
            Number quality) {
        this.suffix = suffix != null ? suffix : formatName.toLowerCase();
        Iterator<ImageWriter> imageWriters = ImageIO.getImageWritersByFormatName(formatName);
        if (!imageWriters.hasNext())
            throw new IllegalArgumentException(formatName);
        Iterable<ImageWriter> iterable = () -> imageWriters;
        imageWriter = StreamSupport.stream(iterable.spliterator(), false).filter(matchClassName(clazz)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(StringKit.format(clazz, formatName)));
        imageWriteParam = imageWriter.getDefaultWriteParam();
        if (compressionType != null || quality != null) {
            imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            if (compressionType != null)
                imageWriteParam.setCompressionType(compressionType);
            if (quality != null)
                imageWriteParam.setCompressionQuality(quality.floatValue());
        }
    }

    /**
     * Sets the frame number to be extracted from a multi-frame DICOM image.
     *
     * @param frame The 1-based frame number.
     */
    public final void setFrame(int frame) {
        this.frame = frame;
    }

    /**
     * Sets the window center for grayscale value mapping.
     *
     * @param windowCenter The window center value.
     */
    public final void setWindowCenter(float windowCenter) {
        this.windowCenter = windowCenter;
    }

    /**
     * Sets the window width for grayscale value mapping.
     *
     * @param windowWidth The window width value.
     */
    public final void setWindowWidth(float windowWidth) {
        this.windowWidth = windowWidth;
    }

    /**
     * Sets the index of the VOI window to apply from the DICOM object.
     *
     * @param windowIndex The 0-based index of the window.
     */
    public final void setWindowIndex(int windowIndex) {
        this.windowIndex = windowIndex;
    }

    /**
     * Sets the index of the VOI LUT to apply from the DICOM object.
     *
     * @param voiLUTIndex The 0-based index of the VOI LUT.
     */
    public final void setVOILUTIndex(int voiLUTIndex) {
        this.voiLUTIndex = voiLUTIndex;
    }

    /**
     * Sets the preference for using windowing over VOI LUTs when both are present.
     *
     * @param preferWindow True to prefer windowing, false otherwise.
     */
    public final void setPreferWindow(boolean preferWindow) {
        this.preferWindow = preferWindow;
    }

    /**
     * Enables or disables automatic windowing calculation.
     *
     * @param autoWindowing True to enable auto-windowing, false to disable.
     */
    public final void setAutoWindowing(boolean autoWindowing) {
        this.autoWindowing = autoWindowing;
    }

    /**
     * Checks if the Presentation LUT Shape is ignored.
     *
     * @return True if the Presentation LUT Shape is ignored.
     */
    public boolean isIgnorePresentationLUTShape() {
        return ignorePresentationLUTShape;
    }

    /**
     * Sets whether to ignore the Presentation LUT Shape attribute.
     *
     * @param ignorePresentationLUTShape True to ignore, false to use.
     */
    public void setIgnorePresentationLUTShape(boolean ignorePresentationLUTShape) {
        this.ignorePresentationLUTShape = ignorePresentationLUTShape;
    }

    /**
     * Sets the Presentation State attributes to be applied to the image.
     *
     * @param prState The DICOM attributes of the Presentation State object.
     */
    public final void setPresentationState(Attributes prState) {
        this.prState = prState;
    }

    /**
     * Sets the bitmask for activating overlays.
     *
     * @param overlayActivationMask The overlay activation bitmask.
     */
    public void setOverlayActivationMask(int overlayActivationMask) {
        this.overlayActivationMask = overlayActivationMask;
    }

    /**
     * Sets the grayscale value for rendering overlays.
     *
     * @param overlayGrayscaleValue The 16-bit grayscale value.
     */
    public void setOverlayGrayscaleValue(int overlayGrayscaleValue) {
        this.overlayGrayscaleValue = overlayGrayscaleValue;
    }

    /**
     * Sets the RGB color value for rendering overlays.
     *
     * @param overlayRGBValue The 24-bit RGB color value.
     */
    public void setOverlayRGBValue(int overlayRGBValue) {
        this.overlayRGBValue = overlayRGBValue;
    }

    /**
     * Sets the ICC Profile to be applied to the image.
     *
     * @param iccProfile The ICC Profile option.
     */
    public final void setICCProfile(ICCProfile.Option iccProfile) {
        this.iccProfile = Objects.requireNonNull(iccProfile);
    }

    /**
     * Sets the image reading strategy.
     *
     * @param readImage The functional interface implementation for reading the image.
     */
    public final void setReadImage(ReadImage readImage) {
        this.readImage = readImage;
    }

    /**
     * Recursively converts a source directory or a single file.
     *
     * @param src  The source file or directory.
     * @param dest The destination file or directory.
     */
    private void mconvert(File src, File dest) {
        if (src.isDirectory()) {
            dest.mkdir();
            for (File file : src.listFiles())
                mconvert(file, new File(dest, file.isFile() ? suffix(file) : file.getName()));
            return;
        }
        if (dest.isDirectory())
            dest = new File(dest, suffix(src));
        try {
            convert(src, dest);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    /**
     * Converts a single DICOM file to an image file.
     *
     * @param src  The source DICOM file.
     * @param dest The destination image file.
     * @throws IOException if an I/O error occurs.
     */
    public void convert(File src, File dest) throws IOException {
        writeImage(dest, iccProfile.adjust(readImage.apply(src)));
    }

    /**
     * Reads a {@link BufferedImage} from a DICOM file using a standard {@link javax.imageio.stream.ImageInputStream}.
     *
     * @param file The DICOM file.
     * @return The read image.
     * @throws IOException if an I/O error occurs.
     */
    public BufferedImage readImageFromImageInputStream(File file) throws IOException {
        try (javax.imageio.stream.ImageInputStream iis = new FileImageInputStream(file)) {
            imageReader.setInput(iis);
            return imageReader.read(frame - 1, readParam());
        }
    }

    /**
     * Reads a {@link BufferedImage} from a DICOM file using a {@link org.miaixz.bus.image.galaxy.io.ImageInputStream}.
     *
     * @param file The DICOM file.
     * @return The read image.
     * @throws IOException if an I/O error occurs.
     */
    public BufferedImage readImageFromDicomInputStream(File file) throws IOException {
        try (ImageInputStream dis = new ImageInputStream(file)) {
            imageReader.setInput(dis);
            return imageReader.read(frame - 1, readParam());
        }
    }

    /**
     * Creates and configures an {@link ImageReadParam} with the current settings.
     *
     * @return The configured image read parameters.
     */
    private ImageReadParam readParam() {
        ImageioReadParam param = (ImageioReadParam) imageReader.getDefaultReadParam();
        param.setWindowCenter(windowCenter);
        param.setWindowWidth(windowWidth);
        param.setAutoWindowing(autoWindowing);
        param.setIgnorePresentationLUTShape(ignorePresentationLUTShape);
        param.setWindowIndex(windowIndex);
        param.setVOILUTIndex(voiLUTIndex);
        param.setPreferWindow(preferWindow);
        param.setPresentationState(prState);
        param.setOverlayActivationMask(overlayActivationMask);
        param.setOverlayGrayscaleValue(overlayGrayscaleValue);
        param.setOverlayRGBValue(overlayRGBValue);
        return param;
    }

    /**
     * Writes a {@link BufferedImage} to a file.
     *
     * @param dest The destination file.
     * @param bi   The image to write.
     * @throws IOException if an I/O error occurs.
     */
    private void writeImage(File dest, BufferedImage bi) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(dest, "rw")) {
            raf.setLength(0);
            imageWriter.setOutput(new FileImageOutputStream(raf));
            imageWriter.write(null, new IIOImage(bi, null, null), imageWriteParam);
        }
    }

    /**
     * Generates the output filename with the configured suffix.
     *
     * @param src The source file.
     * @return The new filename string.
     */
    private String suffix(File src) {
        return src.getName() + '.' + suffix;
    }

    /**
     * A functional interface for defining how to read a {@link BufferedImage} from a DICOM file.
     */
    private interface ReadImage {

        /**
         * Reads a {@link BufferedImage} from a file.
         *
         * @param src The source file.
         * @return The read image.
         * @throws IOException if an I/O error occurs.
         */
        BufferedImage apply(File src) throws IOException;
    }

}
