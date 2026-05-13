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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MED;

/**
 * Represents the PrivateKeyword type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class PrivateKeyword {

    /**
     * The private creator value.
     */
    public static final String PrivateCreator = "";

    /**
     * Executes the value of operation.
     *
     * @param tag the tag.
     * @return the operation result.
     */
    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.RecognitionCode:
                return "RecognitionCode";

            case PrivateTag.ByteOffsetOfOriginalHeader:
                return "ByteOffsetOfOriginalHeader";

            case PrivateTag.LengthOfOriginalHeader:
                return "LengthOfOriginalHeader";

            case PrivateTag.ByteOffsetOfPixelmatrix:
                return "ByteOffsetOfPixelmatrix";

            case PrivateTag.LengthOfPixelmatrixInBytes:
                return "LengthOfPixelmatrixInBytes";

            case PrivateTag._0009_xx50_:
                return "_0009_xx50_";

            case PrivateTag._0009_xx51_:
                return "_0009_xx51_";

            case PrivateTag.PDMEFIDPlaceholder:
                return "PDMEFIDPlaceholder";

            case PrivateTag.PDMDataObjectTypeExtension:
                return "PDMDataObjectTypeExtension";

            case PrivateTag.Zoom:
                return "Zoom";

            case PrivateTag.Target:
                return "Target";

            case PrivateTag.TubeAngle:
                return "TubeAngle";

            case PrivateTag.ROIMask:
                return "ROIMask";

            case PrivateTag.Dummy:
                return "Dummy";

            case PrivateTag.Header:
                return "Header";
        }
        return "";
    }

}
