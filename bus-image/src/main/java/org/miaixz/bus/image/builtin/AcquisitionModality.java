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
package org.miaixz.bus.image.builtin;

import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.image.galaxy.data.Code;

/**
 * This class defines various DICOM acquisition modalities as {@link Code} objects. Each modality is represented by a
 * code value and a descriptive name. It provides methods to retrieve, add, and remove modality codes.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class AcquisitionModality {

    /**
     * Constructs a new AcquisitionModality instance.
     */
    public AcquisitionModality() {
        // No initialization required.
    }

    /**
     * The autorefraction value.
     */
    public static final Code Autorefraction = new Code("AR", "DCM", null, "Autorefraction");

    /**
     * The bone mineral densitometry value.
     */
    public static final Code BoneMineralDensitometry = new Code("BMD", "DCM", null, "Bone Mineral Densitometry");

    /**
     * The ultrasound bone densitometry value.
     */
    public static final Code UltrasoundBoneDensitometry = new Code("BDUS", "DCM", null, "Ultrasound Bone Densitometry");

    /**
     * The cardiac electrophysiology value.
     */
    public static final Code CardiacElectrophysiology = new Code("EPS", "DCM", null, "Cardiac Electrophysiology");

    /**
     * The computed radiography value.
     */
    public static final Code ComputedRadiography = new Code("CR", "DCM", null, "Computed Radiography");

    /**
     * The computed tomography value.
     */
    public static final Code ComputedTomography = new Code("CT", "DCM", null, "Computed Tomography");

    /**
     * The digital radiography value.
     */
    public static final Code DigitalRadiography = new Code("DX", "DCM", null, "Digital Radiography");

    /**
     * The electrocardiography value.
     */
    public static final Code Electrocardiography = new Code("ECG", "DCM", null, "Electrocardiography");

    /**
     * The endoscopy value.
     */
    public static final Code Endoscopy = new Code("ES", "DCM", null, "Endoscopy");

    /**
     * The external camera photography value.
     */
    public static final Code ExternalCameraPhotography = new Code("XC", "DCM", null, "External-camera Photography");

    /**
     * The general microscopy value.
     */
    public static final Code GeneralMicroscopy = new Code("GM", "DCM", null, "General Microscopy");

    /**
     * The hemodynamic waveform value.
     */
    public static final Code HemodynamicWaveform = new Code("HD", "DCM", null, "Hemodynamic Waveform");

    /**
     * The intra oral radiography value.
     */
    public static final Code IntraOralRadiography = new Code("IO", "DCM", null, "Intra-oral Radiography");

    /**
     * The intravascular optical coherence value.
     */
    public static final Code IntravascularOpticalCoherence = new Code("IVOCT", "DCM", null,
            "Intravascular Optical Coherence Tomography");

    /**
     * The intravascular ultrasound value.
     */
    public static final Code IntravascularUltrasound = new Code("IVUS", "DCM", null, "Intravascular Ultrasound");

    /**
     * The keratometry value.
     */
    public static final Code Keratometry = new Code("KER", "DCM", null, "Keratometry");

    /**
     * The lensometry value.
     */
    public static final Code Lensometry = new Code("LEN", "DCM", null, "Lensometry");

    /**
     * The magnetic resonance value.
     */
    public static final Code MagneticResonance = new Code("MR", "DCM", null, "Magnetic Resonance");

    /**
     * The mammography value.
     */
    public static final Code Mammography = new Code("MG", "DCM", null, "Mammography");

    /**
     * The nuclear medicine value.
     */
    public static final Code NuclearMedicine = new Code("NM", "DCM", null, "Nuclear Medicine");

    /**
     * The ophthalmic axial measurements value.
     */
    public static final Code OphthalmicAxialMeasurements = new Code("OAM", "DCM", null,
            "Ophthalmic Axial Measurements");

    /**
     * The optical coherence tomography value.
     */
    public static final Code OpticalCoherenceTomography = new Code("OCT", "DCM", null, "Optical Coherence Tomography");

    /**
     * The ophthalmic mapping value.
     */
    public static final Code OphthalmicMapping = new Code("OPM", "DCM", null, "Ophthalmic Mapping");

    /**
     * The ophthalmic photography value.
     */
    public static final Code OphthalmicPhotography = new Code("OP", "DCM", null, "Ophthalmic Photography");

    /**
     * The ophthalmic refraction value.
     */
    public static final Code OphthalmicRefraction = new Code("OPR", "DCM", null, "Ophthalmic Refraction");

    /**
     * The ophthalmic tomography value.
     */
    public static final Code OphthalmicTomography = new Code("OPT", "DCM", null, "Ophthalmic Tomography");

    /**
     * The ophthalmic visual field value.
     */
    public static final Code OphthalmicVisualField = new Code("OPV", "DCM", null, "Ophthalmic Visual Field");

    /**
     * The optical surface scanner value.
     */
    public static final Code OpticalSurfaceScanner = new Code("OSS", "DCM", null, "Optical Surface Scanner");

    /**
     * The panoramic x ray value.
     */
    public static final Code PanoramicXRay = new Code("PX", "DCM", null, "Panoramic X-Ray");

    /**
     * The positron emission tomography value.
     */
    public static final Code PositronEmissionTomography = new Code("PT", "DCM", null, "Positron emission tomography");

    /**
     * The radiofluoroscopy value.
     */
    public static final Code Radiofluoroscopy = new Code("RF", "DCM", null, "Radiofluoroscopy");

    /**
     * The radiographic imaging value.
     */
    public static final Code RadiographicImaging = new Code("RG", "DCM", null, "Radiographic imaging");

    /**
     * The slide microscopy value.
     */
    public static final Code SlideMicroscopy = new Code("SM", "DCM", null, "Slide Microscopy");

    /**
     * The subjective refraction value.
     */
    public static final Code SubjectiveRefraction = new Code("SRF", "DCM", null, "Subjective Refraction");

    /**
     * The ultrasound value.
     */
    public static final Code Ultrasound = new Code("US", "DCM", null, "Ultrasound");

    /**
     * The visual acuity value.
     */
    public static final Code VisualAcuity = new Code("VA", "DCM", null, "Visual Acuity");

    /**
     * The x ray angiography value.
     */
    public static final Code XRayAngiography = new Code("XA", "DCM", null, "X-Ray Angiography");

    /**
     * The modalities value.
     */
    private static final Map<String, Code> MODALITIES = new HashMap<>(50);

    static {
        Code[] codes = { Autorefraction, BoneMineralDensitometry, UltrasoundBoneDensitometry, CardiacElectrophysiology,
                ComputedRadiography, ComputedTomography, DigitalRadiography, Electrocardiography, Endoscopy,
                ExternalCameraPhotography, GeneralMicroscopy, HemodynamicWaveform, IntraOralRadiography,
                IntravascularOpticalCoherence, IntravascularUltrasound, Keratometry, Lensometry, MagneticResonance,
                Mammography, NuclearMedicine, OphthalmicAxialMeasurements, OpticalCoherenceTomography,
                OphthalmicMapping, OphthalmicPhotography, OphthalmicRefraction, OphthalmicTomography,
                OphthalmicVisualField, OpticalSurfaceScanner, PanoramicXRay, PositronEmissionTomography,
                Radiofluoroscopy, RadiographicImaging, SlideMicroscopy, SubjectiveRefraction, Ultrasound, VisualAcuity,
                XRayAngiography };
        for (Code code : codes) {
            MODALITIES.put(code.getCodeValue(), code);
        }

    }

    /**
     * Executes the code of operation.
     *
     * @param modality the modality.
     * @return the operation result.
     */
    public static Code codeOf(String modality) {
        return MODALITIES.get(modality);
    }

    /**
     * Adds the code.
     *
     * @param code the code.
     * @return the operation result.
     */
    public static Code addCode(Code code) {
        return MODALITIES.put(code.getCodeValue(), code);
    }

    /**
     * Removes the code.
     *
     * @param modality the modality.
     * @return the operation result.
     */
    public static Code removeCode(String modality) {
        return MODALITIES.remove(modality);
    }

}
