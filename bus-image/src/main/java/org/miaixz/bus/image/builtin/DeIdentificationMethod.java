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
package org.miaixz.bus.image.builtin;

import org.miaixz.bus.image.galaxy.data.Code;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class DeIdentificationMethod {

    public static final Code BasicApplicationConfidentialityProfile = new Code("113100", "DCM", null, "Basic Application Confidentiality Profile");
    public static final Code CleanPixelDataOption = new Code("113101", "DCM", null, "Clean Pixel Data Option");
    public static final Code CleanRecognizableVisualFeaturesOption = new Code("113102", "DCM", null, "Clean Recognizable Visual Features Option");
    public static final Code CleanGraphicsOption = new Code("113103", "DCM", null, "Clean Graphics Option");
    public static final Code CleanStructuredContentOption = new Code("113104", "DCM", null, "Clean Structured Content Option");
    public static final Code CleanDescriptorsOption = new Code("113105", "DCM", null, "Clean Descriptors Option");
    public static final Code RetainLongitudinalTemporalInformationFullDatesOption = new Code("113106", "DCM", null, "Retain Longitudinal Temporal Information Full Dates Option");
    public static final Code RetainLongitudinalTemporalInformationModifiedDatesOption = new Code("113107", "DCM", null, "Retain Longitudinal Temporal Information Modified Dates Option");
    public static final Code RetainPatientCharacteristicsOption = new Code("113108", "DCM", null, "Retain Patient Characteristics Option");
    public static final Code RetainDeviceIdentityOption = new Code("113109", "DCM", null, "Retain Device Identity Option");
    public static final Code RetainUIDsOption = new Code("113110", "DCM", null, "Retain UIDs Option");
    public static final Code RetainSafePrivateOption = new Code("113111", "DCM", null, "Retain Safe Private Option");
    public static final Code RetainInstitutionIdentityOption = new Code("113112", "DCM", null, "Retain Institution Identity Option");
    public static final Code RetainPatientIDHashOption = new Code("113113", "99DCM", null, "Retain Patient ID Hash Option");

}
