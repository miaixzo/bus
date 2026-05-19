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
package org.miaixz.bus.image.metric.web;

/**
 * IHE Invoke Image Display request constants.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class InvokeImageDisplay {

    /**
     * The series uid value.
     */
    public static final String SERIES_UID = "seriesUID";

    /**
     * The object uid value.
     */
    public static final String OBJECT_UID = "objectUID";

    /**
     * The keywords value.
     */
    public static final String KEYWORDS = "containsInDescription";

    /**
     * The request type value.
     */
    public static final String REQUEST_TYPE = "requestType";

    /**
     * The patient id value.
     */
    public static final String PATIENT_ID = "patientID";

    /**
     * The patient name value.
     */
    public static final String PATIENT_NAME = "patientName";

    /**
     * The patient birthdate value.
     */
    public static final String PATIENT_BIRTHDATE = "patientBirthDate";

    /**
     * The lower datetime value.
     */
    public static final String LOWER_DATETIME = "lowerDateTime";

    /**
     * The upper datetime value.
     */
    public static final String UPPER_DATETIME = "upperDateTime";

    /**
     * The most recent results value.
     */
    public static final String MOST_RECENT_RESULTS = "mostRecentResults";

    /**
     * The modalities in study value.
     */
    public static final String MODALITIES_IN_STUDY = "modalitiesInStudy";

    /**
     * The viewer type value.
     */
    public static final String VIEWER_TYPE = "viewerType";

    /**
     * The diagnostic quality value.
     */
    public static final String DIAGNOSTIC_QUALITY = "diagnosticQuality";

    /**
     * The key images only value.
     */
    public static final String KEY_IMAGES_ONLY = "keyImagesOnly";

    /**
     * The study uid value.
     */
    public static final String STUDY_UID = "studyUID";

    /**
     * The accession number value.
     */
    public static final String ACCESSION_NUMBER = "accessionNumber";

    /**
     * The ihe bir value.
     */
    public static final String IHE_BIR = "IHE_BIR";

    /**
     * The patient level value.
     */
    public static final String PATIENT_LEVEL = "PATIENT";

    /**
     * The study level value.
     */
    public static final String STUDY_LEVEL = "STUDY";

    /**
     * The diagnostic value.
     */
    public static final String DIAGNOSTIC = "DIAGNOSTIC";

    /**
     * The reference value.
     */
    public static final String REFERENCE = "REFERENCE";

    /**
     * Creates a new instance.
     */
    private InvokeImageDisplay() {
        // No initialization required.
    }

}
