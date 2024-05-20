/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.image.plugin;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;
import org.miaixz.bus.image.nimble.BufferedImages;
import org.miaixz.bus.image.nimble.reader.NativeDCMImageReader;
import org.miaixz.bus.logger.Logger;

import javax.imageio.*;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Iterator;

/**
 * DCM-JPG转换
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Dcm2Jpg {

    private final ImageReader imageReader =
            ImageIO.getImageReadersByFormatName("DICOM").next();
    private String suffix;
    private int frame = 1;
    private int windowIndex;
    private int voiLUTIndex;
    private boolean preferWindow = true;
    private float windowCenter;
    private float windowWidth;
    private boolean autoWindowing = true;
    private Attributes prState;
    private ImageWriter imageWriter;
    private ImageWriteParam imageWriteParam;
    private int overlayActivationMask = 0xffff;
    private int overlayGrayscaleValue = 0xffff;

    private static int parseHex(String s) throws InternalException {
        try {
            return Integer.parseInt(s, Normal._16);
        } catch (NumberFormatException e) {
            throw new InternalException(e.getMessage());
        }
    }

    private static Attributes loadDicomObject(File f) throws IOException {
        if (null == f)
            return null;
        ImageInputStream dis = new ImageInputStream(f);
        try {
            return dis.readDataset(-1, -1);
        } finally {
            IoKit.close(dis);
        }
    }

    public static void listSupportedImageWriters(String format) {
        Logger.info(MessageFormat.format("Supported Image Writers for format: {0}", format));
        Iterator<ImageWriter> it = ImageIO.getImageWritersByFormatName(format);
        while (it.hasNext()) {
            ImageWriter writer = it.next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            Logger.info(MessageFormat.format("\\n{0}\\:\\\n" +
                            "\\n   canWriteCompressed\\: {1}\\\n" +
                            "\\n  canWriteProgressive\\: {2}\\\n" +
                            "\\n        canWriteTiles\\: {3}\\\n" +
                            "\\n       canOffsetTiles\\: {4}\\\n" +
                            "\\n    Compression Types\\: {5}",
                    writer.getClass().getName(),
                    param.canWriteCompressed(),
                    param.canWriteProgressive(),
                    param.canWriteTiles(),
                    param.canOffsetTiles(),
                    param.canWriteCompressed()
                            ? Arrays.toString(param.getCompressionTypes())
                            : null));
        }
    }

    public static void listSupportedFormats() {
        Logger.info(
                MessageFormat.format("Supported output image formats: {0}",
                        Arrays.toString(ImageIO.getWriterFormatNames())));
    }

    public void initImageWriter(String formatName, String suffix,
                                String clazz, String compressionType, Number quality) {
        Iterator<ImageWriter> imageWriters =
                ImageIO.getImageWritersByFormatName(formatName);
        if (!imageWriters.hasNext())
            throw new IllegalArgumentException(
                    MessageFormat.format("output image format: {0} not supported",
                            formatName));
        this.suffix = null != suffix ? suffix : formatName.toLowerCase();
        imageWriter = imageWriters.next();
        if (null != clazz)
            while (!clazz.equals(imageWriter.getClass().getName()))
                if (imageWriters.hasNext())
                    imageWriter = imageWriters.next();
                else
                    throw new IllegalArgumentException(
                            MessageFormat.format("no Image Writer: {0} for format {1} found",
                                    clazz, formatName));
        imageWriteParam = imageWriter.getDefaultWriteParam();
        if (null != compressionType || null != quality) {
            imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            if (null != compressionType)
                imageWriteParam.setCompressionType(compressionType);
            if (null != quality)
                imageWriteParam.setCompressionQuality(quality.floatValue());
        }
    }

    public final void setFrame(int frame) {
        this.frame = frame;
    }

    public final void setWindowCenter(float windowCenter) {
        this.windowCenter = windowCenter;
    }

    public final void setWindowWidth(float windowWidth) {
        this.windowWidth = windowWidth;
    }

    public final void setWindowIndex(int windowIndex) {
        this.windowIndex = windowIndex;
    }

    public final void setVOILUTIndex(int voiLUTIndex) {
        this.voiLUTIndex = voiLUTIndex;
    }

    public final void setPreferWindow(boolean preferWindow) {
        this.preferWindow = preferWindow;
    }

    public final void setAutoWindowing(boolean autoWindowing) {
        this.autoWindowing = autoWindowing;
    }

    public final void setPresentationState(Attributes prState) {
        this.prState = prState;
    }

    public void setOverlayActivationMask(int overlayActivationMask) {
        this.overlayActivationMask = overlayActivationMask;
    }

    public void setOverlayGrayscaleValue(int overlayGrayscaleValue) {
        this.overlayGrayscaleValue = overlayGrayscaleValue;
    }

    private void mconvert(File src, File dest) {
        if (src.isDirectory()) {
            dest.mkdir();
            for (File file : src.listFiles())
                mconvert(file, new File(dest,
                        file.isFile() ? suffix(file) : file.getName()));
            return;
        }
        if (dest.isDirectory())
            dest = new File(dest, suffix(src));
        try {
            convert(src, dest);
            Logger.info(
                    MessageFormat.format("{0} -> {1}",
                            src, dest));
        } catch (Exception e) {
            Logger.error(
                    MessageFormat.format("Failed to convert {0}: {1}",
                            src, e.getMessage()));
            throw new InternalException(e);
        }
    }

    public void convert(File src, File dest) throws IOException {
        javax.imageio.stream.ImageInputStream iis = ImageIO.createImageInputStream(src);
        try {
            BufferedImage bi = readImage(iis);
            bi = convert(bi);
            dest.delete();
            ImageOutputStream ios = ImageIO.createImageOutputStream(dest);
            try {
                writeImage(ios, bi);
            } finally {
                try {
                    ios.close();
                } catch (IOException ignore) {
                }
            }
        } finally {
            try {
                iis.close();
            } catch (IOException e) {
                throw new InternalException(e);
            }
        }
    }

    private BufferedImage convert(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        return cm.getNumComponents() == 3 ? BufferedImages.convertToIntRGB(bi) : bi;
    }

    private BufferedImage readImage(javax.imageio.stream.ImageInputStream iis) throws IOException {
        imageReader.setInput(iis);
        return imageReader.read(frame - 1, readParam());
    }

    private ImageReadParam readParam() {
        NativeDCMImageReader.NativeDCMImageReadParam param =
                (NativeDCMImageReader.NativeDCMImageReadParam) imageReader.getDefaultReadParam();
        param.setWindowCenter(windowCenter);
        param.setWindowWidth(windowWidth);
        param.setAutoWindowing(autoWindowing);
        param.setWindowIndex(windowIndex);
        param.setVOILUTIndex(voiLUTIndex);
        param.setPreferWindow(preferWindow);
        param.setPresentationState(prState);
        param.setOverlayActivationMask(overlayActivationMask);
        param.setOverlayGrayscaleValue(overlayGrayscaleValue);
        return param;
    }

    private void writeImage(ImageOutputStream ios, BufferedImage bi)
            throws IOException {
        imageWriter.setOutput(ios);
        imageWriter.write(null, new IIOImage(bi, null, null), imageWriteParam);
    }

    private String suffix(File src) {
        return src.getName() + Symbol.C_DOT + suffix;
    }

}
