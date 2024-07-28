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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_CM_VA0__LAB;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "SIEMENS CM VA0 LAB";

    /** (0009,xx10) VR=LO VM=1 Generator Identification Label */
    public static final int GeneratorIdentificationLabel = 0x00090010;

    /** (0009,xx11) VR=LO VM=1 Gantry Identification Label */
    public static final int GantryIdentificationLabel = 0x00090011;

    /** (0009,xx12) VR=LO VM=1 X-Ray Tube Identification Label */
    public static final int XRayTubeIdentificationLabel = 0x00090012;

    /** (0009,xx13) VR=LO VM=1 Detector Identification Label */
    public static final int DetectorIdentificationLabel = 0x00090013;

    /** (0009,xx14) VR=LO VM=1 DAS Identification Label */
    public static final int DASIdentificationLabel = 0x00090014;

    /** (0009,xx15) VR=LO VM=1 SMI Identification Label */
    public static final int SMIIdentificationLabel = 0x00090015;

    /** (0009,xx16) VR=LO VM=1 CPU Identification Label */
    public static final int CPUIdentificationLabel = 0x00090016;

    /** (0009,xx20) VR=SH VM=1 Header Version */
    public static final int HeaderVersion = 0x00090020;

}
