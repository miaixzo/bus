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
 * This class defines various DICOM De-identification Method codes as {@link Code} objects. These codes specify
 * different methods or options for de-identifying DICOM instances.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DeIdentificationMethod {

    /**
     * Code for Basic Application Confidentiality Profile de-identification method.
     */
    public static final Code BasicApplicationConfidentialityProfile = new Code("113100", "DCM", null,
            "Basic Application Confidentiality Profile");
    /**
     * Code for Clean Pixel Data Option de-identification method.
     */
    public static final Code CleanPixelDataOption = new Code("113101", "DCM", null, "Clean Pixel Data Option");
    /**
     * Code for Clean Recognizable Visual Features Option de-identification method.
     */
    public static final Code CleanRecognizableVisualFeaturesOption = new Code("113102", "DCM", null,
            "Clean Recognizable Visual Features Option");
    /**
     * Code for Clean Graphics Option de-identification method.
     */
    public static final Code CleanGraphicsOption = new Code("113103", "DCM", null, "Clean Graphics Option");
    /**
     * Code for Clean Structured Content Option de-identification method.
     */
    public static final Code CleanStructuredContentOption = new Code("113104", "DCM", null,
            "Clean Structured Content Option");
    /**
     * Code for Clean Descriptors Option de-identification method.
     */
    public static final Code CleanDescriptorsOption = new Code("113105", "DCM", null, "Clean Descriptors Option");
    /**
     * Code for Retain Longitudinal Temporal Information Full Dates Option de-identification method.
     */
    public static final Code RetainLongitudinalTemporalInformationFullDatesOption = new Code("113106", "DCM", null,
            "Retain Longitudinal Temporal Information Full Dates Option");
    /**
     * Code for Retain Longitudinal Temporal Information Modified Dates Option de-identification method.
     */
    public static final Code RetainLongitudinalTemporalInformationModifiedDatesOption = new Code("113107", "DCM", null,
            "Retain Longitudinal Temporal Information Modified Dates Option");
    /**
     * Code for Retain Patient Characteristics Option de-identification method.
     */
    public static final Code RetainPatientCharacteristicsOption = new Code("113108", "DCM", null,
            "Retain Patient Characteristics Option");
    /**
     * Code for Retain Device Identity Option de-identification method.
     */
    public static final Code RetainDeviceIdentityOption = new Code("113109", "DCM", null,
            "Retain Device Identity Option");
    /**
     * Code for Retain UIDs Option de-identification method.
     */
    public static final Code RetainUIDsOption = new Code("113110", "DCM", null, "Retain UIDs Option");
    /**
     * Code for Retain Safe Private Option de-identification method.
     */
    public static final Code RetainSafePrivateOption = new Code("113111", "DCM", null, "Retain Safe Private Option");
    /**
     * Code for Retain Institution Identity Option de-identification method.
     */
    public static final Code RetainInstitutionIdentityOption = new Code("113112", "DCM", null,
            "Retain Institution Identity Option");
    /**
     * Code for Retain Patient ID Hash Option de-identification method.
     */
    public static final Code RetainPatientIDHashOption = new Code("113113", "99DCM", null,
            "Retain Patient ID Hash Option");

}
