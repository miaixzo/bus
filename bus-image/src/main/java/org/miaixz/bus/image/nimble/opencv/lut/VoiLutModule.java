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
package org.miaixz.bus.image.nimble.opencv.lut;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.Sequence;
import org.miaixz.bus.image.nimble.RGBImageVoiLut;
import org.miaixz.bus.image.nimble.opencv.LookupTableCV;
import org.miaixz.bus.logger.Logger;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class VoiLutModule {

    private List<Double> windowCenter;
    private List<Double> windowWidth;
    private List<String> lutExplanation;
    private List<LookupTableCV> lut;
    private List<String> windowCenterWidthExplanation;
    private String voiLutFunction;

    /**
     * VOI LUT Module
     *
     * @see <a href="http://dicom.nema.org/medical/dicom/current/output/chtml/part03/sect_C.12.html">C.11.2 VOI LUT
     *      Module</a>
     */
    public VoiLutModule(Attributes dcm) {
        this.windowCenter = Collections.emptyList();
        this.windowWidth = Collections.emptyList();
        this.lutExplanation = Collections.emptyList();
        this.lut = Collections.emptyList();
        this.windowCenterWidthExplanation = Collections.emptyList();
        this.voiLutFunction = null;
        init(Objects.requireNonNull(dcm));
    }

    private static Double getDouble(Attributes dcm, int tag) {
        Double val = Builder.getDoubleFromDicomElement(dcm, tag, null);
        if (val != null) {
            Attributes parent = dcm.getParent();
            while (parent != null) {
                val = Builder.getDoubleFromDicomElement(parent, tag, null);
                if (val != null) {
                    return val;
                }
                parent = parent.getParent();
            }
        }
        return val;
    }

    private void init(Attributes dcm) {
        String modality = RGBImageVoiLut.getModality(dcm);
        Optional<double[]> wc = Optional.ofNullable(dcm.getDoubles(Tag.WindowCenter));
        Optional<double[]> ww = Optional.ofNullable(dcm.getDoubles(Tag.WindowWidth));
        if (wc.isPresent() && ww.isPresent()) {
            this.windowCenter = DoubleStream.of(wc.get()).boxed().collect(Collectors.toList());
            this.windowWidth = DoubleStream.of(ww.get()).boxed().collect(Collectors.toList());
            this.voiLutFunction = dcm.getString(Tag.VOILUTFunction);
            String[] wexpl = Builder.getStringArrayFromDicomElement(dcm, Tag.WindowCenterWidthExplanation);
            if (wexpl != null) {
                this.windowCenterWidthExplanation = Stream.of(wexpl).toList();
            }

            if ("MR".equals(modality) || "XA".equals(modality) || "XRF".equals(modality) || "PT".equals(modality)) {
                adaptWindowWidth(dcm);
            }
        }

        Sequence voiSeq = dcm.getSequence(Tag.VOILUTSequence);
        if (voiSeq != null && !voiSeq.isEmpty()) {
            this.lutExplanation = voiSeq.stream().map(i -> i.getString(Tag.LUTExplanation, "")).toList();
            this.lut = voiSeq.stream().map(i -> RGBImageVoiLut.createLut(i).orElse(null)).toList();
        }

        if (Logger.isDebugEnabled()) {
            logLutConsistency();
        }
    }

    private void logLutConsistency() {
        // If multiple Window center and window width values are present, both Attributes shall have
        // the same number of values and shall be considered as pairs. Multiple values indicate that
        // multiple alternative views may be presented
        if (windowCenter.isEmpty() && !windowWidth.isEmpty()) {
            Logger.debug("VOI Window Center is required if Window Width is present");
        } else if (!windowCenter.isEmpty() && windowWidth.isEmpty()) {
            Logger.debug("VOI Window Width is required if Window Center is present");
        } else if (windowWidth.size() != windowCenter.size()) {
            Logger.debug("VOI Window Center and Width attributes have different number of values : {} => {}",
                    windowCenter.size(), windowWidth.size());
        }
    }

    private void adaptWindowWidth(Attributes dcm) {
        Double rescaleSlope = getDouble(dcm, Tag.RescaleSlope);
        Double rescaleIntercept = getDouble(dcm, Tag.RescaleIntercept);
        if (rescaleSlope != null && rescaleIntercept != null) {
            /*
             * IHE BIR: Windowing and Rendering 4.16.4.2.2.5.4
             *
             * If Rescale Slope and Rescale Intercept has been removed in ModalityLutModule then the Window Center and
             * Window Width must be adapted. See
             * https://groups.google.com/forum/#!topic/comp.protocols.dicom/iTCxWcsqjnM
             */
            int length = windowCenter.size();
            if (length != windowWidth.size()) {
                length = 0;
            }
            for (int i = 0; i < length; i++) {
                windowWidth.set(i, windowWidth.get(i) / rescaleSlope);
                windowCenter.set(i, (windowCenter.get(i) - rescaleIntercept) / rescaleSlope);
            }
        }
    }

    public List<Double> getWindowCenter() {
        return windowCenter;
    }

    public List<Double> getWindowWidth() {
        return windowWidth;
    }

    public List<String> getLutExplanation() {
        return lutExplanation;
    }

    public List<LookupTableCV> getLut() {
        return lut;
    }

    public List<String> getWindowCenterWidthExplanation() {
        return windowCenterWidthExplanation;
    }

    public Optional<String> getVoiLutFunction() {
        return Optional.ofNullable(voiLutFunction);
    }

}
