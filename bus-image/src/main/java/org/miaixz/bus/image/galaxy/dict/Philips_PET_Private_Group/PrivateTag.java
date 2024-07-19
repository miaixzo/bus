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
package org.miaixz.bus.image.galaxy.dict.Philips_PET_Private_Group;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "Philips PET Private Group";

    /** (0511,xx00) VR=US VM=1 Private Data */
    public static final int PrivateData = 0x05110000;

    /** (7053,xx00) VR=DS VM=1 SUV Factor */
    public static final int SUVFactor = 0x70530000;

    /** (7053,xx03) VR=ST VM=1 Original File Name */
    public static final int OriginalFileName = 0x70530003;

    /** (7053,xx04) VR=OB VM=1 ? */
    public static final int _7053_xx04_ = 0x70530004;

    /** (7053,xx05) VR=LO VM=1 Worklist Info File Name */
    public static final int WorklistInfoFileName = 0x70530005;

    /** (7053,xx06) VR=OB VM=1 ? */
    public static final int _7053_xx06_ = 0x70530006;

    /** (7053,xx07) VR=SQ VM=1 ? */
    public static final int _7053_xx07_ = 0x70530007;

    /** (7053,xx08) VR=SQ VM=1 ? */
    public static final int _7053_xx08_ = 0x70530008;

    /** (7053,xx09) VR=DS VM=1 Activity Concentration Scale Factor */
    public static final int ActivityConcentrationScaleFactor = 0x70530009;

    /** (7053,xx0F) VR=UL VM=1 ? */
    public static final int _7053_xx0F_ = 0x7053000F;

    /** (7053,xx10) VR=US VM=1 ? */
    public static final int _7053_xx10_ = 0x70530010;

    /** (7053,xx11) VR=US VM=1 ? */
    public static final int _7053_xx11_ = 0x70530011;

    /** (7053,xx12) VR=SQ VM=1 ? */
    public static final int _7053_xx12_ = 0x70530012;

    /** (7053,xx13) VR=SS VM=1 ? */
    public static final int _7053_xx13_ = 0x70530013;

    /** (7053,xx14) VR=SS VM=1 ? */
    public static final int _7053_xx14_ = 0x70530014;

    /** (7053,xx15) VR=SS VM=1 ? */
    public static final int _7053_xx15_ = 0x70530015;

    /** (7053,xx16) VR=SS VM=1 ? */
    public static final int _7053_xx16_ = 0x70530016;

    /** (7053,xx17) VR=SS VM=1 ? */
    public static final int _7053_xx17_ = 0x70530017;

    /** (7053,xx18) VR=SS VM=1 ? */
    public static final int _7053_xx18_ = 0x70530018;

    /** (7053,xxC2) VR=UI VM=1 ? */
    public static final int _7053_xxC2_ = 0x705300C2;

}
