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
package org.miaixz.bus.image.nimble;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOInvalidTreeException;

import org.w3c.dom.Node;

import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.VR;

/**
 * Represents the BufferedImages type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class BufferedImages {

    /**
     * Creates a new instance.
     */
    private BufferedImages() {
        // No initialization required.
    }

    /**
     * Executes the convert to int rgb operation.
     *
     * @param bi the bi.
     * @return the operation result.
     */
    public static BufferedImage convertToIntRGB(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        if (cm instanceof DirectColorModel)
            return bi;

        if (cm.getNumComponents() != 3)
            throw new IllegalArgumentException("ColorModel: " + cm);

        WritableRaster raster = bi.getRaster();
        if (cm instanceof PaletteColorModel)
            return ((PaletteColorModel) cm).convertToIntDiscrete(raster);

        BufferedImage intRGB = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics graphics = intRGB.getGraphics();
        try {
            graphics.drawImage(bi, 0, 0, null);
        } finally {
            graphics.dispose();
        }
        return intRGB;
    }

    /**
     * Executes the convert yb rto rgb operation.
     *
     * @param src the src.
     * @param dst the dst.
     * @return the operation result.
     */
    public static BufferedImage convertYBRtoRGB(BufferedImage src, BufferedImage dst) {
        if (src.getColorModel().getTransferType() != DataBuffer.TYPE_BYTE) {
            throw new UnsupportedOperationException("Cannot convert color model to RGB: unsupported transferType"
                    + src.getColorModel().getTransferType());
        }
        if (src.getColorModel().getNumComponents() != 3) {
            throw new IllegalArgumentException("Unsupported colorModel: " + src.getColorModel());
        }

        int width = src.getWidth();
        int height = src.getHeight();
        if (dst == null) {
            ColorModel cmodel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
                    new int[] { 8, 8, 8 }, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
            SampleModel sampleModel = cmodel.createCompatibleSampleModel(width, height);
            DataBuffer dataBuffer = sampleModel.createDataBuffer();
            WritableRaster rasterDst = Raster.createWritableRaster(sampleModel, dataBuffer, null);
            dst = new BufferedImage(cmodel, rasterDst, false, null);
        }
        WritableRaster rasterDst = dst.getRaster();
        WritableRaster raster = src.getRaster();
        ColorSpace cs = src.getColorModel().getColorSpace();
        ColorSpace dstcs = dst.getColorModel().getColorSpace();
        byte[] ba = new byte[3];
        float[] fba = new float[3];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                raster.getDataElements(x, y, ba);
                for (int i = 0; i < 3; i++) {
                    fba[i] = (ba[i] & 0xff) / 255f;
                }
                float[] rgb = cs.toRGB(fba);
                float[] color = dstcs.fromRGB(rgb);
                for (int i = 0; i < 3; i++) {
                    ba[i] = (byte) (color[i] * 255 + 0.5f);
                }
                rasterDst.setDataElements(x, y, ba);
            }
        }
        return dst;
    }

    /**
     * Executes the convert paletteto rgb operation.
     *
     * @param src the src.
     * @param dst the dst.
     * @return the operation result.
     */
    public static BufferedImage convertPalettetoRGB(BufferedImage src, BufferedImage dst) {
        ColorModel pcm = src.getColorModel();
        if (!(pcm instanceof PaletteColorModel || pcm instanceof IndexColorModel)) {
            throw new UnsupportedOperationException("Cannot convert " + pcm.getClass().getName() + " to RGB");
        }

        int width = src.getWidth();
        int height = src.getHeight();
        if (dst == null) {
            ColorModel cmodel = new ComponentColorModel(pcm.getColorSpace(), new int[] { 8, 8, 8 }, false, false,
                    Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
            SampleModel sampleModel = cmodel.createCompatibleSampleModel(width, height);
            DataBuffer dataBuffer = sampleModel.createDataBuffer();
            WritableRaster rasterDst = Raster.createWritableRaster(sampleModel, dataBuffer, null);
            dst = new BufferedImage(cmodel, rasterDst, false, null);
        }
        WritableRaster rasterDst = dst.getRaster();
        WritableRaster raster = src.getRaster();
        byte[] b = new byte[3];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int rgb = pcm.getRGB(raster.getSample(j, i, 0));
                b[0] = (byte) ((rgb >> 16) & 0xff);
                b[1] = (byte) ((rgb >> 8) & 0xff);
                b[2] = (byte) (rgb & 0xff);
                rasterDst.setDataElements(j, i, b);
            }
        }
        return dst;
    }

    /**
     * Executes the convert shorts to bytes operation.
     *
     * @param src the src.
     * @param dst the dst.
     * @return the operation result.
     */
    public static BufferedImage convertShortsToBytes(BufferedImage src, BufferedImage dst) {
        if (dst == null) {
            dst = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        }
        DataBuffer srcBuffer = src.getRaster().getDataBuffer();

        if (srcBuffer instanceof DataBufferByte) {
            byte[] srcData = ((DataBufferByte) srcBuffer).getData();
            byte[] dstData = ((DataBufferByte) dst.getRaster().getDataBuffer()).getData();
            System.arraycopy(srcData, 0, dstData, 0, srcData.length);
            return dst;
        }

        short[] srcData = srcBuffer instanceof DataBufferUShort ? ((DataBufferUShort) srcBuffer).getData()
                : ((DataBufferShort) srcBuffer).getData();
        byte[] dstData = ((DataBufferByte) dst.getRaster().getDataBuffer()).getData();
        for (int i = 0; i < srcData.length; i++) {
            dstData[i] = (byte) srcData[i];
        }
        return dst;
    }

    /**
     * Executes the replace color model operation.
     *
     * @param bi         the bi.
     * @param colorModel the color model.
     * @return the operation result.
     */
    public static BufferedImage replaceColorModel(BufferedImage bi, ColorModel colorModel) {
        return new BufferedImage(colorModel, bi.getRaster(), false, null);
    }

    /**
     * Executes the convert color operation.
     *
     * @param bi         the bi.
     * @param colorModel the color model.
     * @return the operation result.
     */
    public static BufferedImage convertColor(BufferedImage bi, ColorModel colorModel) {
        BufferedImage dest = new BufferedImage(colorModel, Raster
                .createWritableRaster(colorModel.createCompatibleSampleModel(bi.getWidth(), bi.getHeight()), null),
                false, null);
        new ColorConvertOp(null).filter(bi, dest);
        return dest;
    }

    /**
     * Set Image Pixel Module Attributes from Buffered Image. Supports Buffered Images with ColorSpace GRAY or RGB and
     * with a DataBuffer containing one bank of unsigned byte data.
     *
     * @param bi    Buffered Image
     * @param attrs Data Set to supplement with Image Pixel Module Attributes or {@code null}
     * @return Data Set with included Image Pixel Module Attributes
     * @throws UnsupportedOperationException if the ColorSpace or DataBuffer of the Buffered Image is not supported
     */
    public static Attributes toImagePixelModule(BufferedImage bi, Attributes attrs) {
        ColorModel cm = bi.getColorModel();
        if (cm instanceof IndexColorModel || cm instanceof PaletteColorModel) {
            bi = convertPalettetoRGB(bi, null);
            cm = bi.getColorModel();
        }
        ColorSpace cs = cm.getColorSpace();
        Raster raster = bi.getRaster();
        int rows = raster.getHeight();
        int columns = raster.getWidth();
        switch (cs.getType()) {
            case ColorSpace.TYPE_GRAY:
                return toImagePixelModule(1, "MONOCHROME2", rows, columns, toMonochrome2PixelData(raster), attrs);

            case ColorSpace.TYPE_RGB:
                return toImagePixelModule(3, "RGB", rows, columns, toRGBPixelData(raster), attrs);

            default:
                throw new UnsupportedOperationException(toString(cs));
        }
    }

    /**
     * Set Image Pixel Module Attributes from read image. Supports reading animated GIFs and single frame images with
     * ColorSpace GRAY or RGB and with a DataBuffer containing one bank of unsigned byte data.
     *
     * @param reader image reader
     * @param attrs  Data Set to supplement with Image Pixel Module Attributes or {@code null}.
     * @return Data Set with included Image Pixel Module Attributes.
     * @throws IOException                   if an error occurs during reading.
     * @throws UnsupportedOperationException if the read image is not supported.
     */
    public static Attributes toImagePixelModule(ImageReader reader, Attributes attrs) throws IOException {
        IIOImage firstFrame = reader.readAll(0, null);
        BufferedImage bi = (BufferedImage) firstFrame.getRenderedImage();
        IIOImage nextFrame;
        try {
            nextFrame = reader.readAll(1, null);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException("JDK-7132728: stringtable overflow in GIFImageReader", e);
        } catch (IndexOutOfBoundsException e) {
            return toImagePixelModule(bi, attrs);
        }
        List<byte[]> frames = new ArrayList<>();
        List<String> delayTimes = new ArrayList<>();
        BufferedImage rgb = convertPalettetoRGB(bi, null);
        frames.add(toRGBPixelData(rgb.getRaster()));
        delayTimes.add(getDelayTime(getMetadata(firstFrame)));
        Graphics graphics = bi.getGraphics();
        try {
            while (true) {
                Node metadata = getMetadata(nextFrame);
                mergeFrame(graphics, (BufferedImage) nextFrame.getRenderedImage(), metadata);
                frames.add(toRGBPixelData(convertPalettetoRGB(bi, rgb).getRaster()));
                delayTimes.add(getDelayTime(metadata));
                nextFrame = reader.readAll(frames.size(), null);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException("JDK-7132728: stringtable overflow in GIFImageReader", e);
        } catch (IndexOutOfBoundsException ignore) {
        }
        graphics.dispose();
        attrs = toImagePixelModule(3, "RGB", bi.getHeight(), bi.getWidth(), toPixeldata(frames), attrs);
        attrs.setInt(Tag.NumberOfFrames, VR.IS, frames.size());
        setFrameTimeVector(attrs, delayTimes);
        return attrs;
    }

    /**
     * Executes the merge frame operation.
     *
     * @param graphics the graphics.
     * @param src      the src.
     * @param metadata the metadata.
     * @throws IIOInvalidTreeException if the operation cannot be completed.
     */
    private static void mergeFrame(Graphics graphics, BufferedImage src, Node metadata) throws IIOInvalidTreeException {
        Node imageDescriptor = getChildNode(metadata, "ImageDescriptor");
        graphics.drawImage(
                src,
                getIntAttribute(imageDescriptor, "imageLeftPosition"),
                getIntAttribute(imageDescriptor, "imageTopPosition"),
                null);
    }

    /**
     * Gets the metadata.
     *
     * @param iioImage the iio image.
     * @return the metadata.
     */
    private static Node getMetadata(IIOImage iioImage) {
        return iioImage.getMetadata().getAsTree("javax_imageio_gif_image_1.0");
    }

    /**
     * Gets the delay time.
     *
     * @param node the node.
     * @return the delay time.
     * @throws IIOInvalidTreeException if the operation cannot be completed.
     */
    private static String getDelayTime(Node node) throws IIOInvalidTreeException {
        return getStringAttribute(getChildNode(node, "GraphicControlExtension"), "delayTime");
    }

    /**
     * Gets the string attribute.
     *
     * @param node the node.
     * @param name the name.
     * @return the string attribute.
     * @throws IIOInvalidTreeException if the operation cannot be completed.
     */
    private static String getStringAttribute(Node node, String name) throws IIOInvalidTreeException {
        Node attr = node.getAttributes().getNamedItem(name);
        if (attr == null) {
            throw new IIOInvalidTreeException("Required attribute " + name + " not present!", node);
        }
        return attr.getNodeValue();
    }

    /**
     * Gets the int attribute.
     *
     * @param node the node.
     * @param name the name.
     * @return the int attribute.
     * @throws IIOInvalidTreeException if the operation cannot be completed.
     */
    private static int getIntAttribute(Node node, String name) throws IIOInvalidTreeException {
        try {
            return Integer.parseInt(getStringAttribute(node, name));
        } catch (NumberFormatException e) {
            throw new IIOInvalidTreeException("Bad value for " + node.getNodeName() + " attribute " + name + "!", node);
        }
    }

    /**
     * Gets the child node.
     *
     * @param root the root.
     * @param name the name.
     * @return the child node.
     * @throws IIOInvalidTreeException if the operation cannot be completed.
     */
    private static Node getChildNode(Node root, String name) throws IIOInvalidTreeException {
        Node child = root.getFirstChild();
        while (child != null) {
            if (name.equals(child.getLocalName())) {
                return child;
            }
            child = child.getNextSibling();
        }
        throw new IIOInvalidTreeException("Required child " + name + " not present!", root);
    }

    /**
     * Converts this value to pixeldata.
     *
     * @param frames the frames.
     * @return the operation result.
     */
    private static byte[] toPixeldata(List<byte[]> frames) {
        byte[] pixeldata = new byte[frames.get(0).length * frames.size()];
        int pos = 0;
        for (byte[] frame : frames) {
            System.arraycopy(frame, 0, pixeldata, pos, frame.length);
            pos += frame.length;
        }
        return pixeldata;
    }

    /**
     * Sets the frame time vector.
     *
     * @param attrs      the attrs.
     * @param delayTimes the delay times.
     */
    private static void setFrameTimeVector(Attributes attrs, List<String> delayTimes) {
        String delayTime0 = delayTimes.get(0);
        for (int i = 1; i < delayTimes.size(); i++) {
            if (!delayTime0.equals(delayTimes.get(i))) {
                attrs.setString(Tag.FrameTimeVector, VR.DS, toFrameTimes(delayTimes));
                attrs.setInt(Tag.FrameIncrementPointer, VR.AT, Tag.FrameTimeVector);
            }
        }
        attrs.setString(Tag.FrameTime, VR.DS, toFrameTime(delayTime0));
        attrs.setInt(Tag.FrameIncrementPointer, VR.AT, Tag.FrameTimeVector);
    }

    /**
     * Converts this value to frame times.
     *
     * @param delayTimes the delay times.
     * @return the operation result.
     */
    private static String[] toFrameTimes(List<String> delayTimes) {
        String[] frameTimes = new String[delayTimes.size()];
        for (int i = 0; i < frameTimes.length; i++) {
            frameTimes[i] = toFrameTime(delayTimes.get(i));
        }
        return frameTimes;
    }

    /**
     * Converts this value to frame time.
     *
     * @param delayTime the delay time.
     * @return the operation result.
     */
    private static String toFrameTime(String delayTime) {
        return "0".equals(delayTime) ? "0" : (delayTime + "0");
    }

    /**
     * Returns the string representation.
     *
     * @param cs the cs.
     * @return the string representation.
     */
    private static String toString(ColorSpace cs) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0, n = cs.getNumComponents(); i < n; i++) {
            sb.append(i > 0 ? ", 0" : "ColorSpace[").append(cs.getName(i));
        }
        return sb.append(']').toString();
    }

    /**
     * Converts this value to monochrome2 pixel data.
     *
     * @param raster the raster.
     * @return the operation result.
     */
    private static byte[] toMonochrome2PixelData(Raster raster) {
        ComponentSampleModel csb = getComponentSampleModel(raster);
        int pixelStride = csb.getPixelStride();
        int scanlineStride = csb.getScanlineStride();
        int h = csb.getHeight();
        int w = csb.getWidth();
        int offset = csb.getOffset(0, 0);
        byte[] src = getData(raster);
        byte[] dest = new byte[h * w];
        for (int y = 0, j = 0; y < h; y++) {
            for (int x = 0, i = y * scanlineStride; x < w; x++, i += pixelStride) {
                dest[j++] = src[i + offset];
            }
        }
        return dest;
    }

    /**
     * Converts this value to rgb pixel data.
     *
     * @param raster the raster.
     * @return the operation result.
     */
    private static byte[] toRGBPixelData(Raster raster) {
        byte[] dest = new byte[raster.getHeight() * raster.getWidth() * 3];
        copyRGBPixelDataTo(raster, dest, 0);
        return dest;
    }

    /**
     * Copies the rgb pixel data to.
     *
     * @param raster the raster.
     * @param dest   the dest.
     * @param offset the offset.
     */
    private static void copyRGBPixelDataTo(Raster raster, byte[] dest, int offset) {
        ComponentSampleModel csb = getComponentSampleModel(raster);
        int pixelStride = csb.getPixelStride();
        int scanlineStride = csb.getScanlineStride();
        int h = csb.getHeight();
        int w = csb.getWidth();
        int r = csb.getOffset(0, 0, 0);
        int g = csb.getOffset(0, 0, 1);
        int b = csb.getOffset(0, 0, 2);
        byte[] src = getData(raster);
        for (int y = 0, j = offset; y < h; y++) {
            for (int x = 0, i = y * scanlineStride; x < w; x++, i += pixelStride) {
                dest[j++] = src[i + r];
                dest[j++] = src[i + g];
                dest[j++] = src[i + b];
            }
        }
    }

    /**
     * Gets the data.
     *
     * @param raster the raster.
     * @return the data.
     */
    private static byte[] getData(Raster raster) {
        DataBuffer dataBuffer = raster.getDataBuffer();
        if (!(dataBuffer instanceof DataBufferByte) || dataBuffer.getNumBanks() > 1) {
            throw new UnsupportedOperationException(raster.toString());
        }
        return ((DataBufferByte) dataBuffer).getData();
    }

    /**
     * Gets the component sample model.
     *
     * @param raster the raster.
     * @return the component sample model.
     */
    private static ComponentSampleModel getComponentSampleModel(Raster raster) {
        SampleModel sb = raster.getSampleModel();
        if (!(sb instanceof ComponentSampleModel)) {
            throw new UnsupportedOperationException(sb.toString());
        }
        return (ComponentSampleModel) sb;
    }

    /**
     * Converts this value to image pixel module.
     *
     * @param samples   the samples.
     * @param pmi       the pmi.
     * @param rows      the rows.
     * @param columns   the columns.
     * @param pixelData the pixel data.
     * @param attrs     the attrs.
     * @return the operation result.
     */
    private static Attributes toImagePixelModule(
            int samples,
            String pmi,
            int rows,
            int columns,
            byte[] pixelData,
            Attributes attrs) {
        if (attrs == null) {
            attrs = new Attributes(13);
        }
        attrs.setInt(Tag.SamplesPerPixel, VR.US, samples);
        attrs.setString(Tag.PhotometricInterpretation, VR.CS, pmi);
        if (samples > 1) {
            attrs.setInt(Tag.PlanarConfiguration, VR.US, 0);
        }
        attrs.setInt(Tag.Rows, VR.US, rows);
        attrs.setInt(Tag.Columns, VR.US, columns);
        attrs.setInt(Tag.BitsAllocated, VR.US, 8);
        attrs.setInt(Tag.BitsStored, VR.US, 8);
        attrs.setInt(Tag.HighBit, VR.US, 7);
        attrs.setInt(Tag.PixelRepresentation, VR.US, 0);
        attrs.setBytes(Tag.PixelData, VR.OB, pixelData);
        return attrs;
    }

}
