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
package org.miaixz.bus.image.galaxy.dict.Applicare_RadWorks_Version_6_0;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.NonExistentTags:
                return "NonExistentTags";

            case PrivateTag.NonExistentObjects:
                return "NonExistentObjects";

            case PrivateTag.AnnotationType:
                return "AnnotationType";

            case PrivateTag.AnnotationValue:
                return "AnnotationValue";

            case PrivateTag.CutlineImageUID:
                return "CutlineImageUID";

            case PrivateTag.CutlineSetUID:
                return "CutlineSetUID";

            case PrivateTag.AnnotationColor:
                return "AnnotationColor";

            case PrivateTag.AnnotationLineStyle:
                return "AnnotationLineStyle";

            case PrivateTag.AnnotationLabel:
                return "AnnotationLabel";

            case PrivateTag.AnnotationCreator:
                return "AnnotationCreator";

            case PrivateTag.AnnotationCreationDate:
                return "AnnotationCreationDate";

            case PrivateTag.AnnotationCreationTime:
                return "AnnotationCreationTime";

            case PrivateTag.AnnotationModificationSequence:
                return "AnnotationModificationSequence";

            case PrivateTag.AnnotationModifier:
                return "AnnotationModifier";

            case PrivateTag.AnnotationModificationDate:
                return "AnnotationModificationDate";

            case PrivateTag.AnnotationModificationTime:
                return "AnnotationModificationTime";

            case PrivateTag._4105_xx10_:
                return "_4105_xx10_";

            case PrivateTag._4105_xx11_:
                return "_4105_xx11_";

            case PrivateTag.RequestedPaletteColorLUT:
                return "RequestedPaletteColorLUT";
        }
        return "";
    }

}
