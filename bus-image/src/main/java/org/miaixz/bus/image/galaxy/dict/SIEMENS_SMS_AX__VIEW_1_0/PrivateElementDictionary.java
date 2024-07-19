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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_SMS_AX__VIEW_1_0;

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
        
            case PrivateTag.AutoWindowCenter:
            case PrivateTag.AutoWindowWidth:
            case PrivateTag.SigmoidWindowParameter:
                return VR.DS;
            case PrivateTag.AutoWindowCorrectValue:
                return VR.IS;
            case PrivateTag.Quant1KOverlay:
                return VR.OB;
            case PrivateTag.DispayedAreaTopLeftHandCorner:
            case PrivateTag.DispayedAreaBottomRightHandCorner:
                return VR.SL;
            case PrivateTag.PixelShiftArray:
            case PrivateTag.NativeEdgeEnhancementLUTIndex:
            case PrivateTag.NativeEdgeEnhancementKernelSize:
            case PrivateTag.SubtractedEdgeEnhancementLUTIndex:
            case PrivateTag.SubtractedEdgeEnhancementKernelSize:
            case PrivateTag.PanX:
            case PrivateTag.PanY:
            case PrivateTag.NativeEdgeEnhancementAdvPercentGain:
            case PrivateTag.SubtractedEdgeEnhancementAdvPercentGain:
                return VR.SS;
            case PrivateTag.ReviewMode:
            case PrivateTag.AnatomicalBackgroundPercent:
            case PrivateTag.NumberOfPhases:
            case PrivateTag.ApplyAnatomicalBackground:
            case PrivateTag.Brightness:
            case PrivateTag.Contrast:
            case PrivateTag.EnabledShutters:
            case PrivateTag.NativeEdgeEnhancementPercentGain:
            case PrivateTag.SubtractedEdgeEnhancementPercentGain:
            case PrivateTag.FadePercent:
            case PrivateTag.FlippedBeforeLateralityApplied:
            case PrivateTag.ApplyFade:
            case PrivateTag.ReferenceImagesTakenFlag:
            case PrivateTag.Zoom:
            case PrivateTag.InvertFlag:
            case PrivateTag.OriginalResolution:
                return VR.US;
        }
        return VR.UN;
    }
}
