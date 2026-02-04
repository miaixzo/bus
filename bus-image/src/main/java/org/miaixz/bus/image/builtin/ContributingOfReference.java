/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.builtin;

import org.miaixz.bus.image.galaxy.data.Code;

/**
 * This class defines various DICOM Contributing Equipment codes as {@link Code} objects. These codes specify the type
 * of equipment that contributed to the creation or modification of an image.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ContributingOfReference {

    /**
     * Code for Acquisition Equipment.
     */
    public static final Code AcquisitionEquipment = new Code("109101", "DCM", null, "Acquisition Equipment");
    /**
     * Code for Processing Equipment.
     */
    public static final Code ProcessingEquipment = new Code("109102", "DCM", null, "Processing Equipment");
    /**
     * Code for Modifying Equipment.
     */
    public static final Code ModifyingEquipment = new Code("109103", "DCM", null, "Modifying Equipment");
    /**
     * Code for De-identifying Equipment.
     */
    public static final Code DeIdentifyingEquipment = new Code("109104", "DCM", null, "De-identifying Equipment");
    /**
     * Code for Frame Extracting Equipment.
     */
    public static final Code FrameExtractingEquipment = new Code("109105", "DCM", null, "Frame Extracting Equipment");
    /**
     * Code for Enhanced Multi-frame Conversion Equipment.
     */
    public static final Code EnhancedMultiFrameConversionEquipment = new Code("109106", "DCM", null,
            "Enhanced Multi-frame Conversion Equipment Equipment");
    /**
     * Code for Portable Media Importer Equipment.
     */
    public static final Code PortableMediaImporterEquipment = new Code("MEDIM", "DCM", null,
            "Portable Media Importer Equipment");
    /**
     * Code for Film Digitizer.
     */
    public static final Code FilmDigitizer = new Code("FILMD", "DCM", null, "Film Digitizer");
    /**
     * Code for Document Digitizer Equipment.
     */
    public static final Code DocumentDigitizerEquipment = new Code("DOCD", "DCM", null, "Document Digitizer Equipment");
    /**
     * Code for Video Tape Digitizer Equipment.
     */
    public static final Code VideoTapeDigitizerEquipment = new Code("VIDD", "DCM", null,
            "Video Tape Digitizer Equipment");

}
