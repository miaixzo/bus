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
package org.miaixz.bus.image.galaxy.dict.Applicare_Workflow_Version_1_0;

import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;

/**
 * Represents the PrivateElementDictionary type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class PrivateElementDictionary extends ElementDictionary {

    /**
     * The private creator value.
     */
    public static final String PrivateCreator = "";

    /**
     * Creates a new instance.
     */
    public PrivateElementDictionary() {
        super("", PrivateTag.class);
    }

    /**
     * Executes the keyword of operation.
     *
     * @param tag the tag.
     * @return the operation result.
     */
    @Override
    public String keywordOf(int tag) {
        return PrivateKeyword.valueOf(tag);
    }

    /**
     * Executes the vr of operation.
     *
     * @param tag the tag.
     * @return the operation result.
     */
    @Override
    public VR vrOf(int tag) {

        switch (tag & 0xFFFF00FF) {

            case PrivateTag.OrderControl:
            case PrivateTag.LockedByHostname:
            case PrivateTag.LockedByUser:
            case PrivateTag.KfEditLockUser:
                return VR.CS;

            case PrivateTag.ScheduledActionItemCodeMeaning:
            case PrivateTag.RequestedActionItemCodeMeaning:
            case PrivateTag.PerformedActionItemCodeMeaning:
            case PrivateTag.PerformedProcedureCodeMeaning:
                return VR.LO;

            case PrivateTag.ScheduledActionItemCodeValue:
            case PrivateTag.ScheduledActionItemCodingSchemeDesignator:
            case PrivateTag.RequestedActionItemCodeValue:
            case PrivateTag.RequestedActionItemCodingSchemeDesignator:
            case PrivateTag.PerformedActionItemCodeValue:
            case PrivateTag.PerformedActionItemCodingSchemeDesignator:
            case PrivateTag.PerformedProcedureCodeValue:
            case PrivateTag.PerformedProcedureCodingSchemeDesignator:
                return VR.SH;

            case PrivateTag.ReferencedImageSOPClassUID:
            case PrivateTag.ReferencedImageSOPInstanceUID:
                return VR.UI;
        }
        return VR.UN;
    }

}
