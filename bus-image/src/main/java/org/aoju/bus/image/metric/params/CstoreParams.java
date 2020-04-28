/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.image.metric.params;

import java.net.URL;

/**
 * @author Kimi Liu
 * @version 5.8.8
 * @since JDK 1.8+
 */
public class CstoreParams {

    private final DefaultEditor attributeEditor;
    private final boolean extendNegociation;
    private final URL extendSopClassesURL;

    /**
     * @param attributeEditor     a editor to modify DICOM attributes
     * @param extendNegociation   extends SOP classes negotiation
     * @param extendSopClassesURL configuration file of the SOP classes negotiation extension
     */
    public CstoreParams(DefaultEditor attributeEditor,
                        boolean extendNegociation,
                        URL extendSopClassesURL) {
        this.attributeEditor = attributeEditor;
        this.extendNegociation = extendNegociation;
        this.extendSopClassesURL = extendSopClassesURL;
    }

    public DefaultEditor getAttributeEditor() {
        return attributeEditor;
    }

    public boolean isExtendNegociation() {
        return extendNegociation;
    }

    public URL getExtendSopClassesURL() {
        return extendSopClassesURL;
    }

}
