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
package org.aoju.bus.office;

import com.sun.star.lang.XComponent;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.util.XRefreshable;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.office.magic.Formats;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.aoju.bus.office.Builder.cast;

/**
 * @author Kimi Liu
 * @version 5.3.2
 * @since JDK 1.8+
 */
public class Standard extends Storable {

    private final Formats outputFormat;

    private Map<String, ?> defaultLoadProperties;
    private Formats inputFormat;

    public Standard(File inputFile, File outputFile, Formats outputFormat) {
        super(inputFile, outputFile);
        this.outputFormat = outputFormat;
    }

    public static Formats.Type getDocumentFamily(XComponent document) throws InstrumentException {
        XServiceInfo serviceInfo = cast(XServiceInfo.class, document);
        if (serviceInfo.supportsService("com.sun.star.text.GenericTextDocument")) {
            return Formats.Type.TEXT;
        } else if (serviceInfo.supportsService("com.sun.star.sheet.SpreadsheetDocument")) {
            return Formats.Type.SPREADSHEET;
        } else if (serviceInfo.supportsService("com.sun.star.presentation.PresentationDocument")) {
            return Formats.Type.PRESENTATION;
        } else if (serviceInfo.supportsService("com.sun.star.drawing.DrawingDocument")) {
            return Formats.Type.DRAWING;
        } else {
            throw new InstrumentException("document of unknown family: " + serviceInfo.getImplementationName());
        }
    }

    public void setDefaultLoadProperties(Map<String, ?> defaultLoadProperties) {
        this.defaultLoadProperties = defaultLoadProperties;
    }

    public void setInputFormat(Formats inputFormat) {
        this.inputFormat = inputFormat;
    }

    @Override
    protected void modifyDocument(XComponent document) throws InstrumentException {
        XRefreshable refreshable = cast(XRefreshable.class, document);
        if (refreshable != null) {
            refreshable.refresh();
        }
    }

    @Override
    protected Map<String, ?> getLoadProperties(File inputFile) {
        Map<String, Object> loadProperties = new HashMap<String, Object>();
        if (defaultLoadProperties != null) {
            loadProperties.putAll(defaultLoadProperties);
        }
        if (inputFormat != null && inputFormat.getLoadProperties() != null) {
            loadProperties.putAll(inputFormat.getLoadProperties());
        }
        return loadProperties;
    }

    @Override
    protected Map<String, ?> getStoreProperties(File outputFile, XComponent document) {
        Formats.Type family = getDocumentFamily(document);
        return outputFormat.getStoreProperties(family);
    }

}
