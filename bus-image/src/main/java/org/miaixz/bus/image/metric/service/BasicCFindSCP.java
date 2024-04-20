/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.image.metric.service;

import org.miaixz.bus.image.Dimse;
import org.miaixz.bus.image.Status;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.metric.Association;
import org.miaixz.bus.image.metric.ImageException;
import org.miaixz.bus.image.metric.internal.pdu.Presentation;

import java.io.IOException;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class BasicCFindSCP extends AbstractService {

    public BasicCFindSCP(String... sopClasses) {
        super(sopClasses);
    }

    @Override
    public void onDimse(Association as,
                        Presentation pc,
                        Dimse dimse,
                        Attributes rq,
                        Attributes keys) throws IOException {
        if (dimse != Dimse.C_FIND_RQ)
            throw new ImageException(Status.UnrecognizedOperation);

        Query query = calculateMatches(as, pc, rq, keys);
        as.getApplicationEntity().getDevice().execute(query);
    }

    protected Query calculateMatches(Association as,
                                     Presentation pc,
                                     Attributes rq,
                                     Attributes keys) {
        return new BasicQuery(as, pc, rq, keys);
    }

}
