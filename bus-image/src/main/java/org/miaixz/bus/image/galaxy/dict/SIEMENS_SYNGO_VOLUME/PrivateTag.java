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
