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
package org.miaixz.bus.image.galaxy.dict.Applicare_RadStore_Version_1_0;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag._3113_xx01_:
                return "_3113_xx01_";

            case PrivateTag.Id1:
                return "Id1";

            case PrivateTag.Id2:
                return "Id2";

            case PrivateTag.Id3:
                return "Id3";

            case PrivateTag._3113_xx11_:
                return "_3113_xx11_";

            case PrivateTag.InstanceState:
                return "InstanceState";

            case PrivateTag.DateLastModified:
                return "DateLastModified";

            case PrivateTag.DateLastAccessed:
                return "DateLastAccessed";

            case PrivateTag._3113_xx15_:
                return "_3113_xx15_";

            case PrivateTag.InstanceSizeInBytes:
                return "InstanceSizeInBytes";

            case PrivateTag.LibraryId:
                return "LibraryId";

            case PrivateTag.Pathnames:
                return "Pathnames";

            case PrivateTag.DriverPath:
                return "DriverPath";

            case PrivateTag.Source:
                return "Source";

            case PrivateTag.Destination:
                return "Destination";

            case PrivateTag.MediumId:
                return "MediumId";

            case PrivateTag.ArchiveId:
                return "ArchiveId";

            case PrivateTag.InstanceOrigin:
                return "InstanceOrigin";

            case PrivateTag.InstanceVersion:
                return "InstanceVersion";

            case PrivateTag._3113_xx22_:
                return "_3113_xx22_";

            case PrivateTag.InstanceFileLocation:
                return "InstanceFileLocation";

            case PrivateTag._3113_xx31_:
                return "_3113_xx31_";

            case PrivateTag._3113_xx32_:
                return "_3113_xx32_";

            case PrivateTag._3113_xx33_:
                return "_3113_xx33_";

            case PrivateTag.ImageMediumLocation:
                return "ImageMediumLocation";

            case PrivateTag.ImageMediumLabel:
                return "ImageMediumLabel";

            case PrivateTag.ImageMediumState:
                return "ImageMediumState";

            case PrivateTag.SeriesMediumLocation:
                return "SeriesMediumLocation";

            case PrivateTag.SeriesMediumLabel:
                return "SeriesMediumLabel";

            case PrivateTag.SeriesMediumState:
                return "SeriesMediumState";

            case PrivateTag.StudyMediumLocation:
                return "StudyMediumLocation";

            case PrivateTag.StudyMediumLabel:
                return "StudyMediumLabel";

            case PrivateTag.StudyMediumState:
                return "StudyMediumState";

            case PrivateTag.StudyState:
                return "StudyState";

            case PrivateTag.SeriesState:
                return "SeriesState";

            case PrivateTag.ImageStateText:
                return "ImageStateText";

            case PrivateTag.SeriesStateText:
                return "SeriesStateText";

            case PrivateTag.StudyStateText:
                return "StudyStateText";

            case PrivateTag.Expiration:
                return "Expiration";

            case PrivateTag.DeletedTags:
                return "DeletedTags";
        }
        return "";
    }

}
