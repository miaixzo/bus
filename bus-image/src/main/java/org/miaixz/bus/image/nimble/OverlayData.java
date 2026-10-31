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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

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
        if (dcm == null) {
            return Collections.emptyList();
        }

        return IntStream.range(0, MAX_OVERLAY_GROUPS).filter(groupIndex -> isGroupActivated(groupIndex, activationMask))
                .mapToObj(groupIndex -> groupIndex << GROUP_OFFSET_SHIFT)
                .filter(groupOffset -> isLayerActivated(dcm, groupOffset, pr))
                .map(groupOffset -> createOverlayData(dcm, groupOffset)).filter(Optional::isPresent).map(Optional::get)
                .toList();
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
     * Applies overlays to the current image and returns the result.
     *
     * @param imageSource  the source image
     * @param currentImage the current processed image
     * @param desc         the image descriptor
     * @param params       the image read parameters
     * @param frameIndex   the frame index
     * @return the image with overlays applied
     */
    public static PlanarImage getOverlayImage(
            final PlanarImage imageSource,
            PlanarImage currentImage,
            ImageDescriptor desc,
            ImageReadParam params,
            int frameIndex) {
        if (imageSource == null || currentImage == null || desc == null || params == null) {
            return currentImage;
        }
        if (!hasSameDimensions(imageSource, currentImage)) {
            return currentImage;
        }
        ImageCV mask = getOverlayMask(imageSource, desc, params, frameIndex);
        if (mask == null) {
            return currentImage;
        }
        try {
            return applyOverlayMask(currentImage, mask, params);
        } finally {
            mask.release();
        }
    }

    /**
     * Creates an overlay image from a list of overlays for a specific frame.
     *
     * @param imageSource the source image
     * @param overlays    list of overlay data
     * @param frameIndex  the frame index
     * @return the overlay image
     */
    public static PlanarImage getOverlayImage(PlanarImage imageSource, List<OverlayData> overlays, int frameIndex) {
        if (imageSource == null || overlays == null || overlays.isEmpty()) {
            return imageSource;
        }
        var dimensions = new ImageDimensions(imageSource.width(), imageSource.height());
        byte[] pixelData = new byte[dimensions.totalPixels()];

        applyRegularOverlays(overlays, pixelData, frameIndex, dimensions.width());

        return createOverlayImage(pixelData, dimensions);
    }

    /**
     * Executes the equals operation.
     *
     * @param o the o.
     * @return true if the equals condition is true; otherwise false.
     */
    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof OverlayData that && groupOffset == that.groupOffset && rows == that.rows
                && columns == that.columns && imageFrameOrigin == that.imageFrameOrigin
                && framesInOverlay == that.framesInOverlay && Arrays.equals(origin, that.origin)
                && Arrays.equals(data, that.data));
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
        return "OverlayData{groupOffset=%d, rows=%d, columns=%d}".formatted(groupOffset, rows, columns);
    }

    /**
     * Maximum number of overlay groups supported (0x6000-0x601E)
     */
    static final int MAX_OVERLAY_GROUPS = 16;

    /**
     * Bit shift value to calculate group offset (17 bits for overlay group addressing)
     */
    static final int GROUP_OFFSET_SHIFT = 17;

    /**
     * Default overlay pixel value (white)
     */
    private static final byte OVERLAY_PIXEL_VALUE = (byte) 255;

    /**
     * Tests an overlay group bit against the requested activation mask.
     *
     * @param groupIndex     the zero-based overlay group index
     * @param activationMask the bit mask selecting enabled overlay groups
     * @return whether the group is enabled
     */
    private static boolean isGroupActivated(int groupIndex, int activationMask) {
        return (activationMask & (1 << groupIndex)) != 0;
    }

    /**
     * Checks whether an overlay is enabled directly or by a presentation-state layer.
     *
     * @param dcm         the image dataset containing the overlay attributes
     * @param groupOffset the offset from overlay group 6000H
     * @param pr          whether activation is controlled by a presentation state
     * @return whether the overlay layer is active
     */
    private static boolean isLayerActivated(Attributes dcm, int groupOffset, boolean pr) {
        return !pr || dcm.getString(Tag.OverlayActivationLayer | groupOffset) != null;
    }

    /**
     * Reads an overlay plane only when its required dimensions and packed bytes are available.
     *
     * @param dcm         the image dataset containing the overlay attributes
     * @param groupOffset the offset from overlay group 6000H
     * @return the decoded overlay, or an empty result for incomplete data
     */
    private static Optional<OverlayData> createOverlayData(Attributes dcm, int groupOffset) {
        var overlayDataBytes = RGBImageVoiLut.getByteData(dcm, Tag.OverlayData | groupOffset);

        if (overlayDataBytes.isEmpty()) {
            return Optional.empty();
        }

        var attributes = extractOverlayAttributes(dcm, groupOffset);

        return Optional.of(
                new OverlayData(groupOffset, attributes.rows(), attributes.columns(), attributes.imageFrameOrigin(),
                        attributes.framesInOverlay(), attributes.origin(), overlayDataBytes.get()));
    }

    /**
     * Reads dimensions, frame mapping and origin for an overlay group.
     *
     * @param dcm         the image dataset containing the overlay attributes
     * @param groupOffset the offset from overlay group 6000H
     * @return the decoded overlay metadata
     */
    private static OverlayAttributes extractOverlayAttributes(Attributes dcm, int groupOffset) {
        int rows = dcm.getInt(Tag.OverlayRows | groupOffset, 0);
        int columns = dcm.getInt(Tag.OverlayColumns | groupOffset, 0);
        int imageFrameOrigin = dcm.getInt(Tag.ImageFrameOrigin | groupOffset, 1);
        int framesInOverlay = dcm.getInt(Tag.NumberOfFramesInOverlay | groupOffset, 1);
        int[] origin = Builder.getIntArrayFromDicomElement(dcm, (Tag.OverlayOrigin | groupOffset), new int[] { 1, 1 });

        return new OverlayAttributes(rows, columns, imageFrameOrigin, framesInOverlay, origin);
    }

    /**
     * Builds the mask of the overlays of a frame: embedded in the pixel data, stored in the image and brought by the
     * presentation state. It does not depend on the rendering of the image, so a caller can keep it while only the
     * window changes.
     *
     * @param imageSource the stored pixel values, read for the embedded overlays
     * @return an 8-bit image, 255 where an overlay is set, or null when the frame has no overlay
     * @param desc       the image metadata and frame-specific LUT state
     * @param params     the image read options
     * @param frameIndex the zero-based image frame index
     */
    public static ImageCV getOverlayMask(
            PlanarImage imageSource,
            ImageDescriptor desc,
            ImageReadParam params,
            int frameIndex) {
        var context = buildOverlayContext(desc, params);
        if (!context.hasOverlays()) {
            return null;
        }
        var dimensions = new ImageDimensions(imageSource.width(), imageSource.height());
        byte[] overlayPixelData = createOverlayPixelData(imageSource, context, frameIndex, dimensions);
        return createOverlayImage(overlayPixelData, dimensions);
    }

    /**
     * Paints a mask from {@link #getOverlayMask} on a rendered image, in the overlay color of the parameters.
     *
     * @return a new image, or {@code currentImage} when the mask does not have its size
     * @param currentImage the rendered image retained by the caller
     * @param mask         the embedded bit mask or image-sized overlay mask
     * @param params       the image read options
     */
    public static PlanarImage applyOverlayMask(PlanarImage currentImage, ImageCV mask, ImageReadParam params) {
        if (!hasSameDimensions(mask, currentImage)) {
            return currentImage;
        }
        var overlayColor = params.getOverlayColor().orElse(Color.WHITE);
        int bits = params.getOutputBits().orElse(8);
        if (bits > 8 && currentImage.type() == CvType.CV_16UC1) {
            return overlayWithinOutputRange(currentImage, mask, overlayColor, Math.min(bits, 16));
        }
        return ImageTransformer.overlay(currentImage.toMat(), mask, overlayColor);
    }

    /**
     * Combines image and presentation-state overlay planes with the current read options.
     *
     * @param desc   the image metadata and frame-specific LUT state
     * @param params the image read options
     * @return the overlay context for the render operation
     */
    private static OverlayContext buildOverlayContext(ImageDescriptor desc, ImageReadParam params) {

        var allOverlays = new ArrayList<>(
                params.getPresentationState().map(PresentationLutObject::getOverlays).orElse(List.of()));

        // Add regular overlays
        allOverlays.addAll(desc.getOverlayData());

        return new OverlayContext(desc.getEmbeddedOverlay(), allOverlays, params);
    }

    /**
     * Compares the pixel dimensions of two image surfaces.
     *
     * @param image1 the first image
     * @param image2 the second image
     * @return whether their widths and heights match
     */
    private static boolean hasSameDimensions(PlanarImage image1, PlanarImage image2) {
        return image1.width() == image2.width() && image1.height() == image2.height();
    }

    /**
     * Paints an overlay using the requested grayscale precision without modifying the source.
     *
     * @param image the image to process
     * @param mask  the embedded bit mask or image-sized overlay mask
     * @param color the overlay display color
     * @param bits  the grayscale output precision
     * @return a new grayscale image with overlay intensities in the configured output range
     */
    private static ImageCV overlayWithinOutputRange(PlanarImage image, ImageCV mask, Color color, int bits) {
        int component = Math.max(color.getRed(), Math.max(color.getGreen(), color.getBlue()));
        var result = new ImageCV();
        image.toMat().copyTo(result);
        result.setTo(new Scalar(component * ((1 << bits) - 1) / 255.0), mask);
        return result;
    }

    /**
     * Builds an 8-bit mask from embedded and standalone overlays for the selected frame.
     *
     * @param imageSource the source image retained by the caller
     * @param context     the combined image and presentation-state overlay context
     * @param frameIndex  the zero-based image frame index
     * @param dimensions  the dimensions of the image-sized output mask
     * @return the row-major overlay mask bytes
     */
    private static byte[] createOverlayPixelData(
            PlanarImage imageSource,
            OverlayContext context,
            int frameIndex,
            ImageDimensions dimensions) {

        byte[] pixelData = new byte[dimensions.totalPixels()];

        context.embeddedOverlays().forEach(overlay -> {
            int mask = 1 << overlay.bitPosition();
            applyEmbeddedOverlayMask(imageSource, mask, pixelData);
        });

        applyRegularOverlays(context.overlays(), pixelData, frameIndex, dimensions.width());

        return pixelData;
    }

    /**
     * Sets mask pixels whose stored sample contains the selected embedded overlay bit.
     *
     * @param imageSource the source image retained by the caller
     * @param mask        the embedded bit mask or image-sized overlay mask
     * @param pixelData   the row-major mask bytes updated in place
     */
    private static void applyEmbeddedOverlayMask(PlanarImage imageSource, int mask, byte[] pixelData) {
        Mat stored = new Mat();
        Mat selected = new Mat();
        try {
            // Embedded overlays belong to single-sample images: only the first channel carries the bit
            Core.extractChannel(imageSource.toMat(), stored, 0);
            if (stored.depth() >= CvType.CV_32F) {
                stored.convertTo(stored, CvType.CV_32S);
            }
            try (ImageCV bit = ImageTransformer.bitwiseAnd(stored, storedBitPattern(mask, stored.depth()))) {
                Core.compare(bit, new Scalar(0), selected, Core.CMP_NE);
            }

            byte[] set = new byte[pixelData.length];
            selected.get(0, 0, set);
            for (int i = 0; i < set.length; i++) {
                if (set[i] != 0) {
                    pixelData[i] = OVERLAY_PIXEL_VALUE;
                }
            }
        } finally {
            stored.release();
            selected.release();
        }
    }

    /**
     * Converts an overlay bit mask to the signed or unsigned value used by the native pixel depth.
     *
     * @param mask  the embedded bit mask or image-sized overlay mask
     * @param depth the native OpenCV sample depth
     * @return the mask represented in the source sample type
     */
    private static int storedBitPattern(int mask, int depth) {
        return switch (depth) {
            case CvType.CV_8S -> (byte) mask;
            case CvType.CV_16S -> (short) mask;
            default -> mask;
        };
    }

    /**
     * Combines standalone overlay planes applicable to the selected image frame.
     *
     * @param overlays   the standalone overlay planes
     * @param pixelData  the row-major mask bytes updated in place
     * @param frameIndex the zero-based image frame index
     * @param imageWidth the output image width in pixels
     */
    private static void applyRegularOverlays(
            List<OverlayData> overlays,
            byte[] pixelData,
            int frameIndex,
            int imageWidth) {

        Objects.requireNonNullElse(overlays, List.<OverlayData>of()).stream()
                .filter(overlay -> isOverlayApplicableToFrame(overlay, frameIndex))
                .forEach(overlay -> applyOverlayToPixelData(overlay, pixelData, frameIndex, imageWidth));
    }

    /**
     * Checks the selected image frame against the overlay frame origin and count.
     *
     * @param overlay    the overlay plane being positioned
     * @param frameIndex the zero-based image frame index
     * @return whether the overlay has data for the image frame
     */
    private static boolean isOverlayApplicableToFrame(OverlayData overlay, int frameIndex) {
        int overlayFrameIndex = frameIndex - overlay.imageFrameOrigin() + 1;
        return overlayFrameIndex >= 0 && overlayFrameIndex < overlay.framesInOverlay();
    }

    /**
     * Positions a standalone overlay frame in the output mask.
     *
     * @param overlay    the overlay plane being positioned
     * @param pixelData  the row-major mask bytes updated in place
     * @param frameIndex the zero-based image frame index
     * @param imageWidth the output image width in pixels
     */
    private static void applyOverlayToPixelData(OverlayData overlay, byte[] pixelData, int frameIndex, int imageWidth) {

        var frameInfo = calculateOverlayFrameInfo(overlay, frameIndex);
        applyOverlayBits(overlay, frameInfo, pixelData, imageWidth);
    }

    /**
     * Calculates the packed-bit offset and zero-based image origin of an overlay frame.
     *
     * @param overlay    the overlay plane being positioned
     * @param frameIndex the zero-based image frame index
     * @return the overlay frame geometry
     */
    private static OverlayFrameInfo calculateOverlayFrameInfo(OverlayData overlay, int frameIndex) {
        int overlayFrameIndex = frameIndex - overlay.imageFrameOrigin() + 1;
        int overlayOffset = overlay.rows() * overlay.columns() * overlayFrameIndex;
        int originX = overlay.origin()[1] - 1; // Convert from 1-based to 0-based
        int originY = overlay.origin()[0] - 1; // Convert from 1-based to 0-based

        return new OverlayFrameInfo(overlayOffset, originX, originY, overlay.rows(), overlay.columns());
    }

    /**
     * Copies active packed overlay bits into the image-sized mask, clipping to its bounds.
     *
     * @param overlay    the overlay plane being positioned
     * @param frameInfo  the selected overlay frame offset and geometry
     * @param pixelData  the row-major mask bytes updated in place
     * @param imageWidth the output image width in pixels
     */
    private static void applyOverlayBits(
            OverlayData overlay,
            OverlayFrameInfo frameInfo,
            byte[] pixelData,
            int imageWidth) {

        byte[] overlayData = overlay.data();
        int endRow = Math.min(frameInfo.originY() + frameInfo.height(), pixelData.length / imageWidth);
        int endCol = Math.min(frameInfo.originX() + frameInfo.width(), imageWidth);

        for (int row = Math.max(0, frameInfo.originY()); row < endRow; row++) {
            for (int col = Math.max(0, frameInfo.originX()); col < endCol; col++) {
                if (isOverlayPixelSet(overlayData, frameInfo, row, col)) {
                    pixelData[row * imageWidth + col] = OVERLAY_PIXEL_VALUE;
                }
            }
        }
    }

    /**
     * Tests a packed bit within the selected overlay frame.
     *
     * @param overlayData the packed overlay frame bits
     * @param frameInfo   the selected overlay frame offset and geometry
     * @param row         the zero-based overlay row
     * @param col         the zero-based overlay column
     * @return whether the overlay pixel is set
     */
    private static boolean isOverlayPixelSet(byte[] overlayData, OverlayFrameInfo frameInfo, int row, int col) {

        int pixelIndex = frameInfo.overlayOffset() + (row - frameInfo.originY()) * frameInfo.width()
                + (col - frameInfo.originX());
        int byteIndex = pixelIndex / 8;
        int bitIndex = pixelIndex % 8;

        return byteIndex < overlayData.length && ((overlayData[byteIndex] & 0xff) & (1 << bitIndex)) != 0;
    }

    /**
     * Copies row-major mask bytes into a newly allocated single-channel image.
     *
     * @param pixelData  the row-major mask bytes updated in place
     * @param dimensions the dimensions of the image-sized output mask
     * @return the new 8-bit overlay mask
     */
    private static ImageCV createOverlayImage(byte[] pixelData, ImageDimensions dimensions) {
        var overlay = new ImageCV(dimensions.height(), dimensions.width(), CvType.CV_8UC1);
        overlay.put(0, 0, pixelData);
        return overlay;
    }

    /**
     * Stores overlay dimensions, frame origin and pixel origin extracted from DICOM attributes.
     *
     * @param rows             Number of rows in the overlay plane.
     * @param columns          Number of columns in the overlay plane.
     * @param imageFrameOrigin One-based image frame corresponding to the first overlay frame.
     * @param framesInOverlay  Number of consecutive frames encoded by this overlay.
     * @param origin           One-based overlay row and column origin relative to the image.
     * @author Kimi Liu
     */
    private record OverlayAttributes( // NOSONAR only internal use
            int rows, int columns, int imageFrameOrigin, int framesInOverlay, int[] origin) {

    }

    /**
     * Collects regular and embedded overlays for the current image and frame.
     *
     * @param embeddedOverlays Overlay planes stored in the image pixel bits.
     * @param overlays         Standalone overlay planes from the image and presentation state.
     * @param params           Read parameters controlling overlay activation and color.
     * @author Kimi Liu
     */
    private record OverlayContext(List<EmbeddedOverlay> embeddedOverlays, List<OverlayData> overlays,
            ImageReadParam params) {

        /**
         * Tests whether embedded or standalone overlay planes are available.
         *
         * @return whether at least one overlay plane is present
         */
        boolean hasOverlays() {
            return !embeddedOverlays.isEmpty() || !overlays.isEmpty();
        }

    }

    /**
     * Stores the width and height of an overlay mask.
     *
     * @param width  Image or overlay width in pixels.
     * @param height Image or overlay height in pixels.
     * @author Kimi Liu
     */
    private record ImageDimensions(int width, int height) {

        /**
         * Calculates the number of samples in this image-sized mask.
         *
         * @return the image width multiplied by its height
         */
        int totalPixels() {
            return width * height;
        }

    }

    /**
     * Stores frame offsets and dimensions used to copy packed overlay pixels.
     *
     * @param overlayOffset Bit offset of the selected frame within packed overlay data.
     * @param originX       Zero-based horizontal overlay origin.
     * @param originY       Zero-based vertical overlay origin.
     * @param height        Image or overlay height in pixels.
     * @param width         Image or overlay width in pixels.
     * @author Kimi Liu
     */
    private record OverlayFrameInfo(int overlayOffset, int originX, int originY, int height, int width) {

    }

}
