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
