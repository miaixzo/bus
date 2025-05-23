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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MED_SMS_USG_S2000;

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

        case PrivateTag._0019_xx30_:
            return VR.DS;
        case PrivateTag.FrameRate:
        case PrivateTag.BModeDynamicRange:
        case PrivateTag.BModeOverallGain:
        case PrivateTag.ColorFlowOverallGain:
        case PrivateTag.ColorFlowMaximumVelocity:
        case PrivateTag.DopplerDynamicRange:
        case PrivateTag.DopplerOverallGain:
        case PrivateTag.DopplerWallFilter:
        case PrivateTag.DopplerGateSize:
            return VR.FD;
        case PrivateTag._0019_xx3B_:
            return VR.LT;
        case PrivateTag.PrivateCreatorVersion:
        case PrivateTag.SieClearIndex:
        case PrivateTag.BModeSubmode:
        case PrivateTag.ClarifyVEIndex:
        case PrivateTag.ColorFlowState:
        case PrivateTag.ColorFlowSubmode:
        case PrivateTag.DopplerSubmode:
        case PrivateTag.MModeSubmode:
            return VR.SH;
        case PrivateTag.BurnedInGraphics:
        case PrivateTag._0019_xx0E_:
        case PrivateTag.BModeResolutionSpeedIndex:
        case PrivateTag.BModeEdgeEnhanceIndex:
        case PrivateTag.BModePersistenceIndex:
        case PrivateTag.BModeMapIndex:
        case PrivateTag._0019_xx27_:
        case PrivateTag._0019_xx28_:
        case PrivateTag._0019_xx29_:
        case PrivateTag.BModeTintType:
        case PrivateTag.BModeTintIndex:
        case PrivateTag._0019_xx31_:
        case PrivateTag.ImageFlag:
        case PrivateTag.ColorFlowWallFilterIndex:
        case PrivateTag.ColorFlowResolutionSpeedIndex:
        case PrivateTag.ColorFlowSmoothIndex:
        case PrivateTag.ColorFlowPersistenceIndex:
        case PrivateTag.ColorFlowMapIndex:
        case PrivateTag.ColorFlowPriorityIndex:
        case PrivateTag.DopplerMapIndex:
        case PrivateTag._0019_xx67_:
        case PrivateTag.DopplerTimeFreqResIndex:
        case PrivateTag.DopplerTraceInverted:
        case PrivateTag.DopplerTintType:
        case PrivateTag.DopplerTintIndex:
        case PrivateTag.MModeDynamicRange:
        case PrivateTag.MModeOverallGain:
        case PrivateTag.MModeEdgeEnhanceIndex:
        case PrivateTag.MModeMapIndex:
        case PrivateTag.MModeTintType:
        case PrivateTag.MModeTintIndex:
        case PrivateTag._0019_xx95_:
            return VR.US;
        }
        return VR.UN;
    }

}
