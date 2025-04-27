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
package org.miaixz.bus.image.galaxy.dict.Philips_MR_Imaging_DD_003;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "Philips MR Imaging DD 003";

    /** (2005,xx00) VR=UL VM=1 Number of SOP Common */
    public static final int NumberOfSOPCommon = 0x20050000;

    /** (2005,xx01) VR=UL VM=1 Number of Film Consumption */
    public static final int NoOfFilmConsumption = 0x20050001;

    /** (2005,xx13) VR=UL VM=1 Number of Codes */
    public static final int NumberOfCodes = 0x20050013;

    /** (2005,xx34) VR=SL VM=1 Number of Image Per Series Ref */
    public static final int NumberOfImagePerSeriesRef = 0x20050034;

    /** (2005,xx43) VR=SS VM=1 No Date of Last Calibration */
    public static final int NoDateOfLastCalibration = 0x20050043;

    /** (2005,xx44) VR=SS VM=1 No Time of Last Calibration */
    public static final int NoTimeOfLastCalibration = 0x20050044;

    /** (2005,xx45) VR=SS VM=1 Number of Software Version */
    public static final int NrOfSoftwareVersion = 0x20050045;

    /** (2005,xx47) VR=SS VM=1 Number of Patient Other Names */
    public static final int NrOfPatientOtherNames = 0x20050047;

    /** (2005,xx48) VR=SS VM=1 Number of Req Recipe of Results */
    public static final int NrOfReqRecipeOfResults = 0x20050048;

    /** (2005,xx49) VR=SS VM=1 Number of Series Operators Name */
    public static final int NrOfSeriesOperatorsName = 0x20050049;

    /** (2005,xx50) VR=SS VM=1 Number of Series Performing Physicians Name */
    public static final int NrOfSeriesPerfPhysiName = 0x20050050;

    /**
     * (2005,xx51) VR=SS VM=1 Number of Study Admitting Diagnostic Description
     */
    public static final int NrOfStudyAdmittingDiagnosticDescr = 0x20050051;

    /**
     * (2005,xx52) VR=SS VM=1 Number of Study Patient Contrast Allergies
     */
    public static final int NrOfStudyPatientContrastAllergies = 0x20050052;

    /**
     * (2005,xx53) VR=SS VM=1 Number of Study Patient Medical Alerts
     */
    public static final int NrOfStudyPatientMedicalAlerts = 0x20050053;

    /** (2005,xx54) VR=SS VM=1 Number of Study Physicians of Record */
    public static final int NrOfStudyPhysiciansOfRecord = 0x20050054;

    /** (2005,xx55) VR=SS VM=1 Number of Study Physicians Reading Study */
    public static final int NrOfStudyPhysiReadingStudy = 0x20050055;

    /** (2005,xx56) VR=SS VM=1 Number of SC Software Versions */
    public static final int NrSCSoftwareVersions = 0x20050056;

    /** (2005,xx57) VR=SS VM=1 Number of Running Attributes */
    public static final int NrRunningAttributes = 0x20050057;

    /** (2005,xx70) VR=OW VM=1 Spectrum Pixel Data */
    public static final int SpectrumPixelData = 0x20050070;

    /** (2005,xx81) VR=UI VM=1 Default Image UID */
    public static final int DefaultImageUID = 0x20050081;

    /** (2005,xx82) VR=CS VM=1-n Running Attributes */
    public static final int RunningAttributes = 0x20050082;

}
