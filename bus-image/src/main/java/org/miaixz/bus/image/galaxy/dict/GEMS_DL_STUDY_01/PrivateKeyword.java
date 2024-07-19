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

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {
    
        switch (tag & 0xFFFF00FF) {
                case PrivateTag.StudyDose:
            return "StudyDose";
        case PrivateTag.StudyTotalDap:
            return "StudyTotalDap";
        case PrivateTag.FluoroDoseAreaProduct:
            return "FluoroDoseAreaProduct";
        case PrivateTag.StudyFluoroTime:
            return "StudyFluoroTime";
        case PrivateTag.CineDoseAreaProduct:
            return "CineDoseAreaProduct";
        case PrivateTag.StudyRecordTime:
            return "StudyRecordTime";
        case PrivateTag.LastXANumber:
            return "LastXANumber";
        case PrivateTag.DefOperatorName:
            return "DefOperatorName";
        case PrivateTag.DefPerformingPhysicianName:
            return "DefPerformingPhysicianName";
        case PrivateTag.DefPatientOrientation:
            return "DefPatientOrientation";
        case PrivateTag.LastScNumber:
            return "LastScNumber";
        case PrivateTag.CommonSeriesInstanceUID:
            return "CommonSeriesInstanceUID";
        case PrivateTag.StudyNumber:
            return "StudyNumber";
        case PrivateTag._0015_xx92_:
            return "_0015_xx92_";
        case PrivateTag._0015_xx93_:
            return "_0015_xx93_";
        case PrivateTag._0015_xx94_:
            return "_0015_xx94_";
        case PrivateTag._0015_xx95_:
            return "_0015_xx95_";
        case PrivateTag._0015_xx96_:
            return "_0015_xx96_";
        case PrivateTag._0015_xx97_:
            return "_0015_xx97_";
        case PrivateTag._0015_xx98_:
            return "_0015_xx98_";
        case PrivateTag._0015_xx99_:
            return "_0015_xx99_";
        case PrivateTag._0015_xx9A_:
            return "_0015_xx9A_";
        case PrivateTag._0015_xx9B_:
            return "_0015_xx9B_";
        case PrivateTag._0015_xx9C_:
            return "_0015_xx9C_";
        case PrivateTag._0015_xx9D_:
            return "_0015_xx9D_";
        }
        return "";
    }

}
