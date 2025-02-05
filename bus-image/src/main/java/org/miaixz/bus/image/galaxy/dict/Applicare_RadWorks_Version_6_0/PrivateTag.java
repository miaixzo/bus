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
package org.miaixz.bus.image.galaxy.dict.Applicare_RadWorks_Version_6_0;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "Applicare/RadWorks/Version 6.0";

    /** (4103,xx01) VR=AT VM=1-n Non-existent tags */
    public static final int NonExistentTags = 0x41030001;

    /** (4103,xx02) VR=UI VM=1-n Non-existent objects */
    public static final int NonExistentObjects = 0x41030002;

    /** (4105,xx01) VR=CS VM=1 Annotation Type */
    public static final int AnnotationType = 0x41050001;

    /** (4105,xx02) VR=LO VM=1 Annotation Value */
    public static final int AnnotationValue = 0x41050002;

    /** (4105,xx03) VR=UI VM=1 Cutline Image UID */
    public static final int CutlineImageUID = 0x41050003;

    /** (4105,xx04) VR=UI VM=1 Cutline Set UID */
    public static final int CutlineSetUID = 0x41050004;

    /** (4105,xx05) VR=US VM=3 Annotation Color (RGB) */
    public static final int AnnotationColor = 0x41050005;

    /** (4105,xx06) VR=CS VM=1 Annotation Line Style */
    public static final int AnnotationLineStyle = 0x41050006;

    /** (4105,xx07) VR=SH VM=1 Annotation Label */
    public static final int AnnotationLabel = 0x41050007;

    /** (4105,xx08) VR=PN VM=1 Annotation Creator */
    public static final int AnnotationCreator = 0x41050008;

    /** (4105,xx09) VR=DA VM=1 Annotation Creation Date */
    public static final int AnnotationCreationDate = 0x41050009;

    /** (4105,xx0A) VR=TM VM=1 Annotation Creation Time */
    public static final int AnnotationCreationTime = 0x4105000A;

    /** (4105,xx0B) VR=SQ VM=1 Annotation Modification Sequence */
    public static final int AnnotationModificationSequence = 0x4105000B;

    /** (4105,xx0C) VR=PN VM=1 Annotation Modifier */
    public static final int AnnotationModifier = 0x4105000C;

    /** (4105,xx0D) VR=DA VM=1 Annotation Modification Date */
    public static final int AnnotationModificationDate = 0x4105000D;

    /** (4105,xx0E) VR=TM VM=1 Annotation Modification Time */
    public static final int AnnotationModificationTime = 0x4105000E;

    /** (4105,xx10) VR=US VM=1 ? */
    public static final int _4105_xx10_ = 0x41050010;

    /** (4105,xx11) VR=ST VM=1 ? */
    public static final int _4105_xx11_ = 0x41050011;

    /** (4107,xx01) VR=SQ VM=1 Requested Palette Color LUT */
    public static final int RequestedPaletteColorLUT = 0x41070001;

}
