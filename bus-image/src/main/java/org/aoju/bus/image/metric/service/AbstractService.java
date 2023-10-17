/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.image.metric.service;

import org.aoju.bus.image.Dimse;
import org.aoju.bus.image.galaxy.data.Attributes;
import org.aoju.bus.image.metric.Association;
import org.aoju.bus.image.metric.PDVInputStream;
import org.aoju.bus.image.metric.internal.pdu.Presentation;
import org.aoju.bus.logger.Logger;

import java.io.IOException;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractService implements ImageService {

    private final String[] sopClasses;

    protected AbstractService(String... sopClasses) {
        this.sopClasses = sopClasses.clone();
    }

    @Override
    public final String[] getSOPClasses() {
        return sopClasses;
    }

    @Override
    public void onClose(Association as) {

    }

    @Override
    public void onDimse(Association as,
                        Presentation pc,
                        Dimse dimse,
                        Attributes cmd,
                        PDVInputStream data) throws IOException {
        onDimse(as, pc, dimse, cmd, readDataset(pc, data));
    }

    private Attributes readDataset(Presentation pc, PDVInputStream data)
            throws IOException {
        if (null == data)
            return null;

        Attributes dataset = data.readDataset(pc.getTransferSyntax());
        Logger.debug("Dataset:\n{}", dataset);
        return dataset;
    }

    protected abstract void onDimse(Association as,
                                    Presentation pc,
                                    Dimse dimse,
                                    Attributes cmd,
                                    Attributes data) throws IOException;

}
