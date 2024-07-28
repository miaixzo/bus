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
package org.miaixz.bus.image.galaxy.dict.SPI_P_Release_1_1;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "SPI-P Release 1;1";

    /** (0009,xxC0) VR=LT VM=1 ? */
    public static final int _0009_xxC0_ = 0x000900C0;

    /** (0009,xxC1) VR=LT VM=1 ? */
    public static final int _0009_xxC1_ = 0x000900C1;

    /** (0019,xx00) VR=UN VM=1 Physiological Data Type */
    public static final int PhysiologicalDataType = 0x00190000;

    /** (0019,xx01) VR=UN VM=1 Physiological Data Channel And Kind */
    public static final int PhysiologicalDataChannelAndKind = 0x00190001;

    /** (0019,xx02) VR=US VM=1 Sample Bits Allocated */
    public static final int SampleBitsAllocated = 0x00190002;

    /** (0019,xx03) VR=US VM=1 Sample Bits Stored */
    public static final int SampleBitsStored = 0x00190003;

    /** (0019,xx04) VR=US VM=1 Sample High Bit */
    public static final int SampleHighBit = 0x00190004;

    /** (0019,xx05) VR=US VM=1 Sample Representation */
    public static final int SampleRepresentation = 0x00190005;

    /** (0019,xx06) VR=UN VM=1 Smallest Sample Value */
    public static final int SmallestSampleValue = 0x00190006;

    /** (0019,xx07) VR=UN VM=1 Largest Sample Value */
    public static final int LargestSampleValue = 0x00190007;

    /** (0019,xx08) VR=UN VM=1 Number Of Samples */
    public static final int NumberOfSamples = 0x00190008;

    /** (0019,xx09) VR=UN VM=1 Sample Data */
    public static final int SampleData = 0x00190009;

    /** (0019,xx0A) VR=UN VM=1 Sample Rate */
    public static final int SampleRate = 0x0019000A;

    /** (0019,xx10) VR=UN VM=1 Physiological Data Type 2 */
    public static final int PhysiologicalDataType2 = 0x00190010;

    /** (0019,xx11) VR=UN VM=1 Physiological Data Channel And Kind 2 */
    public static final int PhysiologicalDataChannelAndKind2 = 0x00190011;

    /** (0019,xx12) VR=US VM=1 Sample Bits Allocated 2 */
    public static final int SampleBitsAllocated2 = 0x00190012;

    /** (0019,xx13) VR=US VM=1 Sample Bits Stored 2 */
    public static final int SampleBitsStored2 = 0x00190013;

    /** (0019,xx14) VR=US VM=1 Sample High Bit 2 */
    public static final int SampleHighBit2 = 0x00190014;

    /** (0019,xx15) VR=US VM=1 Sample Representation 2 */
    public static final int SampleRepresentation2 = 0x00190015;

    /** (0019,xx16) VR=UN VM=1 Smallest Sample Value 2 */
    public static final int SmallestSampleValue2 = 0x00190016;

    /** (0019,xx17) VR=UN VM=1 Largest Sample Value 2 */
    public static final int LargestSampleValue2 = 0x00190017;

    /** (0019,xx18) VR=UN VM=1 Number Of Samples 2 */
    public static final int NumberOfSamples2 = 0x00190018;

    /** (0019,xx19) VR=UN VM=1 Sample Data 2 */
    public static final int SampleData2 = 0x00190019;

    /** (0019,xx1A) VR=UN VM=1 Sample Rate 2 */
    public static final int SampleRate2 = 0x0019001A;

    /** (0029,xx00) VR=LT VM=1 Zoom ID */
    public static final int ZoomID = 0x00290000;

    /** (0029,xx01) VR=DS VM=1-n Zoom Rectangle */
    public static final int ZoomRectangle = 0x00290001;

    /** (0029,xx03) VR=DS VM=1 Zoom Factor */
    public static final int ZoomFactor = 0x00290003;

    /** (0029,xx04) VR=US VM=1 Zoom Function */
    public static final int ZoomFunction = 0x00290004;

    /** (0029,xx0E) VR=CS VM=1 Zoom Enable Status */
    public static final int ZoomEnableStatus = 0x0029000E;

    /** (0029,xx0F) VR=CS VM=1 Zoom Select Status */
    public static final int ZoomSelectStatus = 0x0029000F;

    /** (0029,xx40) VR=LT VM=1 Magnifying Glass ID */
    public static final int MagnifyingGlassID = 0x00290040;

    /** (0029,xx41) VR=DS VM=1-n Magnifying Glass Rectangle */
    public static final int MagnifyingGlassRectangle = 0x00290041;

    /** (0029,xx43) VR=DS VM=1 Magnifying Glass Factor */
    public static final int MagnifyingGlassFactor = 0x00290043;

    /** (0029,xx44) VR=US VM=1 Magnifying Glass Function */
    public static final int MagnifyingGlassFunction = 0x00290044;

    /** (0029,xx4E) VR=CS VM=1 Magnifying Glass Enable Status */
    public static final int MagnifyingGlassEnableStatus = 0x0029004E;

    /** (0029,xx4F) VR=CS VM=1 Magnifying Glass Select Status */
    public static final int MagnifyingGlassSelectStatus = 0x0029004F;

}
