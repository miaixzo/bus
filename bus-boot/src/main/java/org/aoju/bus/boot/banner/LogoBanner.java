/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.boot.banner;


import com.taobao.text.Color;
import com.taobao.text.Decoration;
import com.taobao.text.ui.Element;
import com.taobao.text.ui.LabelElement;
import com.taobao.text.ui.TableElement;
import com.taobao.text.util.RenderUtil;


/**
 * LOGO旗标生成
 *
 * @author Kimi Liu
 * @version 3.2.6
 * @since JDK 1.8
 */
public class LogoBanner extends AbstractBanner {

    // Logo元素的总个数
    private int elementCount;
    // Logo元素的单个占行数
    private int elementLineCount;
    // Logo元素的颜色数组
    private Color[] elementColors;
    // Logo字体是否发亮
    private boolean boldOff;

    public LogoBanner(Class<?> resourceClass, String resourceLocation, String defaultBanner, int elementCount, int elementLineCount, Color[] elementColors, boolean boldOff) {
        super(resourceClass, resourceLocation, defaultBanner);

        this.elementCount = elementCount;
        this.elementLineCount = elementLineCount;
        this.elementColors = elementColors;
        this.boldOff = boldOff;

        initialize();
    }

    @Override
    protected String generateBanner(String bannerText) {
        if (bannerText != null) {
            StringBuilder stringBuilder = new StringBuilder();
            // Logo元素的总个数
            String[] elementTexts = new String[elementCount];
            int i = 0, j = 0;
            for (String line : bannerText.split("\n")) {
                stringBuilder.append(line);
                stringBuilder.append("\n");

                // Logo元素的单个占行数减1
                if (i++ == elementLineCount - 1) {
                    elementTexts[j++] = stringBuilder.toString();
                    i = 0;
                    stringBuilder.setLength(0);
                }
            }

            LabelElement[] labelElements = new LabelElement[elementCount];
            for (int k = 0; k < elementCount; k++) {
                if (boldOff) {
                    labelElements[k] = Element.label(elementTexts[k]).style(Decoration.bold_off.fg(elementColors[k]));
                } else {
                    labelElements[k] = Element.label(elementTexts[k]).style(Decoration.bold.fg(elementColors[k]));
                }
            }

            TableElement tableElement = new TableElement();
            tableElement.row(labelElements);

            return RenderUtil.render(tableElement);
        } else {
            return defaultBanner;
        }
    }

}