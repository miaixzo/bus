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
package org.miaixz.bus.image.galaxy.dict.Siemens__Thorax_Multix_FD_Lab_Settings;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "Siemens: Thorax/Multix FD Lab Settings";

    /** (0019,xx00) VR=LO VM=1 ? */
    public static final int _0019_xx00_ = 0x00190000;

    /** (0019,xx01) VR=LO VM=1 ? */
    public static final int _0019_xx01_ = 0x00190001;

    /** (0019,xx02) VR=LO VM=1 Total Dose Area Product uGy*cm*cm */
    public static final int TotalDoseAreaProduct = 0x00190002;

    /** (0019,xx03) VR=US VM=1 ? */
    public static final int _0019_xx03_ = 0x00190003;

    /** (0019,xx04) VR=LO VM=1 ? */
    public static final int _0019_xx04_ = 0x00190004;

    /** (0019,xx05) VR=US VM=1 ? */
    public static final int _0019_xx05_ = 0x00190005;

    /** (0019,xx06) VR=FD VM=1 Table Object Distance */
    public static final int TableObjectDistance = 0x00190006;

    /** (0019,xx07) VR=FD VM=1 Table Detector Distance */
    public static final int TableDetectorDistance = 0x00190007;

    /** (0019,xx08) VR=US VM=1-n Ortho Step Distance */
    public static final int OrthoStepDistance = 0x00190008;

    /** (0021,xx08) VR=US VM=1 Auto Window Flag */
    public static final int AutoWindowFlag = 0x00210008;

    /** (0021,xx09) VR=SL VM=1 Auto Window Center */
    public static final int AutoWindowCenter = 0x00210009;

    /** (0021,xx0A) VR=SL VM=1 Auto Window Width */
    public static final int AutoWindowWidth = 0x0021000A;

    /** (0021,xx0B) VR=SS VM=1 Filter ID */
    public static final int FilterID = 0x0021000B;

    /** (0021,xx14) VR=US VM=1 Anatomic Correct View */
    public static final int AnatomicCorrectView = 0x00210014;

    /** (0021,xx15) VR=SS VM=1 Auto Window Shift */
    public static final int AutoWindowShift = 0x00210015;

    /** (0021,xx16) VR=DS VM=1 Auto Window Expansion */
    public static final int AutoWindowExpansion = 0x00210016;

    /** (0021,xx17) VR=LO VM=1 System Type */
    public static final int SystemType = 0x00210017;

    /** (0021,xx30) VR=SH VM=1 Anatomic Sort Number */
    public static final int AnatomicSortNumber = 0x00210030;

    /** (0021,xx31) VR=SH VM=1 AcquisitionSortNumber */
    public static final int AcquisitionSortNumber = 0x00210031;

}
