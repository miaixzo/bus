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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_SYNGO_EVIDENCE_DOCUMENT_DATA;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "SIEMENS SYNGO EVIDENCE DOCUMENT DATA";

    /** (0077,xx10) VR=LO VM=1 Evidence Document Template Name */
    public static final int EvidenceDocumentTemplateName = 0x00770010;

    /** (0077,xx11) VR=DS VM=1 Evidence Document Template Version */
    public static final int EvidenceDocumentTemplateVersion = 0x00770011;

    /** (0077,xx20) VR=OB VM=1 Clinical Finding Data */
    public static final int ClinicalFindingData = 0x00770020;

    /** (0077,xx21) VR=OB VM=1 Metadata */
    public static final int Metadata = 0x00770021;

    /** (0077,xx30) VR=DS VM=1 Implementation Version */
    public static final int ImplementationVersion = 0x00770030;

    /** (0077,xx40) VR=OB VM=1 Predecessor */
    public static final int Predecessor = 0x00770040;

    /** (0077,xx50) VR=LO VM=1 Logical ID */
    public static final int LogicalID = 0x00770050;

    /** (0077,xx60) VR=OB VM=1 Application Data */
    public static final int ApplicationData = 0x00770060;

    /** (0077,xx70) VR=LO VM=1 Owner Clinical Task Name */
    public static final int OwnerClinicalTaskName = 0x00770070;

    /** (0077,xx71) VR=LO VM=1 Owner Task Name */
    public static final int OwnerTaskName = 0x00770071;

    /** (0077,xx72) VR=OB VM=1 Owner Supported Templates */
    public static final int OwnerSupportedTemplates = 0x00770072;

    /** (0077,xx80) VR=OB VM=1 Volume Catalog */
    public static final int VolumeCatalog = 0x00770080;

}
