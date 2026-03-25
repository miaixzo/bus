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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_SYNGO_EVIDENCE_DOCUMENT_DATA;

/**
 * @author Kimi Liu
 * @since Java 21+
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
