/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import javax.imageio.*;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;

import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;
import org.miaixz.bus.image.nimble.ICCProfile;
import org.miaixz.bus.image.nimble.reader.ImageioReadParam;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class Dcm2Jpg {

    private final ImageReader imageReader = ImageIO.getImageReadersByFormatName("DICOM").next();
    private ReadImage readImage;
    private String suffix;
    private int frame = 1;
    private int windowIndex;
    private int voiLUTIndex;
    private boolean preferWindow = true;
    private float windowCenter;
    private float windowWidth;
    private boolean autoWindowing = true;
    private boolean ignorePresentationLUTShape;
    private Attributes prState;
    private ImageWriter imageWriter;
    private ImageWriteParam imageWriteParam;
    private int overlayActivationMask = 0xffff;
    private int overlayGrayscaleValue = 0xffff;
    private int overlayRGBValue = 0xffffff;
    private ICCProfile.Option iccProfile = ICCProfile.Option.none;

    private static Predicate<Object> matchClassName(String clazz) {
        Predicate<String> predicate = clazz.endsWith("*") ? startsWith(clazz.substring(0, clazz.length() - 1))
                : clazz::equals;
        return w -> predicate.test(w.getClass().getName());
    }

    private static Predicate<String> startsWith(String prefix) {
        return s -> s.startsWith(prefix);
    }

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

    public void initImageWriter(String formatName, String suffix, String clazz, String compressionType,
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

    public boolean isIgnorePresentationLUTShape() {
        return ignorePresentationLUTShape;
    }

    public void setIgnorePresentationLUTShape(boolean ignorePresentationLUTShape) {
        this.ignorePresentationLUTShape = ignorePresentationLUTShape;
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

    public void setOverlayRGBValue(int overlayRGBValue) {
        this.overlayRGBValue = overlayRGBValue;
    }

    public final void setICCProfile(ICCProfile.Option iccProfile) {
        this.iccProfile = Objects.requireNonNull(iccProfile);
    }

    public final void setReadImage(ReadImage readImage) {
        this.readImage = readImage;
    }

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

    public void convert(File src, File dest) throws IOException {
        writeImage(dest, iccProfile.adjust(readImage.apply(src)));
    }

    public BufferedImage readImageFromImageInputStream(File file) throws IOException {
        try (javax.imageio.stream.ImageInputStream iis = new FileImageInputStream(file)) {
            imageReader.setInput(iis);
            return imageReader.read(frame - 1, readParam());
        }
    }

    public BufferedImage readImageFromDicomInputStream(File file) throws IOException {
        try (ImageInputStream dis = new ImageInputStream(file)) {
            imageReader.setInput(dis);
            return imageReader.read(frame - 1, readParam());
        }
    }

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

    private void writeImage(File dest, BufferedImage bi) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(dest, "rw")) {
            raf.setLength(0);
            imageWriter.setOutput(new FileImageOutputStream(raf));
            imageWriter.write(null, new IIOImage(bi, null, null), imageWriteParam);
        }
    }

    private String suffix(File src) {
        return src.getName() + '.' + suffix;
    }

    private interface ReadImage {
        BufferedImage apply(File src) throws IOException;
    }

}
