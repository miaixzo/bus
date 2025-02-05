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
package org.miaixz.bus.image.galaxy.dict.GEMS_IMAG_01;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "GEMS_IMAG_01";

    /** (0027,xx06) VR=SL VM=1 Image Archive Flag */
    public static final int ImageArchiveFlag = 0x00270006;

    /** (0027,xx10) VR=SS VM=1 Scout Type */
    public static final int ScoutType = 0x00270010;

    /** (0027,xx1C) VR=SL VM=1 Vma Mamp */
    public static final int VmaMamp = 0x0027001C;

    /** (0027,xx1D) VR=SS VM=1 Vma Phase */
    public static final int VmaPhase = 0x0027001D;

    /** (0027,xx1E) VR=SL VM=1 Vma Mod */
    public static final int VmaMod = 0x0027001E;

    /** (0027,xx1F) VR=SL VM=1 Vma Clip or Noise Index by 10 */
    public static final int VmaClipOrNoiseIndexBy10 = 0x0027001F;

    /** (0027,xx20) VR=SS VM=1 Smart Scan On Off Flag */
    public static final int SmartScanOnOffFlag = 0x00270020;

    /** (0027,xx30) VR=SH VM=1 Foreign Image Revision */
    public static final int ForeignImageRevision = 0x00270030;

    /** (0027,xx31) VR=SS VM=1 Imaging Mode */
    public static final int ImagingMode = 0x00270031;

    /** (0027,xx32) VR=SS VM=1 Pulse Sequence */
    public static final int PulseSequence = 0x00270032;

    /** (0027,xx33) VR=SL VM=1 Imaging Options */
    public static final int ImagingOptions = 0x00270033;

    /** (0027,xx35) VR=SS VM=1 Plane Type */
    public static final int PlaneType = 0x00270035;

    /** (0027,xx36) VR=SL VM=1 Oblique Plane */
    public static final int ObliquePlane = 0x00270036;

    /** (0027,xx40) VR=SH VM=1 RAS Letter Of Image Location */
    public static final int RASLetterOfImageLocation = 0x00270040;

    /** (0027,xx41) VR=FL VM=1 Image Location */
    public static final int ImageLocation = 0x00270041;

    /** (0027,xx42) VR=FL VM=1 Center R Coord Of Plane Image */
    public static final int CenterRCoordOfPlaneImage = 0x00270042;

    /** (0027,xx43) VR=FL VM=1 Center A Coord Of Plane Image */
    public static final int CenterACoordOfPlaneImage = 0x00270043;

    /** (0027,xx44) VR=FL VM=1 Center S Coord Of Plane Image */
    public static final int CenterSCoordOfPlaneImage = 0x00270044;

    /** (0027,xx45) VR=FL VM=1 Normal R Coord */
    public static final int NormalRCoord = 0x00270045;

    /** (0027,xx46) VR=FL VM=1 Normal A Coord */
    public static final int NormalACoord = 0x00270046;

    /** (0027,xx47) VR=FL VM=1 Normal S Coord */
    public static final int NormalSCoord = 0x00270047;

    /** (0027,xx48) VR=FL VM=1 R Coord Of Top Right Corner */
    public static final int RCoordOfTopRightCorner = 0x00270048;

    /** (0027,xx49) VR=FL VM=1 A Coord Of Top Right Corner */
    public static final int ACoordOfTopRightCorner = 0x00270049;

    /** (0027,xx4A) VR=FL VM=1 S Coord Of Top Right Corner */
    public static final int SCoordOfTopRightCorner = 0x0027004A;

    /** (0027,xx4B) VR=FL VM=1 R Coord Of Bottom Right Corner */
    public static final int RCoordOfBottomRightCorner = 0x0027004B;

    /** (0027,xx4C) VR=FL VM=1 A Coord Of Bottom Right Corner */
    public static final int ACoordOfBottomRightCorner = 0x0027004C;

    /** (0027,xx4D) VR=FL VM=1 S Coord Of Bottom Right Corner */
    public static final int SCoordOfBottomRightCorner = 0x0027004D;

    /** (0027,xx50) VR=FL VM=1 Table Start Location (Scout) */
    public static final int TableStartLocation = 0x00270050;

    /** (0027,xx51) VR=FL VM=1 Table End Location (Scout) */
    public static final int TableEndLocation = 0x00270051;

    /** (0027,xx52) VR=SH VM=1 RAS Letter For Side Of Image */
    public static final int RASLetterForSideOfImage = 0x00270052;

    /** (0027,xx53) VR=SH VM=1 RAS Letter For Anterior Posterior */
    public static final int RASLetterForAnteriorPosterior = 0x00270053;

    /** (0027,xx54) VR=SH VM=1 RAS Letter For Scout Start Loc */
    public static final int RASLetterForScoutStartLoc = 0x00270054;

    /** (0027,xx55) VR=SH VM=1 RAS Letter For Scout End Loc */
    public static final int RASLetterForScoutEndLoc = 0x00270055;

    /** (0027,xx60) VR=FL VM=1 Image Dimension X */
    public static final int ImageDimensionX = 0x00270060;

    /** (0027,xx61) VR=FL VM=1 Image Dimension Y */
    public static final int ImageDimensionY = 0x00270061;

    /** (0027,xx62) VR=FL VM=1 Number Of Excitations */
    public static final int NumberOfExcitations = 0x00270062;

}
