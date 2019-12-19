/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
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
package org.aoju.bus.office.magic.filter.text;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XIndexAccess;
import com.sun.star.container.XNameContainer;
import com.sun.star.graphic.XGraphic;
import com.sun.star.graphic.XGraphicProvider;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.text.XTextContent;
import com.sun.star.text.XTextGraphicObjectsSupplier;
import com.sun.star.uno.Any;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.XComponentContext;
import org.aoju.bus.office.Context;
import org.aoju.bus.office.magic.Info;
import org.aoju.bus.office.magic.Lo;
import org.aoju.bus.office.magic.Props;
import org.aoju.bus.office.magic.Write;
import org.aoju.bus.office.magic.filter.Filter;
import org.aoju.bus.office.magic.filter.FilterChain;
import org.aoju.bus.office.bridge.LocalOfficeContextAware;

/**
 * 此筛选器用于将图形插入到文档中.
 *
 * @author Kimi Liu
 * @version 5.3.3
 * @since JDK 1.8+
 */
public class LinkedImagesEmbedderFilter implements Filter {

    private static void convertLinkedImagesToEmbedded(
            final XComponentContext context, final XComponent document) throws Exception {

        // 创建一个GraphicProvider.
        final XGraphicProvider graphicProvider =
                Lo.createInstanceMCF(
                        context, XGraphicProvider.class, "com.sun.star.graphic.GraphicProvider");
        final XIndexAccess indexAccess =
                Lo.qi(
                        XIndexAccess.class,
                        Lo.qi(XTextGraphicObjectsSupplier.class, document).getGraphicObjects());
        for (int i = 0; i < indexAccess.getCount(); i++) {
            final Any xImageAny = (Any) indexAccess.getByIndex(i);
            final Object xImageObject = xImageAny.getObject();
            final XTextContent xImage = (XTextContent) xImageObject;
            final XServiceInfo xInfo = Lo.qi(XServiceInfo.class, xImage);
            if (xInfo.supportsService("com.sun.star.text.TextGraphicObject")) {
                final XPropertySet xPropSet = Lo.qi(XPropertySet.class, xImage);
                if (Info.isLibreOffice(context)
                        && Info.compareVersions(Info.getOfficeVersionShort(context), "6.1", 2) >= 0) {
                    final XGraphic xGraphic =
                            (XGraphic)
                                    AnyConverter.toObject(XGraphic.class, xPropSet.getPropertyValue("Graphic"));

                    final XPropertySet xGraphixPropSet = Lo.qi(XPropertySet.class, xGraphic);
                    boolean linked = (boolean) xGraphixPropSet.getPropertyValue("Linked");
                    if (linked) {
                        //从6.1开始，我们必须使用“Graphic”而不是“GraphicURL”
                        xPropSet.setPropertyValue(
                                "Graphic",
                                graphicProvider.queryGraphic(
                                        Props.makeProperties(
                                                "URL",
                                                xGraphixPropSet.getPropertyValue("OriginURL").toString(),
                                                "LoadAsLink",
                                                false)));
                    }
                } else {
                    final String name = xPropSet.getPropertyValue("LinkDisplayName").toString();
                    final String graphicURL = xPropSet.getPropertyValue("GraphicURL").toString();
                    if (graphicURL.indexOf("vnd.sun.") == -1) {
                        // 创建位图容器服务
                        final XNameContainer bitmapContainer =
                                Lo.createInstanceMSF(
                                        document, XNameContainer.class, "com.sun.star.drawing.BitmapTable");
                        if (!bitmapContainer.hasByName(name)) {
                            bitmapContainer.insertByName(name, graphicURL);
                            xPropSet.setPropertyValue("GraphicURL", bitmapContainer.getByName(name).toString());
                        }
                    }
                }
            }
        }
    }

    @Override
    public void doFilter(
            final Context context, final XComponent document, final FilterChain chain)
            throws Exception {

        if (Write.isText(document)) {
            convertLinkedImagesToEmbedded(((LocalOfficeContextAware) context).getComponentContext(), document);
        }
        chain.doFilter(context, document);
    }

}
