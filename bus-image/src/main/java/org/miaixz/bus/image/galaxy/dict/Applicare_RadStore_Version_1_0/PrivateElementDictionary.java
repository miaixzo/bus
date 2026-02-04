/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.galaxy.dict.Applicare_RadStore_Version_1_0;

import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateElementDictionary extends ElementDictionary {

    public static final String PrivateCreator = "";

    public PrivateElementDictionary() {
        super("", PrivateTag.class);
    }

    @Override
    public String keywordOf(int tag) {
        return PrivateKeyword.valueOf(tag);
    }

    @Override
    public VR vrOf(int tag) {

        switch (tag & 0xFFFF00FF) {

            case PrivateTag.DeletedTags:
                return VR.AT;

            case PrivateTag.InstanceState:
            case PrivateTag._3113_xx15_:
            case PrivateTag.ImageMediumState:
            case PrivateTag.SeriesMediumState:
            case PrivateTag.StudyMediumState:
            case PrivateTag.StudyState:
            case PrivateTag.SeriesState:
            case PrivateTag.ImageStateText:
            case PrivateTag.SeriesStateText:
            case PrivateTag.StudyStateText:
                return VR.CS;

            case PrivateTag.DateLastModified:
            case PrivateTag.DateLastAccessed:
            case PrivateTag.Expiration:
                return VR.DT;

            case PrivateTag.InstanceSizeInBytes:
                return VR.FD;

            case PrivateTag._3113_xx31_:
            case PrivateTag._3113_xx32_:
            case PrivateTag._3113_xx33_:
                return VR.IS;

            case PrivateTag._3113_xx11_:
            case PrivateTag.LibraryId:
            case PrivateTag.Pathnames:
            case PrivateTag.DriverPath:
            case PrivateTag.Source:
            case PrivateTag.Destination:
            case PrivateTag.ArchiveId:
            case PrivateTag.InstanceOrigin:
            case PrivateTag.ImageMediumLocation:
            case PrivateTag.ImageMediumLabel:
            case PrivateTag.SeriesMediumLocation:
            case PrivateTag.SeriesMediumLabel:
            case PrivateTag.StudyMediumLocation:
            case PrivateTag.StudyMediumLabel:
                return VR.LO;

            case PrivateTag._3113_xx01_:
            case PrivateTag.Id1:
            case PrivateTag.Id2:
            case PrivateTag.Id3:
            case PrivateTag.MediumId:
            case PrivateTag.InstanceVersion:
            case PrivateTag._3113_xx22_:
                return VR.SL;

            case PrivateTag.InstanceFileLocation:
                return VR.ST;
        }
        return VR.UN;
    }

}
