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

    private void init(Attributes dcm) {
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
            Logger.debug(
                    "VOI Window Center and Width attributes have different number of values : {} => {}",
                    windowCenter.size(),
                    windowWidth.size());
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
