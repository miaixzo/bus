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
package org.miaixz.bus.image.nimble.opencv.lut;

import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.nimble.RGBImageVoiLut;
import org.miaixz.bus.image.nimble.opencv.LookupTableCV;
import org.miaixz.bus.logger.Logger;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ModalityLutModule {

    private OptionalDouble rescaleSlope;
    private OptionalDouble rescaleIntercept;
    private Optional<String> rescaleType;
    private Optional<String> lutType;
    private Optional<String> lutExplanation;
    private Optional<LookupTableCV> lut;

    /**
     * Modality LUT Module
     *
     * <p>
     * Note: Either a Modality LUT Sequence containing a single Item or Rescale Slope and Intercept values shall be
     * present but not both. This implementation only applies a warning in such a case.
     *
     * @see <a href="http://dicom.nema.org/medical/dicom/current/output/chtml/part03/sect_C.11.html">C.11.1 Modality LUT
     *      Module</a>
     */
    public ModalityLutModule(Attributes dcm) {
        this.rescaleSlope = OptionalDouble.empty();
        this.rescaleIntercept = OptionalDouble.empty();
        this.rescaleType = Optional.empty();
        this.lutType = Optional.empty();
        this.lutExplanation = Optional.empty();
        this.lut = Optional.empty();
        init(Objects.requireNonNull(dcm));
    }

    private void init(Attributes dcm) {
        String modality = RGBImageVoiLut.getModality(dcm);
        if (dcm.containsValue(Tag.RescaleIntercept) && dcm.containsValue(Tag.RescaleSlope)) {
            if ("MR".equals(modality)
                    // || "PT".equals(modality)
                    || "XA".equals(modality) || "XRF".equals(modality)) {
                // IHE BIR: Windowing and Rendering 4.16.4.2.2.5.4
                Logger.trace("Do not apply RescaleSlope and RescaleIntercept to {}", modality);
            } else {
                this.rescaleSlope = OptionalDouble.of(Builder.getDoubleFromDicomElement(dcm, Tag.RescaleSlope, null));
                this.rescaleIntercept = OptionalDouble
                        .of(Builder.getDoubleFromDicomElement(dcm, Tag.RescaleIntercept, null));
                this.rescaleType = Optional.ofNullable(dcm.getString(Tag.RescaleType));
            }
        }

        initModalityLUTSequence(dcm, modality);
        logModalityLutConsistency();
    }

    private void initModalityLUTSequence(Attributes dcm, String modality) {
        Attributes dcmLut = dcm.getNestedDataset(Tag.ModalityLUTSequence);
        if (dcmLut != null && dcmLut.containsValue(Tag.ModalityLUTType) && dcmLut.containsValue(Tag.LUTDescriptor)
                && dcmLut.containsValue(Tag.LUTData)) {
            applyMLUT(dcm, modality, dcmLut);
        }
    }

    private void applyMLUT(Attributes dcm, String modality, Attributes dcmLut) {
        boolean canApplyMLUT = true;

        // See http://dicom.nema.org/medical/dicom/current/output/html/part04.html#figure_N.2-1 and
        // http://dicom.nema.org/medical/dicom/current/output/html/part03.html#sect_C.8.7.1.1.2
        if ("XA".equals(modality) || "XRF".equals(modality)) {
            String pixRel = dcm.getString(Tag.PixelIntensityRelationship);
            if (("LOG".equalsIgnoreCase(pixRel) || "DISP".equalsIgnoreCase(pixRel))) {
                canApplyMLUT = false;
            }
        }
        if (canApplyMLUT) {
            this.lutType = Optional.ofNullable(dcmLut.getString(Tag.ModalityLUTType));
            this.lutExplanation = Optional.ofNullable(dcmLut.getString(Tag.LUTExplanation));
            this.lut = RGBImageVoiLut.createLut(dcmLut);
        }
    }

    private void logModalityLutConsistency() {
        if (rescaleIntercept.isPresent() && lut.isPresent()) {
            Logger.warn(
                    "Either a Modality LUT Sequence or Rescale Slope and Intercept values shall be present but not both!");
        }

        if (Logger.isTraceEnabled()) {
            if (lut.isPresent()) {
                if (rescaleIntercept.isPresent()) {
                    Logger.trace("Modality LUT Sequence shall NOT be present if Rescale Intercept is present");
                }
                if (lutType.isEmpty()) {
                    Logger.trace("Modality Type is required if Modality LUT Sequence is present.");
                }
            } else if (rescaleIntercept.isPresent() && rescaleSlope.isEmpty()) {
                Logger.trace("Modality Rescale Slope is required if Rescale Intercept is present.");
            }
        }
    }

    public OptionalDouble getRescaleSlope() {
        return rescaleSlope;
    }

    public OptionalDouble getRescaleIntercept() {
        return rescaleIntercept;
    }

    public Optional<String> getRescaleType() {
        return rescaleType;
    }

    public Optional<String> getLutType() {
        return lutType;
    }

    public Optional<String> getLutExplanation() {
        return lutExplanation;
    }

    public Optional<LookupTableCV> getLut() {
        return lut;
    }

    public void adaptWithOverlayBitMask(int shiftHighBit) {
        // Combine to the slope value
        double rs = 1.0; // FIXME: 1.0 should we use rescaleSlope.orElse(1.0) instead?
        if (rescaleSlope.isEmpty()) {
            // Set valid modality LUT values
            if (rescaleIntercept.isEmpty()) {
                rescaleIntercept = OptionalDouble.of(0.0);
            }
            if (rescaleType.isEmpty()) {
                rescaleType = Optional.of("US");
            }
        }
        // Divide pixel value by (2 ^ rightBit) => remove right bits
        rs /= 1 << shiftHighBit;
        this.rescaleSlope = OptionalDouble.of(rs);
    }

}
