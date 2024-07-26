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
package org.miaixz.bus.image.galaxy.dict.SMIL_PB79;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
        case PrivateTag.Analgesia:
            return "Analgesia";
        case PrivateTag.Anesthesia:
            return "Anesthesia";
        case PrivateTag.BedMotion:
            return "BedMotion";
        case PrivateTag.FoodAccess:
            return "FoodAccess";
        case PrivateTag.HistogramVersion:
            return "HistogramVersion";
        case PrivateTag.InjectionDecayCorrection:
            return "InjectionDecayCorrection";
        case PrivateTag.Isotope:
            return "Isotope";
        case PrivateTag.OtherDrugs:
            return "OtherDrugs";
        case PrivateTag.RebinningType:
            return "RebinningType";
        case PrivateTag.RebinningVersion:
            return "RebinningVersion";
        case PrivateTag.Reconstruction:
            return "Reconstruction";
        case PrivateTag.ReconstructionVersion:
            return "ReconstructionVersion";
        case PrivateTag.InjectedCompound:
            return "InjectedCompound";
        case PrivateTag.StudyModel:
            return "StudyModel";
        case PrivateTag.SubjectGenus:
            return "SubjectGenus";
        case PrivateTag.SubjectPhenotype:
            return "SubjectPhenotype";
        case PrivateTag.Version:
            return "Version";
        case PrivateTag.WaterAccess:
            return "WaterAccess";
        case PrivateTag.XOffset:
            return "XOffset";
        case PrivateTag.YOffset:
            return "YOffset";
        case PrivateTag.Zoom:
            return "Zoom";
        case PrivateTag.SubjectOrientation:
            return "SubjectOrientation";
        }
        return "";
    }

}
