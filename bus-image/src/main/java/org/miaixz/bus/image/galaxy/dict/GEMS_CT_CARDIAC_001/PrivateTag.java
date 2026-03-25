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
package org.miaixz.bus.image.galaxy.dict.GEMS_CT_CARDIAC_001;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class PrivateTag {

    public static final String PrivateCreator = "GEMS_CT_CARDIAC_001";

    /** (0049,xx01) VR=SQ VM=1 CT Cardiac Sequence */
    public static final int CTCardiacSequence = 0x00490001;

    /** (0049,xx02) VR=CS VM=1 Heart Rate At Confirm */
    public static final int HeartRateAtConfirm = 0x00490002;

    /** (0049,xx03) VR=FL VM=1 Avg Heart Rate Prior To Confirm */
    public static final int AvgHeartRatePriorToConfirm = 0x00490003;

    /** (0049,xx04) VR=CS VM=1 Min Heart Rate Prior To Confirm */
    public static final int MinHeartRatePriorToConfirm = 0x00490004;

    /** (0049,xx05) VR=CS VM=1 Max Heart Rate Prior To Confirm */
    public static final int MaxHeartRatePriorToConfirm = 0x00490005;

    /** (0049,xx06) VR=FL VM=1 Std Dev Heart Rate Prior To Confirm */
    public static final int StdDevHeartRatePriorToConfirm = 0x00490006;

    /**
     * (0049,xx07) VR=US VM=1 Num Heart Rate Samples Prior To Confirm
     */
    public static final int NumHeartRateSamplesPriorToConfirm = 0x00490007;

    /** (0049,xx08) VR=CS VM=1 Auto Heart Rate Detect Predict */
    public static final int AutoHeartRateDetectPredict = 0x00490008;

    /** (0049,xx09) VR=CS VM=1 System Optimized Heart Rate */
    public static final int SystemOptimizedHeartRate = 0x00490009;

    /** (0049,xx0A) VR=ST VM=1 Ekg Monitor Type */
    public static final int EkgMonitorType = 0x0049000A;

    /** (0049,xx0B) VR=CS VM=1 Num Recon Sectors */
    public static final int NumReconSectors = 0x0049000B;

    /** (0049,xx0C) VR=FL VM=256 Rpeak Time Stamps */
    public static final int RpeakTimeStamps = 0x0049000C;

    /** (0049,xx16) VR=SH VM=1 Ekg Gating Type */
    public static final int EkgGatingType = 0x00490016;

    /** (0049,xx1B) VR=FL VM=1 Ekg Wave Time Off First Data Point */
    public static final int EkgWaveTimeOffFirstDataPoint = 0x0049001B;

    /** (0049,xx22) VR=CS VM=1 Temporal Alg */
    public static final int TemporalAlg = 0x00490022;

    /** (0049,xx23) VR=CS VM=1 Phase Location */
    public static final int PhaseLocation = 0x00490023;

    /** (0049,xx24) VR=OW VM=1 Pre Blended Cycle 1 */
    public static final int PreBlendedCycle1 = 0x00490024;

    /** (0049,xx25) VR=OW VM=1 Pre Blended Cycle 2 */
    public static final int PreBlendedCycle2 = 0x00490025;

    /** (0049,xx26) VR=CS VM=1 Compression Alg */
    public static final int CompressionAlg = 0x00490026;

}
