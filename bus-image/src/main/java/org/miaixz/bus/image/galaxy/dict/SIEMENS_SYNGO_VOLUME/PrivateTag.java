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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_SYNGO_VOLUME;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "SIEMENS SYNGO VOLUME";

    /** (0029,xx12) VR=US VM=1 Slices */
    public static final int Slices = 0x00290012;

    /** (0029,xx14) VR=OB VM=1 Volume Histogram */
    public static final int VolumeHistogram = 0x00290014;

    /** (0029,xx18) VR=IS VM=1 Volume Level */
    public static final int VolumeLevel = 0x00290018;

    /** (0029,xx30) VR=DS VM=3 Voxel Spacing */
    public static final int VoxelSpacing = 0x00290030;

    /** (0029,xx32) VR=DS VM=3 Volume Position (Patient) */
    public static final int VolumePositionPatient = 0x00290032;

    /** (0029,xx37) VR=DS VM=9 Volume Orientation (Patient) */
    public static final int VolumeOrientationPatient = 0x00290037;

    /** (0029,xx40) VR=CS VM=1 Resampling Flag */
    public static final int ResamplingFlag = 0x00290040;

    /** (0029,xx42) VR=CS VM=1 Normalization Flag */
    public static final int NormalizationFlag = 0x00290042;

    /** (0029,xx44) VR=SQ VM=1 SubVolume Sequence */
    public static final int SubVolumeSequence = 0x00290044;

    /** (0029,xx46) VR=UL VM=1 Histogram Number Of Bins */
    public static final int HistogramNumberOfBins = 0x00290046;

    /** (0029,xx47) VR=OB VM=1 Volume Histogram Data */
    public static final int VolumeHistogramData = 0x00290047;

}
