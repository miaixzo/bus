/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.image.galaxy.media;

/**
 * @author Kimi Liu
 * @version 5.8.9
 * @since JDK 1.8+
 */
public class InvokeImageDisplay {

    // Non IID request parameters
    public static final String SERIES_UID = "seriesUID";
    public static final String OBJECT_UID = "objectUID";

    /* IHE Radiology Technical Framework Supplement – Invoke Image Display (IID) */
    // HTTP Request Parameters – Patient-based
    public static final String REQUEST_TYPE = "requestType";
    public static final String PATIENT_ID = "patientID";
    public static final String PATIENT_NAME = "patientName";
    public static final String PATIENT_BIRTHDATE = "patientBirthDate";
    public static final String LOWER_DATETIME = "lowerDateTime";
    public static final String UPPER_DATETIME = "upperDateTime";
    public static final String MOST_RECENT_RESULTS = "mostRecentResults";
    public static final String MODALITIES_IN_STUDY = "modalitiesInStudy";
    public static final String VIEWER_TYPE = "viewerType";
    public static final String DIAGNOSTIC_QUALITY = "diagnosticQuality";
    public static final String KEY_IMAGES_ONLY = "keyImagesOnly";
    // Additional patient-based parameters (not IID profile)
    public static final String KEYWORDS = "containsInDescription";

    // HTTP Request Parameters – Study-based
    public static final String STUDY_UID = "studyUID";
    public static final String ACCESSION_NUMBER = "accessionNumber";

    // Well-Known Values for Viewer Type Parameter
    public static final String IHE_BIR = "IHE_BIR";
    public static final String PATIENT_LEVEL = "PATIENT";
    public static final String STUDY_LEVEL = "STUDY";

}
