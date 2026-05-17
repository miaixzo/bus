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
import java.util.*;
import java.util.List;

import org.opencv.core.CvType;

import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.nimble.opencv.ImageCV;
import org.miaixz.bus.image.nimble.opencv.ImageTransformer;
import org.miaixz.bus.image.nimble.opencv.PlanarImage;
import org.miaixz.bus.image.nimble.stream.ImageDescriptor;

/**
 * Represents DICOM overlay data for an image.
 *
 * @param groupOffset      the group offset for the overlay
 * @param rows             the number of rows in the overlay
 * @param columns          the number of columns in the overlay
 * @param imageFrameOrigin the image frame origin (1-based)
 * @param framesInOverlay  the number of frames in the overlay
 * @param origin           the overlay origin [row, column] (1-based)
 * @param data             the overlay pixel data
 * @author Kimi Liu
 * @since Java 21+
 */
public record OverlayData(int groupOffset, int rows, int columns, int imageFrameOrigin, int framesInOverlay,
        int[] origin, byte[] data) {

    /**
     * Returns the overlay data.
     *
     * @param dcm            the DCM.
     * @param activationMask the activation mask.
     * @return the overlay data.
     */
    public static List<OverlayData> getOverlayData(Attributes dcm, int activationMask) {
        return getOverlayData(dcm, activationMask, false);
    }

    /**
     * Returns the overlay data.
     *
     * @param dcm            the DCM.
     * @param activationMask the activation mask.
     * @param pr             the pr.
     * @return the overlay data.
     */
    private static List<OverlayData> getOverlayData(Attributes dcm, int activationMask, boolean pr) {
        List<OverlayData> data = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            int gg0000 = i << 17;
            if ((activationMask & (1 << i)) != 0 && isLayerActivate(dcm, gg0000, pr)) {
                Optional<byte[]> overData = RGBImageVoiLut.getByteData(dcm, Tag.OverlayData | gg0000);
                if (overData.isPresent()) {
                    int rows = dcm.getInt(Tag.OverlayRows | gg0000, 0);
                    int columns = dcm.getInt(Tag.OverlayColumns | gg0000, 0);
                    int imageFrameOrigin = dcm.getInt(Tag.ImageFrameOrigin | gg0000, 1);
                    int framesInOverlay = dcm.getInt(Tag.NumberOfFramesInOverlay | gg0000, 1);
                    int[] origin = Builder
                            .getIntArrayFromDicomElement(dcm, (Tag.OverlayOrigin | gg0000), new int[] { 1, 1 });
                    data.add(
                            new OverlayData(gg0000, rows, columns, imageFrameOrigin, framesInOverlay, origin,
                                    overData.get()));
                }
            }
        }
        return data.isEmpty() ? Collections.emptyList() : data;
    }

    /**
     * Checks whether the layer activate condition is true.
     *
     * @param dcm    the DCM.
     * @param gg0000 the gg 0000.
     * @param pr     the pr.
     * @return true if the layer activate condition is true; otherwise false.
     */
    private static boolean isLayerActivate(Attributes dcm, int gg0000, boolean pr) {
        if (pr) {
            String layerName = dcm.getString(Tag.OverlayActivationLayer | gg0000);
            return layerName != null;
        }
        return true;
    }

    /**
     * Returns the pr overlay data.
     *
     * @param dcm            the DCM.
     * @param activationMask the activation mask.
     * @return the pr overlay data.
     */
    public static List<OverlayData> getPrOverlayData(Attributes dcm, int activationMask) {
        return getOverlayData(dcm, activationMask, true);
    }

    /**
     * Returns the overlay image.
     *
     * @param imageSource  the image source.
     * @param currentImage the current image.
     * @param desc         the desc.
     * @param params       the params.
     * @param frameIndex   the frame index.
     * @return the overlay image.
     */
    public static PlanarImage getOverlayImage(
            final PlanarImage imageSource,
            PlanarImage currentImage,
            ImageDescriptor desc,
            ImageReadParam params,
            int frameIndex) {
        Optional<PresentationLutObject> prDcm = params.getPresentationState();
        List<OverlayData> overlays = new ArrayList<>();
        prDcm.ifPresent(prDicomObject -> overlays.addAll(prDicomObject.getOverlays()));
        List<EmbeddedOverlay> embeddedOverlays = desc.getEmbeddedOverlay();
        overlays.addAll(desc.getOverlayData());

        if (!embeddedOverlays.isEmpty() || !overlays.isEmpty()) {
            int width = currentImage.width();
            int height = currentImage.height();
            if (width == imageSource.width() && height == imageSource.height()) {
                return getOverlayImage(
                        imageSource,
                        currentImage,
                        params,
                        frameIndex,
                        height,
                        width,
                        embeddedOverlays,
                        overlays);
            }
        }
        return currentImage;
    }

    /**
     * Returns the overlay image.
     *
     * @param imageSource      the image source.
     * @param currentImage     the current image.
     * @param params           the params.
     * @param frameIndex       the frame index.
     * @param height           the height.
     * @param width            the width.
     * @param embeddedOverlays the embedded overlays.
     * @param overlays         the overlays.
     * @return the overlay image.
     */
    private static ImageCV getOverlayImage(
            PlanarImage imageSource,
            PlanarImage currentImage,
            ImageReadParam params,
            int frameIndex,
            int height,
            int width,
            List<EmbeddedOverlay> embeddedOverlays,
            List<OverlayData> overlays) {
        ImageCV overlay = new ImageCV(height, width, CvType.CV_8UC1);
        byte[] pixelData = new byte[height * width];
        byte pixVal = (byte) 255;

        for (EmbeddedOverlay data : embeddedOverlays) {
            int mask = 1 << data.bitPosition();
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    double[] pix = imageSource.get(j, i);
                    if ((((int) pix[0]) & mask) != 0) {
                        pixelData[j * width + i] = pixVal;
                    }
                }
            }
        }

        applyOverlay(overlays, pixelData, frameIndex, width);
        overlay.put(0, 0, pixelData);
        return ImageTransformer.overlay(currentImage.toMat(), overlay, params.getOverlayColor().orElse(Color.WHITE));
    }

    /**
     * Applies the overlay.
     *
     * @param overlays   the overlays.
     * @param pixelData  the pixel data.
     * @param frameIndex the frame index.
     * @param width      the width.
     */
    private static void applyOverlay(List<OverlayData> overlays, byte[] pixelData, int frameIndex, int width) {
        byte pixVal = (byte) 255;
        for (OverlayData data : overlays) {
            int imageFrameOrigin = data.imageFrameOrigin();
            int framesInOverlay = data.framesInOverlay();
            int overlayFrameIndex = frameIndex - imageFrameOrigin + 1;
            if (overlayFrameIndex >= 0 && overlayFrameIndex < framesInOverlay) {
                int ovHeight = data.rows();
                int ovWidth = data.columns();
                int ovOff = ovHeight * ovWidth * overlayFrameIndex;
                byte[] pix = data.data();
                int x0 = data.origin()[1] - 1;
                int y0 = data.origin()[0] - 1;
                setOverlayPixelData(ovOff, ovWidth, ovHeight, x0, y0, pix, pixelData, pixVal, width);
            }
        }
    }

    /**
     * Sets the overlay pixel data.
     *
     * @param ovOff     the ov off.
     * @param ovWidth   the ov width.
     * @param ovHeight  the ov height.
     * @param x0        the x 0.
     * @param y0        the y 0.
     * @param pix       the pix.
     * @param pixelData the pixel data.
     * @param pixVal    the pix val.
     * @param width     the width.
     */
    private static void setOverlayPixelData(
            int ovOff,
            int ovWidth,
            int ovHeight,
            int x0,
            int y0,
            byte[] pix,
            byte[] pixelData,
            byte pixVal,
            int width) {
        for (int j = y0; j < ovHeight; j++) {
            for (int i = x0; i < ovWidth; i++) {
                int index = ovOff + (j - y0) * ovWidth + (i - x0);
                int b = pix[index / 8] & 0xff;
                if ((b & (1 << (index % 8))) != 0) {
                    pixelData[j * width + i] = pixVal;
                }
            }
        }
    }

    /**
     * Returns the overlay image.
     *
     * @param imageSource the image source.
     * @param overlays    the overlays.
     * @param frameIndex  the frame index.
     * @return the overlay image.
     */
    public static PlanarImage getOverlayImage(PlanarImage imageSource, List<OverlayData> overlays, int frameIndex) {
        int width = imageSource.width();
        int height = imageSource.height();
        ImageCV overlay = new ImageCV(height, width, CvType.CV_8UC1);
        byte[] pixelData = new byte[height * width];
        applyOverlay(overlays, pixelData, frameIndex, width);
        overlay.put(0, 0, pixelData);
        return overlay;
    }

    /**
     * Executes the equals operation.
     *
     * @param o the o.
     * @return true if the equals condition is true; otherwise false.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        OverlayData that = (OverlayData) o;
        return groupOffset == that.groupOffset && rows == that.rows && columns == that.columns
                && imageFrameOrigin == that.imageFrameOrigin && framesInOverlay == that.framesInOverlay
                && Arrays.equals(origin, that.origin) && Arrays.equals(data, that.data);
    }

    /**
     * Checks whether the hash code condition is true.
     *
     * @return true if the hash code condition is true; otherwise false.
     */
    @Override
    public int hashCode() {
        int result = Objects.hash(groupOffset, rows, columns, imageFrameOrigin, framesInOverlay);
        result = 31 * result + Arrays.hashCode(origin);
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        return "OverlayData{" + "groupOffset=" + groupOffset + ", rows=" + rows + ", columns=" + columns + '}';
    }

}
