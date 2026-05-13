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
package org.miaixz.bus.image.nimble.codec;

import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.VR;

/**
 * Defines the TransferSyntaxType values.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum TransferSyntaxType {

    /**
     * Constant for the native value.
     */
    NATIVE(false, false, true, 16, 0),
    /**
     * Constant for the jpeg baseline value.
     */
    JPEG_BASELINE(true, true, false, 8, 0),
    /**
     * Constant for the jpeg extended value.
     */
    JPEG_EXTENDED(true, true, false, 12, 0),
    /**
     * Constant for the jpeg spectral value.
     */
    JPEG_SPECTRAL(true, true, false, 12, 0),
    /**
     * Constant for the jpeg progressive value.
     */
    JPEG_PROGRESSIVE(true, true, false, 12, 0),
    /**
     * Constant for the jpeg lossless value.
     */
    JPEG_LOSSLESS(true, true, true, 16, 0),
    /**
     * Constant for the jpeg ls value.
     */
    JPEG_LS(true, true, true, 16, 0),
    /**
     * Constant for the jpeg 2000 value.
     */
    JPEG_2000(true, true, true, 16, 0),
    /**
     * Constant for the rle value.
     */
    RLE(true, false, true, 16, 1),
    /**
     * Constant for the jpip value.
     */
    JPIP(false, false, true, 16, 0),
    /**
     * Constant for the mpeg value.
     */
    MPEG(true, false, false, 8, 0),
    /**
     * Constant for the deflated value.
     */
    DEFLATED(false, false, true, 16, 0),
    /**
     * Constant for the unknown value.
     */
    UNKNOWN(false, false, true, 16, 0);

    /**
     * The pixeldata encapsulated value.
     */
    private final boolean pixeldataEncapsulated;

    /**
     * The frame span multiple fragments value.
     */
    private final boolean frameSpanMultipleFragments;

    /**
     * The encode signed value.
     */
    private final boolean encodeSigned;

    /**
     * The max bits stored value.
     */
    private final int maxBitsStored;

    /**
     * The planar configuration value.
     */
    private final int planarConfiguration;

    /**
     * Creates a new instance.
     *
     * @param pixeldataEncapsulated      the pixeldata encapsulated.
     * @param frameSpanMultipleFragments the frame span multiple fragments.
     * @param encodeSigned               the encode signed.
     * @param maxBitsStored              the max bits stored.
     * @param planarConfiguration        the planar configuration.
     */
    TransferSyntaxType(boolean pixeldataEncapsulated, boolean frameSpanMultipleFragments, boolean encodeSigned,
            int maxBitsStored, int planarConfiguration) {
        this.pixeldataEncapsulated = pixeldataEncapsulated;
        this.frameSpanMultipleFragments = frameSpanMultipleFragments;
        this.encodeSigned = encodeSigned;
        this.maxBitsStored = maxBitsStored;
        this.planarConfiguration = planarConfiguration;
    }

    /**
     * Executes the for uid operation.
     *
     * @param uid the uid.
     * @return the operation result.
     */
    public static TransferSyntaxType forUID(String uid) {
        switch (UID.from(uid)) {
            case UID.ImplicitVRLittleEndian:
            case UID.ExplicitVRLittleEndian:
            case UID.ExplicitVRBigEndian:
                return NATIVE;

            case UID.DeflatedExplicitVRLittleEndian:
                return DEFLATED;

            case UID.JPEGBaseline8Bit:
                return JPEG_BASELINE;

            case UID.JPEGExtended12Bit:
                return JPEG_EXTENDED;

            case UID.JPEGSpectralSelectionNonHierarchical68:
                return JPEG_SPECTRAL;

            case UID.JPEGFullProgressionNonHierarchical1012:
                return JPEG_PROGRESSIVE;

            case UID.JPEGLossless:
            case UID.JPEGLosslessSV1:
                return JPEG_LOSSLESS;

            case UID.JPEGLSLossless:
            case UID.JPEGLSNearLossless:
                return JPEG_LS;

            case UID.JPEG2000Lossless:
            case UID.JPEG2000:
            case UID.JPEG2000MCLossless:
            case UID.JPEG2000MC:
            case UID.HTJ2KLossless:
            case UID.HTJ2KLosslessRPCL:
            case UID.HTJ2K:
                return JPEG_2000;

            case UID.JPIPReferenced:
            case UID.JPIPReferencedDeflate:
            case UID.JPIPHTJ2KReferenced:
            case UID.JPIPHTJ2KReferencedDeflate:
                return JPIP;

            case UID.MPEG2MPML:
            case UID.MPEG2MPMLF:
            case UID.MPEG2MPHL:
            case UID.MPEG2MPHLF:
            case UID.MPEG4HP41:
            case UID.MPEG4HP41F:
            case UID.MPEG4HP41BD:
            case UID.MPEG4HP41BDF:
            case UID.MPEG4HP422D:
            case UID.MPEG4HP422DF:
            case UID.MPEG4HP423D:
            case UID.MPEG4HP423DF:
            case UID.MPEG4HP42STEREO:
            case UID.MPEG4HP42STEREOF:
            case UID.HEVCMP51:
            case UID.HEVCM10P51:
                return MPEG;

            case UID.RLELossless:
                return RLE;

            default:
                return UNKNOWN;
        }
    }

    /**
     * Determines whether lossy compression.
     *
     * @param uid the uid.
     * @return true if the condition is met; otherwise false.
     */
    public static boolean isLossyCompression(String uid) {
        switch (UID.from(uid)) {
            case UID.JPEGBaseline8Bit:
            case UID.JPEGExtended12Bit:
            case UID.JPEGSpectralSelectionNonHierarchical68:
            case UID.JPEGFullProgressionNonHierarchical1012:
            case UID.JPEGLSNearLossless:
            case UID.JPEG2000:
            case UID.JPEG2000MC:
            case UID.HTJ2K:
            case UID.MPEG2MPML:
            case UID.MPEG2MPMLF:
            case UID.MPEG2MPHL:
            case UID.MPEG2MPHLF:
            case UID.MPEG4HP41:
            case UID.MPEG4HP41F:
            case UID.MPEG4HP41BD:
            case UID.MPEG4HP41BDF:
            case UID.MPEG4HP422D:
            case UID.MPEG4HP422DF:
            case UID.MPEG4HP423D:
            case UID.MPEG4HP423DF:
            case UID.MPEG4HP42STEREO:
            case UID.MPEG4HP42STEREOF:
            case UID.HEVCMP51:
            case UID.HEVCM10P51:
                return true;

            default:
                return false;
        }
    }

    /**
     * Determines whether ybr compression.
     *
     * @param uid the uid.
     * @return true if the condition is met; otherwise false.
     */
    public static boolean isYBRCompression(String uid) {
        switch (UID.from(uid)) {
            case UID.JPEGBaseline8Bit:
            case UID.JPEGExtended12Bit:
            case UID.JPEGSpectralSelectionNonHierarchical68:
            case UID.JPEGFullProgressionNonHierarchical1012:
            case UID.JPEG2000Lossless:
            case UID.JPEG2000:
            case UID.HTJ2KLossless:
            case UID.HTJ2KLosslessRPCL:
            case UID.HTJ2K:
                return true;

            default:
                return false;
        }
    }

    /**
     * Determines whether pixeldata encapsulated.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isPixeldataEncapsulated() {
        return pixeldataEncapsulated;
    }

    /**
     * Determines whether encode signed.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean canEncodeSigned() {
        return encodeSigned;
    }

    /**
     * Executes the may frame span multiple fragments operation.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean mayFrameSpanMultipleFragments() {
        return frameSpanMultipleFragments;
    }

    /**
     * Gets the planar configuration.
     *
     * @return the planar configuration.
     */
    public int getPlanarConfiguration() {
        return planarConfiguration;
    }

    /**
     * Gets the max bits stored.
     *
     * @return the max bits stored.
     */
    public int getMaxBitsStored() {
        return maxBitsStored;
    }

    /**
     * Executes the adjust bits stored to12 operation.
     *
     * @param attrs the attrs.
     * @return true if the condition is met; otherwise false.
     */
    public boolean adjustBitsStoredTo12(Attributes attrs) {
        if (maxBitsStored == 12) {
            int bitsStored = attrs.getInt(Tag.BitsStored, 8);
            if (bitsStored > 8 && bitsStored < 12) {
                attrs.setInt(Tag.BitsStored, VR.US, 12);
                attrs.setInt(Tag.HighBit, VR.US, 11);
                return true;
            }
        }
        return false;
    }

}
