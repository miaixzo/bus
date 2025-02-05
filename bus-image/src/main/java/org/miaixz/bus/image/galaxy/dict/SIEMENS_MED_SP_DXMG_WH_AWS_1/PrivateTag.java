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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MED_SP_DXMG_WH_AWS_1;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "SIEMENS MED SP DXMG WH AWS 1";

    /** (0019,xx01) VR=UL VM=1-n AEC Coordinates */
    public static final int AECCoordinates = 0x00190001;

    /** (0019,xx02) VR=US VM=2 AEC Coordinates Size */
    public static final int AECCoordinatesSize = 0x00190002;

    /** (0019,xx10) VR=ST VM=1 Derivation Description */
    public static final int DerivationDescription = 0x00190010;

    /** (0041,xx02) VR=SH VM=1 Reason for the Requested Procedure */
    public static final int ReasonForTheRequestedProcedure = 0x00410002;

    /** (0051,xx10) VR=DS VM=1 ? */
    public static final int _0051_xx10_ = 0x00510010;

    /** (0051,xx20) VR=DS VM=1-n ? */
    public static final int _0051_xx20_ = 0x00510020;

    /** (0051,xx21) VR=LO VM=1 ? */
    public static final int _0051_xx21_ = 0x00510021;

    /** (0051,xx32) VR=DS VM=3 ? */
    public static final int _0051_xx32_ = 0x00510032;

    /** (0051,xx37) VR=DS VM=6 ? */
    public static final int _0051_xx37_ = 0x00510037;

    /** (0051,xx50) VR=UI VM=1 ? */
    public static final int _0051_xx50_ = 0x00510050;

    /** (0051,xx60) VR=DS VM=1 Primary Positioner Scan Arc */
    public static final int PrimaryPositionerScanArc = 0x00510060;

    /** (0051,xx61) VR=DS VM=1 Secondary Positioner Scan Arc */
    public static final int SecondaryPositionerScanArc = 0x00510061;

    /** (0051,xx62) VR=DS VM=1 Primary Positioner Scan Start Angle */
    public static final int PrimaryPositionerScanStartAngle = 0x00510062;

    /**
     * (0051,xx63) VR=DS VM=1 Secondary Positioner Scan Start Angle
     */
    public static final int SecondaryPositionerScanStartAngle = 0x00510063;

    /** (0051,xx64) VR=DS VM=1 Primary Positioner Increment */
    public static final int PrimaryPositionerIncrement = 0x00510064;

    /** (0051,xx65) VR=DS VM=1 Secondary Positioner Increment */
    public static final int SecondaryPositionerIncrement = 0x00510065;

    /** (0055,xx01) VR=LO VM=1 Projection View Display String */
    public static final int ProjectionViewDisplayString = 0x00550001;

}
