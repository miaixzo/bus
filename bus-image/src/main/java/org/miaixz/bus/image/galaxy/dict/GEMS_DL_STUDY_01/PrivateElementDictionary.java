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
package org.miaixz.bus.image.galaxy.dict.GEMS_DL_STUDY_01;

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

        case PrivateTag.DefPatientOrientation:
            return VR.CS;
        case PrivateTag.StudyDose:
        case PrivateTag.StudyTotalDap:
        case PrivateTag.FluoroDoseAreaProduct:
        case PrivateTag.CineDoseAreaProduct:
            return VR.DS;
        case PrivateTag._0015_xx92_:
        case PrivateTag._0015_xx93_:
        case PrivateTag._0015_xx94_:
        case PrivateTag._0015_xx96_:
        case PrivateTag._0015_xx98_:
        case PrivateTag._0015_xx99_:
        case PrivateTag._0015_xx9A_:
        case PrivateTag._0015_xx9C_:
            return VR.FL;
        case PrivateTag.StudyFluoroTime:
        case PrivateTag.StudyRecordTime:
        case PrivateTag.LastXANumber:
        case PrivateTag.LastScNumber:
        case PrivateTag.StudyNumber:
        case PrivateTag._0015_xx95_:
        case PrivateTag._0015_xx97_:
        case PrivateTag._0015_xx9B_:
        case PrivateTag._0015_xx9D_:
            return VR.IS;
        case PrivateTag.DefOperatorName:
        case PrivateTag.DefPerformingPhysicianName:
            return VR.PN;
        case PrivateTag.CommonSeriesInstanceUID:
            return VR.UI;
        }
        return VR.UN;
    }
}
