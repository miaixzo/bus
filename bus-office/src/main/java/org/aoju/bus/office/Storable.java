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

import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XStorable;
import com.sun.star.io.IOException;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.XComponent;
import com.sun.star.task.ErrorCodeIOException;
import com.sun.star.util.CloseVetoException;
import com.sun.star.util.XCloseable;
import org.aoju.bus.core.lang.exception.InstrumentException;

import java.io.File;
import java.util.Map;

import static org.aoju.bus.office.Builder.*;

/**
 * @author Kimi Liu
 * @version 5.3.1
 * @since JDK 1.8+
 */
public abstract class Storable implements Office {

    private final File inputFile;
    private final File outputFile;

    public Storable(File inputFile, File outputFile) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
    }

    protected abstract Map<String, ?> getLoadProperties(File inputFile);

    protected abstract Map<String, ?> getStoreProperties(File outputFile, XComponent document);

    public void execute(Context context) throws InstrumentException {
        XComponent document = null;
        try {
            document = loadDocument(context, inputFile);
            modifyDocument(document);
            storeDocument(document, outputFile);
        } catch (InstrumentException officeException) {
            throw officeException;
        } catch (Exception exception) {
            throw new InstrumentException("conversion failed", exception);
        } finally {
            if (document != null) {
                XCloseable closeable = cast(XCloseable.class, document);
                if (closeable != null) {
                    try {
                        closeable.close(true);
                    } catch (CloseVetoException closeVetoException) {
                        // whoever raised the veto should close the document
                    }
                } else {
                    document.dispose();
                }
            }
        }
    }

    private XComponent loadDocument(Context context, File inputFile) throws InstrumentException {
        if (!inputFile.exists()) {
            throw new InstrumentException("input document not found");
        }
        XComponentLoader loader = cast(XComponentLoader.class, context.getService(SERVICE_DESKTOP));
        Map<String, ?> loadProperties = getLoadProperties(inputFile);
        XComponent document = null;
        try {
            document = loader.loadComponentFromURL(toUrl(inputFile), "_blank", 0, toUnoProperties(loadProperties));
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new InstrumentException("could not load document: " + inputFile.getName(), illegalArgumentException);
        } catch (ErrorCodeIOException errorCodeIOException) {
            throw new InstrumentException("could not load document: " + inputFile.getName() + "; errorCode: " + errorCodeIOException.ErrCode, errorCodeIOException);
        } catch (IOException ioException) {
            throw new InstrumentException("could not load document: " + inputFile.getName(), ioException);
        }
        if (document == null) {
            throw new InstrumentException("could not load document: " + inputFile.getName());
        }
        return document;
    }

    /**
     * Override to modify the document after it has been loaded and before it gets
     * saved in the new format.
     * <p>
     * Does nothing by default.
     *
     * @param document doc
     * @throws InstrumentException exception
     */
    protected void modifyDocument(XComponent document) throws InstrumentException {
        // noop
    }

    private void storeDocument(XComponent document, File outputFile) throws InstrumentException {
        Map<String, ?> storeProperties = getStoreProperties(outputFile, document);
        if (storeProperties == null) {
            throw new InstrumentException("unsupported conversion");
        }
        try {
            cast(XStorable.class, document).storeToURL(toUrl(outputFile), toUnoProperties(storeProperties));
        } catch (ErrorCodeIOException errorCodeIOException) {
            throw new InstrumentException("could not store document: " + outputFile.getName() + "; errorCode: " + errorCodeIOException.ErrCode, errorCodeIOException);
        } catch (IOException ioException) {
            throw new InstrumentException("could not store document: " + outputFile.getName(), ioException);
        }
    }

}
