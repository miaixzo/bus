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
package org.miaixz.bus.image.galaxy.media;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class InvokeImageDisplay {

    // 非IID请求参数
    public static final String SERIES_UID = "seriesUID";
    public static final String OBJECT_UID = "objectUID";

    /* IHE放射学技术框架补充–调用图像显示(IID) */
    // HTTP请求参数–基于患者
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
    // 其他基于患者的参数(不是IID配置文件)
    public static final String KEYWORDS = "containsInDescription";

    // HTTP请求参数–基于研究
    public static final String STUDY_UID = "studyUID";
    public static final String ACCESSION_NUMBER = "accessionNumber";

    // 查看器类型参数的已知值
    public static final String IHE_BIR = "IHE_BIR";
    public static final String PATIENT_LEVEL = "PATIENT";
    public static final String STUDY_LEVEL = "STUDY";

}
