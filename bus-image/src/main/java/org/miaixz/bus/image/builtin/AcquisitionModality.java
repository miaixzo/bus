/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.builtin;

import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.image.galaxy.data.Code;

/**
 * This class defines various DICOM acquisition modalities as {@link Code} objects. Each modality is represented by a
 * code value and a descriptive name. It provides methods to retrieve, add, and remove modality codes.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AcquisitionModality {

    /**
     * Autorefraction modality.
     */
    public static final Code Autorefraction = new Code("AR", "DCM", null, "Autorefraction");
    /**
     * Bone Mineral Densitometry modality.
     */
    public static final Code BoneMineralDensitometry = new Code("BMD", "DCM", null, "Bone Mineral Densitometry");
    /**
     * Ultrasound Bone Densitometry modality.
     */
    public static final Code UltrasoundBoneDensitometry = new Code("BDUS", "DCM", null, "Ultrasound Bone Densitometry");
    /**
     * Cardiac Electrophysiology modality.
     */
    public static final Code CardiacElectrophysiology = new Code("EPS", "DCM", null, "Cardiac Electrophysiology");
    /**
     * Computed Radiography modality.
     */
    public static final Code ComputedRadiography = new Code("CR", "DCM", null, "Computed Radiography");
    /**
     * Computed Tomography modality.
     */
    public static final Code ComputedTomography = new Code("CT", "DCM", null, "Computed Tomography");
    /**
     * Digital Radiography modality.
     */
    public static final Code DigitalRadiography = new Code("DX", "DCM", null, "Digital Radiography");
    /**
     * Electrocardiography modality.
     */
    public static final Code Electrocardiography = new Code("ECG", "DCM", null, "Electrocardiography");
    /**
     * Endoscopy modality.
     */
    public static final Code Endoscopy = new Code("ES", "DCM", null, "Endoscopy");
    /**
     * External-camera Photography modality.
     */
    public static final Code ExternalCameraPhotography = new Code("XC", "DCM", null, "External-camera Photography");
    /**
     * General Microscopy modality.
     */
    public static final Code GeneralMicroscopy = new Code("GM", "DCM", null, "General Microscopy");
    /**
     * Hemodynamic Waveform modality.
     */
    public static final Code HemodynamicWaveform = new Code("HD", "DCM", null, "Hemodynamic Waveform");
    /**
     * Intra-oral Radiography modality.
     */
    public static final Code IntraOralRadiography = new Code("IO", "DCM", null, "Intra-oral Radiography");
    /**
     * Intravascular Optical Coherence Tomography modality.
     */
    public static final Code IntravascularOpticalCoherence = new Code("IVOCT", "DCM", null,
            "Intravascular Optical Coherence Tomography");
    /**
     * Intravascular Ultrasound modality.
     */
    public static final Code IntravascularUltrasound = new Code("IVUS", "DCM", null, "Intravascular Ultrasound");
    /**
     * Keratometry modality.
     */
    public static final Code Keratometry = new Code("KER", "DCM", null, "Keratometry");
    /**
     * Lensometry modality.
     */
    public static final Code Lensometry = new Code("LEN", "DCM", null, "Lensometry");
    /**
     * Magnetic Resonance modality.
     */
    public static final Code MagneticResonance = new Code("MR", "DCM", null, "Magnetic Resonance");
    /**
     * Mammography modality.
     */
    public static final Code Mammography = new Code("MG", "DCM", null, "Mammography");
    /**
     * Nuclear Medicine modality.
     */
    public static final Code NuclearMedicine = new Code("NM", "DCM", null, "Nuclear Medicine");
    /**
     * Ophthalmic Axial Measurements modality.
     */
    public static final Code OphthalmicAxialMeasurements = new Code("OAM", "DCM", null,
            "Ophthalmic Axial Measurements");
    /**
     * Optical Coherence Tomography modality.
     */
    public static final Code OpticalCoherenceTomography = new Code("OCT", "DCM", null, "Optical Coherence Tomography");
    /**
     * Ophthalmic Mapping modality.
     */
    public static final Code OphthalmicMapping = new Code("OPM", "DCM", null, "Ophthalmic Mapping");
    /**
     * Ophthalmic Photography modality.
     */
    public static final Code OphthalmicPhotography = new Code("OP", "DCM", null, "Ophthalmic Photography");
    /**
     * Ophthalmic Refraction modality.
     */
    public static final Code OphthalmicRefraction = new Code("OPR", "DCM", null, "Ophthalmic Refraction");
    /**
     * Ophthalmic Tomography modality.
     */
    public static final Code OphthalmicTomography = new Code("OPT", "DCM", null, "Ophthalmic Tomography");
    /**
     * Ophthalmic Visual Field modality.
     */
    public static final Code OphthalmicVisualField = new Code("OPV", "DCM", null, "Ophthalmic Visual Field");
    /**
     * Optical Surface Scanner modality.
     */
    public static final Code OpticalSurfaceScanner = new Code("OSS", "DCM", null, "Optical Surface Scanner");
    /**
     * Panoramic X-Ray modality.
     */
    public static final Code PanoramicXRay = new Code("PX", "DCM", null, "Panoramic X-Ray");
    /**
     * Positron Emission Tomography modality.
     */
    public static final Code PositronEmissionTomography = new Code("PT", "DCM", null, "Positron emission tomography");
    /**
     * Radiofluoroscopy modality.
     */
    public static final Code Radiofluoroscopy = new Code("RF", "DCM", null, "Radiofluoroscopy");
    /**
     * Radiographic imaging modality.
     */
    public static final Code RadiographicImaging = new Code("RG", "DCM", null, "Radiographic imaging");
    /**
     * Slide Microscopy modality.
     */
    public static final Code SlideMicroscopy = new Code("SM", "DCM", null, "Slide Microscopy");
    /**
     * Subjective Refraction modality.
     */
    public static final Code SubjectiveRefraction = new Code("SRF", "DCM", null, "Subjective Refraction");
    /**
     * Ultrasound modality.
     */
    public static final Code Ultrasound = new Code("US", "DCM", null, "Ultrasound");
    /**
     * Visual Acuity modality.
     */
    public static final Code VisualAcuity = new Code("VA", "DCM", null, "Visual Acuity");
    /**
     * X-Ray Angiography modality.
     */
    public static final Code XRayAngiography = new Code("XA", "DCM", null, "X-Ray Angiography");

    /**
     * A map holding all defined acquisition modalities, keyed by their code value.
     */
    private static final Map<String, Code> MODALITIES = new HashMap<>(50);

    /**
     * Static initializer block to populate the {@link #MODALITIES} map with predefined acquisition modalities.
     */
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
     * Retrieves the {@link Code} object for a given modality string.
     *
     * @param modality The string representation of the modality code value.
     * @return The {@link Code} object corresponding to the modality, or {@code null} if not found.
     */
    public static Code codeOf(String modality) {
        return MODALITIES.get(modality);
    }

    /**
     * Adds a new {@link Code} object representing an acquisition modality to the internal map. If a code with the same
     * code value already exists, it will be replaced.
     *
     * @param code The {@link Code} object to add.
     * @return The previous {@link Code} associated with the specified code value, or {@code null} if there was no
     *         mapping for the code value.
     */
    public static Code addCode(Code code) {
        return MODALITIES.put(code.getCodeValue(), code);
    }

    /**
     * Removes the {@link Code} object associated with the specified modality string from the internal map.
     *
     * @param modality The string representation of the modality code value to remove.
     * @return The {@link Code} object that was removed, or {@code null} if no code was associated with the modality.
     */
    public static Code removeCode(String modality) {
        return MODALITIES.remove(modality);
    }

}
