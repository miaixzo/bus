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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MR_MRS_05;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "SIEMENS MR MRS 05";

    /** (0021,xx01) VR=FD VM=1 Transmitter Reference Amplitude */
    public static final int TransmitterReferenceAmplitude = 0x00210001;

    /** (0021,xx02) VR=US VM=1 Hamming Filter Width */
    public static final int HammingFilterWidth = 0x00210002;

    /** (0021,xx03) VR=FD VM=3 CSI Gridshift Vector */
    public static final int CSIGridshiftVector = 0x00210003;

    /** (0021,xx04) VR=FD VM=1 Mixing Time */
    public static final int MixingTime = 0x00210004;

    /** (0021,xx40) VR=CS VM=1 Series Protocol Instance */
    public static final int SeriesProtocolInstance = 0x00210040;

    /** (0021,xx41) VR=CS VM=1 Spectro Result Type */
    public static final int SpectroResultType = 0x00210041;

    /** (0021,xx42) VR=CS VM=1 Spectro Result Extend Type */
    public static final int SpectroResultExtendType = 0x00210042;

    /** (0021,xx43) VR=CS VM=1 Post Proc Protocol */
    public static final int PostProcProtocol = 0x00210043;

    /** (0021,xx44) VR=CS VM=1 Rescan Level */
    public static final int RescanLevel = 0x00210044;

    /** (0021,xx45) VR=OF VM=1 Spectro Algo Result */
    public static final int SpectroAlgoResult = 0x00210045;

    /** (0021,xx46) VR=OF VM=1 Spectro Display Params */
    public static final int SpectroDisplayParams = 0x00210046;

    /** (0021,xx47) VR=IS VM=1 Voxel Number */
    public static final int VoxelNumber = 0x00210047;

    /** (0021,xx48) VR=SQ VM=1 APR Sequence */
    public static final int APRSequence = 0x00210048;

    /** (0021,xx49) VR=CS VM=1 Sync Data */
    public static final int SyncData = 0x00210049;

    /** (0021,xx4A) VR=CS VM=1 Post Proc Detailed Protocol */
    public static final int PostProcDetailedProtocol = 0x0021004A;

    /** (0021,xx4B) VR=CS VM=1 Spectro Result Extend Type Detailed */
    public static final int SpectroResultExtendTypeDetailed = 0x0021004B;

}
